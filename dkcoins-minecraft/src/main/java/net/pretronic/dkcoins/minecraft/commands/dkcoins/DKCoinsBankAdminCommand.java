package net.pretronic.dkcoins.minecraft.commands.dkcoins;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.common.account.DefaultBankAccount;
import net.pretronic.dkcoins.common.account.TransferCause;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.dkcoins.admin.DKCoinsBankAdminChangeCommand;
import net.pretronic.dkcoins.minecraft.commands.dkcoins.admin.DKCoinsBankAdminResetCommand;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.DefinedNotFindable;
import net.pretronic.libraries.command.command.object.MainObjectCommand;
import net.pretronic.libraries.command.command.object.ObjectCompletable;
import net.pretronic.libraries.command.command.object.ObjectNotFindable;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.MinecraftPlayer;

import java.util.Collection;

public class DKCoinsBankAdminCommand extends MainObjectCommand<BankAccount> implements DefinedNotFindable<BankAccount>, ObjectNotFindable, ObjectCompletable {

    public DKCoinsBankAdminCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.newBuilder()
                .name("admin").aliases("bankAdmin")
                .create());

        registerCommand(new DKCoinsBankAdminChangeCommand(owner, "add", Messages.COMMAND_BANK_ADMIN_ADD, (account, currency, member, amount, reason) ->
                account.getCredit(currency).addAmount(member, amount, reason, TransferCause.ADMIN, DKCoins.getInstance().getTransactionPropertyBuilder().build(member))));

        registerCommand(new DKCoinsBankAdminChangeCommand(owner, "remove", Messages.COMMAND_BANK_ADMIN_REMOVE, (account, currency, member, amount, reason) ->
                account.getCredit(currency).removeAmount(member, amount, reason, TransferCause.ADMIN, DKCoins.getInstance().getTransactionPropertyBuilder().build(member))));

        registerCommand(new DKCoinsBankAdminChangeCommand(owner, "set", Messages.COMMAND_BANK_ADMIN_SET, (account, currency, member, amount, reason) ->
                account.getCredit(currency).setAmount(member, amount, reason, TransferCause.ADMIN, DKCoins.getInstance().getTransactionPropertyBuilder().build(member))));

        registerCommand(new DKCoinsBankAdminResetCommand(owner));
    }

    @Override
    public BankAccount getObject(CommandSender commandSender, String name) {
        if(DKCoinsConfig.isPaymentAllAlias(name)) {
            return DefaultBankAccount.DUMMY_ALL;
        } else {
            return DKCoins.getInstance().getAccountManager().searchAccount(name);
        }
    }

    @Override
    public void commandNotFound(CommandSender commandSender, BankAccount account, String command, String[] args) {
        commandSender.sendMessage(Messages.COMMAND_DKCOINS_HELP);
    }

    @Override
    public void objectNotFound(CommandSender commandSender, String bankAccount, String[] args) {
        commandSender.sendMessage(Messages.ERROR_ACCOUNT_NOT_EXISTS, VariableSet.create()
                .add("name", bankAccount));
    }

    @Override
    public Collection<String> complete(CommandSender commandSender, String name) {
        return Iterators.map(McNative.getInstance().getLocal().getConnectedPlayers()
                , MinecraftPlayer::getName);
    }
}
