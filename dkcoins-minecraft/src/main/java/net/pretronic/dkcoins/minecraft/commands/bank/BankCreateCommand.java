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

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.message.bml.variable.reflect.ReflectVariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
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
        BankAccount account = DKCoins.getInstance().getAccountManager().searchAccount(bankName);
        if(account != null) {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_ALREADY_EXISTS, new ReflectVariableSet().add("account", account));
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
        account = DKCoins.getInstance().getAccountManager().createAccount(bankName, accountType, false, null,
                DKCoins.getInstance().getUserManager().getUser(player.getUniqueId()));
        player.sendMessage(Messages.COMMAND_BANK_CREATE_DONE, new ReflectVariableSet()
                .add("account", account));
    }
}
