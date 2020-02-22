/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 16.02.20, 16:51
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.commands;

import net.prematic.libraries.command.command.Command;
import net.prematic.libraries.command.command.MainObjectCommand;
import net.prematic.libraries.command.command.NotFoundHandler;
import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.minecraft.commands.account.AccountExchangeCommand;
import net.pretronic.dkcoins.minecraft.commands.account.AccountTransferCommand;
import net.pretronic.dkcoins.minecraft.commands.bank.BankCreateCommand;
import net.pretronic.dkcoins.minecraft.commands.bank.BankDeleteCommand;

public class BankObjectCommand extends MainObjectCommand<BankAccount> {

    private final Command createCommand;

    public BankObjectCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("bank"));
        registerCommand(new AccountTransferCommand(owner));
        registerCommand(new AccountExchangeCommand(owner));
        this.createCommand = new BankCreateCommand(owner);
        registerCommand(new BankDeleteCommand(owner));
    }

    @Override
    public BankAccount getObject(String name) {
        return DKCoins.getInstance().getAccountManager().searchAccount(name);
    }

    private class NotFoundHandler implements net.prematic.libraries.command.command.NotFoundHandler {

        @Override
        public void handle(CommandSender commandSender, String command, String[] args) {
            switch (command.toLowerCase()) {
                case "create" : {
                    createCommand.execute(commandSender, args);
                    break;
                }
            }
        }
    }
}