/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 03.02.20, 00:13
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.common.account;

import net.pretronic.databasequery.api.query.Aggregation;
import net.pretronic.databasequery.api.query.result.QueryResult;
import net.pretronic.databasequery.api.query.result.QueryResultEntry;
import net.pretronic.databasequery.api.query.type.FindQuery;
import net.pretronic.databasequery.api.query.type.InsertQuery;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.MasterBankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitation;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitationCalculationType;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitationInterval;
import net.pretronic.dkcoins.api.account.limitation.LimitationAble;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.member.RoleAble;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.account.transferresult.TransferResult;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.events.account.DKCoinsAccountTransactEvent;
import net.pretronic.dkcoins.api.events.account.credit.DKCoinsAccountCreditPreCreateEvent;
import net.pretronic.dkcoins.api.events.account.member.DKCoinsAccountMemberAddEvent;
import net.pretronic.dkcoins.api.events.account.member.DKCoinsAccountMemberRemoveEvent;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.common.DefaultDKCoins;
import net.pretronic.dkcoins.common.DefaultDKCoinsStorage;
import net.pretronic.dkcoins.common.SyncAction;
import net.pretronic.dkcoins.common.account.member.DefaultAccountMember;
import net.pretronic.dkcoins.common.account.member.DefaultAccountMemberRole;
import net.pretronic.dkcoins.common.account.transaction.DefaultAccountTransaction;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.synchronisation.Synchronizable;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.annonations.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.UUID;

public class DefaultBankAccount implements BankAccount, Synchronizable {

    public static final BankAccount DUMMY_ALL = new DefaultBankAccount(-1, "DUMMY_ALL", null, false, null);
    public static final BankAccount DUMMY_ALL_OFFLINE = new DefaultBankAccount(-2, "DUMMY_ALL_OFFLINE", null, false, null);

    private final int id;
    private String name;
    private final AccountType type;
    private boolean disabled;
    private final MasterBankAccount parent;
    private final Collection<AccountCredit> credits;
    private final Collection<AccountLimitation> limitations;
    private final Collection<AccountMember> members;
    private final Collection<AccountMemberRole> roles;

    public DefaultBankAccount(int id, String name, AccountType type, boolean disabled, MasterBankAccount parent) {
        if(id > 0) Validate.notNull(type);
        Validate.notNull(name);

        this.id = id;
        this.name = name;
        this.type = type;
        this.disabled = disabled;
        this.parent = parent;

        this.limitations = new ArrayList<>();
        this.credits = new ArrayList<>();
        this.members = new ArrayList<>();

        this.roles = new ArrayList<>();
        initDefaultRoles();
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public boolean isMasterAccount() {
        return this instanceof MasterBankAccount;
    }

    @Override
    public MasterBankAccount asMasterAccount() {
        Validate.isTrue(this instanceof MasterBankAccount, "Bank account is not a master account");
        return (MasterBankAccount) this;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public AccountType getType() {
        return this.type;
    }

    @Override
    public boolean isDisabled() {
        return this.disabled;
    }

    @Override
    public boolean hasParent() {
        return this.parent != null;
    }

    @Override
    public MasterBankAccount getParent() {
        return this.parent;
    }

    @Override
    public Collection<AccountCredit> getCredits() {
        return this.credits;
    }

    @Override
    public AccountCredit getCredit(int id) {
        return Iterators.findOne(this.credits, credit -> credit.getId() == id);
    }

    @Override
    public AccountCredit getCredit(Currency currency) {
        if(currency == null) throw new IllegalArgumentException("Currency is not available");
        AccountCredit credit = Iterators.findOne(this.credits, credit0 -> credit0.getCurrency().getId() == currency.getId());
        if(credit == null) {
            credit = addCredit(currency, 0);
        }
        return credit;
    }

    @Override
    public BankAccount getAccount() {
        return this;
    }

    @Override
    public Collection<AccountLimitation> getLimitations() {
        return this.limitations;
    }

    @Override
    public AccountLimitation getLimitation(Currency comparativeCurrency, AccountLimitationCalculationType calculationType, double amount, AccountLimitationInterval interval) {
        return getLimitationInternal(null, null, comparativeCurrency, calculationType, amount, interval);
    }

    @Override
    public boolean hasLimitation(Currency currency, double amount) {
        return hasLimitationInternal(this, currency, amount);
    }

    @Override
    public AccountLimitation addLimitation(Currency comparativeCurrency, AccountLimitationCalculationType calculationType, double amount, AccountLimitationInterval interval) {
        return addLimitationInternal(null, null, comparativeCurrency, calculationType, amount, interval);
    }

    @Override
    public AccountLimitation getLimitation(int id) {
        return Iterators.findOne(this.limitations, limitation -> limitation.getId() == id);
    }

    @Internal
    public AccountLimitation getLimitationInternal(AccountMember member, AccountMemberRole role, Currency comparativeCurrency,
                                                   AccountLimitationCalculationType calculationType, double amount, AccountLimitationInterval interval) {
        return Iterators.findOne(this.limitations, limitation -> {
            if(!(member == null || member.equals(limitation.getMember()))) return false;
            if(!(role == null || role.equals(limitation.getMemberRole()))) return false;
            if(!comparativeCurrency.equals(limitation.getComparativeCurrency())) return false;
            if(calculationType != limitation.getCalculationType()) return false;
            if(amount != limitation.getAmount()) return false;
            if(interval != limitation.getInterval()) return false;
            return true;
        });
    }

    @Internal
    public boolean hasLimitationInternal(LimitationAble entity, Currency currency, double amount) {
        BankAccount account = entity.getAccount();
        DefaultDKCoinsStorage storage = DefaultDKCoins.getInstance().getStorage();

        for (AccountLimitation limitation : entity.getLimitations()) {

            if(limitation.getComparativeCurrency().equals(currency)) {
                FindQuery query = storage.getAccountTransaction().find()
                        .getAs(Aggregation.SUM, storage.getAccountTransaction(), "Amount", "TotalAmount")
                        .where("SenderAccountId", account.getCredit(currency).getAccount().getId())
                        .where(storage.getAccountTransaction().getName() + ".CurrencyId", currency.getId())
                        .join(storage.getAccountCredit()).on("SenderAccountId", storage.getAccountCredit(), "AccountId")
                        .join(storage.getAccountMember()).on(storage.getAccountTransaction(), "SenderId", storage.getAccountMember(), "Id");
                if(limitation.getMemberRole() != null && entity instanceof RoleAble) {
                    RoleAble roleAble = ((RoleAble) entity);
                    if(limitation.getMemberRole() == roleAble.getRole()) {
                        query.where("RoleId", roleAble.getRole().getId());
                    } else {
                        continue;
                    }
                }
                if(entity instanceof AccountMember) {
                    AccountMember member = ((AccountMember) entity);
                    if(limitation.getMember() != null && limitation.getMember().getId() == member.getId()) {
                        query.where("SenderId", member.getId());
                    } else if(limitation.getCalculationType() == AccountLimitationCalculationType.USER_BASED) {
                        query.where("SenderId", member.getId());
                    }
                }

                query.whereHigher("Time", getStartLimitationTime(limitation));
                QueryResult result = query.execute();
                if(!result.isEmpty()) {
                    if(result.first().getObject("TotalAmount") == null) continue;
                    double totalAmount = result.first().getDouble("TotalAmount");
                    if(totalAmount+amount >= limitation.getAmount()) return true;
                }
            }
        }
        return false;
    }

    private long getStartLimitationTime(AccountLimitation limitation) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        switch (limitation.getInterval()) {
            case MONTHLY: {
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            }
            case WEEKLY: {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                break;
            }
            case DAILY: {
                break;
            }
        }
        return calendar.getTimeInMillis();
    }

    @Override
    public boolean isMember(DKCoinsUser user) {
        return getMember(user) != null;
    }

    @Override
    public Collection<AccountMember> getMembers() {
        return this.members;
    }

    @Override
    public AccountMember getMember(DKCoinsUser user) {
        return Iterators.findOne(this.members, member -> member.getUser().equals(user));
    }

    @Override
    public AccountMember getMember(int id) {
        return Iterators.findOne(this.members, member -> member.getId() == id);
    }

    @Override
    public AccountMember getMember(UUID uniqueId) {
        return Iterators.findOne(this.members, member -> member.getUser().getUniqueId().equals(uniqueId));
    }

    @Override
    public void setName(String name) {
        DefaultDKCoinsStorage storage = DefaultDKCoins.getInstance().getStorage();
        DefaultAccountManager accountManager = DefaultDKCoins.getInstance().getAccountManager();
        storage.getAccount().update()
                .set("Name", name)
                .where("Id", id)
                .execute();
        updateName(name);
        accountManager.getAccountCache().getCaller().updateAndIgnore(getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_UPDATE_NAME)
                .add("name", getName()));
    }

    @Override
    public void setDisabled(boolean disabled) {
        DefaultDKCoinsStorage storage = DefaultDKCoins.getInstance().getStorage();
        DefaultAccountManager accountManager = DefaultDKCoins.getInstance().getAccountManager();

        storage.getAccount().update()
                .set("Disabled", disabled)
                .where("Id", id)
                .execute();

        updateDisabled(disabled);
        accountManager.getAccountCache().getCaller().updateAndIgnore(getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_UPDATE_DISABLED)
                .add("disabled", isDisabled()));
    }

    @Override
    public AccountCredit addCredit(Currency currency, double amount) {
        DefaultDKCoinsStorage storage = DefaultDKCoins.getInstance().getStorage();
        DefaultAccountManager accountManager = DefaultDKCoins.getInstance().getAccountManager();

        int creditId = storage.getAccountCredit().insert()
                .set("AccountId", getId())
                .set("CurrencyId", currency.getId())
                .set("Amount", amount)
                .executeAndGetGeneratedKeyAsInt("Id");
        AccountCredit credit = new DefaultAccountCredit(creditId, this, currency, amount);
        addLoadedAccountCredit(credit);
        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountCreditPreCreateEvent(credit));
        accountManager.getAccountCache().getCaller().updateAndIgnore(getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_CREDIT_NEW)
                .add("creditId", credit.getId()));
        return credit;
    }

    @Override
    public void deleteCredit(Currency currency) {
        DefaultDKCoinsStorage storage = DefaultDKCoins.getInstance().getStorage();
        DefaultAccountManager accountManager = DefaultDKCoins.getInstance().getAccountManager();

        AccountCredit credit = getCredit(currency);
        storage.getAccountCredit().delete().where("Id", credit.getId()).execute();
        ((DefaultBankAccount)credit.getAccount()).deleteLoadedAccountCredit(credit);
        accountManager.getAccountCache().getCaller().updateAndIgnore(getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_CREDIT_DELETE)
                .add("creditId", credit.getId()));
    }

    @Internal
    public AccountLimitation addLimitationInternal(@Nullable AccountMember member, @Nullable AccountMemberRole role, Currency comparativeCurrency,
                                                   AccountLimitationCalculationType calculationType, double amount, AccountLimitationInterval interval) {
        Validate.notNull(comparativeCurrency);
        Validate.isTrue(amount > 0);

        DefaultDKCoinsStorage storage = DefaultDKCoins.getInstance().getStorage();
        DefaultAccountManager accountManager = DefaultDKCoins.getInstance().getAccountManager();

        InsertQuery query = storage.getAccountLimitation().insert()
                .set("AccountId", getId())
                .set("ComparativeCurrencyId", comparativeCurrency.getId())
                .set("CalculationType", calculationType)
                .set("Amount", amount)
                .set("Interval", interval);

        if(member != null) query.set("MemberId", member.getId());
        if(role != null) query.set("MemberRoleId", role.getId());
        int id = query.executeAndGetGeneratedKeyAsInt("Id");
        AccountLimitation limitation = new DefaultAccountLimitation(id, this, member, role, comparativeCurrency, calculationType, amount, interval);

        addLoadedLimitation(limitation);
        accountManager.getAccountCache().getCaller().updateAndIgnore(getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_LIMITATION_ADD)
                .add("limitationId", limitation.getId()));
        return limitation;
    }

    @Override
    public boolean removeLimitation(AccountLimitation limitation) {
        if(limitation == null) return false;

        DefaultDKCoinsStorage storage = DefaultDKCoins.getInstance().getStorage();
        DefaultAccountManager accountManager = DefaultDKCoins.getInstance().getAccountManager();

        storage.getAccountLimitation().delete().where("Id", limitation.getId()).execute();
        accountManager.getAccountCache().getCaller().updateAndIgnore(getId(),
                Document.newDocument()
                        .add("action", SyncAction.ACCOUNT_LIMITATION_REMOVE)
                        .add("limitationId", limitation.getId()));
        return ((DefaultBankAccount)limitation.getAccount()).removeLoadedLimitation(limitation);
    }

    @Override
    public AccountMember addMember(DKCoinsUser user, AccountMember adder, AccountMemberRole role, boolean receiveNotifications) {
        DefaultDKCoinsStorage storage = DefaultDKCoins.getInstance().getStorage();
        DefaultAccountManager accountManager = DefaultDKCoins.getInstance().getAccountManager();

        int id = storage.getAccountMember().insert()
                .set("AccountId", getId())
                .set("UserId", user.getUniqueId())
                .set("RoleId", role.getId())
                .set("ReceiveNotifications", receiveNotifications)
                .executeAndGetGeneratedKeyAsInt("Id");

        AccountMember member = new DefaultAccountMember(id, this, user, role, receiveNotifications);
        addLoadedMember(member);
        accountManager.getAccountCache().getCaller().updateAndIgnore(getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_MEMBER_ADD)
                .add("memberId", member.getId()));

        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountMemberAddEvent(member, adder));
        return member;
    }

    @Override
    public boolean removeMember(AccountMember member, AccountMember remover) {
        DefaultDKCoinsStorage storage = DefaultDKCoins.getInstance().getStorage();
        DefaultAccountManager accountManager = DefaultDKCoins.getInstance().getAccountManager();

        storage.getAccountMember().delete().where("Id", member.getId()).execute();
        accountManager.getAccountCache().getCaller().updateAndIgnore(getId(), Document.newDocument()
                .add("action", SyncAction.ACCOUNT_MEMBER_REMOVE)
                .add("memberId", member.getId()));

        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountMemberRemoveEvent(member.getUser(), remover));
        return ((DefaultBankAccount)member.getAccount()).removeLoadedMember(member);
    }

    @Override
    public Collection<AccountMemberRole> getRoles() {
        return this.roles;
    }

    @Override
    public AccountMemberRole getRole(int id) {
        return Iterators.findOne(this.roles, role -> role.getId() == id);
    }

    @Override
    public AccountMemberRole getRole(String name) {
        return Iterators.findOne(this.roles, role -> role.getName().equalsIgnoreCase(name));
    }

    @Override
    public AccountTransaction addTransaction(AccountCredit source, AccountMember sender, AccountCredit receiver, double amount,
                                             String reason, String cause, Collection<AccountTransactionProperty> properties) {
        Validate.notNull(source, receiver);
        DefaultDKCoinsStorage storage = DefaultDKCoins.getInstance().getStorage();

        double exchangeRate = source.getCurrency().getExchangeRate(receiver.getCurrency()).getExchangeAmount();
        long time = System.currentTimeMillis();

        int id = storage.getAccountTransaction().insert()
                .set("SenderAccountId", getId())
                .set("SenderAccountName", getName())
                .set("SenderId", sender == null ? null : sender.getId())
                .set("SenderName", sender == null ? "API" : sender.getName())
                .set("DestinationId", receiver.getAccount().getId())
                .set("DestinationName", receiver.getAccount().getName())
                .set("CurrencyId", source.getCurrency().getId())
                .set("CurrencyName", source.getCurrency().getName())
                .set("Amount", amount)
                .set("ExchangeRate", exchangeRate)
                .set("Reason", reason)
                .set("Cause", cause)
                .set("Time", time)
                .executeAndGetGeneratedKeyAsInt("Id");

        if(!properties.isEmpty()) {
            InsertQuery propertyInsertQuery = storage.getAccountTransactionProperty().insert();
            for (AccountTransactionProperty property : properties) {
                propertyInsertQuery.set("Key", property.getKey()).set("Value", property.asObject());
            }
            propertyInsertQuery.execute();
        }
        AccountTransaction transaction = new DefaultAccountTransaction(id, source, sender, receiver, amount, exchangeRate, reason, cause, time, properties);
        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsAccountTransactEvent(transaction));
        return transaction;
    }

    @Override
    public TransferResult exchangeAccountCredit(AccountMember member, Currency from, Currency to, double amount,
                                                String reason, Collection<AccountTransactionProperty> properties) {
        return getCredit(from).transfer(member, amount, getCredit(to), reason, TransferCause.EXCHANGE, properties);
    }

    @Internal
    public void addLoadedAccountCredit(AccountCredit credit) {
        this.credits.add(credit);
    }

    @Internal
    public boolean deleteLoadedAccountCredit(AccountCredit credit) {
        return this.credits.remove(credit);
    }

    @Internal
    public void addLoadedLimitation(AccountLimitation limitation) {
        this.limitations.add(limitation);
    }

    @Internal
    public boolean removeLoadedLimitation(AccountLimitation limitation) {
        return this.limitations.remove(limitation);
    }

    @Internal
    public void addLoadedMember(AccountMember member) {
        if(getMember(member.getId()) == null) {
            this.members.add(member);
        }
    }

    @Internal
    public boolean removeLoadedMember(AccountMember member) {
        return this.members.remove(member);
    }

    @Internal
    public void updateName(String name) {
        this.name = name;
    }

    @Internal
    public void updateDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    private void initDefaultRoles() {
        DefaultAccountMemberRole owner = new DefaultAccountMemberRole(this, 1, "OWNER", null, AccessRight.ADMIN_MANAGEMENT, AccessRight.DELETE);
        DefaultAccountMemberRole admin = new DefaultAccountMemberRole(this, 2, "ADMIN", owner, AccessRight.LIMIT_MANAGEMENT, AccessRight.ROLE_MANAGEMENT);
        DefaultAccountMemberRole manager = new DefaultAccountMemberRole(this, 3, "MANAGER", admin, AccessRight.MEMBER_MANAGEMENT, AccessRight.EXCHANGE);
        DefaultAccountMemberRole user = new DefaultAccountMemberRole(this, 4, "USER", manager, AccessRight.WITHDRAW, AccessRight.DEPOSIT);
        DefaultAccountMemberRole guest = new DefaultAccountMemberRole(this, 5, "GUEST", user, AccessRight.VIEW);

        this.roles.add(owner);
        this.roles.add(admin);
        this.roles.add(manager);
        this.roles.add(user);
        this.roles.add(guest);
    }

    @Override
    public void onUpdate(Document data) {
        switch (data.getString("action")) {
            case SyncAction.ACCOUNT_UPDATE_NAME: {
                this.name = data.getString("name");
                break;
            }
            case SyncAction.ACCOUNT_UPDATE_DISABLED: {
                this.disabled = data.getBoolean("disabled");
                break;
            }
            case SyncAction.ACCOUNT_CREDIT_NEW: {
                int id = data.getInt("creditId");
                if(getCredit(id) == null) {
                    addLoadedAccountCredit(DKCoins.getInstance().getAccountManager().getAccountCredit(id));
                }
                break;
            }
            case SyncAction.ACCOUNT_CREDIT_DELETE: {
                Iterators.removeOne(this.credits, credit -> credit.getId() == data.getInt("creditId"));
                break;
            }
            case SyncAction.ACCOUNT_CREDIT_AMOUNT_UPDATE: {
                int creditId = data.getInt("creditId");
                DefaultAccountCredit credit = (DefaultAccountCredit) getCredit(creditId);
                if(credit != null) {
                    credit.reloadAmount();
                }
                break;
            }
            case SyncAction.ACCOUNT_LIMITATION_ADD: {
                DefaultDKCoinsStorage storage = DefaultDKCoins.getInstance().getStorage();

                QueryResultEntry entry = storage.getAccountLimitation().find()
                        .where("Id", data.getInt("limitationId"))
                        .where("AccountId", getId())
                        .execute().firstOrNull();
                if(entry == null) break;

                int memberRoleId = entry.getInt("MemberRoleId");
                addLoadedLimitation(new DefaultAccountLimitation(entry.getInt("Id"),
                        this,
                        null,
                        getRole(memberRoleId),
                        DKCoins.getInstance().getCurrencyManager().getCurrency(entry.getInt("CurrencyId")),
                        AccountLimitationCalculationType.valueOf(entry.getString("CalculationType")),
                        entry.getDouble("Amount"),
                        AccountLimitationInterval.valueOf(entry.getString("Interval"))));

                break;
            }
            case SyncAction.ACCOUNT_LIMITATION_REMOVE: {
                Iterators.removeOne(this.limitations, limitation -> limitation.getId() == data.getInt("limitationId"));
                break;
            }
            case SyncAction.ACCOUNT_MEMBER_ADD: {
                addLoadedMember(DKCoins.getInstance().getAccountManager().getAccountMember(data.getInt("memberId")));
                break;
            }
            case SyncAction.ACCOUNT_MEMBER_REMOVE: {
                Iterators.removeOne(this.members, member -> member.getId() == data.getInt("memberId"));
                break;
            }
            case SyncAction.ACCOUNT_MEMBER_UPDATE_ROLE: {
                DefaultAccountMember member = (DefaultAccountMember) getMember(data.getInt("memberId"));
                member.updateRole(getRole(data.getInt("roleId")));
                break;
            }
            case SyncAction.ACCOUNT_MEMBER_UPDATE_RECEIVE_NOTIFICATIONS: {
                DefaultAccountMember member = (DefaultAccountMember) getMember(data.getInt("memberId"));
                member.updateReceiveNotifications(data.getBoolean("receiveNotifications"));
                break;
            }
            default: {
                DKCoins.getInstance().getLogger().warn("Account (id={}) update without action", getId());
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BankAccount && ((BankAccount)obj).getId() == this.id;
    }
}
