package net.pretronic.dkcoins.minecraft.commands.bank.member;

import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.command.object.ObjectCommand;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.message.bml.variable.VariableSet;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.minecraft.Messages;
import org.mcnative.common.McNative;
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
        member.getAccount().removeMember(member);
        commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_REMOVE, VariableSet.create()
                .add("name", McNative.getInstance().getPlayerManager().getPlayer(member.getUser().getUniqueId()).getName()));
    }
}
