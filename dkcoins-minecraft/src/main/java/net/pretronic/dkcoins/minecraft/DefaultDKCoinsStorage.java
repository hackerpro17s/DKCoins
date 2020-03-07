/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 18.11.19, 21:22
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft;

import net.prematic.databasequery.api.Database;
import net.prematic.databasequery.api.collection.DatabaseCollection;
import net.prematic.databasequery.api.collection.field.FieldOption;
import net.prematic.databasequery.api.datatype.DataType;
import net.prematic.databasequery.api.query.ForeignKey;
import net.prematic.databasequery.api.query.SearchOrder;
import net.prematic.databasequery.api.query.result.QueryResult;
import net.prematic.databasequery.api.query.result.QueryResultEntry;
import net.prematic.databasequery.api.query.type.FindQuery;
import net.prematic.databasequery.api.query.type.InsertQuery;
import net.prematic.libraries.utility.Validate;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.DKCoinsStorage;
import net.pretronic.dkcoins.api.account.*;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.account.transaction.TransactionFilter;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.account.*;
import net.pretronic.dkcoins.minecraft.account.DefaultBankAccount;
import net.pretronic.dkcoins.minecraft.account.transaction.DefaultAccountTransaction;
import net.pretronic.dkcoins.minecraft.account.transaction.DefaultTransactionFilter;
import net.pretronic.dkcoins.minecraft.currency.DefaultCurrency;
import net.pretronic.dkcoins.minecraft.currency.DefaultCurrencyExchangeRate;
import net.pretronic.dkcoins.minecraft.user.DefaultDKCoinsUser;

import java.util.*;

public class DefaultDKCoinsStorage implements DKCoinsStorage {

    private final Database database;
    private final DatabaseCollection accountType;
    private final DatabaseCollection account;
    private final DatabaseCollection accountCredit;
    private final DatabaseCollection accountMember;
    private final DatabaseCollection accountTransaction;
    private final DatabaseCollection accountTransactionProperty;
    private final DatabaseCollection accountLimitation;
    private final DatabaseCollection currency;
    private final DatabaseCollection currencyExchangeRate;

    public DefaultDKCoinsStorage(Database database) {
        this.database = database;
        this.accountType = createAccountTypeDatabaseCollection();
        this.account = createAccountDatabaseCollection();
        this.currency = createCurrencyDatabaseCollection();
        this.currencyExchangeRate = createCurrencyExchangeRateDatabaseCollection();
        this.accountCredit = createAccountCreditDatabaseCollection();
        this.accountMember = createAccountMemberDatabaseCollection();
        this.accountTransaction = createAccountTransactionDatabaseCollection();
        this.accountTransactionProperty = createAccountTransactionPropertyCollection();
        this.accountLimitation = createAccountLimitationsDatabaseCollection();
    }

    @Override
    public AccountType getAccountType(int id) {
        QueryResultEntry result = this.accountType.find().get("name", "symbol").where("id", id).execute().first();
        return new DefaultAccountType(id, result.getString("name"), result.getString("symbol"));
    }

    @Override
    public AccountType searchAccountType(Object identifier) {
        QueryResultEntry result = this.accountType.find()
                .or(query -> query.where("name", identifier).where("symbol", identifier).where("id", identifier))
                .execute().firstOrNull();
        if(result == null) return null;
        return new DefaultAccountType(result.getInt("id"), result.getString("name"), result.getString("symbol"));
    }

    @Override
    public AccountType createAccountType(String name, String symbol) {
        int id = this.accountType.insert().set("name", name).set("symbol", symbol).executeAndGetGeneratedKeyAsInt("id");
        return new DefaultAccountType(id, name, symbol);
    }

    @Override
    public void updateAccountTypeName(int id, String name) {
        this.accountType.update().set("name", name).where("id", id).execute();
    }

    @Override
    public void updateAccountTypeSymbol(int id, String symbol) {
        this.accountType.update().set("symbol", symbol).where("id", id).execute();
    }

    @Override
    public void deleteAccountType(int id) {
        this.accountType.delete().where("id", id).execute();
    }

    @Override
    public Collection<Integer> getAccountIds(UUID userId) {
        Collection<Integer> accountIds = new ArrayList<>();
        this.account.find().get("dkcoins_account.id")
                .join(this.accountMember).on("id", this.accountMember, "accountId")
                .where("userId", userId).execute().loadIn(accountIds, entry -> entry.getInt("id"));
        return accountIds;
    }

    @Override
    public BankAccount getAccount(int id) {
        return getAccount(this.account.find().where("id", id).execute().firstOrNull());
    }

    @Override
    public BankAccount searchAccount(Object identifier) {
        return getAccount(this.account.find().or(query -> query.where("id", identifier).where("name", identifier)).execute().firstOrNull());
    }

    @Override
    public BankAccount getAccountByCredit(int creditId) {
        return getAccount(this.account.find().join(this.accountCredit).
                on("id", this.accountCredit, "accountId")
                .where("id", creditId)
                .execute().firstOrNull());
    }

    @Override
    public MasterBankAccount getMasterAccount(int id) {
        return (MasterBankAccount) getAccount(this.account.find().where("id", id).where("master", true).execute().firstOrNull());
    }

    @Override
    public BankAccount getSubAccount(int masterAccountId, int id) {
        return getAccount(this.account.find().where("id", id).where("parentId", masterAccountId).execute().firstOrNull());
    }

    @Override
    public MasterBankAccount getSubMasterAccount(int masterAccountId, int id) {
        return (MasterBankAccount) getAccount(this.account.find().where("id", id).where("master", true)
                .where("parentId", masterAccountId).execute().firstOrNull());
    }

    private BankAccount getAccount(QueryResultEntry resultEntry) {
        if(resultEntry == null) return null;
        int id = resultEntry.getInt("id");
        boolean master = resultEntry.getBoolean("master");

        DefaultBankAccount account;
        if(master) {
            account = new DefaultMasterBankAccount(id, resultEntry.getString("name"),
                    DKCoins.getInstance().getAccountManager().getAccountType(resultEntry.getInt("typeId")),
                    resultEntry.getBoolean("disabled"),
                    DKCoins.getInstance().getAccountManager().getMasterAccount(resultEntry.getInt("parentId")));
        } else {
            account = new DefaultBankAccount(id, resultEntry.getString("name"),
                    DKCoins.getInstance().getAccountManager().getAccountType(resultEntry.getInt("typeId")),
                    resultEntry.getBoolean("disabled"),
                    DKCoins.getInstance().getAccountManager().getMasterAccount(resultEntry.getInt("parentId")));
        }

        for (QueryResultEntry entry : this.accountLimitation.find().where("accountId", id)
                .whereIsNull("memberId").execute()) {
            int memberRoleId = entry.getInt("memberRoleId");
            account.addLoadedLimitation(new DefaultAccountLimitation(entry.getInt("id"), account, null,
                    AccountMemberRole.byIdOrNull(memberRoleId),
                    DKCoins.getInstance().getCurrencyManager().getCurrency(entry.getInt("currencyId")),
                    entry.getDouble("amount"), entry.getLong("interval")));
        }
        for(QueryResultEntry entry : this.accountCredit.find().where("accountId", id).execute()) {
            account.addLoadedAccountCredit(new DefaultAccountCredit(entry.getInt("id"), account,
                    DKCoins.getInstance().getCurrencyManager().getCurrency(entry.getInt("currencyId")),
                    entry.getDouble("amount")));
        }
        for (QueryResultEntry entry : this.accountMember.find().where("accountId", id).execute()) {
            DefaultAccountMember member = new DefaultAccountMember(entry.getInt("id"), account,
                    DKCoins.getInstance().getUserManager().getUser(entry.getUniqueId("userId")),
                    AccountMemberRole.byId(entry.getInt("roleId")));
            loadAccountMemberLimitations(member);
            account.addLoadedMember(member);
        }
        return account;
    }

    @Override
    public BankAccount createAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator) {
        return createAccount(name, type, disabled, parent, creator, false);
    }

    @Override
    public MasterBankAccount createMasterAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator) {
        return (MasterBankAccount) createAccount(name, type, disabled, parent, creator, true);
    }

    private BankAccount createAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser user, boolean master) {
        int id = this.account.insert().set("name", name).set("typeId", type.getId()).set("disabled", disabled)
                .set("parentId", parent == null ? null : parent.getId()).set("master", master).executeAndGetGeneratedKeyAsInt("id");
        DefaultBankAccount account;
        if(master) {
            account = new DefaultMasterBankAccount(id, name, type, disabled, parent);
        } else {
            account = new DefaultBankAccount(id, name, type,
                    disabled, parent);
        }
        account.addMember(user, AccountMemberRole.OWNER);
        return account;
    }

    @Override
    public void updateAccountName(int id, String name) {
        this.account.update().set("name", name).where("id", id).execute();
    }

    @Override
    public void updateAccountTypeId(int id, int typeId) {
        this.account.update().set("typeId", typeId).where("id", id).execute();
    }

    @Override
    public void updateAccountDisabled(int id, boolean disabled) {
        this.account.update().set("disabled", disabled).where("id", id).execute();
    }

    @Override
    public void updateAccountParentId(int id, int parentId) {
        this.account.update().set("parentId", parentId).where("id", id).execute();
    }

    @Override
    public void deleteAccount(int id) {
        this.account.delete().where("id", id).execute();
    }

    @Override
    public AccountCredit addAccountCredit(BankAccount account, Currency currency, double amount) {
        int id = this.accountCredit.insert().set("accountId", account.getId()).set("currencyId", currency.getId()).set("amount", amount)
                .executeAndGetGeneratedKeyAsInt("id");
        System.out.println("addAccountCredit2");
        return new DefaultAccountCredit(id, account, currency, amount);
    }

    @Override
    public void setAccountCreditAmount(int id, double amount) {
        this.accountCredit.update().set("amount", amount).where("id", id).execute();
    }

    @Override
    public void deleteAccountCredit(int id) {
        this.accountCredit.delete().where("id", id).execute();
    }

    @Override
    public AccountLimitation addAccountLimitation(BankAccount account, AccountMember accountMember,
                                                  AccountMemberRole memberRole, Currency comparativeCurrency, double amount, long interval) {

        InsertQuery query = this.accountLimitation.insert().set("accountId", account.getId())
                .set("comparativeCurrencyId", comparativeCurrency.getId())
                .set("amount", amount).set("interval", interval);
        if(accountMember != null) query.set("memberId", accountMember.getId());
        if(memberRole != null) query.set("memberRoleId", memberRole.getId());
        int id = query.executeAndGetGeneratedKeyAsInt("id");
        return new DefaultAccountLimitation(id, account, accountMember, memberRole, comparativeCurrency, amount, interval);
    }

    @Override
    public void removeAccountLimitation(int id) {
        this.accountLimitation.delete().where("id", id).execute();
    }

    @Override
    public int getAccountMember(int id) {
        /*QueryResultEntry result = this.accountMember.find().where("id", id).execute().firstOrNull();
        if(result == null) return null;
        DefaultAccountMember member = new DefaultAccountMember(id, DKCoins.getInstance().getAccountManager()
                .getAccount(result.getInt("accountId")),
                DKCoins.getInstance().getUserManager().getUser(result.getUniqueId("userId")),
                AccountMemberRole.byId(result.getInt("roleId")));
        loadAccountMemberLimitations(member);
        return member;*/
        throw new UnsupportedOperationException();
    }

    @Override
    public AccountMember getAccountMember(DKCoinsUser user, BankAccount account) {
        QueryResultEntry result = this.accountMember.find().where("userId", user.getUniqueId())
                .where("accountId", account.getId()).execute().firstOrNull();
        if(result == null) return null;
        int id = result.getInt("id");
        DefaultAccountMember member = new DefaultAccountMember(id, account, user, AccountMemberRole.byId(result.getInt("roleId")));
        loadAccountMemberLimitations(member);
        return member;
    }

    private void loadAccountMemberLimitations(DefaultAccountMember member) {
        for (QueryResultEntry entry : this.accountLimitation.find().where("accountId",
                member.getAccount().getId()).where("memberId", member.getId()).execute()) {
            member.addLoadedLimitation(new DefaultAccountLimitation(entry.getInt("id"), member.getAccount(), member,
                    null,
                    DKCoins.getInstance().getCurrencyManager().getCurrency(entry.getInt("comparativeCurrencyId")),
                    entry.getDouble("amount"), entry.getLong("interval")));
        }
    }

    @Override
    public AccountMember addAccountMember(BankAccount account, DKCoinsUser user, AccountMemberRole role) {
        int id = this.accountMember.insert().set("accountId", account.getId())
                .set("userId", user.getUniqueId()).set("roleId", role.getId()).executeAndGetGeneratedKeyAsInt("id");
        return new DefaultAccountMember(id, account, user, role);
    }

    @Override
    public void updateAccountMemberRole(AccountMember member) {
        this.accountMember.update().set("roleId", member.getRole().getId()).where("id", member.getId()).execute();
    }

    @Override
    public void removeAccountMember(int id) {
        this.accountMember.delete().where("id", id).execute();
    }

    @Override
    public Collection<AccountTransaction> getAccountTransactions(int senderId, int start, int end) {
        Collection<AccountTransaction> transactions = new ArrayList<>();
        this.accountTransaction.find().where("senderId", senderId).orderBy("id", SearchOrder.DESC).limit(end-start, start);
        return transactions;
    }

    @Override
    public AccountTransaction addAccountTransaction(AccountCredit source, AccountMember sender, AccountCredit receiver,
                                                    double amount, double exchangeRate, String reason, String cause,
                                                    long time, Collection<AccountTransactionProperty> properties) {
        int id = this.accountTransaction.insert()
                .set("sourceId", source.getId())
                .set("senderId", sender.getId())
                .set("receiverId", receiver.getId())
                .set("amount", amount)
                .set("exchangeRate", exchangeRate)
                .set("reason", reason)
                .set("cause", cause)
                .set("time", time)
                .executeAndGetGeneratedKeyAsInt("id");
        if(!properties.isEmpty()) {
            InsertQuery propertyInsertQuery = this.accountTransactionProperty.insert();
            for (AccountTransactionProperty property : properties) {
                propertyInsertQuery.set("key", property.getKey()).set("value", property.asObject());
            }
            propertyInsertQuery.execute();
        }
        return new DefaultAccountTransaction(id, source, sender, receiver, amount, exchangeRate, reason, cause, time, properties);
    }

    @Override
    public Collection<Currency> getCurrencies() {
        Collection<Currency> currencies = new ArrayList<>();
        this.currency.find().execute().loadIn(currencies, entry ->
                new DefaultCurrency(entry.getInt("id"), entry.getString("name"), entry.getString("symbol")));
        return currencies;
    }

    @Override
    public Currency getCurrency(int id) {
        return getCurrency(this.currency.find().where("id", id).execute().firstOrNull());
    }

    @Override
    public Currency getCurrency(String name) {
        return getCurrency(this.currency.find().where("name", name).execute().firstOrNull());
    }

    @Override
    public Currency searchCurrency(Object identifier) {
        QueryResultEntry result = this.currency.find()
                .or(query -> query.where("name", identifier).where("symbol", identifier).where("id", identifier))
                .execute().firstOrNull();
        return getCurrency(result);
    }

    private Currency getCurrency(QueryResultEntry result) {
        if(result == null) return null;
        DefaultCurrency currency = new DefaultCurrency(result.getInt("id"), result.getString("name"), result.getString("symbol"));
        for (QueryResultEntry entry : this.currencyExchangeRate.find().where("currencyId", result.getInt("id")).execute()) {
            currency.addInternalExchangeRate(new DefaultCurrencyExchangeRate(entry.getInt("id"), currency,
                    DKCoins.getInstance().getCurrencyManager().getCurrency(entry.getInt("targetCurrencyId")), entry.getDouble("exchangeAmount")));
        }
        return currency;
    }

    @Override
    public Currency createCurrency(String name, String symbol) {
        int id = this.currency.insert().set("name", name).set("symbol", symbol).executeAndGetGeneratedKeyAsInt("id");
        return new DefaultCurrency(id, name, symbol);
    }

    @Override
    public void updateCurrencyName(int id, String name) {
        this.currency.update().set("name", name).where("id", id).execute();
    }

    @Override
    public void updateCurrencySymbol(int id, String symbol) {
        this.currency.update().set("symbol", symbol).where("id", id).execute();
    }

    @Override
    public void deleteCurrency(int id) {
        this.currency.delete().where("id", id).execute();
    }

    @Override
    public CurrencyExchangeRate getCurrencyExchangeRate(int currencyId, int targetCurrencyId) {
        QueryResultEntry entry = this.currencyExchangeRate.find().where("currencyId", currencyId)
                .where("targetCurrencyId", targetCurrencyId).execute().firstOrNull();
        if(entry == null) return null;
        return new DefaultCurrencyExchangeRate(entry.getInt("id"), DKCoins.getInstance().getCurrencyManager().getCurrency(currencyId),
                DKCoins.getInstance().getCurrencyManager().getCurrency(targetCurrencyId), entry.getDouble("exchangeAmount"));
    }

    @Override
    public CurrencyExchangeRate createCurrencyExchangeRate(int currencyId, int targetCurrencyId, double exchangeAmount) {
        int id = this.currencyExchangeRate.insert().set("currencyId", currencyId).set("targetCurrencyId", targetCurrencyId)
                .set("exchangeAmount", exchangeAmount).executeAndGetGeneratedKeyAsInt("id");
        return new DefaultCurrencyExchangeRate(id, DKCoins.getInstance().getCurrencyManager().getCurrency(currencyId),
                DKCoins.getInstance().getCurrencyManager().getCurrency(targetCurrencyId), exchangeAmount);
    }

    @Override
    public void updateCurrencyExchangeAmount(int selectedId, int targetId, double exchangeAmount) {
        this.currencyExchangeRate.update().set("exchangeAmount", exchangeAmount).where("currencyId", selectedId)
                .where("targetCurrencyId", targetId).execute();
    }

    @Override
    public void deleteCurrencyExchangeRate(int id) {
        this.currencyExchangeRate.delete().where("id", id).execute();
    }

    @Override
    public DKCoinsUser getUser(UUID uniqueId) {
        return new DefaultDKCoinsUser(uniqueId);
    }

    @Override
    public List<AccountTransaction> filterAccountTransactions(TransactionFilter filter0) {
        Validate.isTrue(filter0 instanceof DefaultTransactionFilter);
        DefaultTransactionFilter filter = (DefaultTransactionFilter) filter0;
        Validate.notNull(filter.getAccount());
        FindQuery query = this.accountTransaction
                .find()
                .get("id", "sourceId", "senderId","receiverId", "amount", "exchangeRate", "reason", "cause", "time")
                .join(this.accountCredit).on("sourceId", "dkcoins_account_credit.id")
                .where("accountId", filter.getAccount().getId())
                .join(this.accountTransactionProperty).on("dkcoins_account_transaction.id", "transactionId");
        if(filter.getWorld() != null) {
            query.and(subQuery ->
                    subQuery.where("dkcoins_account_transaction_property.key", "world")
                            .where("dkcoins_account_transaction_property.value", filter.getWorld()));
        }
        if(filter.getServer() != null) {
            query.and(subQuery ->
                    subQuery.where("dkcoins_account_transaction_property.key", "server")
                            .where("dkcoins_account_transaction_property.value", filter.getServer()));
        }
        if(filter.getTime() != -1) {
            query.where("time", filter.getTime());
        }
        if(filter.getReceiver() != null) {
            query.where("receiverId", filter.getReceiver().getId());
        }
        if(filter.getCurrency() != null) {
            query.where("sourceId", filter.getAccount().getCredit(filter.getCurrency()).getId());
        }
        if(filter.getReason() != null) {
            query.where("reason", filter.getReason());
        }
        if(filter.getCause() != null) {
            query.where("cause", filter.getCause());
        }
        int page = filter.getPage() > 0 ? filter.getPage() : 1;
        query.page(page, 10);

        List<AccountTransaction> transactions = new ArrayList<>();
        QueryResult result = query.execute();
        for (QueryResultEntry entry : result) {
            AccountCredit credit = DKCoins.getInstance().getAccountManager().getAccountCredit(entry.getInt("sourceId"));
            transactions.add(new DefaultAccountTransaction(entry.getInt("id"), credit,
                    DKCoins.getInstance().getAccountManager().getAccountMember(entry.getInt("senderId")),
                    DKCoins.getInstance().getAccountManager().getAccountCredit(entry.getInt("receiverId")),
                    entry.getDouble("amount"),
                    entry.getDouble("exchangeRate"),
                    entry.getString("reason"),
                    entry.getString("cause"),
                    entry.getLong("time"),
                    new ArrayList<>()));
        }
        return transactions;
    }

    private DatabaseCollection createAccountTypeDatabaseCollection() {
        return this.database.createCollection("dkcoins_account_type")
                .field("id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("name", DataType.STRING, FieldOption.NOT_NULL, FieldOption.UNIQUE)
                .field("symbol", DataType.STRING, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createCurrencyDatabaseCollection() {
        return this.database.createCollection("dkcoins_currency")
                .field("id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("name", DataType.STRING, FieldOption.NOT_NULL, FieldOption.UNIQUE)
                .field("symbol", DataType.STRING, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createCurrencyExchangeRateDatabaseCollection() {
        return this.database.createCollection("dkcoins_currency_exchange_rate")
                .field("id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("currencyId", DataType.INTEGER, ForeignKey.of(this.currency, "id", ForeignKey.Option.CASCADE, null))
                .field("targetCurrencyId", DataType.INTEGER, ForeignKey.of(this.currency, "id", ForeignKey.Option.CASCADE, null))
                .field("exchangeAmount", DataType.DOUBLE)
                .create();
    }

    private DatabaseCollection createAccountDatabaseCollection() {
        return this.database.createCollection("dkcoins_account")
                .field("id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("name", DataType.STRING, FieldOption.NOT_NULL)
                .field("typeId", DataType.INTEGER, ForeignKey.of(this.accountType, "id", ForeignKey.Option.CASCADE, null), FieldOption.NOT_NULL)
                .field("disabled", DataType.BOOLEAN, FieldOption.NOT_NULL)
                .field("parentId", DataType.INTEGER, ForeignKey.of(this.database.getName(),"dkcoins_account", "id", ForeignKey.Option.CASCADE, null))
                .field("master", DataType.BOOLEAN, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createAccountCreditDatabaseCollection() {
        return this.database.createCollection("dkcoins_account_credit")
                .field("id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("accountId", DataType.INTEGER, ForeignKey.of(this.account, "id", ForeignKey.Option.CASCADE, null), FieldOption.NOT_NULL)
                .field("currencyId", DataType.INTEGER, ForeignKey.of(this.currency, "id", ForeignKey.Option.CASCADE, null), FieldOption.NOT_NULL)
                .field("amount", DataType.DOUBLE, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createAccountMemberDatabaseCollection() {
        return this.database.createCollection("dkcoins_account_member")
                .field("id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("accountId", DataType.INTEGER, ForeignKey.of(this.account, "id", ForeignKey.Option.CASCADE, null), FieldOption.NOT_NULL)
                .field("userId", DataType.UUID, FieldOption.NOT_NULL)
                .field("roleId", DataType.INTEGER, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createAccountTransactionDatabaseCollection() {
        return this.database.createCollection("dkcoins_account_transaction")
                .field("id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("sourceId", DataType.INTEGER, ForeignKey.of(this.accountCredit, "id", ForeignKey.Option.CASCADE, null), FieldOption.NOT_NULL)
                .field("senderId", DataType.INTEGER)
                .field("receiverId",  DataType.INTEGER, FieldOption.NOT_NULL)
                .field("amount", DataType.DOUBLE, FieldOption.NOT_NULL)
                .field("exchangeRate", DataType.DOUBLE, FieldOption.NOT_NULL)
                .field("reason", DataType.STRING)
                .field("cause", DataType.STRING, FieldOption.NOT_NULL)
                .field("time", DataType.LONG, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createAccountTransactionPropertyCollection() {
        return this.database.createCollection("dkcoins_account_transaction_property")
                .field("transactionId", DataType.INTEGER,
                        ForeignKey.of(this.accountTransaction, "id", ForeignKey.Option.CASCADE, null),
                        FieldOption.NOT_NULL, FieldOption.INDEX)
                .field("key", DataType.STRING, FieldOption.NOT_NULL)
                .field("value", DataType.LONG_TEXT)
                .create();
    }

    private DatabaseCollection createAccountLimitationsDatabaseCollection() {
        return this.database.createCollection("dkcoins_account_limitations")
                .field("id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("accountId", DataType.INTEGER, ForeignKey.of(this.account, "id", ForeignKey.Option.CASCADE, null), FieldOption.NOT_NULL)
                .field("memberId", DataType.INTEGER, ForeignKey.of(this.accountMember, "id", ForeignKey.Option.CASCADE, null))
                .field("memberRoleId", DataType.INTEGER)
                .field("comparativeCurrencyId", DataType.INTEGER, ForeignKey.of(this.currency, "id", ForeignKey.Option.CASCADE, null))
                .field("amount", DataType.DOUBLE, FieldOption.NOT_NULL)
                .field("interval", DataType.LONG, FieldOption.NOT_NULL)
                .create();
    }
}
