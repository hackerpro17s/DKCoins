package net.pretronic.dkcoins.minecraft.commands.bank.member;

import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.command.object.ObjectCommand;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.command.sender.ConsoleCommandSender;
import net.prematic.libraries.message.bml.variable.VariableSet;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import org.mcnative.common.McNative;
import org.mcnative.common.player.MinecraftPlayer;

public class BankMemberRoleCommand extends ObjectCommand<AccountMember> {

    public BankMemberRoleCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("role"));
    }

    @Override
    public void execute(CommandSender commandSender, AccountMember member, String[] args) {
        if(CommandUtil.hasAccessAndSendMessage(commandSender, member.getAccount(), AccessRight.ROLE_MANAGEMENT)) {
            if(args.length == 0) {
                commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_ROLE_HELP);
                return;
            }
            String role0 = args[0];
            AccountMemberRole role = AccountMemberRole.byName(role0);
            if(role == null) {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_ROLE_NOT_EXISTS, VariableSet.create().add("name", role0));
                return;
            }
            if(commandSender instanceof ConsoleCommandSender || (commandSender instanceof MinecraftPlayer
                    && member.getAccount().getMember(DKCoins.getInstance().getUserManager()
                    .getUser(((MinecraftPlayer)commandSender).getUniqueId())).getRole().isHigher(member.getRole()))) {
                AccountMember self = CommandUtil.getAccountMemberByCommandSender(commandSender, member.getAccount());
                if(commandSender instanceof MinecraftPlayer && member.equals(self)) {
                    System.out.println(member.getUser().getUniqueId().toString());
                    System.out.println(self.getUser().getUniqueId().toString());
                    System.out.println(member.getId());
                    System.out.println(self.getId());
                    System.out.println(member.equals(self));
                    commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_YOURSELF);
                    return;
                }
                member.setRole(role);
                commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_ROLE, VariableSet.create().add("role", role)
                        .add("name", McNative.getInstance().getPlayerManager().getPlayer(member.getUser().getUniqueId()).getName()));
                if(role == AccountMemberRole.OWNER) {
                    if(self != null) {
                        self.setRole(AccountMemberRole.ADMIN);
                    }
                }
            } else {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_ROLE_LOWER, VariableSet.create().add("targetRole", member.getRole()));
            }
        }
    }
}
