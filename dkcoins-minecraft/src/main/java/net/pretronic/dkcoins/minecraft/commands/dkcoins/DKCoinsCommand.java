package net.pretronic.dkcoins.minecraft.commands.dkcoins;

import net.pretronic.dkcoins.minecraft.DKCoinsPlugin;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.NotFindable;
import net.pretronic.libraries.command.command.MainCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

public class DKCoinsCommand extends MainCommand implements NotFindable {

    public DKCoinsCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("dkcoins"));
        registerCommand(new DKCoinsMigrateCommand(owner));
        registerCommand(new DKCoinsBankAdminCommand(owner));
    }

    @Override
    public void commandNotFound(CommandSender sender, String command, String[] args) {
        if(sender.hasPermission("dkcoins.admin")) {
            sender.sendMessage(Messages.COMMAND_DKCOINS_HELP);
        } else {
            sender.sendMessage(String.format("DKCoins v%s was programmed by Pretronic (https://pretronic.net)",
                    DKCoinsPlugin.getInstance().getDescription().getVersion().getName()));
        }
    }
}
