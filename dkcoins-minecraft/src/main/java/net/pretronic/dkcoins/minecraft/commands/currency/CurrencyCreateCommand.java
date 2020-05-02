package net.pretronic.dkcoins.minecraft.commands.currency;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.message.bml.variable.describer.DescribedHashVariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.common.player.OnlineMinecraftPlayer;

public class CurrencyCreateCommand extends ObjectCommand<String> {

    public CurrencyCreateCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("create"));
    }

    @Override
    public void execute(CommandSender commandSender, String currencyName, String[] args) {
        if(!(commandSender instanceof OnlineMinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        if(args.length < 1) {
            commandSender.sendMessage(Messages.COMMAND_CURRENCY_CREATE_HELP);
            return;
        }
        String symbol = args[0];
        Currency currency = DKCoins.getInstance().getCurrencyManager().searchCurrency(currencyName);
        if(currency != null) {
            commandSender.sendMessage(Messages.ERROR_CURRENCY_ALREADY_EXISTS, VariableSet.create()
                    .addDescribed("currency", currency));
            return;
        }
        currency = DKCoins.getInstance().getCurrencyManager().createCurrency(currencyName, symbol);
        commandSender.sendMessage(Messages.COMMAND_CURRENCY_CREATE_DONE, VariableSet.create()
                .addDescribed("currency", currency));
    }
}
