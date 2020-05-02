package net.pretronic.dkcoins.minecraft.account;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.RankedAccountCredit;

public class DefaultRankedAccountCredit implements RankedAccountCredit {

    private final int rank;
    private final int creditId;

    public DefaultRankedAccountCredit(int rank, int creditId) {
        this.rank = rank;
        this.creditId = creditId;
    }

    @Override
    public int getPosition() {
        return this.rank;
    }

    @Override
    public AccountCredit getCredit() {
        return DKCoins.getInstance().getAccountManager().getAccountCredit(this.creditId);
    }
}
