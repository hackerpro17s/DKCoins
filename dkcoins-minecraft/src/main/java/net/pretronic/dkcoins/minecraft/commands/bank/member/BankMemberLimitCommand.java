package net.pretronic.dkcoins.minecraft.commands.bank.member;

import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.command.sender.ConsoleCommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.message.bml.variable.reflect.ReflectVariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountLimitation;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.minecraft.DKCoinsConfig;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import org.mcnative.common.player.MinecraftPlayer;

public class BankMemberLimitCommand extends ObjectCommand<AccountMember> {

    public BankMemberLimitCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("limit"));
    }

    @Override
    public void execute(CommandSender commandSender, AccountMember member, String[] args) {
        if(CommandUtil.hasAccessAndSendMessage(commandSender, member.getAccount(), AccessRight.LIMIT_MANAGEMENT)) {
            if(args.length == 0) {
                listLimitations(commandSender, member);
                return;
            } else {
                switch (args[0].toLowerCase()) {
                    case "list": {
                        listLimitations(commandSender, member);
                        return;
                    }
                    case "set":
                    case "remove": {
                        if(!(commandSender instanceof ConsoleCommandSender || (commandSender instanceof MinecraftPlayer
                                && member.getAccount().getMember(DKCoins.getInstance().getUserManager()
                                .getUser(((MinecraftPlayer)commandSender).getUniqueId())).getRole().isHigher(member.getRole())))) {
                            commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_ROLE_LOWER,
                                    VariableSet.create().add("targetRole", member.getRole()));
                            return;
                        }
                        if(args.length == 3) {
                            String interval0 = args[1];
                            if(!GeneralUtil.isNaturalNumber(interval0)) {
                                commandSender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create().add("value", interval0));
                                return;
                            }
                            String amount0 = args[2];
                            if(!GeneralUtil.isNumber(amount0)) {
                                commandSender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create().add("value", amount0));
                                return;
                            }
                            long interval = Long.parseLong(interval0);
                            double amount = Double.parseDouble(amount0);

                            if(args[0].equalsIgnoreCase("set")) {
                                AccountLimitation limitation = member.addLimitation(DKCoinsConfig.CURRENCY_DEFAULT, amount, interval);
                                commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_LIMIT_SET, new ReflectVariableSet()
                                        .add("limitation", limitation)
                                        .add("member", member));
                            } else {
                                AccountLimitation limitation = member.getLimitation(DKCoinsConfig.CURRENCY_DEFAULT, amount, interval);
                                if(member.removeLimitation(limitation)) {
                                    commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_LIMIT_REMOVE, new ReflectVariableSet()
                                            .add("limitation", limitation)
                                            .add("member", member));
                                } else {
                                    commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_LIMIT_REMOVE_FAILURE);
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
        commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_LIMIT_HELP);
    }

    private void listLimitations(CommandSender commandSender, AccountMember member) {
        if(member.getLimitations().isEmpty()) {
            commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_INFO_NO_LIMITATION, new ReflectVariableSet()
                    .add("member", member));
        } else {
            commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_INFO_LIMITATION, new ReflectVariableSet()
                    .add("limitations", member.getLimitations()));
        }
    }
}
