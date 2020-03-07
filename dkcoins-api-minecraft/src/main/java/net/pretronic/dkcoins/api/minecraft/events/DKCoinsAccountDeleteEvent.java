package net.pretronic.dkcoins.api.minecraft.events;

import net.pretronic.dkcoins.api.user.DKCoinsUser;
import org.mcnative.common.event.MinecraftEvent;

public interface DKCoinsAccountDeleteEvent extends MinecraftEvent {

    DKCoinsUser getDeleter();

    int getBankAccountId();
}
