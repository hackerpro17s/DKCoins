package net.pretronic.dkcoins.api.minecraft.events;

import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

public class DKCoinsAccountMemberRemoveEvent extends DKCoinsAccountEvent {

    private final DKCoinsUser removedUser;
    private final AccountMember remover;

    public DKCoinsAccountMemberRemoveEvent(DKCoinsUser removedUser, AccountMember remover) {
        super(remover.getAccount());
        this.removedUser = removedUser;
        this.remover = remover;
    }

    public DKCoinsUser getRemovedUser() {
        return removedUser;
    }

    public AccountMember getRemover() {
        return remover;
    }
}
