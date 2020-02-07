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
import net.prematic.libraries.utility.Validate;
import net.prematic.libraries.utility.annonations.Nullable;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.*;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

import java.util.Arrays;
import java.util.Collection;

public class DefaultAccountManager implements AccountManager {

    private final Cache<AccountType> accountTypeCache;

    public DefaultAccountManager() {
        this.accountTypeCache = new ArrayCache<AccountType>().setMaxSize(100)
                .registerQuery("byId", new CacheQuery<AccountType>() {
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
    }

    @Override
    public AccountType getAccountType(int id) {
        return this.accountTypeCache.get("byId", id);
    }


    @Override
    public BankAccount getAccount(int id) {
        return DKCoins.getInstance().getStorage().getAccount(id);
    }

    @Override
    public BankAccount searchAccount(Object identifier) {
        return null;
    }

    @Override
    public MasterBankAccount getMasterAccount(int id) {
        return DKCoins.getInstance().getStorage().getMasterAccount(id);
    }

    @Override
    public BankAccount getSubAccount(MasterBankAccount account, int id) {
        return DKCoins.getInstance().getStorage().getSubAccount(account.getId(), id);
    }

    @Override
    public MasterBankAccount getSubMasterAccount(MasterBankAccount account, int id) {
        return DKCoins.getInstance().getStorage().getSubMasterAccount(account.getId(), id);
    }

    @Override
    public BankAccount createAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator) {
        return DKCoins.getInstance().getStorage()
                .createAccount(name, type.getId(), disabled, parent.getId(), creator.getId());
    }

    @Override
    public MasterBankAccount createMasterAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator) {
        return DKCoins.getInstance().getStorage()
                .createMasterAccount(name, type.getId(), disabled, parent.getId(), creator.getId());
    }

    @Override
    public void deleteAccount(BankAccount account) {
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
        //return DKCoins.getInstance().getStorage().getAccountCredit(id);
    }


    @Override
    public AccountCredit addAccountCredit(BankAccount account, Currency currency, double amount) {
        return DKCoins.getInstance().getStorage().addAccountCredit(account.getId(), currency.getId(), amount);
    }

    @Override
    public void setAccountCreditAmount(AccountCredit credit, double amount) {
        DKCoins.getInstance().getStorage().setAccountCreditAmount(credit.getId(), amount);
    }


    @Override
    public AccountMember getAccountMember(int id) {
        return DKCoins.getInstance().getStorage().getAccountMember(id);
    }

    @Override
    public AccountMember getAccountMember(DKCoinsUser user, BankAccount account) {
        return DKCoins.getInstance().getStorage().getAccountMember(user.getId(), account.getId());
    }

    @Override
    public AccountMember addAccountMember(BankAccount account, DKCoinsUser user, AccountMemberRole memberRole) {
        return DKCoins.getInstance().getStorage().addAccountMember(account.getId(), user.getId(), memberRole);
    }

    @Override
    public void deleteAccountMember(AccountMember member) {
        DKCoins.getInstance().getStorage().deleteAccountMember(member.getId());
    }


    @Override
    public Collection<AccountTransaction> getAccountTransactions(AccountMember member, int start, int end) {
        return DKCoins.getInstance().getStorage().getAccountTransactions(member.getId(), start, end);
    }

    @Override
    public AccountTransaction addAccountTransaction(AccountCredit source, AccountMember sender, AccountCredit receiver,
                                                    double amount, double exchangeRate, String reason, String cause,
                                                    long time, Collection<AccountTransactionProperty> properties) {
        return DKCoins.getInstance().getStorage().addAccountTransaction(source.getId(), sender.getId(), receiver.getId(),
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
        return DKCoins.getInstance().getStorage().addAccountLimitation(account.getId(), member.getId(), memberRole.getId(),
                comparativeCurrency.getId(), amount, interval);
    }

    @Override
    public void deleteAccountLimitation(AccountLimitation accountLimitation) {
        DKCoins.getInstance().getStorage().deleteAccountLimitation(accountLimitation.getId());
    }
}
