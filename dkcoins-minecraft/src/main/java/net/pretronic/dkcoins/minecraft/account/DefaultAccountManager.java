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

import net.prematic.libraries.caching.ArrayCache;
import net.prematic.libraries.caching.Cache;
import net.prematic.libraries.caching.CacheQuery;
import net.prematic.libraries.caching.synchronisation.ArraySynchronizableCache;
import net.prematic.libraries.caching.synchronisation.SynchronizableCache;
import net.prematic.libraries.utility.Validate;
import net.prematic.libraries.utility.annonations.Nullable;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.*;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.account.transaction.TransactionFilter;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.DKCoinsPlugin;
import net.pretronic.dkcoins.minecraft.user.DefaultDKCoinsUser;
import org.mcnative.common.McNative;

import java.util.*;

public class DefaultAccountManager implements AccountManager {

    private final SynchronizableCache<AccountType, Integer> accountTypeCache;
    private final Cache<BankAccount> accountCache;

    public DefaultAccountManager() {
        this.accountTypeCache = new ArraySynchronizableCache<>();
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
                });
        McNative.getInstance().getLocal().registerSynchronizingChannel("", DKCoinsPlugin.getInstance(), Integer.class, accountTypeCache);
        this.accountCache = new ArrayCache<BankAccount>().setMaxSize(100)
                .registerQuery("search", new CacheQuery<BankAccount>() {

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
    }

    @Override
    public AccountType getAccountType(int id) {
        return this.accountTypeCache.get("byId", id);
    }

    @Override
    public AccountType searchAccountType(Object identifier) {
        return DKCoins.getInstance().getStorage().searchAccountType(identifier);
    }

    @Override
    public AccountType createAccountType(String name, String symbol) {
        AccountType accountType = DKCoins.getInstance().getStorage().createAccountType(name, symbol);
        this.accountTypeCache.insert(accountType);
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
        //@Todo own query
        return this.accountCache.get("search", id);
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
        return account;
    }

    @Override
    public MasterBankAccount createMasterAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator) {
        MasterBankAccount account = DKCoins.getInstance().getStorage()
                .createMasterAccount(name, type, disabled, parent, creator);
        accountCache.insert(account);
        return account;
    }

    @Override
    public void deleteAccount(BankAccount account) {
        accountCache.remove(account);
        DKCoins.getInstance().getStorage().deleteAccount(account.getId());
    }

    @Override
    public void updateAccountName(BankAccount account) {
        DKCoins.getInstance().getStorage().updateAccountName(account.getId(), account.getName());
    }

    @Override
    public void updateAccountDisabled(BankAccount account) {
        DKCoins.getInstance().getStorage().updateAccountDisabled(account.getId(), account.isDisabled());
    }

    @Override
    public AccountCredit getAccountCredit(int id) {
        return null;
    }


    @Override
    public AccountCredit addAccountCredit(BankAccount account, Currency currency, double amount) {
        return DKCoins.getInstance().getStorage().addAccountCredit(account, currency, amount);
    }

    @Override
    public void deleteAccountCredit(AccountCredit credit) {
        DKCoins.getInstance().getStorage().deleteAccountCredit(credit.getId());
    }

    @Override
    public void setAccountCreditAmount(AccountCredit credit, double amount) {
        DKCoins.getInstance().getStorage().setAccountCreditAmount(credit.getId(), amount);
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
    public AccountMember addAccountMember(BankAccount account, DKCoinsUser user, AccountMemberRole memberRole) {
        return DKCoins.getInstance().getStorage().addAccountMember(account, user, memberRole);
    }

    @Override
    public void updateAccountMemberRole(AccountMember member) {
        DKCoins.getInstance().getStorage().updateAccountMemberRole(member);
    }

    @Override
    public void removeAccountMember(AccountMember member) {
        DKCoins.getInstance().getStorage().removeAccountMember(member.getId());
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
    public boolean hasLimitation(BankAccount account, Currency currency, double amount) {
        return false;
    }

    @Override
    public boolean hasLimitation(BankAccount account, AccountMemberRole memberRole, Currency currency, double amount) {
        return false;
    }

    @Override
    public boolean hasLimitation(AccountMember member, Currency currency, double amount) {
        return false;
    }

    @Override
    public AccountLimitation addAccountLimitation(BankAccount account, @Nullable AccountMember member,
                                                  @Nullable AccountMemberRole memberRole, Currency comparativeCurrency,
                                                  double amount, long interval) {
        Validate.notNull(account, comparativeCurrency);
        Validate.isTrue(amount > 0);
        return DKCoins.getInstance().getStorage().addAccountLimitation(account, member, memberRole,
                comparativeCurrency, amount, interval);
    }

    @Override
    public void removeAccountLimitation(AccountLimitation accountLimitation) {
        Validate.notNull(accountLimitation);
        DKCoins.getInstance().getStorage().removeAccountLimitation(accountLimitation.getId());
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
}
