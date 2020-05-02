package net.pretronic.dkcoins.minecraft.commands.dkcoins;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.migration.Migration;
import net.pretronic.dkcoins.minecraft.DKCoinsPlugin;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.command.BasicCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.common.McNative;

import java.util.concurrent.TimeUnit;

public class DKCoinsMigrateCommand extends BasicCommand {

    public DKCoinsMigrateCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.newBuilder().name("migrate").permission("dkcoins.admin").create());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
     
            if(!sender.equals(McNative.getInstance().getConsoleSender())) {
                sender.sendMessage(Messages.ERROR_ONLY_FROM_CONSOLE);
                return;
            }
            if(args.length == 1 || args.length == 2) {
                String migrationName = args[0];

                Migration migration = DKCoins.getInstance().getMigration(migrationName);
                if(migration == null) {
                    DKCoinsPlugin.getInstance().getLogger().error("Migration " + migrationName + " does not exist");
                    return;
                }
                Currency currency = args.length == 2 ? DKCoins.getInstance().getCurrencyManager().getCurrency(args[1])
                        : DKCoinsConfig.CURRENCY_DEFAULT;

                if(currency == null) {
                    DKCoinsPlugin.getInstance().getLogger().error("{} currency not found", args[1]);
                    return;
                }

                DKCoinsPlugin.getInstance().getLogger().info("Starting migration of " + migration.getName());
                DKCoinsPlugin.getInstance().getLogger().info("This may take a while...");
                try {
                    Migration.Result result = migration.migrate(currency);
                    if(result.isSuccess()) {
                        DKCoinsPlugin.getInstance().getLogger().info("Migration was successful");
                        DKCoins.getInstance().getLogger().info("A total of {} was migrated. {} users skipped. {} McNative user data and {} DKCoins account were created",
                                result.getTotalMigrateCount(), result.getSkipped(), result.getMcNativeMigrateCount(), result.getDKCoinsAccountMigrateCount());

                        long millis = result.getTime();
                        String time = String.format("%02d:%02d:%02d",
                                TimeUnit.MILLISECONDS.toHours(millis),
                                TimeUnit.MILLISECONDS.toMinutes(millis)-TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                                TimeUnit.MILLISECONDS.toSeconds(millis)-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
                        DKCoins.getInstance().getLogger().info("It took " + time);
                    } else {
                        DKCoinsPlugin.getInstance().getLogger().error("Migration failed");
                    }
                } catch (Exception exception) {
                    DKCoinsPlugin.getInstance().getLogger().error("Migration failed");
                    throw new RuntimeException(exception);
                }
            } else {
                DKCoinsPlugin.getInstance().getLogger().info("Invalid usage of migration command. Use \"dkcoins migrate <name> [currency]\"");
                DKCoins.getInstance().getLogger().info("Following migrations are available:");
                DKCoins.getInstance().getMigrations().forEach(migration -> DKCoins.getInstance().getLogger()
                        .info("- {}", migration.getName()));
            }
    }
}
