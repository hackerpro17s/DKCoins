/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 22.02.20, 15:28
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.commands.bank;

import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.command.object.ObjectCommand;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.message.bml.variable.VariableSet;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.dkcoins.minecraft.Messages;
import org.mcnative.common.player.OnlineMinecraftPlayer;

public class BankCreateCommand extends ObjectCommand<String> {

    public BankCreateCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("create"));
    }

    @Override
    public void execute(CommandSender commandSender, String bankName, String[] args) {
        if(!(commandSender instanceof OnlineMinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        OnlineMinecraftPlayer player = (OnlineMinecraftPlayer)commandSender;
        if(args.length < 1) {
            commandSender.sendMessage(Messages.COMMAND_BANK_CREATE_HELP);
            return;
        }
        String accountType0 = args[0];
        if(DKCoins.getInstance().getAccountManager().searchAccount(bankName) != null) {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_ALREADY_EXISTS);
            return;
        }
        AccountType accountType = DKCoins.getInstance().getAccountManager().searchAccountType(accountType0);
        if(accountType == null) {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_TYPE_NOT_EXISTS, VariableSet.create().add("name", accountType0));
            return;
        }
        String requiredPermission = "dkcoins.account.type.permission."+accountType0;
        if(!commandSender.hasPermission(requiredPermission)) {
            commandSender.sendMessage(Messages.ERROR_NO_PERMISSION);
            return;
        }
        DKCoins.getInstance().getAccountManager().createAccount(bankName, accountType, false, null,
                DKCoins.getInstance().getUserManager().getUser(player.getUniqueId()));
        player.sendMessage(Messages.COMMAND_BANK_CREATE_DONE, VariableSet.create().add("name", bankName).add("type", accountType.getName()));
    }
}
