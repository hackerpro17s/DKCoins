/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 18.11.19, 21:19
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.user;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

public class DefaultDKCoinsUser implements DKCoinsUser {

    private final int id;

    public DefaultDKCoinsUser(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public BankAccount getDefaultAccount() {
        return null;
    }

    @Override
    public AccountMember getAsMember(BankAccount account) {
        return DKCoins.getInstance().getAccountManager().getAccountMember(this, account);
    }
}
