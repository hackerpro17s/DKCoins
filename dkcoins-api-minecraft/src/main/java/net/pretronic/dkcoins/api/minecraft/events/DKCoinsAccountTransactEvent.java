package net.pretronic.dkcoins.api.minecraft.events;

import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import org.mcnative.common.event.MinecraftEvent;

public interface DKCoinsAccountTransactEvent extends MinecraftEvent {

    AccountTransaction getTransaction();
}
