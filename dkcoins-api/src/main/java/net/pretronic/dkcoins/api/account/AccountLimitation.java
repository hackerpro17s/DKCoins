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

import net.prematic.libraries.utility.annonations.Nullable;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

public interface AccountLimitation {

    int getId();

    Account getAccount();

    @Nullable
    DKCoinsUser getUser();

    @Nullable
    Currency getCurrency();

    double getAmount();

    long getInterval();
}
