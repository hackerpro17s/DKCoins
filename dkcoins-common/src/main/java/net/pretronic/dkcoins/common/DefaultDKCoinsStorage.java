/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 18.11.19, 21:22
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.common;

import net.pretronic.databasequery.api.Database;
import net.pretronic.databasequery.api.collection.DatabaseCollection;
import net.pretronic.databasequery.api.collection.field.FieldOption;
import net.pretronic.databasequery.api.datatype.DataType;
import net.pretronic.databasequery.api.query.ForeignKey;

public class DefaultDKCoinsStorage {

    private final Database database;
    private final DatabaseCollection accountType;
    private final DatabaseCollection account;
    //private final DatabaseCollection accountRoles;
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
        //this.accountRoles = createAccountRolesDatabaseCollection();
        this.currency = createCurrencyDatabaseCollection();
        this.currencyExchangeRate = createCurrencyExchangeRateDatabaseCollection();
        this.accountCredit = createAccountCreditDatabaseCollection();
        this.accountMember = createAccountMemberDatabaseCollection();
        this.accountTransaction = createAccountTransactionDatabaseCollection();
        this.accountTransactionProperty = createAccountTransactionPropertyCollection();
        this.accountLimitation = createAccountLimitationsDatabaseCollection();
    }

    public Database getDatabase() {
        return database;
    }

    public DatabaseCollection getAccountType() {
        return accountType;
    }

    public DatabaseCollection getAccount() {
        return account;
    }

    /*public DatabaseCollection getAccountRoles() {
        return accountRoles;
    }*/

    public DatabaseCollection getAccountCredit() {
        return accountCredit;
    }

    public DatabaseCollection getAccountMember() {
        return accountMember;
    }

    public DatabaseCollection getAccountTransaction() {
        return accountTransaction;
    }

    public DatabaseCollection getAccountTransactionProperty() {
        return accountTransactionProperty;
    }

    public DatabaseCollection getAccountLimitation() {
        return accountLimitation;
    }

    public DatabaseCollection getCurrency() {
        return currency;
    }

    public DatabaseCollection getCurrencyExchangeRate() {
        return currencyExchangeRate;
    }

    private DatabaseCollection createAccountTypeDatabaseCollection() {
        return this.database.createCollection("dkcoins_account_type")
                .field("Id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("Name", DataType.STRING, FieldOption.NOT_NULL, FieldOption.UNIQUE)
                .field("Symbol", DataType.STRING, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createCurrencyDatabaseCollection() {
        return this.database.createCollection("dkcoins_currency")
                .field("Id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("Name", DataType.STRING, FieldOption.NOT_NULL, FieldOption.UNIQUE)
                .field("Symbol", DataType.STRING, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createCurrencyExchangeRateDatabaseCollection() {
        return this.database.createCollection("dkcoins_currency_exchangerate")
                .field("Id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("CurrencyId", DataType.INTEGER, ForeignKey.of(this.currency, "Id", ForeignKey.Option.CASCADE, null))
                .field("TargetCurrencyId", DataType.INTEGER, ForeignKey.of(this.currency, "Id", ForeignKey.Option.CASCADE, null))
                .field("ExchangeAmount", DataType.DOUBLE)
                .create();
    }

    private DatabaseCollection createAccountDatabaseCollection() {
        return this.database.createCollection("dkcoins_account")
                .field("Id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("Name", DataType.STRING, FieldOption.NOT_NULL,FieldOption.UNIQUE)
                .field("TypeId", DataType.INTEGER, ForeignKey.of(this.accountType, "Id", ForeignKey.Option.CASCADE, null), FieldOption.NOT_NULL)
                .field("Disabled", DataType.BOOLEAN, FieldOption.NOT_NULL)
                .field("ParentId", DataType.INTEGER, ForeignKey.of(this.database.getName(),"dkcoins_account", "Id", ForeignKey.Option.CASCADE, null))
                .field("Master", DataType.BOOLEAN, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createAccountCreditDatabaseCollection() {
        return this.database.createCollection("dkcoins_account_credit")
                .field("Id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("AccountId", DataType.INTEGER, ForeignKey.of(this.account, "Id", ForeignKey.Option.CASCADE, null), FieldOption.NOT_NULL)
                .field("CurrencyId", DataType.INTEGER, ForeignKey.of(this.currency, "Id", ForeignKey.Option.CASCADE, null), FieldOption.NOT_NULL)
                .field("Amount", DataType.DOUBLE, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createAccountMemberDatabaseCollection() {
        return this.database.createCollection("dkcoins_account_member")
                .field("Id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("AccountId", DataType.INTEGER, ForeignKey.of(this.account, "Id", ForeignKey.Option.CASCADE, null), FieldOption.NOT_NULL)
                .field("UserId", DataType.UUID, FieldOption.NOT_NULL)
                .field("RoleId", DataType.INTEGER, FieldOption.NOT_NULL)
                .field("ReceiveNotifications", DataType.BOOLEAN, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createAccountTransactionDatabaseCollection() {
        return this.database.createCollection("dkcoins_account_transaction")
                .field("Id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("SenderAccountId", DataType.INTEGER)
                .field("SenderAccountName", DataType.STRING, FieldOption.NOT_NULL)
                .field("SenderId", DataType.INTEGER)
                .field("SenderName", DataType.STRING, FieldOption.NOT_NULL)
                .field("DestinationId",  DataType.INTEGER, FieldOption.NOT_NULL)
                .field("DestinationName", DataType.STRING, FieldOption.NOT_NULL)
                .field("CurrencyId", DataType.INTEGER, FieldOption.NOT_NULL)
                .field("CurrencyName", DataType.STRING, FieldOption.NOT_NULL)
                .field("Amount", DataType.DOUBLE, FieldOption.NOT_NULL)
                .field("ExchangeRate", DataType.DOUBLE, FieldOption.NOT_NULL)
                .field("Reason", DataType.STRING)
                .field("Cause", DataType.STRING, FieldOption.NOT_NULL)
                .field("Time", DataType.LONG, FieldOption.NOT_NULL)
                .create();
    }

    private DatabaseCollection createAccountTransactionPropertyCollection() {
        return this.database.createCollection("dkcoins_account_transaction_property")
                .field("TransactionId", DataType.INTEGER,
                        ForeignKey.of(this.accountTransaction, "Id", ForeignKey.Option.CASCADE, null),
                        FieldOption.NOT_NULL, FieldOption.INDEX)
                .field("Key", DataType.STRING, FieldOption.NOT_NULL)
                .field("Value", DataType.LONG_TEXT)
                .create();
    }

    private DatabaseCollection createAccountLimitationsDatabaseCollection() {
        return this.database.createCollection("dkcoins_account_limitations")
                .field("Id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("AccountId", DataType.INTEGER, ForeignKey.of(this.account, "Id", ForeignKey.Option.CASCADE, null), FieldOption.NOT_NULL)
                .field("MemberId", DataType.INTEGER, ForeignKey.of(this.accountMember, "Id", ForeignKey.Option.CASCADE, null))
                .field("MemberRoleId", DataType.INTEGER)
                .field("ComparativeCurrencyId", DataType.INTEGER, ForeignKey.of(this.currency, "Id", ForeignKey.Option.CASCADE, null))
                .field("CalculationType", DataType.STRING, FieldOption.NOT_NULL)
                .field("Amount", DataType.DOUBLE, FieldOption.NOT_NULL)
                .field("Interval", DataType.STRING, FieldOption.NOT_NULL)
                .create();
    }

    /*private DatabaseCollection createAccountRolesDatabaseCollection() {
        return this.database.createCollection("dkcoins_account_role")
                .field("Id", DataType.INTEGER, FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT)
                .field("Name", DataType.STRING, FieldOption.NOT_NULL)
                .field("ParentRoleId", DataType.INTEGER, ForeignKey.of(this.database.getName(), "dkcoins_account_role", "Id", ForeignKey.Option.CASCADE))
                .field("AccessRights", DataType.LONG_TEXT, FieldOption.NOT_NULL)
                .create();
    }*/
}
