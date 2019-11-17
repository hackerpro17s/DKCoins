/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 14:19
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.storage;

import net.prematic.libraries.document.Document;
import net.prematic.libraries.utility.annonations.Nullable;
import net.pretronic.dkcoins.api.account.Account;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.AccountLimitation;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

import java.util.Collection;

public interface DKCoinsStorage {

    DKCoinsUser loadUser(int id);

    Collection<AccountType> loadAccountTypes();

    Collection<Account> loadAccounts(DKCoinsUser user);

    Collection<AccountTransaction> loadTransactions(long start, long end);

    Collection<Currency> loadCurrencies();

    AccountType createAccountType(String name, String symbol);

    //update

    void deleteAccountType(int id);

    Account createAccount(String name, int typeId, boolean blocked, int parentId, DKCoinsUser creator);

    void deleteAccount(int id);

    AccountCredit addAccountCredit(int accountId, int currencyId, double amount);

    void deleteAccountCredit(int id);

    AccountLimitation addAccountLimitation(int accountId, @Nullable int userId, int currencyId, double amount, long interval);

    void updateAccountLimitation(int accountId, @Nullable int userId, int currencyId, double amount, long interval);

    void deleteAccountLimitation(int accountId, @Nullable int userId);

    AccountMember addAccountMember(int accountId, int userId, AccountMemberRole role);

    void updateAccountMember(int accountId, int userId, AccountMemberRole role);

    void removeAccountMember(int memberId);

    void removeAccountMember(int accountId, int userId);

    AccountTransaction addTransaction(int accountCreditId, int userId, double amount, int currencyId, String reason, String server, String world, long time, Document properties);

    Currency addCurrency(String name, String symbol);

    void updateCurrency(int id, String name, String symbol);

    void deleteCurrency(int id);

    CurrencyExchangeRate addCurrencyExchangeRate(int selectedCurrencyId, int targetCurrencyId, double exchangeAmount);

    CurrencyExchangeRate updateCurrencyExchangeRate(int selectedCurrencyId, int targetCurrencyId, double exchangeAmount);

    void deleteCurrencyExchangeRate(int selectedCurrencyId, int targetCurrencyId);
}