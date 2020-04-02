/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 18.11.19, 21:21
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api;

import net.pretronic.dkcoins.api.account.*;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.account.transaction.TransactionFilter;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DKCoinsStorage {

    AccountType getAccountType(int id);

    AccountType searchAccountType(Object identifier);

    AccountType createAccountType(String name, String symbol);

    void updateAccountTypeName(int id, String name);

    void updateAccountTypeSymbol(int id, String symbol);

    void deleteAccountType(int id);


    Collection<Integer> getAccountIds(UUID userId);

    BankAccount getAccount(int id);

    BankAccount searchAccount(Object identifier);

    BankAccount getAccountByCredit(int creditId);

    MasterBankAccount getMasterAccount(int id);

    BankAccount getSubAccount(int masterAccountId, int id);

    MasterBankAccount getSubMasterAccount(int masterAccountId, int id);

    BankAccount createAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator);

    MasterBankAccount createMasterAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator);

    void updateAccountName(int id, String name);

    void updateAccountTypeId(int id, int typeId);

    void updateAccountDisabled(int id, boolean disabled);

    void updateAccountParentId(int id, int parentId);

    void deleteAccount(int id);

    List<Integer> getTopAccountIds(Currency currency, AccountType[] excludedAccountTypes, int limit);

    int getAccountIdByRank(Currency currency, int rank);


    int getAccountCreditAccountId(int id);

    AccountCredit addAccountCredit(BankAccount account, Currency currency, double amount);

    void setAccountCreditAmount(int id, double amount);

    void deleteAccountCredit(int id);


    int getAccountLimitationAccountId(int id);

    AccountLimitation addAccountLimitation(BankAccount account, AccountMember accountMember, AccountMemberRole memberRole,
                                           Currency comparativeCurrency, double amount, long interval);

    void removeAccountLimitation(int id);


    //Returns bank id to get by bank
    int getAccountMemberAccountId(int id);

    AccountMember addAccountMember(BankAccount account, DKCoinsUser user, AccountMemberRole role);

    void updateAccountMemberRole(AccountMember member);

    void removeAccountMember(int id);


    Collection<AccountTransaction> getAccountTransactions(int senderId, int start, int end);

    AccountTransaction addAccountTransaction(AccountCredit source, AccountMember sender, AccountCredit receiver,
                                             double amount, double exchangeRate, String reason, String cause,
                                             long time, Collection<AccountTransactionProperty> properties);


    Collection<Currency> getCurrencies();

    Currency getCurrency(int id);

    Currency getCurrency(String name);

    Currency searchCurrency(Object identifier);

    Currency createCurrency(String name, String symbol);

    void updateCurrencyName(int id, String name);

    void updateCurrencySymbol(int id, String symbol);

    void deleteCurrency(int id);


    CurrencyExchangeRate getCurrencyExchangeRate(int id);

    CurrencyExchangeRate getCurrencyExchangeRate(int currencyId, int targetCurrencyId);

    CurrencyExchangeRate createCurrencyExchangeRate(int selectedCurrencyId, int targetCurrencyId, double exchangeAmount);

    void updateCurrencyExchangeAmount(int selectedId, int targetId, double exchangeAmount);

    void deleteCurrencyExchangeRate(int id);



    DKCoinsUser getUser(UUID uniqueId);

    List<AccountTransaction> filterAccountTransactions(TransactionFilter filter);
}