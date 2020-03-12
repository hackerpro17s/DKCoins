package net.pretronic.dkcoins.api.minecraft.events;

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

public class DKCoinsAccountCreateEvent extends DKCoinsAccountEvent {

    private final DKCoinsUser creator;

    public DKCoinsAccountCreateEvent(BankAccount account, DKCoinsUser creator) {
        super(account);
        this.creator = creator;
    }

    public DKCoinsUser getCreator() {
        return creator;
    }
}
