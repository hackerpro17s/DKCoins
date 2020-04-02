package net.pretronic.dkcoins.minecraft.commands.currency;

import net.pretronic.dkcoins.minecraft.DKCoinsConfig;
import net.pretronic.libraries.command.NotFindable;
import net.pretronic.libraries.command.command.Command;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.MainObjectCommand;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.command.object.ObjectNotFindable;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;

import java.util.Arrays;

public class CurrencyCommand extends MainObjectCommand<Currency> implements NotFindable, ObjectNotFindable {

    private final ObjectCommand<String> createCommand;
    private final Command listCommand;
    private final ObjectCommand<Currency> infoCommand;

    public CurrencyCommand(ObjectOwner owner) {
        super(owner, DKCoinsConfig.COMMAND_CURRENCY);
        this.createCommand = new CurrencyCreateCommand(owner);
        this.listCommand = new CurrencyListCommand(owner);
        this.infoCommand = new CurrencyInfoCommand(owner);
        registerCommand(infoCommand);
        registerCommand(new CurrencyDeleteCommand(owner));
        registerCommand(new CurrencyEditCommand(owner));
    }

    @Override
    public Currency getObject(CommandSender sender, String currency) {
        return DKCoins.getInstance().getCurrencyManager().getCurrency(currency);
    }

    @Override
    public void commandNotFound(CommandSender commandSender, String command, String[] args) {
        System.out.println("not found");
        if(command == null || command.equals("")) {
            listCommand.execute(commandSender, args);
        } else {
            Currency currency = getObject(commandSender, command);
            if(currency != null) {
                infoCommand.execute(commandSender, currency, args);
            } else {
                commandSender.sendMessage(Messages.COMMAND_CURRENCY_HELP);
            }
        }
    }

    @Override
    public void objectNotFound(CommandSender commandSender, String command, String[] args) {
        System.out.println("obj not fond");
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
