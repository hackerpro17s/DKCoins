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

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;

import java.util.Collection;
import java.util.UUID;

public interface DKCoinsUser {

    UUID getUniqueId();

    Collection<BankAccount> getAccounts();

    BankAccount getDefaultAccount();

    AccountMember getAsMember(BankAccount account);
}