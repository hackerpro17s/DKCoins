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
            printHelpMessage(sender);
        } else {
            sender.sendMessage(String.format("DKCoins v%s was programmed by Pretronic (https://pretronic.net)",
                    DKCoinsPlugin.getInstance().getDescription().getVersion().getName()));
        }
    }

    private void printHelpMessage(CommandSender sender) {
        sender.sendMessage(Messages.PREFIX + " Invalid usage of dkcoins command:");
        sender.sendMessage("&7/dkcoins migrate <name> [currency] &8- &7Migrate old coin system to DKCoins");
    }
}
