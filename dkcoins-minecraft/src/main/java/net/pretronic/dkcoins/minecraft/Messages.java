/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 13.02.20, 16:02
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft;

import org.mcnative.runtime.api.text.Text;
import org.mcnative.runtime.api.text.components.MessageKeyComponent;

public class Messages {

    public static final String PREFIX = "&8» &6DKCoins &8|&f";

    public static final MessageKeyComponent ERROR_NOT_FROM_CONSOLE = Text.ofMessageKey("dkcoins.error.notFromConsole");
    public static final MessageKeyComponent ERROR_ONLY_FROM_CONSOLE = Text.ofMessageKey("dkcoins.error.onlyFromConsole");
    public static final MessageKeyComponent ERROR_NO_PERMISSION = Text.ofMessageKey("dkcoins.error.notPermission");
    public static final MessageKeyComponent ERROR_ACCOUNT_ALREADY_EXISTS = Text.ofMessageKey("dkcoins.error.account.alreadyExists");
    public static final MessageKeyComponent ERROR_CURRENCY_ALREADY_EXISTS = Text.ofMessageKey("dkcoins.error.currency.alreadyExists");
    public static final MessageKeyComponent ERROR_CURRENCY_NOT_EXISTS = Text.ofMessageKey("dkcoins.error.currency.notExists");
    public static final MessageKeyComponent ERROR_ACCOUNT_TYPE_NOT_EXISTS = Text.ofMessageKey("dkcoins.error.account.type.notExists");
    public static final MessageKeyComponent ERROR_NOT_NUMBER = Text.ofMessageKey("dkcoins.error.notNumber");
    public static final MessageKeyComponent ERROR_NOT_BOOLEAN = Text.ofMessageKey("dkcoins.error.notBoolean");
    public static final MessageKeyComponent ERROR_ACCOUNT_NOT_EXISTS = Text.ofMessageKey("dkcoins.error.account.notExists");
    public static final MessageKeyComponent ERROR_ACCOUNT_MEMBER_NOT_EXISTS = Text.ofMessageKey("dkcoins.error.account.member.notExists");
    public static final MessageKeyComponent ERROR_ACCOUNT_MEMBER_ROLE_NOT_EXISTS = Text.ofMessageKey("dkcoins.error.account.memberRole.notExists");
    public static final MessageKeyComponent ERROR_ACCOUNT_MEMBER_NOT_ENOUGH_ACCESS_RIGHTS = Text.ofMessageKey("dkcoins.error.account.member.notEnoughAccessRights");
    public static final MessageKeyComponent ERROR_ACCOUNT_MEMBER_ROLE_LOWER = Text.ofMessageKey("dkcoins.error.memberRole.lower");
    public static final MessageKeyComponent ERROR_ACCOUNT_USER_DELETE_NOT_POSSIBLE = Text.ofMessageKey("dkcoins.error.account.user.deleteNotPossible");
    public static final MessageKeyComponent ERROR_ACCOUNT_MEMBER_YOURSELF = Text.ofMessageKey("dkcoins.error.account.member.yourself");
    public static final MessageKeyComponent ERROR_ACCOUNT_NO_ACCESS = Text.ofMessageKey("dkcoins.error.account.noAccess");
    public static final MessageKeyComponent ERROR_USER_NOT_EXISTS = Text.ofMessageKey("dkcoins.error.user.notExists");
    public static final MessageKeyComponent ERROR_ACCOUNT_MEMBER_ALREADY = Text.ofMessageKey("dkcoins.error.member.already");
    public static final MessageKeyComponent ERROR_ACCOUNT_USER_NOT_POSSIBLE = Text.ofMessageKey("dkcoins.error.account.user.notPossible");
    public static final MessageKeyComponent ERROR_ACCOUNT_PAYMENT_USER_TOO_LITTLE = Text.ofMessageKey("dkcoins.error.account.payment.user.too.little");
    public static final MessageKeyComponent ERROR_ACCOUNT_PAYMENT_ALL_TOO_LITTLE = Text.ofMessageKey("dkcoins.error.account.payment.all.too.little");
    public static final MessageKeyComponent ERROR_ACCOUNT_LIMITATION_INTERVAL_NOT_VALID = Text.ofMessageKey("dkcoins.error.account.limitation.interval.notValid");
    public static final MessageKeyComponent ERROR_ACCOUNT_LIMITATION_CALCULATION_TYPE_NOT_VALID = Text.ofMessageKey("dkcoins.error.account.limitation.calculationType.notValid");

    public static final MessageKeyComponent COMMAND_BANK_CREATE_HELP = Text.ofMessageKey("dkcoins.command.bank.create.help");
    public static final MessageKeyComponent COMMAND_BANK_CREATE_DONE = Text.ofMessageKey("dkcoins.command.bank.create.done");

    public static final MessageKeyComponent COMMAND_BANK_DELETE_DONE = Text.ofMessageKey("dkcoins.command.bank.delete.done");

    public static final MessageKeyComponent COMMAND_BANK_HELP = Text.ofMessageKey("dkcoins.command.bank.help");
    public static final MessageKeyComponent COMMAND_BANK_LIST = Text.ofMessageKey("dkcoins.command.bank.list");

    public static final MessageKeyComponent COMMAND_BANK_CREDITS = Text.ofMessageKey("dkcoins.command.bank.credits");


    public static final MessageKeyComponent COMMAND_BANK_EXCHANGE_HELP = Text.ofMessageKey("dkcoins.command.bank.exchange.help");
    public static final MessageKeyComponent COMMAND_BANK_EXCHANGE_SUCCESS = Text.ofMessageKey("dkcoins.command.bank.exchange.success");
    public static final MessageKeyComponent COMMAND_BANK_EXCHANGE_FAILURE_NOT_ENOUGH_AMOUNT =
            Text.ofMessageKey("dkcoins.command.bank.exchange.failure.notEnoughAmount");
    public static final MessageKeyComponent COMMAND_BANK_EXCHANGE_FAILURE_MASTER_ACCOUNT_NOT_ENOUGH_AMOUNT =
            Text.ofMessageKey("dkcoins.command.bank.exchange.failure.masterAccountNotEnoughAmount");
    public static final MessageKeyComponent COMMAND_BANK_EXCHANGE_FAILURE_LIMIT =
            Text.ofMessageKey("dkcoins.command.bank.exchange.failure.limit");
    public static final MessageKeyComponent COMMAND_BANK_EXCHANGE_FAILURE_NOT_ENOUGH_ACCESS_RIGHTS =
            Text.ofMessageKey("dkcoins.command.bank.exchange.failure.notEnoughAccessRights");
    public static final MessageKeyComponent COMMAND_BANK_EXCHANGE_FAILURE_DISABLED =
            Text.ofMessageKey("dkcoins.command.bank.exchange.failure.disabled");

    public static final MessageKeyComponent COMMAND_BANK_TRANSFER_HELP = Text.ofMessageKey("dkcoins.command.bank.transfer.help");
    public static final MessageKeyComponent COMMAND_BANK_TRANSFER_SUCCESS = Text.ofMessageKey("dkcoins.command.bank.transfer.success");
    public static final MessageKeyComponent COMMAND_BANK_TRANSFER_SUCCESS_RECEIVER = Text.ofMessageKey("dkcoins.command.bank.transfer.success.receiver");
    public static final MessageKeyComponent COMMAND_BANK_TRANSFER_FAILURE_NOT_ENOUGH_AMOUNT =
            Text.ofMessageKey("dkcoins.command.bank.transfer.failure.notEnoughAmount");
    public static final MessageKeyComponent COMMAND_BANK_TRANSFER_FAILURE_MASTER_ACCOUNT_NOT_ENOUGH_AMOUNT =
            Text.ofMessageKey("dkcoins.command.bank.transfer.failure.masterAccountNotEnoughAmount");
    public static final MessageKeyComponent COMMAND_BANK_TRANSFER_FAILURE_LIMIT =
            Text.ofMessageKey("dkcoins.command.bank.transfer.failure.limit");
    public static final MessageKeyComponent COMMAND_BANK_TRANSFER_FAILURE_NOT_ENOUGH_ACCESS_RIGHTS =
            Text.ofMessageKey("dkcoins.command.bank.transfer.failure.notEnoughAccessRights");
    public static final MessageKeyComponent COMMAND_BANK_TRANSFER_FAILURE_DISABLED =
            Text.ofMessageKey("dkcoins.command.bank.transfer.failure.disabled");


    public static final MessageKeyComponent COMMAND_BANK_MEMBER_LIST = Text.ofMessageKey("dkcoins.command.bank.member.list");
    public static final MessageKeyComponent COMMAND_BANK_MEMBER_INFO = Text.ofMessageKey("dkcoins.command.bank.member.info");
    public static final MessageKeyComponent COMMAND_BANK_MEMBER_ROLE = Text.ofMessageKey("dkcoins.command.bank.member.role");
    public static final MessageKeyComponent COMMAND_BANK_MEMBER_REMOVE = Text.ofMessageKey("dkcoins.command.bank.member.remove");
    public static final MessageKeyComponent COMMAND_BANK_MEMBER_ROLE_HELP = Text.ofMessageKey("dkcoins.command.bank.member.role.help");

    public static final MessageKeyComponent COMMAND_BANK_LIMIT_SET = Text.ofMessageKey("dkcoins.command.bank.limit.set");
    public static final MessageKeyComponent COMMAND_BANK_LIMIT_REMOVE = Text.ofMessageKey("dkcoins.command.bank.limit.remove");
    public static final MessageKeyComponent COMMAND_BANK_LIMIT_REMOVE_FAILURE = Text.ofMessageKey("dkcoins.command.bank.limit.remove.failure");
    public static final MessageKeyComponent COMMAND_BANK_INFO_LIMITATION = Text.ofMessageKey("dkcoins.command.bank.info.limitation");
    public static final MessageKeyComponent COMMAND_BANK_INFO_NO_LIMITATION = Text.ofMessageKey("dkcoins.command.bank.info.noLimitation");

    public static final MessageKeyComponent COMMAND_BANK_MEMBER_LIMIT_HELP = Text.ofMessageKey("dkcoins.command.bank.member.limit.help");
    public static final MessageKeyComponent COMMAND_BANK_ROLE_LIMIT_HELP = Text.ofMessageKey("dkcoins.command.bank.role.limit.help");
    public static final MessageKeyComponent COMMAND_BANK_LIMIT_HELP = Text.ofMessageKey("dkcoins.command.bank.limit.help");

    public static final MessageKeyComponent COMMAND_BANK_MEMBER_HELP = Text.ofMessageKey("dkcoins.command.bank.member.help");

    public static final MessageKeyComponent COMMAND_BANK_MEMBER_ADD = Text.ofMessageKey("dkcoins.command.bank.member.add");

    public static final MessageKeyComponent COMMAND_BANK_ROLE_HELP = Text.ofMessageKey("dkcoins.command.bank.role.help");

    public static final MessageKeyComponent COMMAND_BANK_ADMIN_ADD = Text.ofMessageKey("dkcoins.command.bank.admin.add");
    public static final MessageKeyComponent COMMAND_BANK_ADMIN_REMOVE = Text.ofMessageKey("dkcoins.command.bank.admin.remove");
    public static final MessageKeyComponent COMMAND_BANK_ADMIN_SET = Text.ofMessageKey("dkcoins.command.bank.admin.set");

    public static final MessageKeyComponent COMMAND_BANK_BANK_STATEMENT_FILTER_OPTION_WRONG = Text.ofMessageKey("dkcoins.command.bank.bankStatement.filterOption.wrong");
    public static final MessageKeyComponent COMMAND_BANK_BANK_STATEMENT_FILTER_OPTION_NOT_FOUND = Text.ofMessageKey("dkcoins.command.bank.bankStatement.filterOption.notFound");

    public static final MessageKeyComponent COMMAND_BANK_BANK_STATEMENT = Text.ofMessageKey("dkcoins.command.bank.bankStatement");

    public static final MessageKeyComponent COMMAND_TRANSFER_HELP = Text.ofMessageKey("dkcoins.command.transfer.help");

    public static final MessageKeyComponent COMMAND_CURRENCY_HELP = Text.ofMessageKey("dkcoins.command.currency.help");

    public static final MessageKeyComponent COMMAND_CURRENCY_CREATE_HELP = Text.ofMessageKey("dkcoins.command.currency.create.help");
    public static final MessageKeyComponent COMMAND_CURRENCY_CREATE_DONE = Text.ofMessageKey("dkcoins.command.currency.create.done");

    public static final MessageKeyComponent COMMAND_CURRENCY_DELETE_DONE = Text.ofMessageKey("dkcoins.command.currency.delete.done");

    public static final MessageKeyComponent COMMAND_CURRENCY_LIST = Text.ofMessageKey("dkcoins.command.currency.list");

    public static final MessageKeyComponent COMMAND_CURRENCY_INFO = Text.ofMessageKey("dkcoins.command.currency.info");

    public static final MessageKeyComponent COMMAND_CURRENCY_EDIT_HELP = Text.ofMessageKey("dkcoins.command.currency.edit.help");
    public static final MessageKeyComponent COMMAND_CURRENCY_EDIT_DONE_NAME = Text.ofMessageKey("dkcoins.command.currency.edit.done.name");
    public static final MessageKeyComponent COMMAND_CURRENCY_EDIT_DONE_SYMBOL = Text.ofMessageKey("dkcoins.command.currency.edit.done.symbol");
    public static final MessageKeyComponent COMMAND_CURRENCY_EDIT_DONE_EXCHANGE_RATE = Text.ofMessageKey("dkcoins.command.currency.edit.done.exchangeRate");
    public static final MessageKeyComponent COMMAND_CURRENCY_EDIT_DISABLE_EXCHANGE_RATE = Text.ofMessageKey("dkcoins.command.currency.edit.disable.exchangeRate");
    public static final MessageKeyComponent COMMAND_CURRENCY_EDIT_EXCHANGE_RATE_AMOUNT_NOT_VALID = Text.ofMessageKey("dkcoins.command.currency.edit.exchangeRate.amountNotValid");

    public static final MessageKeyComponent COMMAND_USER_BANK_AMOUNT = Text.ofMessageKey("dkcoins.command.userBank.amount");
    public static final MessageKeyComponent COMMAND_USER_BANK_AMOUNT_OTHER = Text.ofMessageKey("dkcoins.command.userBank.amount.other");
    public static final MessageKeyComponent COMMAND_USER_BANK_HELP = Text.ofMessageKey("dkcoins.command.userBank.help");
    public static final MessageKeyComponent COMMAND_USER_BANK_WORLD_DISABLED = Text.ofMessageKey("dkcoins.command.userBank.world.disabled");
    public static final MessageKeyComponent COMMAND_USER_BANK_TRANSFER_HELP  = Text.ofMessageKey("dkcoins.command.userBank.transfer.help");

    public static final MessageKeyComponent TOP = Text.ofMessageKey("dkcoins.top");
    public static final MessageKeyComponent TOP_PAGE_NO_ENTRIES = Text.ofMessageKey("dkcoins.top.pageNoEntries");

    public static final MessageKeyComponent COMMAND_BANK_SETTINGS_HELP = Text.ofMessageKey("dkcoins.bank.settings.help");
    public static final MessageKeyComponent COMMAND_BANK_SETTINGS_RECEIVE_NOTIFICATIONS_ON = Text.ofMessageKey("dkcoins.bank.settings.receiveNotifications.on");
    public static final MessageKeyComponent COMMAND_BANK_SETTINGS_RECEIVE_NOTIFICATIONS_OFF = Text.ofMessageKey("dkcoins.bank.settings.receiveNotifications.off");
    public static final MessageKeyComponent COMMAND_BANK_SETTINGS_NOT_VALID = Text.ofMessageKey("dkcoins.bank.settings.notValid");

    public static final MessageKeyComponent COMMAND_DKCOINS_HELP = Text.ofMessageKey("dkcoins.command.dkcoins.help");
}
