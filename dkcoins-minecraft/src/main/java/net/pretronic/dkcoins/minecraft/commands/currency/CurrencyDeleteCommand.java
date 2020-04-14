package net.pretronic.dkcoins.minecraft.commands.currency;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.describer.DescribedHashVariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
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
        commandSender.sendMessage(Messages.COMMAND_CURRENCY_DELETE_DONE, new DescribedHashVariableSet()
                .add("currency", currency));
    }
}
