package net.pretronic.dkcoins.minecraft.commands.bank.member;

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.message.bml.variable.describer.DescribedHashVariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

public class BankMemberListCommand extends ObjectCommand<BankAccount> {

    public BankMemberListCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("list"));
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] strings) {
        commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_LIST, VariableSet.create()
                .addDescribed("members", account.getMembers()));
    }
}
