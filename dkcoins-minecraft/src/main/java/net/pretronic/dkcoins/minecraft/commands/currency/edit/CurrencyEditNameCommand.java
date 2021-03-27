package net.pretronic.dkcoins.minecraft.commands.currency.edit;

import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

public class CurrencyEditNameCommand extends ObjectCommand<Currency> {

    public CurrencyEditNameCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("name"));
    }

    @Override
    public void execute(CommandSender sender, Currency currency, String[] args) {
        if(args.length != 1) {
            sender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_HELP);
            return;
        }
        final String oldName = currency.getName();
        String name = args[0];
        currency.setName(name);
        sender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_DONE_NAME, VariableSet.create()
                .add("oldName", oldName)
                .addDescribed("currency", currency));
    }
}
