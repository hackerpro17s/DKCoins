package net.pretronic.dkcoins.minecraft.commands.bank.member;

import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.message.bml.variable.reflect.ReflectVariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.map.Pair;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.Messages;

public class BankMemberAddCommand extends ObjectCommand<Pair<BankAccount, String>> {

    public BankMemberAddCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("add"));
    }

    @Override
    public void execute(CommandSender commandSender, Pair<BankAccount, String> pair, String[] strings) {
        BankAccount account = pair.getKey();
        String userName = pair.getValue();

        DKCoinsUser user = DKCoins.getInstance().getUserManager().getUser(userName);
        if(user == null) {
            commandSender.sendMessage(Messages.ERROR_USER_NOT_EXISTS, VariableSet.create().add("name", userName));
            return;
        }
        AccountMember member = account.getMember(user);
        if(member == null) {
            member = account.addMember(user, CommandUtil.getAccountMemberByCommandSender(commandSender, account), AccountMemberRole.GUEST, true);
            commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_ADD, new ReflectVariableSet()
                    .add("member", member));
        } else {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_ALREADY, new ReflectVariableSet()
                    .add("member", member));
        }
    }
}
