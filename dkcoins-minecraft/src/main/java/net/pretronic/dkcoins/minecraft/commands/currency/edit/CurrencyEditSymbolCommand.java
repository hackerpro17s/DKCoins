package net.pretronic.dkcoins.minecraft.commands.currency.edit;

import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

public class CurrencyEditSymbolCommand extends ObjectCommand<Currency> {

    public CurrencyEditSymbolCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("symbol"));
    }

    @Override
    public void execute(CommandSender sender, Currency currency, String[] args) {
        if(args.length != 1) {
            sender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_HELP);
            return;
        }
        final String oldSymbol = currency.getSymbol();
        String symbol = args[0];
        currency.setSymbol(symbol);
        sender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_DONE_SYMBOL, VariableSet.create()
                .add("oldSymbol", oldSymbol)
                .addDescribed("currency", currency));
    }
}
