package net.pretronic.dkcoins.minecraft.commands.bank;

import net.prematic.libraries.command.command.BasicCommand;
import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.message.bml.variable.VariableSet;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.Messages;
import org.mcnative.common.player.OnlineMinecraftPlayer;

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
        player.sendMessage(Messages.COMMAND_BANK_LIST_HEADER);
        for (BankAccount account : user.getAccounts()) {
            AccountMember member = account.getMember(user);
            player.sendMessage(Messages.COMMAND_BANK_LIST_ACCOUNTS, VariableSet.create().add("account", account.getName())
                    .add("role", member.getRole()));
        }
    }
}
