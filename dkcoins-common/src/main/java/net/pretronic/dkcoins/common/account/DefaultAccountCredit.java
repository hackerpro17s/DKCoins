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

import net.pretronic.databasequery.api.query.SearchOrder;
import net.pretronic.databasequery.api.query.function.QueryFunction;
import net.pretronic.databasequery.api.query.result.QueryResult;
import net.pretronic.databasequery.api.query.result.QueryResultEntry;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.account.transferresult.TransferResult;
import net.pretronic.dkcoins.api.account.transferresult.TransferResultFailCause;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.events.account.DKCoinsAccountTransactEvent;
import net.pretronic.dkcoins.common.DefaultDKCoins;
import net.pretronic.dkcoins.common.SyncAction;
import net.pretronic.libraries.document.Document;
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

        setAmountInternal(amount);



        return getAccount().addTransaction(this, executor, this, amount, reason, cause, properties);
    }

    @Override
    public AccountTransaction addAmount(AccountMember executor, double amount, String reason, String cause, Collection<AccountTransactionProperty> properties) {
        Validate.notNull(cause);
        if(properties == null) properties = new ArrayList<>();
        if(reason == null) reason = "none";

        addAmountInternal(amount);



        return getAccount().addTransaction(this, executor, this, amount, reason, cause, properties);
    }

    @Override
    public AccountTransaction removeAmount(AccountMember executor, double amount, String reason, String cause, Collection<AccountTransactionProperty> properties) {
        Validate.notNull(cause);
        if(properties == null) properties = new ArrayList<>();
        if(reason == null) reason = "none";

        removeAmountInternal(amount);



        return getAccount().addTransaction(this, executor, this, amount, reason, cause, properties);
    }

    @Override
    public void setAmount(double amount) {
        setAmountInternal(amount);
    }

    @Override
    public void addAmount(double amount) {
        addAmountInternal(amount);
    }

    @Override
    public void removeAmount(double amount) {
        removeAmountInternal(amount);
    }

    @Override
    public int getTopPos() {
        QueryResult result = DefaultDKCoins.getInstance().getStorage().getDatabase()
                .getRowNumberInnerQueryCollection(DefaultDKCoins.getInstance().getStorage().getAccountCredit(), "Position",
                        QueryFunction.rowNumberFunction("Amount", SearchOrder.DESC))
                .find()
                .get("RowNumber")
                .where("Id", getId())
                .execute();
        QueryResultEntry resultEntry = result.firstOrNull();
        if(resultEntry == null) return -1;
        return resultEntry.getInt("RowNumber");
    }

    @Override
    public TransferResult canTransfer(AccountMember member, AccountCredit target, double amount) {
        if(getCurrency().isTransferDisabled(target.getCurrency())) {
            return new DefaultTransferResult(TransferResultFailCause.TRANSFER_DISABLED);
        }
        if(getAmount() < amount) {
            return new DefaultTransferResult(TransferResultFailCause.NOT_ENOUGH_AMOUNT);
        }
        if(!member.canAccess(AccessRight.WITHDRAW)) {
            return new DefaultTransferResult(TransferResultFailCause.NOT_ENOUGH_ACCESS_RIGHTS);
        }
        if(getAccount().isMasterAccount() && getAccount().asMasterAccount().getCredit(getCurrency()).getAmount() < amount) {
            return new DefaultTransferResult(TransferResultFailCause.MASTER_ACCOUNT_NOT_ENOUGH_AMOUNT);
        }
        if(member.hasLimitation(getCurrency(), amount)) {
            return new DefaultTransferResult(TransferResultFailCause.LIMIT);
        }
        return new DefaultTransferResult(null);
    }

    @Override
    public TransferResult deposit(AccountMember member, double amount, String reason, Collection<AccountTransactionProperty> properties) {
        BankAccount account = member.getUser().getDefaultAccount();
        AccountMember accountMember = account.getMember(member.getUser());
        return account.getCredit(getCurrency()).transfer(accountMember, amount, this, reason, TransferCause.API, properties);
    }

    @Override
    public TransferResult withdraw(AccountMember member, double amount, String reason, Collection<AccountTransactionProperty> properties) {
        return transfer(member, amount, member.getUser().getDefaultAccount()
                .getCredit(getCurrency()), reason, TransferCause.API, properties);
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

    @Internal
    public void reloadAmount() {
        this.amount = DefaultDKCoins.getInstance().getStorage().getAccountCredit().find()
                .get("Amount")
                .where("Id", id)
                .execute().firstOrNull()
                .getDouble("Amount");
    }

    @Internal
    private void setAmountInternal(double amount) {
        DefaultDKCoins.getInstance().getStorage().getAccountCredit().update()
                .set("Amount", amount)
                .where("Id", getId())
                .execute();
        updateAmount(amount);
        DefaultDKCoins.getInstance().getAccountManager().getAccountCache().getCaller().updateAndIgnore(getAccount().getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_CREDIT_AMOUNT_UPDATE)
                .add("creditId", getId()));
    }

    @Internal
    private void addAmountInternal(double amount) {
        DefaultDKCoins.getInstance().getStorage().getAccountCredit().update()
                .add("Amount", amount)
                .where("Id", id)
                .execute();
        updateAmount(getAmount()+amount);
        DefaultDKCoins.getInstance().getAccountManager().getAccountCache().getCaller().updateAndIgnore(getAccount().getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_CREDIT_AMOUNT_UPDATE)
                .add("creditId", getId()));
    }

    @Internal
    private void removeAmountInternal(double amount) {
        DefaultDKCoins.getInstance().getStorage().getAccountCredit().update()
                .subtract("Amount", amount)
                .where("Id", id)
                .execute();
        updateAmount(getAmount()-amount);
        DefaultDKCoins.getInstance().getAccountManager().getAccountCache().getCaller().updateAndIgnore(getAccount().getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_CREDIT_AMOUNT_UPDATE)
                .add("creditId", getId()));
    }
}
