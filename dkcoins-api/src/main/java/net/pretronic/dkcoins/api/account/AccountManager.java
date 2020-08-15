/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 15:02
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.account;

import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.account.transaction.TransactionFilter;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.libraries.utility.annonations.Nullable;

import java.util.Collection;
import java.util.List;

public interface AccountManager {

    AccountType getAccountType(int id);

    AccountType searchAccountType(Object identifier);

    AccountType createAccountType(String name, String symbol);


    Collection<BankAccount> getCachedAccounts();

    Collection<BankAccount> getAccounts(DKCoinsUser user);

    BankAccount getAccount(int id);

    BankAccount getAccount(String name, AccountType type);

    default BankAccount getAccount(String name, String type) {
        return getAccount(name, searchAccountType(type));
    }

    BankAccount searchAccount(Object identifier);

    MasterBankAccount getMasterAccount(int id);

    BankAccount getSubAccount(MasterBankAccount account, int id);

    MasterBankAccount getSubMasterAccount(MasterBankAccount account, int id);

    BankAccount createAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator);

    MasterBankAccount createMasterAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator);

    void updateAccountName(BankAccount account, String name);

    void updateAccountDisabled(BankAccount account, boolean disabled);

    void deleteAccount(BankAccount account, DKCoinsUser user);

    List<RankedAccountCredit> getTopAccountCredits(Currency currency, AccountType[] excludedAccountTypes, int entriesPerPage, int page);

    BankAccount getAccountByRank(Currency currency, int rank);


    AccountCredit getAccountCredit(int id);

    AccountCredit addAccountCredit(BankAccount account, Currency currency, double amount);

    void deleteAccountCredit(AccountCredit credit);

    void setAccountCreditAmount(AccountCredit credit, double amount);

    void addAccountCreditAmount(AccountCredit credit, double amount);

    void removeAccountCreditAmount(AccountCredit credit, double amount);


    AccountMember getAccountMember(int id);

    AccountMember getAccountMember(DKCoinsUser user, BankAccount account);

    AccountMember addAccountMember(BankAccount account, DKCoinsUser user, AccountMember adder, AccountMemberRole memberRole, boolean receiveNotifications);

    void updateAccountMemberRole(AccountMember member, AccountMemberRole role);

    void updateAccountMemberReceiveNotifications(AccountMember member, boolean receiveNotification);

    boolean removeAccountMember(AccountMember member, AccountMember remover);


    List<AccountTransaction> filterAccountTransactions(TransactionFilter filter);

    AccountTransaction addAccountTransaction(AccountCredit source, AccountMember sender, AccountCredit receiver,
                                             double amount, double exchangeRate, String reason, String cause,
                                             long time, Collection<AccountTransactionProperty> properties);


    AccountLimitation getAccountLimitation(int id);

    boolean hasAccountLimitation(AccountMember member, Currency currency, double amount);

    AccountLimitation addAccountLimitation(BankAccount account, @Nullable AccountMember member, @Nullable AccountMemberRole memberRole,
                                           Currency comparativeCurrency, AccountLimitation.CalculationType calculationType,
                                           double amount, AccountLimitation.Interval interval);

    boolean removeAccountLimitation(AccountLimitation accountLimitation);
}