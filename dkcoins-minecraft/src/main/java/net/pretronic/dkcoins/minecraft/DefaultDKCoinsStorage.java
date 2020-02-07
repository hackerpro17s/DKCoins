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
import net.prematic.databasequery.api.query.type.InsertQuery;
import net.prematic.libraries.utility.Validate;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.DKCoinsStorage;
import net.pretronic.dkcoins.api.account.*;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.account.*;
import net.pretronic.dkcoins.minecraft.account.DefaultBankAccount;
import net.pretronic.dkcoins.minecraft.currency.DefaultCurrency;
import net.pretronic.dkcoins.minecraft.currency.DefaultCurrencyExchangeRate;
import net.pretronic.dkcoins.minecraft.user.DefaultDKCoinsUser;

import java.util.ArrayList;
import java.util.Collection;

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
        this.accountCredit = createAccountCreditDatabaseCollection();
        this.accountMember = createAccountMemberDatabaseCollection();
        this.accountTransaction = createAccountTransactionDatabaseCollection();
        this.accountTransactionProperty = createAccountTransactionPropertyCollection();
        this.accountLimitation = createAccountLimitationsDatabaseCollection();
        this.currency = createCurrencyDatabaseCollection();
        this.currencyExchangeRate = createCurrencyExchangeRateDatabaseCollection();
    }

    @Override
    public AccountType getAccountType(int id) {
        QueryResultEntry result = this.accountType.find().get("name", "symbol").where("id", id).execute().first();
        return new DefaultAccountType(id, result.getString("name"), result.getString("symbol"));
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
    public BankAccount getAccount(int id) {
        return getAccount(this.account.find().where("id", id).execute());
    }

    @Override
    public BankAccount getAccountByCredit(int creditId) {
        return getAccount(this.account.find().join(this.accountCredit).
                on("id", this.accountCredit, "accountId")
                .where("id", creditId)
                .execute());
    }

    @Override
    public MasterBankAccount getMasterAccount(int id) {
        return (MasterBankAccount) getAccount(this.account.find().where("id", id).where("master", true).execute());
    }

    @Override
    public BankAccount getSubAccount(int masterAccountId, int id) {
        return getAccount(this.account.find().where("id", id).where("parentId", masterAccountId).execute());
    }

    @Override
    public MasterBankAccount getSubMasterAccount(int masterAccountId, int id) {
        return (MasterBankAccount) getAccount(this.account.find().where("id", id).where("master", true)
                .where("parentId", masterAccountId).execute());
    }

    private BankAccount getAccount(QueryResult result) {
        if(result == null) return null;
        QueryResultEntry resultEntry = result.first();
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
        return account;
    }

    @Override
    public BankAccount createAccount(String name, int typeId, boolean disabled, int parentId, int creatorId) {
        return createAccount(name, typeId, disabled, parentId, creatorId, false);
    }

    @Override
    public MasterBankAccount createMasterAccount(String name, int typeId, boolean disabled, int parentId, int creatorId) {
        return (MasterBankAccount) createAccount(name, typeId, disabled, parentId, creatorId, true);
    }

    private BankAccount createAccount(String name, int typeId, boolean disabled, int parentId, int creatorId, boolean master) {
        int id = this.account.insert().set("name", name).set("typeId", typeId).set("disabled", disabled)
                .set("parentId", parentId).set("master", master).executeAndGetGeneratedKeyAsInt("id");
        DefaultBankAccount account;
        if(master) {
            account = new DefaultMasterBankAccount(id, name, DKCoins.getInstance().getAccountManager().getAccountType(typeId),
                    disabled, DKCoins.getInstance().getAccountManager().getMasterAccount(parentId));
        } else {
            account = new DefaultBankAccount(id, name, DKCoins.getInstance().getAccountManager().getAccountType(typeId),
                    disabled, DKCoins.getInstance().getAccountManager().getMasterAccount(parentId));
        }
        DKCoins.getInstance().getAccountManager().addAccountMember(account, DKCoins.getInstance().getUserManager().getUser(creatorId), AccountMemberRole.OWNER);
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
    public AccountCredit addAccountCredit(int accountId, int currencyId, double amount) {
        int id = this.accountCredit.insert().set("accountId", accountId).set("currencyId", currencyId).set("amount", accountId).executeAndGetGeneratedKeyAsInt("id");
        return new DefaultAccountCredit(id, DKCoins.getInstance().getAccountManager().getAccount(accountId),
                DKCoins.getInstance().getCurrencyManager().getCurrency(currencyId), amount);
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
    public AccountLimitation addAccountLimitation(int accountId, int memberId, int memberRoleId,
                                                  int comparativeCurrencyId, double amount, long interval) {
        InsertQuery query = this.accountLimitation.insert().set("accountId", accountId).set("comparativeCurrencyId", comparativeCurrencyId)
                .set("amount", amount).set("interval", interval);
        if(memberId > 0) query.set("memberId", memberId);
        if(memberRoleId > 0) query.set("memberRoleId", memberRoleId);
        int id = query.executeAndGetGeneratedKeyAsInt("id");
        return new DefaultAccountLimitation(id, DKCoins.getInstance().getAccountManager().getAccount(accountId),
                DKCoins.getInstance().getAccountManager().getAccountMember(memberId), AccountMemberRole.byIdOrNull(memberRoleId),
                DKCoins.getInstance().getCurrencyManager().getCurrency(comparativeCurrencyId), amount, interval);
    }

    @Override
    public void deleteAccountLimitation(int id) {
        this.accountLimitation.delete().where("id", id).execute();
    }

    @Override
    public AccountMember getAccountMember(int id) {
        return getAccountMember("{"+id+"}", this.accountMember.find().where("id", id).execute().firstOrNull());
    }

    @Override
    public AccountMember getAccountMember(int userId, int accountId) {
        return getAccountMember("{"+userId+","+accountId+"}", this.accountMember.find().where("userId", userId).where("accountId", accountId).execute().firstOrNull());
    }

    private AccountMember getAccountMember(String identifier, QueryResultEntry result) {
        if(result == null) throw new IllegalArgumentException(String.format("AccountMember with identifier %s doesn't exist", identifier));
        int id = result.getInt("id");

        DefaultAccountMember member = new DefaultAccountMember(id, DKCoins.getInstance().getAccountManager()
                .getAccount(result.getInt("accountId")),
                DKCoins.getInstance().getUserManager().getUser(result.getInt("userId")),
                AccountMemberRole.byId(result.getInt("id")));

        for (QueryResultEntry entry : this.accountLimitation.find().where("accountId",
                member.getId()).where("memberId", id).execute()) {
            member.addLoadedLimitation(new DefaultAccountLimitation(entry.getInt("id"), member.getAccount(), member,
                    null,
                    DKCoins.getInstance().getCurrencyManager().getCurrency(entry.getInt("comparativeCurrencyId")),
                    entry.getDouble("amount"), entry.getLong("interval")));
        }
        return member;
    }

    @Override
    public AccountMember addAccountMember(int accountId, int userId, AccountMemberRole role) {
        int id = this.accountMember.insert().set("accountId", accountId).set("userId", userId).set("roleId", role.getId()).executeAndGetGeneratedKeyAsInt("id");
        return new DefaultAccountMember(id, DKCoins.getInstance().getAccountManager().getAccount(accountId),
                DKCoins.getInstance().getUserManager().getUser(userId), role);
    }

    @Override
    public void deleteAccountMember(int id) {
        this.accountMember.delete().where("id", id).execute();
    }

    @Override
    public Collection<AccountTransaction> getAccountTransactions(int senderId, int start, int end) {
        Collection<AccountTransaction> transactions = new ArrayList<>();
        this.accountTransaction.find().where("senderId", senderId).orderBy("id", SearchOrder.DESC).limit(end-start, start);
        return transactions;
    }

    @Override
    public AccountTransaction addAccountTransaction(int sourceId, int senderId, int receiverId, double amount, double exchangeRate,
                                                    String reason, String cause, long time, Collection<AccountTransactionProperty> properties) {
        int id = this.accountTransaction.insert()
                .set("sourceId", sourceId)
                .set("senderId", senderId)
                .set("receiverId", receiverId)
                .set("amount", amount)
                .set("exchangeRate", exchangeRate)
                .set("reason", reason)
                .set("cause", cause)
                .set("time", time)
                .executeAndGetGeneratedKeyAsInt("id");
        InsertQuery propertyInsertQuery = this.accountTransactionProperty.insert();
        for (AccountTransactionProperty property : properties) {
            propertyInsertQuery.set("key", property.getKey()).set("value", property.asObject());
        }
        propertyInsertQuery.execute();
        return new DefaultAccountTransaction(id,
                DKCoins.getInstance().getAccountManager().getAccountCredit(sourceId),
                DKCoins.getInstance().getAccountManager().getAccountMember(senderId),
                DKCoins.getInstance().getAccountManager().getAccountCredit(receiverId),
                amount, exchangeRate, reason, cause, time, properties);
    }

    @Override
    public Currency getCurrency(int id) {
        return getCurrency(id, this.currency.find().where("id", id).execute().firstOrNull());
    }

    @Override
    public Currency getCurrency(String name) {
        return getCurrency(name, this.currency.find().where("name", name).execute().firstOrNull());
    }

    private Currency getCurrency(Object identifier, QueryResultEntry result) {
        if(result == null) throw new IllegalArgumentException(String.format("Currency with identifier %s doesn't exist", identifier));
        return new DefaultCurrency(result.getInt("id"), result.getString("name"), result.getString("symbol"));
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
    public CurrencyExchangeRate addCurrencyExchangeRate(int currencyId, int targetCurrencyId, double exchangeAmount) {
        int id = this.currencyExchangeRate.insert().set("currencyId", currencyId).set("targetCurrencyId", targetCurrencyId)
                .set("exchangeAmount", exchangeAmount).executeAndGetGeneratedKeyAsInt("id");
        return new DefaultCurrencyExchangeRate(id, DKCoins.getInstance().getCurrencyManager().getCurrency(currencyId),
                DKCoins.getInstance().getCurrencyManager().getCurrency(targetCurrencyId), exchangeAmount);
    }

    @Override
    public void deleteCurrencyExchangeRate(int id) {
        this.currencyExchangeRate.delete().where("id", id).execute();
    }

    @Override
    public DKCoinsUser getUser(int id) {
        return new DefaultDKCoinsUser(id);
    }

    private DatabaseCollection createAccountTypeDatabaseCollection() {
        return this.database.createCollection("dkcoins_account_type")
                .field("id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("name", DataType.STRING, FieldOption.NOT_NULL, FieldOption.UNIQUE)
                .field("symbol", DataType.STRING, FieldOption.NOT_NULL, FieldOption.UNIQUE)
                .create();
    }

    private DatabaseCollection createAccountDatabaseCollection() {
        return this.database.createCollection("dkcoins_account")
                .field("id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("name", DataType.STRING, FieldOption.NOT_NULL)
                .field("typeId", DataType.INTEGER, FieldOption.NOT_NULL)
                .field("disabled", DataType.BOOLEAN, FieldOption.NOT_NULL)
                .field("parentId", DataType.INTEGER, FieldOption.NOT_NULL)
                .field("master", DataType.BOOLEAN, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createAccountCreditDatabaseCollection() {
        return this.database.createCollection("dkcoins_account_credit")
                .field("id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("accountId", DataType.INTEGER, FieldOption.NOT_NULL)
                .field("currencyId", DataType.INTEGER, FieldOption.NOT_NULL)
                .field("amount", DataType.DOUBLE, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createAccountMemberDatabaseCollection() {
        return this.database.createCollection("dkcoins_account_member")
                .field("id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("accountId", DataType.INTEGER, FieldOption.NOT_NULL)
                .field("userId", DataType.INTEGER, FieldOption.NOT_NULL)
                .field("roleId", DataType.INTEGER, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createAccountTransactionDatabaseCollection() {
        return this.database.createCollection("dkcoins_account_transaction")
                .field("id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("sourceId", DataType.INTEGER, FieldOption.NOT_NULL)
                .field("senderId", DataType.INTEGER, FieldOption.NOT_NULL)
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
                .field("accountId", DataType.INTEGER, FieldOption.NOT_NULL)
                .field("memberId", DataType.INTEGER)
                .field("memberRoleId", DataType.INTEGER)
                .field("comparativeCurrencyId", DataType.INTEGER)
                .field("amount", DataType.DOUBLE, FieldOption.NOT_NULL)
                .field("interval", DataType.LONG, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createCurrencyDatabaseCollection() {
        return this.database.createCollection("dkcoins_currency")
                .field("id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("name", DataType.STRING, FieldOption.NOT_NULL, FieldOption.UNIQUE)
                .field("symbol", DataType.STRING, FieldOption.NOT_NULL, FieldOption.UNIQUE)
                .create();
    }

    private DatabaseCollection createCurrencyExchangeRateDatabaseCollection() {
        return this.database.createCollection("dkcoins_currency_exchange_rate")
                .field("id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("currencyId", DataType.INTEGER)
                .field("targetCurrencyId", DataType.INTEGER)
                .field("exchangeAmount", DataType.DOUBLE)
                .create();
    }
}
