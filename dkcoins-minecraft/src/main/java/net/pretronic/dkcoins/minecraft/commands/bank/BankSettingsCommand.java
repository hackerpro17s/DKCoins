package net.pretronic.dkcoins.minecraft.commands.bank;

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Convert;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.common.McNative;

public class BankSettingsCommand extends ObjectCommand<BankAccount> {

    public BankSettingsCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("settings", "edit"));
    }

    //settings <setting> <value>
    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        if(commandSender.equals(McNative.getInstance().getConsoleSender())) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        if(args.length != 2) {
            commandSender.sendMessage(Messages.COMMAND_BANK_SETTINGS_HELP);
            return;
        }

        String setting = args[0];
        switch (setting.toLowerCase()) {
            case "receivenotifications": {
                AccountMember member = CommandUtil.getAccountMemberByCommandSender(commandSender, account);
                boolean receiveNotifications;
                try {
                    receiveNotifications = Convert.toBoolean(args[1]);
                } catch (IllegalArgumentException ignored) {
                    commandSender.sendMessage(Messages.ERROR_NOT_BOOLEAN, VariableSet.create().add("value", args[1]));
                    return;
                }
                member.setReceiveNotifications(receiveNotifications);
                if(member.receiveNotifications()) {
                    commandSender.sendMessage(Messages.COMMAND_BANK_SETTINGS_RECEIVE_NOTIFICATIONS_ON);
                } else {
                    commandSender.sendMessage(Messages.COMMAND_BANK_SETTINGS_RECEIVE_NOTIFICATIONS_OFF);
                }
                return;
            }
            default: {
                commandSender.sendMessage(Messages.COMMAND_BANK_SETTINGS_NOT_VALID, VariableSet.create().add("value", args[1]));
            }
        }
    }
}
