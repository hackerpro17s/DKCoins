package net.pretronic.dkcoins.minecraft.commands.bank.member;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.command.sender.ConsoleCommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
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
                    commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_YOURSELF);
                    return;
                }
                member.setRole(role);
                commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_ROLE, VariableSet.create()
                        .addDescribed("member", member));
                if(role == AccountMemberRole.OWNER) {
                    if(self != null) {
                        self.setRole(AccountMemberRole.ADMIN);
                    }
                }
            } else {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_ROLE_LOWER,
                        VariableSet.create().addDescribed("targetRole", member.getRole()));
            }
        }
    }
}
