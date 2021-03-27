package net.pretronic.dkcoins.minecraft.commands.bank;

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

public class BankInfoCommand extends ObjectCommand<BankAccount> {

    public BankInfoCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("info", "i"));
    }

    @Override
    public void execute(CommandSender sender, BankAccount account, String[] args) {
        sender.sendMessage(Messages.COMMAND_BANK_CREDITS, VariableSet.create()
                .addDescribed("credits", account.getCredits()));
    }
}
