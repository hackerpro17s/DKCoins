package net.pretronic.dkcoins.minecraft.commands.currency;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.command.BasicCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.common.player.OnlineMinecraftPlayer;

public class CurrencyListCommand extends BasicCommand {

    public CurrencyListCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("list"));
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof OnlineMinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        commandSender.sendMessage(Messages.COMMAND_CURRENCY_LIST, VariableSet.create()
                        .addDescribed("currencies", DKCoins.getInstance().getCurrencyManager().getCurrencies()));
    }
}
