/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 14:44
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.account.member;

import net.pretronic.dkcoins.api.account.Account;
import net.pretronic.dkcoins.api.account.AccountLimitation;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

import java.util.Collection;

public interface AccountMember {

    Account getAccount();

    DKCoinsUser getUser();

    AccountMemberRole getRole();

    Collection<AccountLimitation> getLimitations();
}
