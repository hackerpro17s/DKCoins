/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 15.02.20, 21:35
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.common.account.transaction;

import net.pretronic.databasequery.api.query.result.QueryResultEntry;
import net.pretronic.databasequery.api.query.type.FindQuery;
import net.pretronic.databasequery.api.query.type.join.JoinType;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.TransactionFilter;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.common.DefaultDKCoins;
import net.pretronic.dkcoins.common.DefaultDKCoinsStorage;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;

import java.util.ArrayList;
import java.util.List;

public class DefaultTransactionFilter implements TransactionFilter {

    private BankAccount account;
    private String world;
    private String server;
    private Long time = null;
    private BankAccount receiver;
    private Currency currency;
    private String reason;
    private String cause;
    private int page;
    
    @Override
    public TransactionFilter account(BankAccount account) {
        this.account = account;
        return this;
    }

    @Override
    public TransactionFilter world(String world) {
        this.world = world;
        return this;
    }

    @Override
    public TransactionFilter server(String server) {
        this.server = server;
        return this;
    }

    @Override
    public TransactionFilter time(long time) {
        this.time = time;
        return this;
    }

    @Override
    public TransactionFilter receiver(BankAccount receiver) {
        this.receiver = receiver;
        return this;
    }

    @Override
    public TransactionFilter currency(Currency currency) {
        this.currency = currency;
        return this;
    }

    @Override
    public TransactionFilter reason(String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    public TransactionFilter cause(String cause) {
        this.cause = cause;
        return this;
    }

    @Override
    public TransactionFilter page(int page) {
        this.page = page;
        return this;
    }

    @Override
    public List<AccountTransaction> filter() {
        DefaultDKCoinsStorage storage = DefaultDKCoins.getInstance().getStorage();

        Validate.notNull(getAccount());
        FindQuery query = storage.getAccountTransaction()
                .find()
                .get("dkcoins_account_transaction.Id", "SenderAccountId","SenderAccountName", "SenderId","DestinationId","DestinationName", "dkcoins_account_transaction.Amount",
                        "ExchangeRate", "Reason", "Cause", "Time", "Key", "Value")
                .join(storage.getAccount(), JoinType.INNER).on("SenderAccountId", storage.getAccount(), "Id")
                .join(storage.getAccountTransactionProperty(), JoinType.LEFT).on("Id", storage.getAccountTransactionProperty(), "TransactionId")
                .or(subQuery -> subQuery.where("SenderAccountId", getAccount().getId()).where("DestinationId", getAccount().getId()));

        if(getWorld() != null) {
            query.and(subQuery ->
                    subQuery.where("dkcoins_account_transaction_property.Key", "world")
                            .where("dkcoins_account_transaction_property.Value", getWorld()));
        }

        if(getServer() != null) {
            query.and(subQuery ->
                    subQuery.where("dkcoins_account_transaction_property.key", "server")
                            .where("dkcoins_account_transaction_property.Value", getServer()));
        }
        if(getTime() != null) {
            query.where("Time", getTime());
        }
        if(getReceiver() != null) {
            query.where("ReceiverId", getReceiver().getId());
        }
        if(getCurrency() != null) {
            query.where("SourceId", getAccount().getCredit(getCurrency()).getId());
        }
        if(getReason() != null) {
            query.where("Reason", getReason());
        }
        if(getCause() != null) {
            query.where("Cause", getCause());
        }
        int page = getPage() > 0 ? getPage() : 1;
        query.page(page, 5);

        List<AccountTransaction> transactions = new ArrayList<>();

        DefaultAccountTransaction last = null;
        for (QueryResultEntry entry : query.execute()) {
            int id = entry.getInt("Id");
            DefaultAccountTransaction transaction;

            if(last != null && last.getId() == id) {
                transaction = last;
            } else {
                Currency currency = DKCoins.getInstance().getCurrencyManager().getCurrency(entry.getInt("CurrencyId"));
                BankAccount destination = DKCoins.getInstance().getAccountManager().getAccount(entry.getInt("DestinationId"));
                transaction = new DefaultAccountTransaction(id,
                        getAccount().getCredit(currency),
                        DKCoins.getInstance().getAccountManager().getAccountMember(entry.getInt("SenderId")),
                        destination.getCredit(currency),
                        entry.getDouble("Amount"),
                        entry.getDouble("ExchangeRate"),
                        entry.getString("Reason"),
                        entry.getString("Cause"),
                        entry.getLong("Time"),
                        new ArrayList<>());
                transactions.add(transaction);
            }

            if(entry.contains("Key")) {
                Object value = entry.getObject("Value");
                if(value == null) continue;
                transaction.getProperties().add(new DefaultAccountTransactionProperty(entry.getString("Key"), value));
            }
            last = transaction;
        }
        return transactions;
    }

    @Internal
    public BankAccount getAccount() {
        return account;
    }

    @Internal
    public String getWorld() {
        return world;
    }

    @Internal
    public String getServer() {
        return server;
    }

    @Internal
    public Long getTime() {
        return time;
    }

    @Internal
    public BankAccount getReceiver() {
        return receiver;
    }

    @Internal
    public Currency getCurrency() {
        return currency;
    }

    @Internal
    public String getReason() {
        return reason;
    }

    @Internal
    public String getCause() {
        return cause;
    }

    @Internal
    public int getPage() {
        return page;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof DefaultTransactionFilter)) return false;
        DefaultTransactionFilter filter = (DefaultTransactionFilter) obj;
        return filter.account.equals(this.account)
                && filter.world.equals(this.world)
                && filter.server.equals(this.server)
                && filter.time.equals(this.time)
                && filter.receiver.equals(this.receiver)
                && filter.currency.equals(this.currency)
                && filter.reason.equals(this.reason)
                && filter.cause.equals(this.cause)
                && filter.page == this.page;
    }
}
