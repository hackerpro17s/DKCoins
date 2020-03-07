package net.pretronic.dkcoins.minecraft.commands.currency;

import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.command.object.ObjectCommand;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.message.bml.variable.VariableSet;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.minecraft.Messages;
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
        if(DKCoins.getInstance().getCurrencyManager().searchCurrency(currencyName) != null) {
            commandSender.sendMessage(Messages.ERROR_CURRENCY_ALREADY_EXISTS, VariableSet.create().add("name", currencyName));
            return;
        }
        DKCoins.getInstance().getCurrencyManager().createCurrency(currencyName, symbol);
        commandSender.sendMessage(Messages.COMMAND_CURRENCY_CREATE_DONE, VariableSet.create()
                .add("currency", currencyName).add("symbol", symbol));
    }
}
