/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 14:29
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.account.transaction;

import net.prematic.libraries.document.Document;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.member.AccountMember;

public interface AccountTransaction {

    int getId();

    AccountMember getExecutor();

    AccountCredit getSender();

    AccountCredit getReceiver();

    double getAmount();

    double getExchangeRate();

    String getReason();

    String getServer();

    String getWorld();

    long getTime();

    Document getProperties();
}
