package net.pretronic.dkcoins.minecraft.commands.bank.member;

import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.command.object.ObjectCommand;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.message.bml.variable.VariableSet;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.account.AccountLimitation;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import org.mcnative.common.McNative;

public class BankMemberInfoCommand extends ObjectCommand<AccountMember> {

    public BankMemberInfoCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("info"));
    }

    @Override
    public void execute(CommandSender commandSender, AccountMember member, String[] strings) {
        commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_INFO_HEADER, VariableSet.create()
                .add("name", McNative.getInstance().getPlayerManager().getPlayer(member.getUser().getUniqueId()).getName())
                .add("role", member.getRole()));
        if(CommandUtil.hasAccess(commandSender, member.getAccount(), AccessRight.LIMIT_MANAGEMENT)) {
            if(member.getLimitations().isEmpty()) {
                commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_INFO_NO_LIMITATION);
            } else {
                for (AccountLimitation limitation : member.getLimitations()) {
                    commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_INFO_LIMITATION, VariableSet.create()
                            .add("amount", limitation.getAmount()).add("interval", limitation.getInterval()));
                }
            }
        }
    }
}
