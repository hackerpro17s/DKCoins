package net.pretronic.dkcoins.minecraft.commands.currency;

import net.prematic.libraries.command.NotFindable;
import net.prematic.libraries.command.command.Command;
import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.command.object.MainObjectCommand;
import net.prematic.libraries.command.command.object.ObjectCommand;
import net.prematic.libraries.command.command.object.ObjectNotFindable;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.message.bml.variable.VariableSet;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;

import java.util.Arrays;

public class CurrencyCommand extends MainObjectCommand<Currency> implements NotFindable, ObjectNotFindable {

    private final ObjectCommand<String> createCommand;
    private final Command listCommand;
    private final ObjectCommand<Currency> infoCommand;

    public CurrencyCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.newBuilder().name("currency").permission("dkcoins.command.currency").create());
        this.createCommand = new CurrencyCreateCommand(owner);
        this.listCommand = new CurrencyListCommand(owner);
        this.infoCommand = new CurrencyInfoCommand(owner);
        registerCommand(infoCommand);
        registerCommand(new CurrencyDeleteCommand(owner));
        registerCommand(new CurrencyEditCommand(owner));
    }

    @Override
    public Currency getObject(String currency) {
        return DKCoins.getInstance().getCurrencyManager().getCurrency(currency);
    }

    @Override
    public void commandNotFound(CommandSender commandSender, String command, String[] args) {
        if(command == null || command.equals("")) {
            listCommand.execute(commandSender, args);
        } else {
            Currency currency = getObject(command);
            if(currency != null) {
                infoCommand.execute(commandSender, currency, args);
            } else {
                commandSender.sendMessage(Messages.COMMAND_CURRENCY_HELP);
            }
        }
    }

    @Override
    public void objectNotFound(CommandSender commandSender, String command, String[] args) {
        if(command.equalsIgnoreCase("list")) {
            listCommand.execute(commandSender, args);
        } else if(command.equalsIgnoreCase("help")) {
            commandSender.sendMessage(Messages.COMMAND_CURRENCY_HELP);
        } else if(args.length > 0) {
            if(args[0].equalsIgnoreCase("create")) {
                createCommand.execute(commandSender, command, args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : args);
            }
        } else {
            commandSender.sendMessage(Messages.ERROR_CURRENCY_NOT_EXISTS, VariableSet.create().add("name", command));
        }
    }
}
