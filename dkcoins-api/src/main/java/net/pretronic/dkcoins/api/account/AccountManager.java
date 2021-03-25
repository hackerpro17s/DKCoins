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

import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

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

    BankAccount createAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator);

    MasterBankAccount createMasterAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator);

    void deleteAccount(BankAccount account, DKCoinsUser user);


    List<RankedAccountCredit> getTopAccountCredits(Currency currency, AccountType[] excludedAccountTypes, int entriesPerPage, int page);

    BankAccount getAccountByRank(Currency currency, int rank);


    AccountCredit getAccountCredit(int id);

    AccountMember getAccountMember(int id);

    AccountMember getAccountMember(DKCoinsUser user, BankAccount account);
}