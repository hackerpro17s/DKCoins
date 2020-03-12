package net.pretronic.dkcoins.api.minecraft.events;

import net.pretronic.dkcoins.api.account.BankAccount;

public class DKCoinsAccountEvent extends DKCoinsEvent {

    private final BankAccount account;

    public DKCoinsAccountEvent(BankAccount account) {
        this.account = account;
    }

    public BankAccount getAccount() {
        return account;
    }
}
