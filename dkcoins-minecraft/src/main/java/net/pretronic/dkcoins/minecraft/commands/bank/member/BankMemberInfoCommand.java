package net.pretronic.dkcoins.minecraft.commands.bank.member;

import net.pretronic.dkcoins.api.account.AccountLimitation;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.reflect.ReflectVariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

public class BankMemberInfoCommand extends ObjectCommand<AccountMember> {

    public BankMemberInfoCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("info"));
    }

    @Override
    public void execute(CommandSender commandSender, AccountMember member, String[] strings) {
        commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_INFO,
                new ReflectVariableSet()
                        .add("member", member));
        if(CommandUtil.hasAccess(commandSender, member.getAccount(), AccessRight.LIMIT_MANAGEMENT)) {
            //@Todo custom bml method for check this
            if(member.getLimitations().isEmpty()) {
                commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_INFO_NO_LIMITATION, new ReflectVariableSet()
                        .add("member", member));
            } else {
                commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_INFO_LIMITATION, new ReflectVariableSet()
                        .add("limitations", member.getLimitations())
                        .add("member", member));
            }
        }
    }
}
