package net.pretronic.dkcoins.api.minecraft.events;

import net.pretronic.dkcoins.api.account.member.AccountMember;
import org.mcnative.common.event.MinecraftEvent;

public interface DKCoinsAccountMemberAddEvent extends MinecraftEvent {

    AccountMember getMember();

    AccountMember getAdder();
}
