package net.pretronic.dkcoins.minecraft.commands.bank;

import net.pretronic.libraries.command.command.BasicCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.message.bml.variable.reflect.ReflectVariableSet;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.Messages;
import org.mcnative.common.player.OnlineMinecraftPlayer;

import java.util.List;

public class BankListCommand extends BasicCommand {

    public BankListCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("list"));
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof OnlineMinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        OnlineMinecraftPlayer player = (OnlineMinecraftPlayer) commandSender;
        DKCoinsUser user = DKCoins.getInstance().getUserManager().getUser(player.getUniqueId());
        List<AccountMember> members = Iterators.map(user.getAccounts(), account -> account.getMember(user));
        player.sendMessage(Messages.COMMAND_BANK_LIST, new ReflectVariableSet().add("members", members));
        /*for (BankAccount account : ) {
            AccountMember member = account.getMember(user);
            /*player.sendMessage(Messages.COMMAND_BANK_LIST_ACCOUNTS, VariableSet.create()
                    .add("account.name", account.getName())
                    .add("role", member.getRole()));*
        }
        //member.account.name
        //member.role.name*/
    }
}
