package net.pretronic.dkcoins.minecraft.commands.bank.role;

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

public class BankRoleListCommand extends ObjectCommand<BankAccount> {

    public BankRoleListCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("list"));
    }

    @Override
    public void execute(CommandSender sender, BankAccount account, String[] args) {
        sender.sendMessage(Messages.COMMAND_BANK_ROLE_LIST, VariableSet.create().addDescribed("roles", account.getRoles()));
    }
}
