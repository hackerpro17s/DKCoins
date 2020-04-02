/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 31.01.20, 22:01
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.account.transaction;

import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.currency.Currency;

import java.util.Collection;

public interface AccountTransaction {

    int getId();

    AccountCredit getSource();

    AccountMember getSender();

    AccountCredit getReceiver();

    double getAmount();

    String getFormattedAmount();

    double getExchangeRate();

    String getFormattedExchangeRate();

    default Currency getCurrency() {
        return getSource().getCurrency();
    }

    String getReason();

    String getCause();

    long getTime();

    String getFormattedTime();

    Collection<AccountTransactionProperty> getProperties();

    AccountTransactionProperty getProperty(String key);
}
