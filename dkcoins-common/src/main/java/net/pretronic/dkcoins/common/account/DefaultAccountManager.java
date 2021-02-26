/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 01.12.19, 15:19
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.common.account;

import net.pretronic.databasequery.api.query.SearchOrder;
import net.pretronic.databasequery.api.query.result.QueryResultEntry;
import net.pretronic.databasequery.api.query.type.FindQuery;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.*;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitationCalculationType;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitationInterval;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.events.account.DKCoinsAccountCreateEvent;
import net.pretronic.dkcoins.api.events.account.DKCoinsAccountDeleteEvent;
import net.pretronic.dkcoins.api.events.account.DKCoinsAccountPreCreateEvent;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.common.DefaultDKCoins;
import net.pretronic.dkcoins.common.DefaultDKCoinsStorage;
import net.pretronic.dkcoins.common.user.DefaultDKCoinsUser;
import net.pretronic.libraries.caching.CacheQuery;
import net.pretronic.libraries.caching.synchronisation.ArraySynchronizableCache;
import net.pretronic.libraries.caching.synchronisation.SynchronizableCache;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultAccountManager implements AccountManager {

    private final SynchronizableCache<AccountType, Integer> accountTypeCache;
    private final SynchronizableCache<BankAccount, Integer> accountCache;

    public DefaultAccountManager() {
        this.accountTypeCache = new ArraySynchronizableCache<>();
        registerAccountTypeCacheQueries();

        this.accountCache = new ArraySynchronizableCache<>();
        registerAccountCacheQueries();
    }

    @Override
    public AccountType getAccountType(int id) {
        return this.accountTypeCache.get("byId", id);
    }

    @Override
    public AccountType searchAccountType(Object identifier) {
        return this.accountTypeCache.get("search", identifier);
    }

    @Override
    public AccountType createAccountType(String name, String symbol) {
        int id = DefaultDKCoins.getInstance().getStorage().getAccountType().insert()
                .set("Name", name).set("Symbol", symbol)
                .executeAndGetGeneratedKeyAsInt("Id");

        AccountType accountType = new DefaultAccountType(id, name, symbol);
        this.accountTypeCache.insert(accountType);
        this.accountTypeCache.getCaller().createAndIgnore(accountType.getId(), Document.newDocument());

        return accountType;
    }

    @Override
    public Collection<BankAccount> getCachedAccounts() {
        return this.accountCache.getCachedObjects();
    }

    @Override
    public Collection<BankAccount> getAccounts(DKCoinsUser user) {
        if(((DefaultDKCoinsUser)user).isUserAccountsLoaded()) {
            Collection<BankAccount> accounts = new ArrayList<>();
            for (BankAccount account : this.accountCache.getCachedObjects()) {
                if(account.getMember(user) != null) accounts.add(account);
            }
            return accounts;
        } else {
            return loadAccounts(user);
        }
    }

    private Collection<BankAccount> loadAccounts(DKCoinsUser user) {
        Collection<BankAccount> accounts = new ArrayList<>();

        DefaultDKCoins.getInstance().getStorage().getAccount().find().get("dkcoins_account.Id")
                .join(DefaultDKCoins.getInstance().getStorage().getAccountMember())
                .on("Id", DefaultDKCoins.getInstance().getStorage().getAccountMember(), "AccountId")
                .where("UserId", user.getUniqueId())
                .execute().loadIn(accounts, entry -> this.accountCache.get("search", entry.getInt("Id")));

        ((DefaultDKCoinsUser)user).setUserAccountsLoaded(true);
        return accounts;
    }

    @Override
    public BankAccount getAccount(int id) {
        return this.accountCache.get("byId", id);
    }

    @Override
    public BankAccount getAccount(String name, AccountType type) {
        return this.accountCache.get("nameAndType", name, type);
    }

    @Override
    public BankAccount searchAccount(Object identifier) {
        return this.accountCache.get("search", identifier);
    }

    @Override
    public MasterBankAccount getMasterAccount(int id) {
        if(id <= 0) return null;
        return (MasterBankAccount) getAccount(id);
    }

    @Override
    public BankAccount createAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator) {
        Validate.notNull(name, type, creator);

        int id = DefaultDKCoins.getInstance().getStorage().getAccount()
                .insert().set("Name", name).set("TypeId", type.getId()).set("Disabled", disabled)
                .set("ParentId", parent == null ? null : parent.getId()).set("Master", false)
                .executeAndGetGeneratedKeyAsInt("Id");
        DefaultBankAccount account = new DefaultBankAccount(id, name, type,
                disabled, parent);

        account.addMember(creator, null, AccountMemberRole.OWNER, true);

        accountCache.insert(account);
        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountPreCreateEvent(account, creator));
        this.accountCache.getCaller().createAndIgnore(account.getId(), Document.newDocument());
        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountCreateEvent(account, creator));
        return account;
    }

    @Override
    public MasterBankAccount createMasterAccount(String name, AccountType type, boolean disabled, MasterBankAccount parent, DKCoinsUser creator) {
        int id = DefaultDKCoins.getInstance().getStorage().getAccount().insert()
                .set("Name", name).set("TypeId", type.getId()).set("Disabled", disabled)
                .set("ParentId", parent == null ? null : parent.getId()).set("Master", true)
                .executeAndGetGeneratedKeyAsInt("Id");

        DefaultMasterBankAccount account = new DefaultMasterBankAccount(id, name, type, disabled, parent);;

        account.addMember(creator, null, AccountMemberRole.OWNER, true);

        accountCache.insert(account);
        this.accountCache.getCaller().createAndIgnore(account.getId(), Document.newDocument());
        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountCreateEvent(account, creator));
        return account;
    }

    @Override
    public void deleteAccount(BankAccount account, DKCoinsUser user) {
        Validate.notNull(account, user);

        accountCache.remove(account);
        DefaultDKCoins.getInstance().getStorage().getAccount().delete().where("Id", account.getId()).execute();
        this.accountCache.getCaller().deleteAndIgnore(account.getId(), Document.newDocument());
        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountDeleteEvent(account.getId(), user));
    }

    @Override
    public List<RankedAccountCredit> getTopAccountCredits(Currency currency, AccountType[] excludedAccountTypes, int entriesPerPage, int page) {
        AtomicInteger rank = new AtomicInteger();

        List<RankedAccountCredit> accountCredits = new ArrayList<>();
        FindQuery query = DefaultDKCoins.getInstance().getStorage().getAccountCredit().find()
                .get("dkcoins_account_credit.Id")
                .join(DefaultDKCoins.getInstance().getStorage().getAccount())
                .on("AccountId", DefaultDKCoins.getInstance().getStorage().getAccount(), "Id")
                .where("CurrencyId", currency.getId())
                .orderBy("Amount", SearchOrder.DESC);
        if(excludedAccountTypes != null) {
            for (AccountType type : excludedAccountTypes) {
                query.whereNot("TypeId", type.getId());
            }
        }
        query.page(page, entriesPerPage);
        query.execute().loadIn(accountCredits, entry -> new DefaultRankedAccountCredit(rank.getAndIncrement(), entry.getInt("Id")));

        return accountCredits;
    }

    @Override
    public BankAccount getAccountByRank(Currency currency, int rank) {
        Validate.notNull(currency);

        int accountId = DefaultDKCoins.getInstance().getStorage().getAccountCredit().find()
                .get("AccountId")
                .where("CurrencyId", currency.getId())
                .index(rank, rank)
                .execute().firstOrNull()
                .getInt("AccountId");

        return getAccount(accountId);
    }



    @Override
    public AccountCredit getAccountCredit(int id) {
        QueryResultEntry entry = DefaultDKCoins.getInstance().getStorage().getAccountCredit().find().where("Id", id).execute().firstOrNull();
        if(entry == null) throw new IllegalArgumentException("No account found for credit id " + id);
        int accountId = entry.getInt("AccountId");

        if(accountId == -1) throw new IllegalArgumentException("No account found for credit id " + id);
        return getAccount(accountId).getCredit(id);
    }

    @Override
    public AccountMember getAccountMember(int id) {
        QueryResultEntry result = DefaultDKCoins.getInstance().getStorage().getAccountMember().find()
                .where("Id", id)
                .execute().firstOrNull();

        if(result == null) return null;
        int accountId =  result.getInt("AccountId");
        if(accountId == -1) throw new IllegalArgumentException("No account found for member id " + id);

        return getAccount(accountId).getMember(id);
    }

    @Override
    public AccountMember getAccountMember(DKCoinsUser user, BankAccount account) {
        return account.getMember(user);
    }

    @Internal
    public SynchronizableCache<AccountType, Integer> getAccountTypeCache() {
        return accountTypeCache;
    }

    @Internal
    public SynchronizableCache<BankAccount, Integer> getAccountCache() {
        return accountCache;
    }

    private void createMissingAccountCredits(BankAccount account) {
        if(account == null) return;
        for (Currency currency : DKCoins.getInstance().getCurrencyManager().getCurrencies()) {
            if(account.getCredit(currency) == null) {
                account.addCredit(currency, 0);
            }
        }
    }

    private void registerAccountTypeCacheQueries() {
        accountTypeCache.setClearOnDisconnect(true);
        accountTypeCache.setSkipOnDisconnect(true);
        this.accountTypeCache.setIdentifierQuery(new CacheQuery<AccountType>() {

            @Override
            public boolean check(AccountType accountType, Object[] identifiers) {
                return accountType.getId() == (int) identifiers[0];
            }

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof Integer, "AccountTypeCache: Wrong identifiers for identifier query");
            }
        });

        this.accountTypeCache.setCreateHandler((id, data) -> {
            for (AccountType cachedObject : this.accountTypeCache.getCachedObjects()) {
                if(cachedObject.getId() == id) return null;
            }
            return getAccountTypeInternal(id);
        });

        this.accountTypeCache.setMaxSize(100).registerQuery("byId", new CacheQuery<AccountType>() {

            @Override
            public boolean check(AccountType accountType, Object[] identifiers) {
                return accountType.getId() == (int) identifiers[0];
            }

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof Integer,
                        "AccountTypeCache: Wrong identifiers: %s", Arrays.toString(identifiers));
            }

            @Override
            public AccountType load(Object[] identifiers) {
                return getAccountTypeInternal(identifiers[0]);
            }
        }).registerQuery("search", new CacheQuery<AccountType>() {

            @Override
            public boolean check(AccountType accountType, Object[] identifiers) {
                if(identifiers[0] instanceof Integer) return accountType.getId() == (int) identifiers[0];
                return identifiers[0] instanceof String && accountType.getName().equalsIgnoreCase((String) identifiers[0]);
            }

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1,
                        "AccountTypeCache: Wrong identifiers: %s", Arrays.toString(identifiers));
            }

            @Override
            public AccountType load(Object[] identifiers) {
                return getAccountTypeInternal(identifiers[0]);
            }
        });
    }

    private AccountType getAccountTypeInternal(Object identifier) {
        QueryResultEntry result = DefaultDKCoins.getInstance().getStorage().getAccountType().find()
                .or(query -> {
                    query.where("Name", identifier).where("Symbol", identifier);
                    if(identifier instanceof Integer) query.where("Id", identifier);
                })
                .execute().firstOrNull();
        if(result == null) return null;
        return new DefaultAccountType(result.getInt("Id"), result.getString("Name"), result.getString("Symbol"));
    }

    private void registerAccountCacheQueries() {
        accountCache.setClearOnDisconnect(true);
        accountCache.setSkipOnDisconnect(true);
        this.accountCache.setMaxSize(100);

        this.accountCache.setIdentifierQuery(new CacheQuery<BankAccount>() {
            @Override
            public boolean check(BankAccount account, Object[] identifiers) {
                return (int) identifiers[0] == account.getId();
            }

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof Integer,
                        "AccountCache: Wrong identifiers for identifier query");
            }
        });

        this.accountCache.registerQuery("search", new CacheQuery<BankAccount>() {

            @Override
            public boolean check(BankAccount account, Object[] identifiers) {
                Object identifier = identifiers[0];
                if(identifier instanceof Integer) {
                    return account.getId() == (int) identifier;
                }
                return identifier instanceof String && account.getName().equalsIgnoreCase((String) identifier);
            }

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1, "AccountCache: (search) Invalid length of identifiers");
            }

            @Override
            public BankAccount load(Object[] identifiers) {
                Object identifier = identifiers[0];
                BankAccount account = getAccountInternal(DefaultDKCoins.getInstance().getStorage().getAccount().find()
                        .or(query -> {
                            if(identifier instanceof Integer) query.where("Id", identifier);
                            else query.where("Name", identifier);
                        }).execute().firstOrNull());
                createMissingAccountCredits(account);
                return account;
            }
        }).registerQuery("byId", new CacheQuery<BankAccount>() {

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof Integer, "AccountCache: (byId) Invalid length of identifiers or incorrect identifier");
            }

            @Override
            public BankAccount load(Object[] identifiers) {
                BankAccount account = getAccountInternalById((Integer) identifiers[0]);
                createMissingAccountCredits(account);
                return account;
            }

            @Override
            public boolean check(BankAccount account, Object[] identifiers) {
                return account.getId() == (int) identifiers[0];
            }
        }).registerQuery("nameAndType", new CacheQuery<BankAccount>() {

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 2
                        && identifiers[0] instanceof String
                        && identifiers[1] instanceof AccountType);
            }

            @Override
            public BankAccount load(Object[] identifiers) {
                String name = (String) identifiers[0];
                AccountType type = (AccountType) identifiers[1];
                BankAccount account = getAccountInternal(DefaultDKCoins.getInstance().getStorage().getAccount().find()
                        .where("Name", name).where("TypeId", type.getId()).execute().firstOrNull());
                createMissingAccountCredits(account);
                return account;
            }

            @Override
            public boolean check(BankAccount account, Object[] identifiers) {
                String name = (String) identifiers[0];
                AccountType type = (AccountType) identifiers[1];
                return account.getName().equalsIgnoreCase(name) && account.getType().equals(type);
            }
        }).registerQuery("byCreditId", new CacheQuery<BankAccount>() {
            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof Integer);
            }

            @Override
            public BankAccount load(Object[] identifiers) {
                QueryResultEntry entry = DefaultDKCoins.getInstance().getStorage().getAccountCredit().find()
                        .where("Id", identifiers[0]).execute().firstOrNull();
                if(entry == null) return null;
                int accountId =  entry.getInt("AccountId");
                BankAccount account = DKCoins.getInstance().getAccountManager().getAccount(accountId);
                createMissingAccountCredits(account);
                return account;
            }
            @Override
            public boolean check(BankAccount account, Object[] identifiers) {
                return account.getCredit((int) identifiers[0]) != null;
            }
        });

        this.accountCache.setCreateHandler((id, data) -> {
            for (BankAccount account : this.accountCache.getCachedObjects()) {
                if(account.getId() == id) return null;
            }
            return getAccountInternalById(id);
        });
    }

    private BankAccount getAccountInternalById(int id) {
        return getAccountInternal(DefaultDKCoins.getInstance().getStorage().getAccount().find()
                .where("Id", id).execute().firstOrNull());
    }

    private BankAccount getAccountInternal(QueryResultEntry resultEntry) {
        if(resultEntry == null) return null;

        DefaultDKCoinsStorage storage = DefaultDKCoins.getInstance().getStorage();

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

        for (QueryResultEntry entry : storage.getAccountLimitation().find().where("AccountId", id).whereIsNull("MemberId").execute()) {
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
        for(QueryResultEntry entry : storage.getAccountCredit().find().where("AccountId", id).execute()) {
            account.addLoadedAccountCredit(new DefaultAccountCredit(entry.getInt("Id"), account,
                    DKCoins.getInstance().getCurrencyManager().getCurrency(entry.getInt("CurrencyId")),
                    entry.getDouble("Amount")));
        }
        for (QueryResultEntry entry : storage.getAccountMember().find().where("AccountId", id).execute()) {
            DefaultAccountMember member = new DefaultAccountMember(entry.getInt("Id"), account,
                    DKCoins.getInstance().getUserManager().getUser(entry.getUniqueId("UserId")),
                    AccountMemberRole.byId(entry.getInt("RoleId")), entry.getBoolean("ReceiveNotifications"));
            loadAccountMemberLimitations(member);
            account.addLoadedMember(member);
        }
        return account;
    }

    private void loadAccountMemberLimitations(DefaultAccountMember member) {
        DefaultDKCoinsStorage storage = DefaultDKCoins.getInstance().getStorage();
        for (QueryResultEntry entry : storage.getAccountLimitation().find()
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

    @Internal
    public void clearCaches() {
        this.accountCache.clear();
    }
}
