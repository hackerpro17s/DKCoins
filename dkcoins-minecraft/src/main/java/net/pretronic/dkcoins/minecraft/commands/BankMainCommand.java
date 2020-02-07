/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 04.02.20, 15:12
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.commands;

import net.prematic.libraries.command.command.MainCommand;
import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.utility.interfaces.ObjectOwner;

public class BankMainCommand extends MainCommand {

    public BankMainCommand(ObjectOwner owner, CommandConfiguration configuration) {
        super(owner, configuration);
    }
}
