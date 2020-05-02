/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 19.11.19, 21:05
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.account;

import net.pretronic.dkcoins.api.account.AccountType;

public class DefaultAccountType implements AccountType {

    private final int id;
    private final String name;
    private final String symbol;

    public DefaultAccountType(int id, String name, String symbol) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSymbol() {
        return this.symbol;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AccountType && ((AccountType)obj).getId() == this.id;
    }
}
