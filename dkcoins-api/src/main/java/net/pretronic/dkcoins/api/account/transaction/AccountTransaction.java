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

import net.prematic.libraries.document.Document;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.member.AccountMember;

import java.util.Collection;

public interface AccountTransaction {

    int getId();

    AccountCredit getSource();

    AccountMember getSender();

    AccountCredit getReceiver();

    double getAmount();

    double getExchangeRate();

    String getReason();

    String getCause();

    long getTime();

    Collection<AccountTransactionProperty> getProperties();

    AccountTransactionProperty getProperty(String key);
}
