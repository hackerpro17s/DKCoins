package net.pretronic.dkcoins.minecraft.commands.currency;

import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.command.object.ObjectCommand;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.message.bml.variable.VariableSet;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
import org.mcnative.common.player.OnlineMinecraftPlayer;

public class CurrencyDeleteCommand extends ObjectCommand<Currency> {

    public CurrencyDeleteCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("delete"));
    }

    @Override
    public void execute(CommandSender commandSender, Currency currency, String[] strings) {
        if(!(commandSender instanceof OnlineMinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        DKCoins.getInstance().getCurrencyManager().deleteCurrency(currency);
        commandSender.sendMessage(Messages.COMMAND_CURRENCY_DELETE_DONE, VariableSet.create().add("name", currency.getName()));
    }
}
