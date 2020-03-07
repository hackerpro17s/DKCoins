package net.pretronic.dkcoins.minecraft.commands.currency;

import net.prematic.libraries.command.command.BasicCommand;
import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.message.bml.variable.VariableSet;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
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
        commandSender.sendMessage(Messages.COMMAND_CURRENCY_LIST_HEADER);
        for (Currency currency : DKCoins.getInstance().getCurrencyManager().getCurrencies()) {
            commandSender.sendMessage(Messages.COMMAND_CURRENCY_LIST_CURRENCIES, VariableSet.create()
                    .add("name", currency.getName()).add("symbol", currency.getSymbol()));
        }
    }
}
