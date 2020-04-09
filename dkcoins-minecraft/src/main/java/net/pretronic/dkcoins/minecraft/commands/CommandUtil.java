/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 13.02.20, 16:21
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.commands;

import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.command.sender.ConsoleCommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.TransferResult;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.Messages;
import org.mcnative.common.McNative;
import org.mcnative.common.player.MinecraftPlayer;

public final class CommandUtil {

    public static String buildReason(String[] args, int start, int end) {
        StringBuilder reasonBuilder = new StringBuilder();
        if(args.length >= start) {
            for (int i = start; i < end; i++) {
                reasonBuilder.append(args[i]);
            }
        } else {
            reasonBuilder = new StringBuilder("none");
        }
        return reasonBuilder.toString();
    }

    public static String buildReason(String[] args, int start) {
        return buildReason(args, start, args.length);
    }

    public static String getPlayerName(DKCoinsUser user) {
        return McNative.getInstance().getPlayerManager().getPlayer(user.getUniqueId()).getName();
    }

    public static boolean hasAccess(CommandSender commandSender, BankAccount account, AccessRight accessRight) {
        if(commandSender instanceof ConsoleCommandSender) return true;
        if(commandSender instanceof MinecraftPlayer) {
            AccountMember member = account.getMember(DKCoins.getInstance().getUserManager()
                    .getUser(((MinecraftPlayer)commandSender).getUniqueId()));
            if(member != null) {
                return member.canAccess(accessRight);
            } else {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_NO_ACCESS);
            }
        }
        return false;
    }

    public static boolean hasAccessAndSendMessage(CommandSender commandSender, BankAccount account, AccessRight accessRight) {
        if(!hasAccess(commandSender, account, accessRight)) {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_NOT_ENOUGH_ACCESS_RIGHTS,
                    VariableSet.create().add("accessRight", accessRight));
            return false;
        }
        return true;
    }

    public static boolean hasAccountAccess(CommandSender commandSender, BankAccount account) {
        return (commandSender instanceof ConsoleCommandSender
                || commandSender.hasPermission("dkcoins.admin")
                || (commandSender instanceof MinecraftPlayer
                && account.isMember(DKCoins.getInstance().getUserManager().getUser(((MinecraftPlayer)commandSender).getUniqueId()))));
    }

    public static boolean hasAccountAccessAndSendMessage(CommandSender commandSender, BankAccount account) {
        System.out.println("has access check");
        if(!hasAccountAccess(commandSender, account)) {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_NO_ACCESS);
            return false;
        }
        return true;
    }

    public static AccountMember parseAccountMember(CommandSender commandSender, String name, BankAccount account) {
        DKCoinsUser user = DKCoins.getInstance().getUserManager().getUser(name);
        if(user == null) {
            commandSender.sendMessage(Messages.ERROR_USER_NOT_EXISTS, VariableSet.create().add("name", name));
            return null;
        }
        AccountMember accountMember = account.getMember(user);
        if(accountMember == null) {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_NOT_EXISTS, VariableSet.create().add("name", name));
            return null;
        }
        return accountMember;
    }

    public static AccountMember getAccountMemberByCommandSender(CommandSender commandSender, BankAccount account) {
        if(commandSender instanceof MinecraftPlayer) {
            DKCoinsUser user = DKCoins.getInstance().getUserManager().getUser(((MinecraftPlayer)commandSender).getUniqueId());
            return account.getMember(user);
        }
        return null;
    }

    public static void handleTransferFailCauses(TransferResult result, CommandSender commandSender) {
        switch (result.getFailCause()) {
            case LIMIT: {
                commandSender.sendMessage(Messages.COMMAND_ACCOUNT_TRANSFER_FAILURE_LIMIT);
                break;
            }
            case NOT_ENOUGH_AMOUNT: {
                commandSender.sendMessage(Messages.COMMAND_ACCOUNT_TRANSFER_FAILURE_NOT_ENOUGH_AMOUNT);
                break;
            }
            case NOT_ENOUGH_ACCESS_RIGHTS: {
                commandSender.sendMessage(Messages.COMMAND_ACCOUNT_TRANSFER_FAILURE_NOT_ENOUGH_ACCESS_RIGHTS);
                break;
            }
            case MASTER_ACCOUNT_NOT_ENOUGH_AMOUNT: {
                commandSender.sendMessage(Messages.COMMAND_ACCOUNT_TRANSFER_FAILURE_MASTER_ACCOUNT_NOT_ENOUGH_AMOUNT);
                break;
            }
            case TRANSFER_DISABLED: {
                commandSender.sendMessage(Messages.COMMAND_ACCOUNT_TRANSFER_FAILURE_DISABLED);
                break;
            }
        }
    }

    public static DKCoinsUser getUserByCommandSender(CommandSender commandSender) {
        if(commandSender instanceof MinecraftPlayer) {
            return DKCoins.getInstance().getUserManager().getUser(((MinecraftPlayer) commandSender).getUniqueId());
        }
        return null;
    }
}
