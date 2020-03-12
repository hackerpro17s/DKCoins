package net.pretronic.dkcoins.minecraft.commands.currency;

import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.dkcoins.minecraft.Messages;
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
        commandSender.sendMessage(Messages.COMMAND_CURRENCY_INFO_HEADER, VariableSet.create()
                .add("name", currency.getName()).add("symbol", currency.getSymbol()));
        commandSender.sendMessage(Messages.COMMAND_CURRENCY_INFO_EXCHANGE_RATE_HEADER);

        for (Currency target : DKCoins.getInstance().getCurrencyManager().getCurrencies()) {
            CurrencyExchangeRate exchangeRate = currency.getExchangeRate(target);
            commandSender.sendMessage(Messages.COMMAND_CURRENCY_INFO_EXCHANGE_RATE_LIST, VariableSet.create()
                    .add("target", target.getName()).add("exchangeAmount", exchangeRate.getExchangeAmount()));
        }
    }
}
