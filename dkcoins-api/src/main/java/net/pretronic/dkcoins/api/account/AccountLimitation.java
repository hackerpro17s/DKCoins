/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 14:37
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.account;

import net.pretronic.libraries.utility.annonations.Nullable;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.currency.Currency;

public interface AccountLimitation {

    int getId();

    BankAccount getAccount();

    @Nullable
    AccountMember getMember();

    @Nullable
    AccountMemberRole getMemberRole();

    Currency getComparativeCurrency();

    double getAmount();

    long getInterval();

    String getFormattedInterval();
}
