package net.pretronic.dkcoins.minecraft.commands.dkcoins.admin;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.common.DefaultDKCoins;
import net.pretronic.dkcoins.common.account.DefaultBankAccount;
import net.pretronic.dkcoins.common.account.TransferCause;
import net.pretronic.dkcoins.minecraft.DKCoinsMessagingChannelAction;
import net.pretronic.dkcoins.minecraft.DKCoinsPlugin;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.libraries.command.Completable;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

import java.util.Collection;
import java.util.Collections;

public class DKCoinsBankAdminResetCommand extends ObjectCommand<BankAccount> implements Completable {

    public DKCoinsBankAdminResetCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("reset"));
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        String currency0 = args.length == 1 ? args[0] : null;

        Currency currency = CommandUtil.parseCurrency(commandSender, currency0);
        if(currency != null) {
            String reason = CommandUtil.buildReason(args, 2);
            AccountMember member = account.getMember(CommandUtil.getUserByCommandSender(commandSender));
            if(account.equals(DefaultBankAccount.DUMMY_ALL)) {
                CommandUtil.loopThroughUserBanks(null, target -> reset(commandSender, target, currency, member, reason));
            } else if(account.equals(DefaultBankAccount.DUMMY_ALL_OFFLINE)) {
                DefaultDKCoins.getInstance().getStorage().getAccountCredit().update()
                        .set("Amount", 0)
                        .execute();
                DefaultDKCoins.getInstance().getAccountManager().clearCaches();
                DKCoinsPlugin.getInstance().broadcastNetworkAction(DKCoinsMessagingChannelAction.CLEAR_CACHES);
                commandSender.sendMessage(Messages.COMMAND_BANK_ADMIN_RESET);
            } else {
                reset(commandSender, account, currency, member, reason);
            }
        }
    }

    private void reset(CommandSender commandSender, BankAccount account, Currency currency, AccountMember member, String reason) {
        AccountTransaction transaction = account.getCredit(currency)
                .setAmount(member, 0, reason, TransferCause.ADMIN, DKCoins.getInstance().getTransactionPropertyBuilder().build(member));
        commandSender.sendMessage(Messages.COMMAND_BANK_ADMIN_RESET, VariableSet.create()
                .addDescribed("transaction", transaction));
    }

    @Override
    public Collection<String> complete(CommandSender commandSender, String[] args) {
        if(args.length == 1) {
            return Iterators.map(DKCoins.getInstance().getCurrencyManager().getCurrencies()
                    ,Currency::getName
                    ,currency -> currency.getName().toLowerCase().startsWith(args[0].toLowerCase()));
        }
        return Collections.emptyList();
    }
}
