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

import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import org.mcnative.common.player.OnlineMinecraftPlayer;

public class BankDeleteCommand extends ObjectCommand<BankAccount> {

    public BankDeleteCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("delete"));
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] strings) {
        if(!(commandSender instanceof OnlineMinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        if(CommandUtil.hasAccessAndSendMessage(commandSender, account, AccessRight.DELETE)) {
            if(!account.getType().getName().equalsIgnoreCase("User")) {
                DKCoins.getInstance().getAccountManager().deleteAccount(account, CommandUtil.getUserByCommandSender(commandSender));
                commandSender.sendMessage(Messages.COMMAND_BANK_DELETE_DONE, VariableSet.create()
                        .add("name", account.getName()));
            } else {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_USER_DELETE_NOT_POSSIBLE);
            }
        }
    }
}
