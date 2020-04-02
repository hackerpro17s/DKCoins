package net.pretronic.dkcoins.minecraft.commands;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.migration.Migration;
import net.pretronic.dkcoins.minecraft.DKCoinsPlugin;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.MinecraftDKCoins;
import net.pretronic.libraries.command.command.BasicCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.command.sender.ConsoleCommandSender;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.common.McNative;

public class DKCoinsCommand extends BasicCommand {

    public DKCoinsCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("dkcoins"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length != 0 && sender.hasPermission("dkcoins.admin")) {
            if(args[0].equalsIgnoreCase("migrate")) {
                if(!sender.equals(McNative.getInstance().getConsoleSender())) {
                    //help
                    return;
                }
                if(args.length == 2) {
                    String migrationName = args[1];

                    Migration migration = DKCoins.getInstance().getMigration(migrationName);
                    if(migration == null) {
                        DKCoinsPlugin.getInstance().getLogger().info("Migration " + migrationName + " does not exist");
                        return;
                    }
                    DKCoinsPlugin.getInstance().getLogger().info("Starting migration of " + migration.getName());
                    DKCoinsPlugin.getInstance().getLogger().info("This may take a while...");
                    try {
                        if(migration.migrate()) {
                            DKCoinsPlugin.getInstance().getLogger().info("Migration was successful");
                        } else {
                            DKCoinsPlugin.getInstance().getLogger().error("Migration failed");
                        }
                        return;
                    } catch (Exception exception) {

                        DKCoinsPlugin.getInstance().getLogger().error("Migration failed");
                    }
                }
            }
            //Help command
        } else {
            //info
        }

    }
}
