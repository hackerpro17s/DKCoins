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

import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

import java.util.Collection;

public interface AccountManager {

    Collection<Account> getAccounts(DKCoinsUser user);

    Collection<AccountTransaction> getTransactions(long start, long end);

    Account createAccount(String name, AccountType type, boolean blocked, Account parent, DKCoinsUser creator);

    void removeAccount(Account account);
}