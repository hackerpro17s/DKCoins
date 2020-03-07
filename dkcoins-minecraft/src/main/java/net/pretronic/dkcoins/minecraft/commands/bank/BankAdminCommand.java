package net.pretronic.dkcoins.minecraft.commands.bank;

import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.command.object.ObjectCommand;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.message.bml.variable.VariableSet;
import net.prematic.libraries.utility.GeneralUtil;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.account.TransferCause;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import org.mcnative.common.player.MinecraftPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class BankAdminCommand extends ObjectCommand<BankAccount> {

    public BankAdminCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.newBuilder().name("admin").permission("dkcoins.admin").create());
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        System.out.println(Arrays.toString(args));
        if(args.length < 3) {
            commandSender.sendMessage(Messages.COMMAND_BANK_ADMIN_HELP);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "help" : {
                commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_HELP);
                return;
            }
            case "addamount" : case "removeamount" : case "setamount" : {
                String amount0 = args[1];
                if(!GeneralUtil.isNumber(amount0)) {
                    commandSender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create().add("value", amount0));
                    return;
                }
                double amount = Double.parseDouble(amount0);
                Currency currency = DKCoins.getInstance().getCurrencyManager().searchCurrency(args[2]);
                if(currency == null) {
                    commandSender.sendMessage(Messages.ERROR_CURRENCY_NOT_EXISTS, VariableSet.create().add("name", args[2]));
                    return;
                }
                AccountMember member = getMemberOrNull(account, commandSender);
                Collection<AccountTransactionProperty> properties = new ArrayList<>();
                if(member != null) {
                    properties = DKCoins.getInstance().getTransactionPropertyBuilder().build(member);
                }
                switch (args[0].toLowerCase()) {
                    case "addamount": {
                        account.getCredit(currency).addAmount(member, amount,
                                CommandUtil.buildReason(args, 3), TransferCause.API, properties);
                        commandSender.sendMessage(Messages.COMMAND_BANK_ADMIN_ADD, VariableSet.create()
                                .add("amount", amount).add("currency_symbol", currency.getSymbol()).add("bank", account.getName()));
                        return;
                    }
                    case "removeamount": {
                        account.getCredit(currency).removeAmount(member, amount,
                                CommandUtil.buildReason(args, 3), TransferCause.API, properties);
                        commandSender.sendMessage(Messages.COMMAND_BANK_ADMIN_REMOVE, VariableSet.create()
                                .add("amount", amount).add("currency_symbol", currency.getSymbol()).add("bank", account.getName()));
                        return;
                    }
                    case "setamount": {
                        account.getCredit(currency).setAmount(member, amount,
                                CommandUtil.buildReason(args, 3), TransferCause.API, properties);
                        commandSender.sendMessage(Messages.COMMAND_BANK_ADMIN_SET, VariableSet.create()
                                .add("amount", amount).add("currency_symbol", currency.getSymbol()).add("bank", account.getName()));
                        return;
                    }
                }
            }
            default: {
                commandSender.sendMessage(Messages.COMMAND_BANK_ADMIN_HELP);
            }
        }
    }

    private AccountMember getMemberOrNull(BankAccount account, CommandSender commandSender) {
        if(commandSender instanceof MinecraftPlayer) {
            return account.getMember(DKCoins.getInstance().getUserManager()
                    .getUser(((MinecraftPlayer)commandSender).getUniqueId()));
        }
        return null;
    }
}