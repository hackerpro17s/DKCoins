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

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.limitation.LimitationAble;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.RoleAble;
import net.pretronic.dkcoins.api.account.transferresult.TransferResult;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.command.sender.ConsoleCommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.MinecraftPlayer;

import java.util.function.Consumer;

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

    public static boolean hasAccountAccess(CommandSender commandSender, BankAccount account, AccessRight accessRight) {
        if(commandSender instanceof ConsoleCommandSender || commandSender.hasPermission(DKCoinsConfig.PERMISSIONS_ADMIN)) return true;
        if(commandSender instanceof MinecraftPlayer) {
            AccountMember member = getAccountMemberByCommandSender(commandSender, account);
            if(member != null) {
                return member.canAccess(accessRight);
            } else {
                return false;
            }
        }
        return false;
    }

    public static boolean hasTargetAccess(CommandSender commandSender, BankAccount account, RoleAble entity) {
        if(commandSender instanceof MinecraftPlayer && !commandSender.hasPermission(DKCoinsConfig.PERMISSIONS_ADMIN)) {
            AccountMember sender = CommandUtil.getAccountMemberByCommandSender(commandSender, account);

            if(!sender.getRole().isHigher(entity.getRole())) {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_ROLE_LOWER,
                        VariableSet.create().addDescribed("targetRole", entity.getRole()));
                return false;
            }
        }
        return true;
    }

    public static AccountMember parseAccountMember(BankAccount account, String name) {
        DKCoinsUser user = McNative.getInstance().getPlayerManager().getPlayer(name).getAs(DKCoinsUser.class);
        if(user == null) {
            return null;
        }
        AccountMember accountMember = account.getMember(user);
        if(accountMember == null) {
            return null;
        }
        return accountMember;
    }

    public static AccountMember getAccountMemberByCommandSender(CommandSender commandSender, BankAccount account) {
        if(commandSender instanceof MinecraftPlayer) {
            DKCoinsUser user = ((MinecraftPlayer) commandSender).getAs(DKCoinsUser.class);
            return account.getMember(user);
        }
        return null;
    }

    public static void handleTransferFailCauses(TransferResult result, CommandSender commandSender) {
        switch (result.getFailCause()) {
            case LIMIT: {
                commandSender.sendMessage(Messages.COMMAND_BANK_TRANSFER_FAILURE_LIMIT);
                break;
            }
            case NOT_ENOUGH_AMOUNT: {
                commandSender.sendMessage(Messages.COMMAND_BANK_TRANSFER_FAILURE_NOT_ENOUGH_AMOUNT);
                break;
            }
            case NOT_ENOUGH_ACCESS_RIGHTS: {
                commandSender.sendMessage(Messages.COMMAND_BANK_TRANSFER_FAILURE_NOT_ENOUGH_ACCESS_RIGHTS);
                break;
            }
            case MASTER_ACCOUNT_NOT_ENOUGH_AMOUNT: {
                commandSender.sendMessage(Messages.COMMAND_BANK_TRANSFER_FAILURE_MASTER_ACCOUNT_NOT_ENOUGH_AMOUNT);
                break;
            }
            case TRANSFER_DISABLED: {
                commandSender.sendMessage(Messages.COMMAND_BANK_TRANSFER_FAILURE_DISABLED);
                break;
            }
            case SAME_ACCOUNT: {
                commandSender.sendMessage(Messages.COMMAND_BANK_TRANSFER_FAILURE_SAME_ACCOUNT);
                break;
            }
        }
    }

    public static DKCoinsUser getUserByCommandSender(CommandSender commandSender) {
        if(commandSender instanceof MinecraftPlayer) {
            return ((MinecraftPlayer)commandSender).getAs(DKCoinsUser.class);
        }
        return null;
    }

    public static void loopThroughUserBanks(BankAccount own, Consumer<BankAccount> accountConsumer) {
        for (ConnectedMinecraftPlayer connectedPlayer : McNative.getInstance().getLocal().getConnectedPlayers()) {
            BankAccount account = DKCoins.getInstance().getAccountManager().getAccount(connectedPlayer.getName(), "User");
            if(account == null || account.equals(own)) continue;
            accountConsumer.accept(account);
        }
    }

    public static boolean canTransferAndSendMessage(CommandSender commandSender, double amount, boolean all) {
        if(all && !(commandSender.hasPermission("dkcoins.transfer.all"))) {
            commandSender.sendMessage(Messages.ERROR_NO_PERMISSION);
            return false;
        }
        if(all && amount < DKCoinsConfig.MINIMUM_PAYMENT_ALL) {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_PAYMENT_ALL_TOO_LITTLE);
            return false;
        }
        if(!all && amount < DKCoinsConfig.MINIMUM_PAYMENT_USER) {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_PAYMENT_USER_TOO_LITTLE);
            return false;
        }
        return true;
    }
}
