/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 03.02.20, 00:12
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.account;

import net.pretronic.dkcoins.api.user.DKCoinsUser;

public interface MasterBankAccount extends BankAccount {

    BankAccount getSubAccount(int id);

    BankAccount createSubAccount(String name, AccountType type, boolean disabled, DKCoinsUser creator);

    MasterBankAccount createSubMasterAccount(String name, AccountType type, boolean disabled, DKCoinsUser creator);

    void deleteSubAccount(BankAccount account);
}