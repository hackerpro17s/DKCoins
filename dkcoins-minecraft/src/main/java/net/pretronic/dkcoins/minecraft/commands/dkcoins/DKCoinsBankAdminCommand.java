package net.pretronic.dkcoins.minecraft.commands.dkcoins;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.account.TransferCause;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.command.BasicCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.message.bml.variable.describer.DescribedHashVariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.common.McNative;
import org.mcnative.common.player.MinecraftPlayer;

import java.util.ArrayList;
import java.util.Collection;

public class DKCoinsBankAdminCommand extends BasicCommand {

    public DKCoinsBankAdminCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.newBuilder().name("bankAdmin").aliases("admin").permission("dkcoins.admin").create());
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(args.length < 3) {
            commandSender.sendMessage(Messages.COMMAND_DKCOINS_ADMIN_HELP);
            return;
        }
        String bankAccount = args[0];
        String action = args[1];
        String amount0 = args[2];
        String currency0 = args.length == 4 ? args[3] : null;

        if(!GeneralUtil.isNumber(amount0)) {
            commandSender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create()
                    .add("value", amount0));
            return;
        }
        double amount = Double.parseDouble(amount0);

        Currency currency;
        if(currency0 == null) {
            currency = DKCoinsConfig.CURRENCY_DEFAULT;
        } else {
            currency = DKCoins.getInstance().getCurrencyManager().searchCurrency(currency0);
            if(currency == null) {
                commandSender.sendMessage(Messages.ERROR_CURRENCY_NOT_EXISTS, VariableSet.create()
                        .add("name", args[2]));
                return;
            }
        }

        if(DKCoinsConfig.isPaymentAllAlias(bankAccount)) {
            CommandUtil.loopThroughUserBanks(null, receiver -> transfer(commandSender, receiver, currency, amount, action, args));
        } else {
            BankAccount receiver = DKCoins.getInstance().getAccountManager().searchAccount(bankAccount);
            if(receiver == null) {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_NOT_EXISTS, VariableSet.create()
                        .add("name", bankAccount));
                return;
            }
            transfer(commandSender, receiver, currency, amount, action, args);
        }
    }

    private void transfer(CommandSender commandSender, BankAccount account, Currency currency, double amount, String action, String[] args) {
        AccountMember member = null;
        Collection<AccountTransactionProperty> properties = new ArrayList<>();
        if(!commandSender.equals(McNative.getInstance().getConsoleSender())) {
            MinecraftPlayer player = (MinecraftPlayer) commandSender;
            DKCoinsUser user = player.getAs(DKCoinsUser.class);
            member = account.getMember(user);
            if(member != null) {
                properties = DKCoins.getInstance().getTransactionPropertyBuilder().build(member);
            }
        }

        if(action.equalsIgnoreCase("setAmount")) {
            AccountTransaction transaction = account.getCredit(currency)
                    .setAmount(member, amount, CommandUtil.buildReason(args, 3), TransferCause.API, properties);
            commandSender.sendMessage(Messages.COMMAND_BANK_ADMIN_SET, VariableSet.create()
                    .addDescribed("transaction", transaction));
        } else if(action.equalsIgnoreCase("addAmount")) {
            AccountTransaction transaction = account.getCredit(currency).addAmount(member, amount,
                    CommandUtil.buildReason(args, 3), TransferCause.API, properties);
            commandSender.sendMessage(Messages.COMMAND_BANK_ADMIN_ADD, VariableSet.create()
                    .addDescribed("transaction", transaction));
        } else if(action.equalsIgnoreCase("removeAmount")) {
            AccountTransaction transaction = account.getCredit(currency).removeAmount(member, amount,
                    CommandUtil.buildReason(args, 3), TransferCause.API, properties);
            commandSender.sendMessage(Messages.COMMAND_BANK_ADMIN_REMOVE, VariableSet.create()
                    .addDescribed("transaction", transaction));
        }
    }
}
