/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 14:19
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.user;

import net.pretronic.dkcoins.api.account.Account;
import net.pretronic.dkcoins.api.account.member.AccountMember;

import java.util.Collection;

public interface DKCoinsUser {

    int getId();

    Collection<Account> getAccounts();

    Account getDefaultAccount();

    AccountMember getAsMember(Account account);
}