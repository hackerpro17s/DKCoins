package net.pretronic.dkcoins.api.minecraft.events;

import net.pretronic.dkcoins.api.user.DKCoinsUser;

public class DKCoinsAccountDeleteEvent extends DKCoinsEvent {

    private final int bankAccountId;
    private final DKCoinsUser deleter;

    public DKCoinsAccountDeleteEvent(int bankAccountId, DKCoinsUser deleter) {
        this.deleter = deleter;
        this.bankAccountId = bankAccountId;
    }

    public int getBankAccountId() {
        return bankAccountId;
    }

    public DKCoinsUser getDeleter() {
        return deleter;
    }
}
