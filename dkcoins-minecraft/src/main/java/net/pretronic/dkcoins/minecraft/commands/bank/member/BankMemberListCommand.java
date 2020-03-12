package net.pretronic.dkcoins.minecraft.commands.bank.member;

import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.minecraft.Messages;
import org.mcnative.common.McNative;
import org.mcnative.common.player.MinecraftPlayer;

public class BankMemberListCommand extends ObjectCommand<BankAccount> {

    public BankMemberListCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("list"));
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] strings) {
        commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_LIST_HEADER);
        for (AccountMember member : account.getMembers()) {
            MinecraftPlayer player = McNative.getInstance().getPlayerManager().getPlayer(member.getUser().getUniqueId());
            commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_LIST_ENTRY, VariableSet.create()
                    .add("name", player.getName()).add("role", member.getRole()));
        }
    }
}
