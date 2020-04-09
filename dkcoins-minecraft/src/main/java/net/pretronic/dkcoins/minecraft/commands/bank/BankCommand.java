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
import net.pretronic.dkcoins.minecraft.DKCoinsConfig;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.dkcoins.minecraft.commands.account.AccountExchangeCommand;
import net.pretronic.dkcoins.minecraft.commands.account.AccountTransferCommand;
import net.pretronic.dkcoins.minecraft.commands.bank.member.BankMemberCommand;
import net.pretronic.libraries.command.command.Command;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.*;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.message.bml.variable.reflect.ReflectVariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

import java.util.Arrays;

public class BankCommand extends MainObjectCommand<BankAccount> implements DefinedNotFindable<BankAccount>, ObjectNotFindable, ObjectCommandPrecondition<BankAccount> {

    private final ObjectCommand<String> createCommand;
    private final Command listCommand;

    public BankCommand(ObjectOwner owner) {
        super(owner, DKCoinsConfig.COMMAND_BANK);
        registerCommand(new AccountTransferCommand(owner, CommandConfiguration.newBuilder().name("transfer").aliases("pay").create()));
        registerCommand(new AccountExchangeCommand(owner));
        this.createCommand = new BankCreateCommand(owner);
        registerCommand(new BankDeleteCommand(owner));
        this.listCommand = new BankListCommand(owner);
        registerCommand(new BankMemberCommand(owner));
        registerCommand(new BankAdminCommand(owner));
        registerCommand(new BankStatementCommand(owner));
        registerCommand(new BankSettingsCommand(owner));
    }

    @Override
    public BankAccount getObject(CommandSender commandSender, String name) {
        return DKCoins.getInstance().getAccountManager().searchAccount(name);
    }

    @Override
    public void commandNotFound(CommandSender commandSender, BankAccount account, String command, String[] args) {
        if(account != null) {
            if(command == null) {
                if(CommandUtil.hasAccountAccessAndSendMessage(commandSender, account)) {
                    commandSender.sendMessage(Messages.COMMAND_BANK_CREDITS, new ReflectVariableSet()
                            .add("credits", account.getCredits()));
                }
            } else {
                commandSender.sendMessage(Messages.COMMAND_BANK_HELP);
            }
        } else {
            listCommand.execute(commandSender, args);
        }
    }

    @Override
    public void objectNotFound(CommandSender commandSender, String command, String[] args) {
        if(command.equalsIgnoreCase("list")) {
            this.listCommand.execute(commandSender, args);
            return;
        } else if(command.equalsIgnoreCase("help")) {
            commandSender.sendMessage(Messages.COMMAND_BANK_HELP);
            return;
        } else if(args.length > 0) {
            if(args[0].equalsIgnoreCase("create")) {
                createCommand.execute(commandSender, command, Arrays.copyOfRange(args, 1, args.length));
                return;
            }
        }
        commandSender.sendMessage(Messages.ERROR_ACCOUNT_NOT_EXISTS, VariableSet.create().add("name", command));
    }

    @Override
    public boolean checkPrecondition(CommandSender commandSender, BankAccount account) {
        System.out.println("pre condition");
        return CommandUtil.hasAccountAccessAndSendMessage(commandSender, account);
    }
}