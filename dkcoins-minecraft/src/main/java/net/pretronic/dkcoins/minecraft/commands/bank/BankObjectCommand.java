/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 04.02.20, 15:18
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.commands.bank;

import net.prematic.libraries.command.command.MainObjectCommand;
import net.prematic.libraries.command.command.ObjectCommand;
import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.account.BankAccount;

public class BankObjectCommand extends ObjectCommand<BankAccount> {

    public BankObjectCommand(ObjectOwner owner, CommandConfiguration configuration) {
        super(owner, configuration);
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] strings) {
        
    }



}
