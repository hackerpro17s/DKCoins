/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 22.02.20, 16:02
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.commands.bank;

import net.prematic.libraries.command.command.ObjectCommand;
import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.minecraft.Messages;
import org.mcnative.common.player.OnlineMinecraftPlayer;

public class BankDeleteCommand extends ObjectCommand<BankAccount> {

    public BankDeleteCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.newBuilder().name("delete").permission("dkcoins.command.account.delete").create());
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] strings) {
        if(!(commandSender instanceof OnlineMinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        OnlineMinecraftPlayer player = (OnlineMinecraftPlayer)commandSender;
        AccountMember member = account.getMember(DKCoins.getInstance().getUserManager().getUser(player.getUniqueId()));
        if(!member.canAccess(AccessRight.DELETE)) {
            commandSender.sendMessage(Messages.ACCOUNT_ACCESS_DENY_DELETE);
            return;
        }
        DKCoins.getInstance().getAccountManager().deleteAccount(account);
    }
}
