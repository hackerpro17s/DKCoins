/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 03.02.20, 00:13
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.common.account;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.MasterBankAccount;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.libraries.utility.Iterators;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultMasterBankAccount extends DefaultBankAccount implements MasterBankAccount {

    private final Collection<BankAccount> subAccounts;

    public DefaultMasterBankAccount(int id, String name, AccountType type, boolean disabled, MasterBankAccount parent) {
        super(id, name, type, disabled, parent);
        this.subAccounts = new ArrayList<>();
    }

    @Override
    public BankAccount getSubAccount(int id) {
        BankAccount account = Iterators.findOne(this.subAccounts, account0 -> account0.getId() == id);
        if(account == null) {
            account = DKCoins.getInstance().getAccountManager().getAccount(id);
            if(account.getParent().getId() != getId()) account = null;
        }
        if(account != null) this.subAccounts.add(account);
        return account;
    }

    @Override
    public BankAccount createSubAccount(String name, AccountType type, boolean disabled, DKCoinsUser creator) {
        BankAccount account = DKCoins.getInstance().getAccountManager().createAccount(name, type, disabled, this, creator);
        this.subAccounts.add(account);
        return account;
    }

    @Override
    public MasterBankAccount createSubMasterAccount(String name, AccountType type, boolean disabled, DKCoinsUser creator) {
        MasterBankAccount account = DKCoins.getInstance().getAccountManager().createMasterAccount(name, type, disabled, this, creator);
        this.subAccounts.add(account);
        return account;
    }

    @Override
    public void deleteSubAccount(BankAccount account) {
        if(getSubAccount(account.getId()) != null) {
            //@Todo deleter
            DKCoins.getInstance().getAccountManager().deleteAccount(account, null);
            this.subAccounts.remove(account);
        } else {
            throw new IllegalArgumentException(String.format("Bank account [%s] is not a sub account of [%s]",
                    account.getId(), this.getId()));
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MasterBankAccount && ((MasterBankAccount)obj).getId() == getId();
    }
}
