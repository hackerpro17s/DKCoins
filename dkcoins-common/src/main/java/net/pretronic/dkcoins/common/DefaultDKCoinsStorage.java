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
import net.pretronic.databasequery.api.query.SearchOrder;
import net.pretronic.databasequery.api.query.function.QueryFunction;
import net.pretronic.databasequery.api.query.result.QueryResult;
import net.pretronic.databasequery.api.query.result.QueryResultEntry;
import net.pretronic.databasequery.api.query.type.FindQuery;
import net.pretronic.databasequery.api.query.type.InsertQuery;
import net.pretronic.databasequery.api.query.type.join.JoinType;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.DKCoinsStorage;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.MasterBankAccount;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitation;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitationCalculationType;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitationInterval;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.account.transaction.TransactionFilter;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.common.account.*;
import net.pretronic.dkcoins.common.account.transaction.DefaultAccountTransaction;
import net.pretronic.dkcoins.common.account.transaction.DefaultAccountTransactionProperty;
import net.pretronic.dkcoins.common.account.transaction.DefaultTransactionFilter;
import net.pretronic.dkcoins.common.currency.DefaultCurrency;
import net.pretronic.dkcoins.common.currency.DefaultCurrencyExchangeRate;
import net.pretronic.libraries.utility.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
        QueryResultEntry result = this.accountType.find().get("Name").get( "Symbol").where("Id", id).execute().first();
        return new DefaultAccountType(id, result.getString("Name"), result.getString("Symbol"));
    }

    @Override
    public AccountType searchAccountType(Object identifier) {
        QueryResultEntry result = this.accountType.find()
                .or(query -> {
                    query.where("Name", identifier).where("Symbol", identifier);
                    if(identifier instanceof Integer) query.where("Id", identifier);
                })
                .execute().firstOrNull();
        if(result == null) return null;
        return new DefaultAccountType(result.getInt("Id"), result.getString("Name"), result.getString("Symbol"));
    }

    @Override
    public AccountType createAccountType(String name, String symbol) {
        int id = this.accountType.insert().set("Name", name).set("Symbol", symbol).executeAndGetGeneratedKeyAsInt("Id");
        return new DefaultAccountType(id, name, symbol);
    }

    @Override
    public void updateAccountTypeName(int id, String name) {
        this.accountType.update().set("Name", name).where("Id", id).execute();
    }

    @Override
    public void updateAccountTypeSymbol(int id, String symbol) {
        this.accountType.update().set("Symbol", symbol).where("Id", id).execute();
    }

    @Override
    public void deleteAccountType(int id) {
        this.accountType.delete().where("Id", id).execute();
    }

    @Override
    public Collection<Integer> getAccountIds(UUID userId) {
        Collection<Integer> accountIds = new ArrayList<>();
        this.account.find().get("dkcoins_account.Id")
                .join(this.accountMember).on("Id", this.accountMember, "AccountId")
                .where("UserId", userId).execute().loadIn(accountIds, entry -> entry.getInt("Id"));
        return accountIds;
    }

    @Override
    public BankAccount getAccount(int id) {
        return getAccount(this.account.find().where("Id", id).execute().firstOrNull());
    }

    @Override
    public BankAccount getAccount(String name, AccountType type) {
        return getAccount(this.account.find().where("Name", name).where("TypeId", type.getId()).execute().firstOrNull());
    }

    @Override
    public BankAccount searchAccount(Object identifier) {
        return getAccount(this.account.find().or(query -> {
            if(identifier instanceof Integer) query.where("Id", identifier);
            query.where("Name", identifier);
        }).execute().firstOrNull());
    }

    @Override
    public BankAccount getAccountByCredit(int creditId) {
        return getAccount(this.account.find().join(this.accountCredit).
                on("Id", this.accountCredit, "AccountId")
                .where("Id", creditId)
                .execute().firstOrNull());
    }

    @Override
    public MasterBankAccount getMasterAccount(int id) {
        return (MasterBankAccount) getAccount(this.account.find().where("Id", id).where("Master", true).execute().firstOrNull());
    }

    @Override
    public BankAccount getSubAccount(int masterAccountId, int id) {
        return getAccount(this.account.find().where("Id", id).where("ParentId", masterAccountId).execute().firstOrNull());
    }

    @Override
    public MasterBankAccount getSubMasterAccount(int masterAccountId, int id) {
        return (MasterBankAccount) getAccount(this.account.find().where("Id", id).where("Master", true)
                .where("ParentId", masterAccountId).execute().firstOrNull());
    }

    private BankAccount getAccount(QueryResultEntry resultEntry) {
        if(resultEntry == null) return null;
        int id = resultEntry.getInt("Id");
        boolean master = resultEntry.getBoolean("Master");

        DefaultBankAccount account;
        if(master) {
            account = new DefaultMasterBankAccount(id, resultEntry.getString("Name"),
                    DKCoins.getInstance().getAccountManager().getAccountType(resultEntry.getInt("TypeId")),
                    resultEntry.getBoolean("Disabled"),
                    DKCoins.getInstance().getAccountManager().getMasterAccount(resultEntry.getInt("ParentId")));
        } else {
            account = new DefaultBankAccount(id, resultEntry.getString("Name"),
                    DKCoins.getInstance().getAccountManager().getAccountType(resultEntry.getInt("TypeId")),
                    resultEntry.getBoolean("Disabled"),
                    DKCoins.getInstance().getAccountManager().getMasterAccount(resultEntry.getInt("ParentId")));
        }

        for (QueryResultEntry entry : this.accountLimitation.find().where("AccountId", id).whereIsNull("MemberId").execute()) {
            int memberRoleId = entry.getInt("MemberRoleId");
            account.addLoadedLimitation(new DefaultAccountLimitation(entry.getInt("Id"),
                    account,
                    null,
                    AccountMemberRole.byIdOrNull(memberRoleId),
                    DKCoins.getInstance().getCurrencyManager().getCurrency(entry.getInt("CurrencyId")),
                    AccountLimitationCalculationType.valueOf(entry.getString("CalculationType")),
                    entry.getDouble("Amount"),
                    AccountLimitationInterval.valueOf(entry.getString("Interval"))));
        }
        for(QueryResultEntry entry : this.accountCredit.find().where("AccountId", id).execute()) {
            account.addLoadedAccountCredit(new DefaultAccountCredit(entry.getInt("Id"), account,
                    DKCoins.getInstance().getCurrencyManager().getCurrency(entry.getInt("CurrencyId")),
                    entry.getDouble("Amount")));
        }
        for (QueryResultEntry entry : this.accountMember.find().where("AccountId", id).execute()) {
            DefaultAccountMember member = new DefaultAccountMember(entry.getInt("Id"), account,
                    DKCoins.getInstance().getUserManager().getUser(entry.getUniqueId("UserId")),
                    AccountMemberRole.byId(entry.getInt("RoleId")), entry.getBoolean("ReceiveNotifications"));
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
        int id = this.account.insert().set("Name", name).set("TypeId", type.getId()).set("Disabled", disabled)
                .set("ParentId", parent == null ? null : parent.getId()).set("Master", master).executeAndGetGeneratedKeyAsInt("Id");
        DefaultBankAccount account;
        if(master) {
            account = new DefaultMasterBankAccount(id, name, type, disabled, parent);
        } else {
            account = new DefaultBankAccount(id, name, type,
                    disabled, parent);
        }
        account.addMember(user, null, AccountMemberRole.OWNER, true);
        return account;
    }

    @Override
    public void updateAccountName(int id, String name) {
        this.account.update().set("Name", name).where("Id", id).execute();
    }

    @Override
    public void updateAccountTypeId(int id, int typeId) {
        this.account.update().set("TypeId", typeId).where("Id", id).execute();
    }

    @Override
    public void updateAccountDisabled(int id, boolean disabled) {
        this.account.update().set("Disabled", disabled).where("Id", id).execute();
    }

    @Override
    public void updateAccountParentId(int id, int parentId) {
        this.account.update().set("ParentId", parentId).where("Id", id).execute();
    }

    @Override
    public void deleteAccount(int id) {
        this.account.delete().where("Id", id).execute();
    }

    @Override
    public List<Integer> getTopAccountCreditIds(Currency currency, AccountType[] excludedAccountTypes, int entriesPerPage, int page) {
        Validate.notNull(currency);
        List<Integer> accountCreditIds = new ArrayList<>();
        FindQuery query = this.accountCredit.find().get("dkcoins_account_credit.Id")
                .join(this.account).on("AccountId", this.account, "Id")
                .where("CurrencyId", currency.getId())
                .orderBy("Amount", SearchOrder.DESC);
        if(excludedAccountTypes != null) {
            for (AccountType type : excludedAccountTypes) {
                query.whereNot("TypeId", type.getId());
            }
        }
        query.page(page, entriesPerPage);
        query.execute().loadIn(accountCreditIds, entry -> entry.getInt("Id"));
        return accountCreditIds;
    }

    @Override
    public int getAccountIdByRank(Currency currency, int rank) {
        return this.accountCredit.find().get("AccountId")
                .where("CurrencyId", currency.getId())
                .index(rank, rank)
                .execute().firstOrNull()
                .getInt("AccountId");
    }

    @Override
    public int getTopAccountPos(int creditId) {
        QueryResult result = this.database.getRowNumberInnerQueryCollection(this.accountCredit, "Position",
                QueryFunction.rowNumberFunction("Amount",SearchOrder.DESC))
                .find()
                .get("Position")
                .where("Id", creditId)
                .execute();
        result.getProperties().forEach((s, o) -> System.out.println(s+":"+o));
        QueryResultEntry resultEntry = result.firstOrNull();
        if(resultEntry == null) return -1;
        return resultEntry.getInt("Position");
    }

    @Override
    public double getAccountCreditAmount(int id) {
        return this.accountCredit.find()
                .get("Amount")
                .where("Id", id)
                .execute().firstOrNull()
                .getDouble("Amount");
    }

    @Override
    public int getAccountCreditAccountId(int id) {
        QueryResultEntry entry = this.accountCredit.find().where("Id", id).execute().firstOrNull();
        if(entry == null) return -1;
        return entry.getInt("AccountId");
    }

    @Override
    public AccountCredit addAccountCredit(BankAccount account, Currency currency, double amount) {
        int id = this.accountCredit.insert().set("AccountId", account.getId()).set("CurrencyId", currency.getId()).set("Amount", amount)
                .executeAndGetGeneratedKeyAsInt("Id");
        return new DefaultAccountCredit(id, account, currency, amount);
    }

    @Override
    public void setAccountCreditAmount(int id, double amount) {
        this.accountCredit.update().set("Amount", amount).where("Id", id).execute();
    }

    @Override
    public void addAccountCreditAmount(int id, double amount) {
        this.accountCredit.update().add("Amount", amount).where("Id", id).execute();
    }

    @Override
    public void removeAccountCreditAmount(int id, double amount) {
        this.accountCredit.update().subtract("Amount", amount).where("Id", id).execute();
    }

    @Override
    public void deleteAccountCredit(int id) {
        this.accountCredit.delete().where("Id", id).execute();
    }

    @Override
    public int getAccountLimitationAccountId(int id) {
        QueryResultEntry entry = this.accountLimitation.find().where("Id", id).execute().firstOrNull();
        if(entry == null) return -1;
        return entry.getInt("AccountId");
    }

    @Override
    public AccountLimitation addAccountLimitation(BankAccount account, AccountMember accountMember,
                                                  AccountMemberRole memberRole, Currency comparativeCurrency, AccountLimitationCalculationType calculationType,
                                                  double amount, AccountLimitationInterval interval) {

        InsertQuery query = this.accountLimitation.insert()
                .set("AccountId", account.getId())
                .set("ComparativeCurrencyId", comparativeCurrency.getId())
                .set("CalculationType", calculationType)
                .set("Amount", amount)
                .set("Interval", interval);
        if(accountMember != null) query.set("MemberId", accountMember.getId());
        if(memberRole != null) query.set("MemberRoleId", memberRole.getId());
        int id = query.executeAndGetGeneratedKeyAsInt("Id");
        return new DefaultAccountLimitation(id, account, accountMember, memberRole, comparativeCurrency, calculationType, amount, interval);
    }

    @Override
    public void removeAccountLimitation(int id) {
        this.accountLimitation.delete().where("Id", id).execute();
    }

    @Override
    public int getAccountMemberAccountId(int id) {
        QueryResultEntry result = this.accountMember.find().where("Id", id).execute().firstOrNull();
        if(result == null) return -1;
        return result.getInt("AccountId");
    }

    private void loadAccountMemberLimitations(DefaultAccountMember member) {//@Todo
        for (QueryResultEntry entry : this.accountLimitation.find()
                .where("AccountId", member.getAccount().getId())
                .where("MemberId", member.getId())
                .execute()) {
            ((DefaultBankAccount)member.getAccount()).addLoadedLimitation(new DefaultAccountLimitation(entry.getInt("Id"),
                    member.getAccount(),
                    member,
                    null,
                    DKCoins.getInstance().getCurrencyManager().getCurrency(entry.getInt("ComparativeCurrencyId")),
                    AccountLimitationCalculationType.valueOf(entry.getString("CalculationType")),
                    entry.getDouble("Amount"), AccountLimitationInterval.valueOf(entry.getString("Interval"))));
        }
    }

    @Override
    public AccountMember addAccountMember(BankAccount account, DKCoinsUser user, AccountMemberRole role, boolean receiveNotifications) {
        int id = this.accountMember.insert()
                .set("AccountId", account.getId())
                .set("UserId", user.getUniqueId())
                .set("RoleId", role.getId())
                .set("ReceiveNotifications", receiveNotifications)
                .executeAndGetGeneratedKeyAsInt("Id");
        return new DefaultAccountMember(id, account, user, role, receiveNotifications);
    }

    @Override
    public void updateAccountMemberRole(AccountMember member) {
        this.accountMember.update()
                .set("RoleId", member.getRole().getId())
                .where("Id", member.getId())
                .execute();
    }

    @Override
    public void updateAccountMemberReceiveNotifications(AccountMember member) {
        this.accountMember.update().set("ReceiveNotifications", member.receiveNotifications())
                .where("Id", member.getId())
                .execute();
    }

    @Override
    public void removeAccountMember(int id) {
        this.accountMember.delete().where("Id", id).execute();
    }

    @Override
    public List<AccountTransaction> filterAccountTransactions(TransactionFilter filter0) {
        Validate.isTrue(filter0 instanceof DefaultTransactionFilter);

        /*DatabaseDriver driver = accountCredit.getDatabase().getDriver();
        PretronicLogger logger = PretronicLoggerFactory.getLogger();
        logger.setLevel(LogLevel.ALL);
        try {

            Field field = ReflectionUtil.getField(Class.forName("net.pretronic.databasequery.common.driver.AbstractDatabaseDriver"), "logger");
            ReflectionUtil.grantFinalPrivileges(field);
            field.setAccessible(true);
            field.set(driver, logger);
        } catch (ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }*/

        DefaultTransactionFilter filter = (DefaultTransactionFilter) filter0;
        Validate.notNull(filter.getAccount());
        FindQuery query = this.accountTransaction
                .find()
                .get("dkcoins_account_transaction.Id", "SourceId", "SenderId","ReceiverId", "dkcoins_account_transaction.Amount",
                        "ExchangeRate", "Reason", "Cause", "Time", "Key", "Value")
                .join(this.accountCredit, JoinType.INNER).on("SourceId", this.accountCredit, "Id")
                .join(this.accountTransactionProperty, JoinType.LEFT).on("Id", this.accountTransactionProperty, "TransactionId");
        //.where("accountId", filter.getAccount().getId())

        if(filter.getWorld() != null) {
            query.and(subQuery ->
                    subQuery.where("dkcoins_account_transaction_property.Key", "world")
                            .where("dkcoins_account_transaction_property.Value", filter.getWorld()));
        }
        if(filter.getServer() != null) {
            query.and(subQuery ->
                    subQuery.where("dkcoins_account_transaction_property.key", "server")
                            .where("dkcoins_account_transaction_property.Value", filter.getServer()));
        }
        if(filter.getTime() != null) {
            query.where("Time", filter.getTime());
        }
        if(filter.getReceiver() != null) {
            query.where("ReceiverId", filter.getReceiver().getId());
        }
        if(filter.getCurrency() != null) {
            query.where("SourceId", filter.getAccount().getCredit(filter.getCurrency()).getId());
        }
        if(filter.getReason() != null) {
            query.where("Reason", filter.getReason());
        }
        if(filter.getCause() != null) {
            query.where("Cause", filter.getCause());
        }
        int page = filter.getPage() > 0 ? filter.getPage() : 1;
        //query.page(page, 5);

        List<AccountTransaction> transactions = new ArrayList<>();

        DefaultAccountTransaction last = null;
        for (QueryResultEntry entry : query.execute()) {
            int id = entry.getInt("Id");
            DefaultAccountTransaction transaction;

            if(last != null && last.getId() == id) {
                transaction = last;
            } else {
                transaction = new DefaultAccountTransaction(id,
                        DKCoins.getInstance().getAccountManager().getAccountCredit(entry.getInt("SourceId")),
                        DKCoins.getInstance().getAccountManager().getAccountMember(entry.getInt("SenderId")),
                        DKCoins.getInstance().getAccountManager().getAccountCredit(entry.getInt("ReceiverId")),
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

    @Override
    public AccountTransaction addAccountTransaction(AccountCredit source, AccountMember sender, AccountCredit receiver,
                                                    double amount, double exchangeRate, String reason, String cause,
                                                    long time, Collection<AccountTransactionProperty> properties) {
        int id = this.accountTransaction.insert()
                .set("SourceId", source.getId())
                .set("SourceName", source.getName())
                .set("SenderId", sender == null ? null : sender.getId())
                .set("SenderName", sender == null ? "API" : sender.getName())
                .set("ReceiverId", receiver.getId())
                .set("ReceiverName", receiver.getName())
                .set("CurrencyName", source.getCurrency().getName())
                .set("Amount", amount)
                .set("ExchangeRate", exchangeRate)
                .set("Reason", reason)
                .set("Cause", cause)
                .set("Time", time)
                .executeAndGetGeneratedKeyAsInt("Id");

        if(!properties.isEmpty()) {
            InsertQuery propertyInsertQuery = this.accountTransactionProperty.insert();
            for (AccountTransactionProperty property : properties) {
                propertyInsertQuery.set("Key", property.getKey()).set("Value", property.asObject());
            }
            propertyInsertQuery.execute();
        }
        return new DefaultAccountTransaction(id, source, sender, receiver, amount, exchangeRate, reason, cause, time, properties);
    }

    @Override
    public Collection<Currency> getCurrencies() {
        Collection<Currency> currencies = new ArrayList<>();
        this.currency.find().execute().loadIn(currencies, entry -> new DefaultCurrency(
                entry.getInt("Id"),
                entry.getString("Name"),
                entry.getString("Symbol")));
        return currencies;
    }

    @Override
    public Currency getCurrency(int id) {
        return getCurrency(this.currency.find()
                .where("Id", id)
                .execute().firstOrNull());
    }

    @Override
    public Currency getCurrency(String name) {
        return getCurrency(this.currency.find()
                .where("Name", name)
                .execute().firstOrNull());
    }

    @Override
    public Currency searchCurrency(Object identifier) {
        QueryResultEntry result = this.currency.find()
                .or(query -> {
                    if(identifier instanceof Integer) query.where("Id", identifier);
                    query.where("Name", identifier).where("Symbol", identifier);
                })
                .execute().firstOrNull();
        return getCurrency(result);
    }

    private Currency getCurrency(QueryResultEntry result) {
        if(result == null) return null;
        return new DefaultCurrency(result.getInt("Id"),
                result.getString("Name"),
                result.getString("Symbol"));
    }

    @Override
    public Currency createCurrency(String name, String symbol) {
        int id = this.currency.insert()
                .set("Name", name)
                .set("Symbol", symbol)
                .executeAndGetGeneratedKeyAsInt("Id");
        return new DefaultCurrency(id, name, symbol);
    }

    @Override
    public void updateCurrencyName(int id, String name) {
        this.currency.update().set("Name", name)
                .where("Id", id)
                .execute();
    }

    @Override
    public void updateCurrencySymbol(int id, String symbol) {
        this.currency.update()
                .set("Symbol", symbol)
                .where("Id", id)
                .execute();
    }

    @Override
    public void deleteCurrency(int id) {
        this.currency.delete().where("Id", id).execute();
    }

    @Override
    public CurrencyExchangeRate getCurrencyExchangeRate(int id) {
        QueryResultEntry entry = this.currencyExchangeRate.find()
                .where("Id", id).execute().firstOrNull();
        if(entry == null) return null;
        return new DefaultCurrencyExchangeRate(id,
                DKCoins.getInstance().getCurrencyManager().getCurrency("CurrencyId"),
                DKCoins.getInstance().getCurrencyManager().getCurrency("TargetCurrencyId"),
                entry.getDouble("ExchangeAmount"));
    }

    @Override
    public int getCurrencyExchangeRateCurrencyId(int id) {
        QueryResultEntry result = this.currencyExchangeRate.find().where("Id", id).execute().firstOrNull();
        if(result == null) return -1;
        return result.getInt("CurrencyId");
    }

    @Override
    public CurrencyExchangeRate getCurrencyExchangeRate(int currencyId, int targetCurrencyId) {
        QueryResultEntry entry = this.currencyExchangeRate.find()
                .where("CurrencyId", currencyId)
                .where("TargetCurrencyId", targetCurrencyId)
                .execute().firstOrNull();
        if(entry == null) return null;
        return new DefaultCurrencyExchangeRate(entry.getInt("Id"),
                DKCoins.getInstance().getCurrencyManager().getCurrency(currencyId),
                DKCoins.getInstance().getCurrencyManager().getCurrency(targetCurrencyId),
                entry.getDouble("ExchangeAmount"));
    }

    @Override
    public CurrencyExchangeRate createCurrencyExchangeRate(int currencyId, int targetCurrencyId, double exchangeAmount) {
        int id = this.currencyExchangeRate.insert()
                .set("CurrencyId", currencyId)
                .set("TargetCurrencyId", targetCurrencyId)
                .set("ExchangeAmount", exchangeAmount)
                .executeAndGetGeneratedKeyAsInt("Id");
        return new DefaultCurrencyExchangeRate(id, DKCoins.getInstance().getCurrencyManager().getCurrency(currencyId),
                DKCoins.getInstance().getCurrencyManager().getCurrency(targetCurrencyId), exchangeAmount);
    }

    @Override
    public void updateCurrencyExchangeAmount(int selectedId, int targetId, double exchangeAmount) {
        this.currencyExchangeRate.update()
                .set("ExchangeAmount", exchangeAmount)
                .where("CurrencyId", selectedId)
                .where("TargetCurrencyId", targetId)
                .execute();
    }

    @Override
    public void deleteCurrencyExchangeRate(int id) {
        this.currencyExchangeRate.delete().where("Id", id).execute();
    }

    @Override
    public DKCoinsUser getUser(UUID uniqueId) {
        return DKCoins.getInstance().getUserManager().constructNewUser(uniqueId);
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
                .field("Name", DataType.STRING, FieldOption.NOT_NULL)
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
                .field("SourceId", DataType.INTEGER, ForeignKey.of(this.accountCredit, "Id", ForeignKey.Option.DEFAULT, null), FieldOption.NOT_NULL)
                .field("SourceName", DataType.STRING, FieldOption.NOT_NULL)
                .field("SenderId", DataType.INTEGER)
                .field("SenderName", DataType.STRING, FieldOption.NOT_NULL)
                .field("ReceiverId",  DataType.INTEGER, FieldOption.NOT_NULL)
                .field("ReceiverName", DataType.STRING, FieldOption.NOT_NULL)
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
}
