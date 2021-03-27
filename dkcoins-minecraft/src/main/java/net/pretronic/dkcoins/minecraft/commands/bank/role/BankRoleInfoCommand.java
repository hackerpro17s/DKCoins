package net.pretronic.dkcoins.minecraft.commands.bank.role;

import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

public class BankRoleInfoCommand extends ObjectCommand<AccountMemberRole> {

    public BankRoleInfoCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("info"));
    }

    @Override
    public void execute(CommandSender commandSender, AccountMemberRole role, String[] args) {
        commandSender.sendMessage(Messages.COMMAND_BANK_ROLE_INFO, VariableSet.create().addDescribed("role", role));

        if(role.getLimitations().isEmpty()) {
            commandSender.sendMessage(Messages.COMMAND_BANK_ROLE_INFO_NO_LIMITATION, VariableSet.create());
        } else {
            commandSender.sendMessage(Messages.COMMAND_BANK_ROLE_INFO_LIMITATION, VariableSet.create()
                    .addDescribed("limitations", role.getLimitations()));
        }
    }
}
