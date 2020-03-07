package net.pretronic.dkcoins.api.minecraft.events;

import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import org.mcnative.common.event.MinecraftEvent;

public interface DKCoinsAccountMemberRemoveEvent extends MinecraftEvent {

    DKCoinsUser getRemovedUser();

    AccountMember getRemover();
}
