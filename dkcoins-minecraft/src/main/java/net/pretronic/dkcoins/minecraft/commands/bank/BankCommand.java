/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 16.02.20, 16:51
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.commands.bank;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.dkcoins.minecraft.commands.bank.limit.BankLimitCommand;
import net.pretronic.dkcoins.minecraft.commands.bank.member.BankMemberCommand;
import net.pretronic.dkcoins.minecraft.commands.bank.role.BankRoleCommand;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.command.Command;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.*;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.map.Triple;

import java.util.Arrays;

public class BankCommand extends MainObjectCommand<BankAccount> implements DefinedNotFindable<BankAccount>, ObjectNotFindable {

    private final ObjectCommand<String> createCommand;
    private final BankInfoCommand infoCommand;
    private final Command listCommand;

    public BankCommand(ObjectOwner owner) {
        super(owner, DKCoinsConfig.COMMAND_BANK);

        this.createCommand = new BankCreateCommand(owner);
        this.infoCommand = new BankInfoCommand(owner);
        this.listCommand = new BankListCommand(owner);

        registerCommand(new BankTransferCommand(owner, CommandConfiguration.newBuilder().name("transfer").aliases("pay").create(),Messages.COMMAND_BANK_TRANSFER_HELP));
        registerCommand(new BankExchangeCommand(owner));
        registerCommand(new BankDeleteCommand(owner));
        registerCommand(new BankMemberCommand(owner));
        registerCommand(new BankStatementCommand(owner));
        registerCommand(new BankSettingsCommand(owner));
        registerCommand(new BankRoleCommand(owner));
        registerCommand(new BankLimitCommand(owner, Messages.COMMAND_BANK_LIMIT_HELP));
        registerCommand(infoCommand);
    }

    @Override
    public BankAccount getObject(CommandSender commandSender, String name) {
        if(name.equalsIgnoreCase("list")) return null;
        return DKCoins.getInstance().getAccountManager().searchAccount(name);
    }

    @Override
    public void commandNotFound(CommandSender commandSender, BankAccount account, String command, String[] args) {
        System.out.println("command not found " + command + "|" + Arrays.toString(args));
        if(account != null && command == null) {
            infoCommand.execute(commandSender, account, args);
        } else {
            if(command == null && (args == null || args.length == 0)) {
                listCommand.execute(commandSender, args);
            } else {
                if(account != null && "create".equalsIgnoreCase(command) || "c".equalsIgnoreCase(command)) {
                    commandSender.sendMessage(Messages.ERROR_ACCOUNT_ALREADY_EXISTS, VariableSet.create()
                            .addDescribed("account", account));
                } else {
                    commandSender.sendMessage(Messages.COMMAND_BANK_HELP);
                }
            }
        }
    }

    @Override
    public void objectNotFound(CommandSender commandSender, String command, String[] args) {
        System.out.println("object not found " + command + "|" + Arrays.toString(args));
        if(command.equalsIgnoreCase("list") || command.equalsIgnoreCase("l")) {
            this.listCommand.execute(commandSender, args);
        } else if(args.length > 0 && (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("c"))) {
            createCommand.execute(commandSender, command, Arrays.copyOfRange(args, 1, args.length));
        } else {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_NOT_EXISTS, VariableSet.create()
                    .add("name", command));
        }
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        if(!CommandUtil.hasAccountAccess(commandSender, account, AccessRight.VIEW)) {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_NO_ACCESS);
        }
        super.execute(commandSender, account, args);
    }
}