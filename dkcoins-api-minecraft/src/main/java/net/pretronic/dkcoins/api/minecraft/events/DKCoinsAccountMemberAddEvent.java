package net.pretronic.dkcoins.api.minecraft.events;

import net.pretronic.dkcoins.api.account.member.AccountMember;

public class DKCoinsAccountMemberAddEvent extends DKCoinsAccountEvent {

    private final AccountMember addedMember;
    private final AccountMember adder;

    public DKCoinsAccountMemberAddEvent(AccountMember addedMember, AccountMember adder) {
        super(addedMember.getAccount());
        this.addedMember = addedMember;
        this.adder = adder;
    }

    public AccountMember getAddedMember() {
        return addedMember;
    }

    public AccountMember getAdder() {
        return adder;
    }
}
