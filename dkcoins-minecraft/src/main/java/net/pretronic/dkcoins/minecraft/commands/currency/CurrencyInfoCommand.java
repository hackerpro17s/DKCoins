package net.pretronic.dkcoins.minecraft.commands.currency;

import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.describer.DescribedHashVariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.common.player.OnlineMinecraftPlayer;

public class CurrencyInfoCommand extends ObjectCommand<Currency> {

    public CurrencyInfoCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("info"));
    }

    @Override
    public void execute(CommandSender commandSender, Currency currency, String[] args) {
        if(!(commandSender instanceof OnlineMinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        commandSender.sendMessage(Messages.COMMAND_CURRENCY_INFO, new DescribedHashVariableSet()
                .add("currency", currency).add("exchangeRates", currency.getExchangeRates()));
    }
}