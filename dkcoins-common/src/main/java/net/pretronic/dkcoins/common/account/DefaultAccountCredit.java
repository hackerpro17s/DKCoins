/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 24.11.19, 15:08
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.common.account;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.TransferResult;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.events.account.DKCoinsAccountTransactEvent;
import net.pretronic.dkcoins.common.DefaultDKCoins;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultAccountCredit implements AccountCredit {

    private final int id;
    private final BankAccount account;
    private final Currency currency;
    private double amount;

    public DefaultAccountCredit(int id, BankAccount account, Currency currency, double amount) {
        Validate.isTrue(id > 0);
        Validate.notNull(account);
        Validate.notNull(currency);
        this.id = id;
        this.account = account;
        this.currency = currency;
        this.amount = amount;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public BankAccount getAccount() {
        return this.account;
    }

    @Override
    public Currency getCurrency() {
        return this.currency;
    }

    @Override
    public boolean hasAmount(double amount) {
        return this.amount >= amount;
    }

    @Override
    public String getFormattedAmount() {
        return DefaultDKCoins.getInstance().getFormatter().formatCurrencyAmount(getAmount());
    }

    @Override
    public double getAmount() {
        return this.amount;
    }

    @Override
    public AccountTransaction setAmount(AccountMember executor, double amount, String reason, String cause, Collection<AccountTransactionProperty> properties) {
        Validate.notNull(cause);
        if(properties == null) properties = new ArrayList<>();
        if(reason == null) reason = "none";
        DKCoins.getInstance().getAccountManager().setAccountCreditAmount(this, amount);
        return getAccount().addTransaction(this, executor, this, amount, reason, cause, properties);
    }

    @Override
    public AccountTransaction addAmount(AccountMember executor, double amount, String reason, String cause, Collection<AccountTransactionProperty> properties) {
        Validate.notNull(cause);
        if(properties == null) properties = new ArrayList<>();
        if(reason == null) reason = "none";
        DKCoins.getInstance().getAccountManager().addAccountCreditAmount(this, amount);
        return getAccount().addTransaction(this, executor, this, amount, reason, cause, properties);
    }

    @Override
    public AccountTransaction removeAmount(AccountMember executor, double amount, String reason, String cause, Collection<AccountTransactionProperty> properties) {
        Validate.notNull(cause);
        if(properties == null) properties = new ArrayList<>();
        if(reason == null) reason = "none";
        DKCoins.getInstance().getAccountManager().removeAccountCreditAmount(this, amount);
        return getAccount().addTransaction(this, executor, this, amount, reason, cause, properties);
    }

    @Override
    public void setAmount(double amount) {
        DKCoins.getInstance().getAccountManager().setAccountCreditAmount(this, amount);
    }

    @Override
    public void addAmount(double amount) {
        DKCoins.getInstance().getAccountManager().addAccountCreditAmount(this, amount);
    }

    @Override
    public void removeAmount(double amount) {
        DKCoins.getInstance().getAccountManager().removeAccountCreditAmount(this, amount);
    }

    @Override
    public TransferResult canTransfer(AccountMember member, AccountCredit target, double amount) {
        if(getCurrency().isTransferDisabled(target.getCurrency())) {
            return new DefaultTransferResult(TransferResult.FailCause.TRANSFER_DISABLED);
        }
        if(getAmount() < amount) {
            return new DefaultTransferResult(TransferResult.FailCause.NOT_ENOUGH_AMOUNT);
        }
        if(!member.canAccess(AccessRight.WITHDRAW)) {
            return new DefaultTransferResult(TransferResult.FailCause.NOT_ENOUGH_ACCESS_RIGHTS);
        }
        if(getAccount().isMasterAccount() && getAccount().asMasterAccount().getCredit(getCurrency()).getAmount() < amount) {
            return new DefaultTransferResult(TransferResult.FailCause.MASTER_ACCOUNT_NOT_ENOUGH_AMOUNT);
        }
        if(member.hasLimitation(getCurrency(), amount)) {
            return new DefaultTransferResult(TransferResult.FailCause.LIMIT);
        }
        return new DefaultTransferResult(null);
    }

    @Override
    public TransferResult deposit(AccountMember member, double amount, String reason, Collection<AccountTransactionProperty> properties) {
        BankAccount account = member.getUser().getDefaultAccount();
        AccountMember accountMember = account.getMember(member.getUser());
        return account.getCredit(getCurrency()).transfer(accountMember, amount, this, reason, TransferCause.DEPOSIT, properties);
    }

    @Override
    public TransferResult withdraw(AccountMember member, double amount, String reason, Collection<AccountTransactionProperty> properties) {
        return transfer(member, amount, member.getUser().getDefaultAccount()
                .getCredit(getCurrency()), reason, TransferCause.WITHDRAW, properties);
    }

    @Override
    public TransferResult transfer(AccountMember member, double amount0, AccountCredit credit, String reason, String cause, Collection<AccountTransactionProperty> properties) {
        TransferResult result = canTransfer(member, credit, amount0);
        if(result.isSuccess()) {
            double amount = getCurrency().exchange(amount0, credit.getCurrency());
            credit.addAmount(amount);
            removeAmount(amount0);
            AccountTransaction transaction = account.addTransaction(this, member, credit, amount0, reason, cause, properties);
            ((DefaultTransferResult)result).setTransaction(transaction);
            DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountTransactEvent(transaction));
        }
        return result;
    }

    @Internal
    public void updateAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AccountCredit && ((AccountCredit)obj).getId() == this.id;
    }
}
