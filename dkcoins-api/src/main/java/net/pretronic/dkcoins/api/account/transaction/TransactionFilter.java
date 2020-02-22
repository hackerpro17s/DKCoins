/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 15.02.20, 21:24
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.account.transaction;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.currency.Currency;

public interface TransactionFilter {

    TransactionFilter account(BankAccount account);

    TransactionFilter world(String world);

    TransactionFilter server(String server);

    TransactionFilter time(long time);

    TransactionFilter receiver(AccountCredit receiver);

    TransactionFilter currency(Currency currency);

    TransactionFilter reason(String reason);

    TransactionFilter cause(String cause);

    TransactionFilter page(int page);

    static TransactionFilter newFilter() {
        return DKCoins.getInstance().newTransactionFilter();
    }
}
