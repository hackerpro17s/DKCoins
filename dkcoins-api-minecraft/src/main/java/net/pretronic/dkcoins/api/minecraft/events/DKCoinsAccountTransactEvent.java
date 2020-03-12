package net.pretronic.dkcoins.api.minecraft.events;

import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;

public class DKCoinsAccountTransactEvent extends DKCoinsAccountEvent {

    private final AccountTransaction transaction;

    public DKCoinsAccountTransactEvent(AccountTransaction transaction) {
        super(transaction.getSource().getAccount());
        this.transaction = transaction;
    }

    public AccountTransaction getTransaction() {
        return transaction;
    }
}
