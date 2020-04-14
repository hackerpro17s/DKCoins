package net.pretronic.dkcoins.minecraft.commands.bank.member;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.describer.DescribedHashVariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.common.player.MinecraftPlayer;

public class BankMemberRemoveCommand extends ObjectCommand<AccountMember> {

    public BankMemberRemoveCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("remove"));
    }

    @Override
    public void execute(CommandSender commandSender, AccountMember member, String[] strings) {
        if(commandSender instanceof MinecraftPlayer && member.equals(member.getAccount().getMember(DKCoins.getInstance().getUserManager()
                .getUser(((MinecraftPlayer)commandSender).getUniqueId())))) {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_YOURSELF);
            return;
        }
        member.getAccount().removeMember(member, CommandUtil.getAccountMemberByCommandSender(commandSender, member.getAccount()));
        commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_REMOVE, new DescribedHashVariableSet()
                .add("user", member.getUser()));
    }
}
