package net.pretronic.dkcoins.api.minecraft.events;

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import org.mcnative.common.event.MinecraftEvent;

public interface DKCoinsAccountCreateEvent extends MinecraftEvent {

    DKCoinsUser getCreator();

    BankAccount getAccount();
}
