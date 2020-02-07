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

import net.prematic.libraries.document.Document;
import net.prematic.libraries.utility.annonations.Nullable;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

import java.util.Collection;

public interface AccountManager {

    AccountType getAccountType(int id);


    BankAccount getAccount(int id);

    BankAccount searchAccount(Object identifier);

    MasterBankAccount getMasterAccount(int id);

    BankAccount getSubAccount(MasterBankAccount account, int id);

    MasterBankAccount getSubMasterAccount(MasterBankAccount account, int id);

    BankAccount createAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator);

    MasterBankAccount createMasterAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator);

    void deleteAccount(BankAccount account);

    void updateAccountName(BankAccount account);

    void updateAccountDisabled(BankAccount account);


    AccountCredit getAccountCredit(int id);

    AccountCredit addAccountCredit(BankAccount account, Currency currency, double amount);

    void setAccountCreditAmount(AccountCredit credit, double amount);


    AccountMember getAccountMember(int id);

    AccountMember getAccountMember(DKCoinsUser user, BankAccount account);

    AccountMember addAccountMember(BankAccount account, DKCoinsUser user, AccountMemberRole memberRole);

    void deleteAccountMember(AccountMember member);


    Collection<AccountTransaction> getAccountTransactions(AccountMember member, int start, int end);

    AccountTransaction addAccountTransaction(AccountCredit source, AccountMember sender, AccountCredit receiver,
                                             double amount, double exchangeRate, String reason, String cause,
                                             long time, Collection<AccountTransactionProperty> properties);


    boolean hasLimitation(BankAccount account, Currency currency, double amount);

    boolean hasLimitation(BankAccount account, AccountMemberRole memberRole, Currency currency, double amount);

    boolean hasLimitation(AccountMember member, Currency currency, double amount);

    AccountLimitation addAccountLimitation(BankAccount account, @Nullable AccountMember member, @Nullable AccountMemberRole memberRole,
                                           Currency comparativeCurrency, double amount, long interval);

    void deleteAccountLimitation(AccountLimitation accountLimitation);
}