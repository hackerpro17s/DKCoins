/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 01.12.19, 15:19
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.common.account;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.*;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.account.transaction.TransactionFilter;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.events.account.DKCoinsAccountCreateEvent;
import net.pretronic.dkcoins.api.events.account.DKCoinsAccountDeleteEvent;
import net.pretronic.dkcoins.api.events.account.DKCoinsAccountPreCreateEvent;
import net.pretronic.dkcoins.api.events.account.credit.DKCoinsAccountCreditPreCreateEvent;
import net.pretronic.dkcoins.api.events.account.member.DKCoinsAccountMemberAddEvent;
import net.pretronic.dkcoins.api.events.account.member.DKCoinsAccountMemberRemoveEvent;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.common.SyncAction;
import net.pretronic.dkcoins.common.user.DefaultDKCoinsUser;
import net.pretronic.libraries.caching.CacheQuery;
import net.pretronic.libraries.caching.synchronisation.ArraySynchronizableCache;
import net.pretronic.libraries.caching.synchronisation.SynchronizableCache;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.annonations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        this.accountTypeCache.getCaller().createAndIgnore(accountType.getId(), Document.newDocument());
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
    public BankAccount getAccount(String name, AccountType type) {
        return this.accountCache.get("nameAndType", name, type);
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
        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountPreCreateEvent(account, creator));
        accountCache.insert(account);
        this.accountCache.getCaller().createAndIgnore(account.getId(), Document.newDocument());
        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountCreateEvent(account, creator));
        return account;
    }

    @Override
    public MasterBankAccount createMasterAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator) {
        MasterBankAccount account = DKCoins.getInstance().getStorage()
                .createMasterAccount(name, type, disabled, parent, creator);
        accountCache.insert(account);
        this.accountCache.getCaller().createAndIgnore(account.getId(), Document.newDocument());
        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountCreateEvent(account, creator));
        return account;
    }

    @Override
    public void deleteAccount(BankAccount account, DKCoinsUser user) {
        accountCache.remove(account);
        DKCoins.getInstance().getStorage().deleteAccount(account.getId());
        this.accountCache.getCaller().deleteAndIgnore(account.getId(), Document.newDocument());
        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountDeleteEvent(account.getId(), user));
    }

    @Override
    public List<RankedAccountCredit> getTopAccountCredits(Currency currency, AccountType[] excludedAccountTypes, int entriesPerPage, int page) {
        AtomicInteger rank = new AtomicInteger();
        return Iterators.map(DKCoins.getInstance().getStorage().getTopAccountCreditIds(currency, excludedAccountTypes, entriesPerPage, page),
                (creditId) -> new DefaultRankedAccountCredit(rank.getAndIncrement(), creditId));
    }

    @Override
    public BankAccount getAccountByRank(Currency currency, int rank) {
        return getAccount(DKCoins.getInstance().getStorage().getAccountIdByRank(currency, rank));
    }

    @Override
    public void updateAccountName(BankAccount account, String name) {
        Validate.notNull(account);
        Validate.notNull(name);
        DKCoins.getInstance().getStorage().updateAccountName(account.getId(), name);
        ((DefaultBankAccount)account).updateName(name);
        this.accountCache.getCaller().updateAndIgnore(account.getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_UPDATE_NAME)
                .add("name", account.getName()));
    }

    @Override
    public void updateAccountDisabled(BankAccount account, boolean disabled) {
        Validate.notNull(account);
        DKCoins.getInstance().getStorage().updateAccountDisabled(account.getId(), disabled);
        ((DefaultBankAccount)account).updateDisabled(disabled);
        this.accountCache.getCaller().updateAndIgnore(account.getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_UPDATE_DISABLED)
                .add("disabled", account.isDisabled()));
    }

    @Override
    public AccountCredit getAccountCredit(int id) {
        int accountId = DKCoins.getInstance().getStorage().getAccountCreditAccountId(id);
        if(accountId == -1) throw new IllegalArgumentException("No account found for credit id " + id);
        return getAccount(accountId).getCredit(id);
    }


    @Override
    public AccountCredit addAccountCredit(BankAccount account, Currency currency, double amount) {
        AccountCredit credit = DKCoins.getInstance().getStorage().addAccountCredit(account, currency, amount);
        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountCreditPreCreateEvent(credit));
        ((DefaultBankAccount)account).addLoadedAccountCredit(credit);
        this.accountCache.getCaller().updateAndIgnore(account.getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_CREDIT_NEW)
                .add("creditId", credit.getId()));
        return credit;
    }

    @Override
    public void deleteAccountCredit(AccountCredit credit) {
        DKCoins.getInstance().getStorage().deleteAccountCredit(credit.getId());
        ((DefaultBankAccount)credit.getAccount()).deleteLoadedAccountCredit(credit);
        this.accountCache.getCaller().updateAndIgnore(credit.getAccount().getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_CREDIT_DELETE)
                .add("creditId", credit.getId()));
    }

    @Override
    public void setAccountCreditAmount(AccountCredit credit, double amount) {
        DKCoins.getInstance().getStorage().setAccountCreditAmount(credit.getId(), amount);
        ((DefaultAccountCredit)credit).updateAmount(amount);
        this.accountCache.getCaller().updateAndIgnore(credit.getAccount().getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_CREDIT_SET_AMOUNT)
                .add("creditId", credit.getId())
                .add("amount", amount));
    }

    @Override
    public void addAccountCreditAmount(AccountCredit credit, double amount) {
        DKCoins.getInstance().getStorage().addAccountCreditAmount(credit.getId(), amount);
        ((DefaultAccountCredit)credit).updateAmount(credit.getAmount()+amount);
        this.accountCache.getCaller().updateAndIgnore(credit.getAccount().getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_CREDIT_ADD_AMOUNT)
                .add("creditId", credit.getId())
                .add("amount", amount));
    }

    @Override
    public void removeAccountCreditAmount(AccountCredit credit, double amount) {
        DKCoins.getInstance().getStorage().removeAccountCreditAmount(credit.getId(), amount);
        ((DefaultAccountCredit)credit).updateAmount(credit.getAmount()-amount);
        this.accountCache.getCaller().updateAndIgnore(credit.getAccount().getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_CREDIT_REMOVE_AMOUNT)
                .add("creditId", credit.getId())
                .add("amount", amount));
    }


    @Override
    public AccountMember getAccountMember(int id) {
        int accountId = DKCoins.getInstance().getStorage().getAccountMemberAccountId(id);
        if(accountId == -1) throw new IllegalArgumentException("No account found for member id " + id);
        return getAccount(accountId).getMember(id);
    }

    @Override
    public AccountMember getAccountMember(DKCoinsUser user, BankAccount account) {
        return account.getMember(user);
    }

    @Override
    public AccountMember addAccountMember(BankAccount account, DKCoinsUser user, AccountMember adder, AccountMemberRole memberRole, boolean receiveNotifications) {
        AccountMember member = DKCoins.getInstance().getStorage().addAccountMember(account, user, memberRole, receiveNotifications);
        ((DefaultBankAccount)account).addLoadedMember(member);
        this.accountCache.getCaller().updateAndIgnore(account.getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_MEMBER_ADD)
                .add("memberId", member.getId()));
        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountMemberAddEvent(member, adder));
        return member;
    }

    @Override
    public void updateAccountMemberRole(AccountMember member, AccountMemberRole role) {
        ((DefaultAccountMember)member).updateRole(role);
        DKCoins.getInstance().getStorage().updateAccountMemberRole(member);
        this.accountCache.getCaller().updateAndIgnore(member.getAccount().getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_MEMBER_UPDATE_ROLE)
                .add("memberId", member.getId())
                .add("roleId", member.getRole().getId()));
    }

    @Override
    public void updateAccountMemberReceiveNotifications(AccountMember member, boolean receiveNotification) {
        Validate.notNull(member);
        ((DefaultAccountMember)member).updateReceiveNotifications(receiveNotification);
        DKCoins.getInstance().getStorage().updateAccountMemberReceiveNotifications(member);
        this.accountCache.getCaller().updateAndIgnore(member.getAccount().getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_MEMBER_UPDATE_RECEIVE_NOTIFICATIONS)
                .add("memberId", member.getId())
                .add("receiveNotifications", member.receiveNotifications()));
    }

    @Override
    public boolean removeAccountMember(AccountMember member, AccountMember remover) {
        DKCoins.getInstance().getStorage().removeAccountMember(member.getId());
        this.accountCache.getCaller().updateAndIgnore(member.getAccount().getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_MEMBER_REMOVE)
                .add("memberId", member.getId()));
        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountMemberRemoveEvent(member.getUser(), remover));
        return ((DefaultBankAccount)member.getAccount()).removeLoadedMember(member);
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
        int accountId = DKCoins.getInstance().getStorage().getAccountLimitationAccountId(id);
        if(accountId == -1) throw new IllegalArgumentException("No account found for limitation id " + id);
        return getAccount(accountId).getLimitation(id);
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
        if(member != null) {
            ((DefaultAccountMember)member).addLoadedLimitation(limitation);
        } else {
            ((DefaultBankAccount)account).addLoadedLimitation(limitation);
        }
        this.accountCache.getCaller().updateAndIgnore(account.getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_LIMITATION_ADD)
                .add("limitationId", limitation.getId()));
        return limitation;
    }

    @Override
    public boolean removeAccountLimitation(AccountMember member, AccountLimitation limitation) {
        Validate.notNull(member);
        if(limitation == null) return false;
        DKCoins.getInstance().getStorage().removeAccountLimitation(limitation.getId());
        this.accountCache.getCaller().updateAndIgnore(limitation.getAccount().getId(),
                Document.newDocument()
                        .add("action", SyncAction.ACCOUNT_LIMITATION_REMOVE)
                        .add("limitationId", limitation.getId()));
        return ((DefaultAccountMember)member).removeLoadedLimitation(limitation);
    }

    @Override
    public boolean removeAccountLimitation(BankAccount account, AccountLimitation limitation) {
        Validate.notNull(account);
        if(limitation == null) return false;
        DKCoins.getInstance().getStorage().removeAccountLimitation(limitation.getId());
        this.accountCache.getCaller().updateAndIgnore(limitation.getAccount().getId(),
                Document.newDocument()
                        .add("action", SyncAction.ACCOUNT_LIMITATION_REMOVE)
                        .add("limitationId", limitation.getId()));
        return ((DefaultBankAccount)account).removeLoadedLimitation(limitation);
    }

    @Override
    public List<AccountTransaction> filterAccountTransactions(TransactionFilter filter) {
        return DKCoins.getInstance().getStorage().filterAccountTransactions(filter);
    }

    @Internal
    public SynchronizableCache<AccountType, Integer> getAccountTypeCache() {
        return accountTypeCache;
    }

    @Internal
    public SynchronizableCache<BankAccount, Integer> getAccountCache() {
        return accountCache;
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
        accountTypeCache.setClearOnDisconnect(true);
        accountTypeCache.setSkipOnDisconnect(true);
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
    }

    private void registerAccountCacheQueries() {
        accountCache.setClearOnDisconnect(true);
        accountCache.setSkipOnDisconnect(true);
        this.accountCache.setMaxSize(100);

        this.accountCache.setIdentifierQuery(new CacheQuery<BankAccount>() {
            @Override
            public boolean check(BankAccount account, Object[] identifiers) {
                return (int) identifiers[0] == account.getId();
            }

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof Integer,
                        "AccountCache: Wrong identifiers for identifier query");
            }
        });

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
        }).registerQuery("nameAndType", new CacheQuery<BankAccount>() {

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 2
                        && identifiers[0] instanceof String
                        && identifiers[1] instanceof AccountType);
            }

            @Override
            public BankAccount load(Object[] identifiers) {
                String name = (String) identifiers[0];
                AccountType type = (AccountType) identifiers[1];
                return DKCoins.getInstance().getStorage().getAccount(name, type);
            }

            @Override
            public boolean check(BankAccount account, Object[] identifiers) {
                String name = (String) identifiers[0];
                AccountType type = (AccountType) identifiers[1];
                return account.getName().equalsIgnoreCase(name) && account.getType().equals(type);
            }
        }).registerQuery("byCreditId", new CacheQuery<BankAccount>() {
            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof Integer);
            }

            @Override
            public BankAccount load(Object[] identifiers) {
                //@Todo direct via join
                int accountId = DKCoins.getInstance().getStorage().getAccountCreditAccountId((int) identifiers[0]);
                return DKCoins.getInstance().getAccountManager().getAccount(accountId);
            }
            @Override
            public boolean check(BankAccount account, Object[] identifiers) {
                return account.getCredit((int) identifiers[0]) != null;
            }
        }).setInsertListener(this::createMissingAccountCredits);

        this.accountCache.setCreateHandler((id, data) -> DKCoins.getInstance().getStorage().getAccount(id));
    }
}
