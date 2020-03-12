/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 01.12.19, 15:19
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.account;

import net.pretronic.libraries.caching.CacheQuery;
import net.pretronic.libraries.caching.synchronisation.ArraySynchronizableCache;
import net.pretronic.libraries.caching.synchronisation.SynchronizableCache;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.annonations.Nullable;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.*;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.account.transaction.TransactionFilter;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.minecraft.events.DKCoinsAccountCreateEvent;
import net.pretronic.dkcoins.api.minecraft.events.DKCoinsAccountDeleteEvent;
import net.pretronic.dkcoins.api.minecraft.events.DKCoinsAccountMemberAddEvent;
import net.pretronic.dkcoins.api.minecraft.events.DKCoinsAccountMemberRemoveEvent;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.DKCoinsPlugin;
import net.pretronic.dkcoins.minecraft.user.DefaultDKCoinsUser;
import org.mcnative.common.McNative;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DefaultAccountManager implements AccountManager {

    private final SynchronizableCache<AccountType, Integer> accountTypeCache;
    private final SynchronizableCache<BankAccount, Integer> accountCache;

    public DefaultAccountManager() {
        this.accountTypeCache = new ArraySynchronizableCache<>();
        registerAccountTypeCacheQueries();

        this.accountCache = new ArraySynchronizableCache<>();
        registerAccountCacheQueries();
    }

    @Override
    public AccountType getAccountType(int id) {
        return this.accountTypeCache.get("byId", id);
    }

    @Override
    public AccountType searchAccountType(Object identifier) {
        return this.accountTypeCache.get("search", identifier);
    }

    @Override
    public AccountType createAccountType(String name, String symbol) {
        AccountType accountType = DKCoins.getInstance().getStorage().createAccountType(name, symbol);
        this.accountTypeCache.insert(accountType);
        this.accountTypeCache.getCaller().create(accountType.getId(), Document.newDocument());
        return accountType;
    }

    @Override
    public Collection<BankAccount> getCachedAccounts() {
        return this.accountCache.getCachedObjects();
    }

    @Override
    public Collection<BankAccount> getAccounts(DKCoinsUser user) {
        if(((DefaultDKCoinsUser)user).isUserAccountsLoaded()) {
            Collection<BankAccount> accounts = new ArrayList<>();
            for (BankAccount account : this.accountCache.getCachedObjects()) {
                if(account.getMember(user) != null) accounts.add(account);
            }
            return accounts;
        } else {
            return loadAccounts(user);
        }
    }

    private Collection<BankAccount> loadAccounts(DKCoinsUser user) {
        Collection<BankAccount> accounts = new ArrayList<>();
        for (int accountId : DKCoins.getInstance().getStorage().getAccountIds(user.getUniqueId())) {
            BankAccount account = this.accountCache.get("search", accountId);
            accounts.add(account);
        }
        ((DefaultDKCoinsUser)user).setUserAccountsLoaded(true);
        return accounts;
    }

    @Override
    public BankAccount getAccount(int id) {
        return this.accountCache.get("byId", id);
    }

    @Override
    public BankAccount searchAccount(Object identifier) {
        return this.accountCache.get("search", identifier);
    }

    @Override
    public MasterBankAccount getMasterAccount(int id) {
        if(id <= 0) return null;
        return (MasterBankAccount) getAccount(id);
    }

    @Override
    public BankAccount getSubAccount(MasterBankAccount account, int id) {
        return accountCache.get("subAccount", account.getId(), id);
    }

    @Override
    public MasterBankAccount getSubMasterAccount(MasterBankAccount account, int id) {
        return (MasterBankAccount) getSubAccount(account, id);
    }

    @Override
    public BankAccount createAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator) {
        Validate.notNull(name, type, creator);
        BankAccount account = DKCoins.getInstance().getStorage()
                .createAccount(name, type, disabled, parent, creator);
        accountCache.insert(account);
        this.accountCache.getCaller().create(account.getId(), Document.newDocument());
        McNative.getInstance().getLocal().getEventBus().callEvent(new DKCoinsAccountCreateEvent(account, creator));
        return account;
    }

    @Override
    public MasterBankAccount createMasterAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator) {
        MasterBankAccount account = DKCoins.getInstance().getStorage()
                .createMasterAccount(name, type, disabled, parent, creator);
        accountCache.insert(account);
        this.accountCache.getCaller().create(account.getId(), Document.newDocument());
        McNative.getInstance().getLocal().getEventBus().callEvent(new DKCoinsAccountCreateEvent(account, creator));
        return account;
    }

    @Override
    public void deleteAccount(BankAccount account, DKCoinsUser user) {
        accountCache.remove(account);
        DKCoins.getInstance().getStorage().deleteAccount(account.getId());
        this.accountCache.getCaller().delete(account.getId(), Document.newDocument());
        McNative.getInstance().getLocal().getEventBus().callEvent(new DKCoinsAccountDeleteEvent(account.getId(), user));
    }

    @Override
    public void updateAccountName(BankAccount account) {
        DKCoins.getInstance().getStorage().updateAccountName(account.getId(), account.getName());
        this.accountCache.getCaller().update(account.getId(), Document.newDocument().add("name", account.getName()));
    }

    @Override
    public void updateAccountDisabled(BankAccount account) {
        DKCoins.getInstance().getStorage().updateAccountDisabled(account.getId(), account.isDisabled());
        this.accountCache.getCaller().update(account.getId(), Document.newDocument().add("disabled", account.isDisabled()));
    }

    @Override
    public AccountCredit getAccountCredit(int id) {
        throw new UnsupportedOperationException();
    }


    @Override
    public AccountCredit addAccountCredit(BankAccount account, Currency currency, double amount) {
        AccountCredit credit = DKCoins.getInstance().getStorage().addAccountCredit(account, currency, amount);
        this.accountCache.getCaller().update(account.getId(), Document.newDocument().add("newCredit", credit.getId()));
        return credit;
    }

    @Override
    public void deleteAccountCredit(AccountCredit credit) {
        DKCoins.getInstance().getStorage().deleteAccountCredit(credit.getId());
        this.accountCache.getCaller().update(credit.getId(), Document.newDocument().add("removeCredit", credit.getId()));
    }

    @Override
    public void setAccountCreditAmount(AccountCredit credit, double amount) {
        DKCoins.getInstance().getStorage().setAccountCreditAmount(credit.getId(), amount);
        this.accountCache.getCaller().update(credit.getId(), Document.newDocument("updateCreditAmount")
                .add("creditId", credit.getId()).add("amount", amount));
    }


    @Override
    public AccountMember getAccountMember(int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AccountMember getAccountMember(DKCoinsUser user, BankAccount account) {
        return account.getMember(user);
    }

    @Override
    public AccountMember addAccountMember(BankAccount account, DKCoinsUser user, AccountMember adder, AccountMemberRole memberRole) {
        AccountMember member = DKCoins.getInstance().getStorage().addAccountMember(account, user, memberRole);
        this.accountCache.getCaller().update(account.getId(), Document.newDocument().add("newMember", member.getId()));
        McNative.getInstance().getLocal().getEventBus().callEvent(new DKCoinsAccountMemberAddEvent(member, adder));
        return member;
    }

    @Override
    public void updateAccountMemberRole(AccountMember member) {
        DKCoins.getInstance().getStorage().updateAccountMemberRole(member);
        this.accountCache.getCaller().update(member.getAccount().getId(), Document.newDocument("updateMemberRole")
                .add("memberId", member.getId()).add("roleId", member.getRole().getId()));
    }

    @Override
    public void removeAccountMember(AccountMember member, AccountMember remover) {
        DKCoins.getInstance().getStorage().removeAccountMember(member.getId());
        this.accountCache.getCaller().update(member.getAccount().getId(), Document.newDocument().add("removeMember", member.getId()));
        McNative.getInstance().getLocal().getEventBus().callEvent(new DKCoinsAccountMemberRemoveEvent(member.getUser(), remover));
    }


    @Override
    public Collection<AccountTransaction> getAccountTransactions(AccountMember member, int start, int end) {
        return DKCoins.getInstance().getStorage().getAccountTransactions(member.getId(), start, end);
    }

    @Override
    public AccountTransaction addAccountTransaction(AccountCredit source, AccountMember sender, AccountCredit receiver,
                                                    double amount, double exchangeRate, String reason, String cause,
                                                    long time, Collection<AccountTransactionProperty> properties) {
        return DKCoins.getInstance().getStorage().addAccountTransaction(source, sender, receiver,
                amount, exchangeRate, reason, cause, time, properties);
    }

    @Override
    public AccountLimitation getAccountLimitation(int id) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean hasAccountLimitation(BankAccount account, Currency currency, double amount) {
        return false;
    }

    @Override
    public boolean hasAccountLimitation(BankAccount account, AccountMemberRole memberRole, Currency currency, double amount) {
        return false;
    }

    @Override
    public boolean hasAccountLimitation(AccountMember member, Currency currency, double amount) {
        return false;
    }

    @Override
    public AccountLimitation addAccountLimitation(BankAccount account, @Nullable AccountMember member,
                                                  @Nullable AccountMemberRole memberRole, Currency comparativeCurrency,
                                                  double amount, long interval) {
        Validate.notNull(account, comparativeCurrency);
        Validate.isTrue(amount > 0);
        AccountLimitation limitation = DKCoins.getInstance().getStorage().addAccountLimitation(account, member, memberRole,
                comparativeCurrency, amount, interval);
        this.accountCache.getCaller().update(account.getId(), Document.newDocument().add("newLimitation", limitation.getId()));
        return limitation;
    }

    @Override
    public void removeAccountLimitation(AccountLimitation accountLimitation) {
        Validate.notNull(accountLimitation);
        DKCoins.getInstance().getStorage().removeAccountLimitation(accountLimitation.getId());
        this.accountCache.getCaller().update(accountLimitation.getAccount().getId(),
                Document.newDocument().add("removeLimitation", accountLimitation.getId()));
    }

    @Override
    public List<AccountTransaction> filterAccountTransactions(TransactionFilter filter) {
        return DKCoins.getInstance().getStorage().filterAccountTransactions(filter);
    }

    private void createMissingAccountCredits(BankAccount account) {
        Validate.notNull(account);
        for (Currency currency : DKCoins.getInstance().getCurrencyManager().getCurrencies()) {
            if(account.getCredit(currency) == null) {
                account.addCredit(currency, 0);
            }
        }
    }

    private void registerAccountTypeCacheQueries() {
        this.accountTypeCache.setIdentifierQuery(new CacheQuery<AccountType>() {

            @Override
            public boolean check(AccountType accountType, Object[] identifiers) {
                return accountType.getId() == (int) identifiers[0];
            }

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof Integer, "AccountTypeCache: Wrong identifiers for identifier query");
            }
        });

        this.accountTypeCache.setCreateHandler((id, data) -> DKCoins.getInstance().getStorage().getAccountType(id));

        this.accountTypeCache.setMaxSize(100).registerQuery("byId", new CacheQuery<AccountType>() {

            @Override
            public boolean check(AccountType accountType, Object[] identifiers) {
                return accountType.getId() == (int) identifiers[0];
            }

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof Integer,
                        "AccountTypeCache: Wrong identifiers: %s", Arrays.toString(identifiers));
            }

            @Override
            public AccountType load(Object[] identifiers) {
                return DKCoins.getInstance().getStorage().getAccountType((int) identifiers[0]);
            }
        }).registerQuery("search", new CacheQuery<AccountType>() {

            @Override
            public boolean check(AccountType accountType, Object[] identifiers) {
                if(identifiers[0] instanceof Integer) return accountType.getId() == (int) identifiers[0];
                return identifiers[0] instanceof String && accountType.getName().equalsIgnoreCase((String) identifiers[0]);
            }

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1,
                        "AccountTypeCache: Wrong identifiers: %s", Arrays.toString(identifiers));
            }

            @Override
            public AccountType load(Object[] identifiers) {
                return DKCoins.getInstance().getStorage().searchAccountType(identifiers[0]);
            }
        });

        McNative.getInstance().getLocal().registerSynchronizingChannel("dkcoins_accountType", DKCoinsPlugin.getInstance(),
                int.class, accountTypeCache);
    }

    private void registerAccountCacheQueries() {
        this.accountCache.setMaxSize(100);
        this.accountCache.registerQuery("search", new CacheQuery<BankAccount>() {

            @Override
            public boolean check(BankAccount account, Object[] identifiers) {
                Object identifier = identifiers[0];
                if(identifier instanceof Integer) {
                    return account.getId() == (int) identifier;
                }
                return identifier instanceof String && account.getName().equalsIgnoreCase((String) identifier);
            }

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1, "AccountCache: (search) Invalid length of identifiers");
            }

            @Override
            public BankAccount load(Object[] identifiers) {
                System.out.println("Load account:" + Arrays.toString(identifiers));
                return DKCoins.getInstance().getStorage().searchAccount(identifiers[0]);
            }
        }).registerQuery("byId", new CacheQuery<BankAccount>() {

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof Integer, "AccountCache: (byId) Invalid length of identifiers or incorrect identifier");
            }

            @Override
            public BankAccount load(Object[] identifiers) {
                return DKCoins.getInstance().getStorage().getAccount((int) identifiers[0]);
            }

            @Override
            public boolean check(BankAccount account, Object[] identifiers) {
                return account.getId() == (int) identifiers[0];
            }
        }).registerQuery("subAccount", new CacheQuery<BankAccount>() {
            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 2 && identifiers[0] instanceof Integer && identifiers[1] instanceof Integer);
            }

            @Override
            public BankAccount load(Object[] identifiers) {
                return DKCoins.getInstance().getStorage().getSubAccount((int)identifiers[0], (int)identifiers[1]);
            }

            @Override
            public boolean check(BankAccount account, Object[] identifiers) {
                return account.getId() == (int) identifiers[1]
                        && account.getParent() != null && account.getParent().getId() == (int) identifiers[0];
            }
        }).setInsertListener(this::createMissingAccountCredits);

        this.accountCache.setCreateHandler((id, data) -> DKCoins.getInstance().getStorage().getAccount(id));

        McNative.getInstance().getLocal().registerSynchronizingChannel("dkcoins_account", DKCoinsPlugin.getInstance(),
                int.class, accountCache);
    }
}
