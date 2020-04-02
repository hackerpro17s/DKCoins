/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 03.02.20, 00:13
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.account;

import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.entry.DocumentEntry;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.annonations.Nullable;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.*;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.DKCoinsConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultBankAccount implements BankAccount {

    private final int id;
    private String name;
    private final AccountType type;
    private boolean disabled;
    private final MasterBankAccount parent;
    private final Collection<AccountCredit> credits;
    private final Collection<AccountLimitation> limitations;
    private final Collection<AccountMember> members;

    public DefaultBankAccount(int id, String name, AccountType type, boolean disabled, MasterBankAccount parent) {
        Validate.isTrue(id > 0);
        Validate.notNull(name, type);
        this.id = id;
        this.name = name;
        this.type = type;
        this.disabled = disabled;
        this.parent = parent;
        this.limitations = new ArrayList<>();
        this.credits = new ArrayList<>();
        this.members = new ArrayList<>();
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
        if(currency == null) return null;
        AccountCredit credit = Iterators.findOne(this.credits, credit0 -> credit0.getCurrency().equals(currency));
        if(credit == null) {
            double amount = 0;
            if(currency.equals(DKCoinsConfig.CURRENCY_DEFAULT)) {
                amount = DKCoinsConfig.ACCOUNT_TYPE_START_AMOUNT.get(getType());
            }
            credit = addCredit(currency, amount);
        }
        return credit;
    }

    @Override
    public Collection<AccountLimitation> getLimitations() {
        return this.limitations;
    }

    @Override
    public AccountLimitation getLimitation(int id) {
        return Iterators.findOne(this.limitations, limitation -> limitation.getId() == id);
    }

    @Override
    public AccountLimitation getLimitation(AccountMember member, AccountMemberRole role, Currency comparativeCurrency, double amount, long interval) {
        return Iterators.findOne(this.limitations, limitation -> {
            if(!(member == null || member.equals(limitation.getMember()))) {
                return false;
            }
            if(!(role == null || role.equals(limitation.getMemberRole()))) {
                return false;
            }
            if(!comparativeCurrency.equals(limitation.getComparativeCurrency())) {
                return false;
            }
            if(amount != limitation.getAmount()) {
                return false;
            }
            if(interval != limitation.getInterval()) {
                return false;
            }
            return true;
        });
    }

    //@Todo caching
    @Override
    public boolean hasLimitation(AccountMemberRole memberRole, Currency currency, double amount) {
        return DKCoins.getInstance().getAccountManager().hasAccountLimitation(this, currency, amount);
    }

    @Override
    public boolean hasLimitation(AccountMember member, Currency currency, double amount) {
        return member.hasLimitation(currency, amount);
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
    public void setName(String name) {
        DKCoins.getInstance().getAccountManager().updateAccountName(this);
        this.name = name;
    }

    @Override
    public void setDisabled(boolean disabled) {
        DKCoins.getInstance().getAccountManager().updateAccountDisabled(this);
        this.disabled = disabled;
    }

    @Override
    public AccountCredit addCredit(Currency currency, double amount) {
        AccountCredit accountCredit = DKCoins.getInstance().getAccountManager().addAccountCredit(this, currency, amount);
        this.credits.add(accountCredit);
        return accountCredit;
    }

    @Override
    public void deleteCredit(Currency currency) {
        AccountCredit credit = getCredit(currency);
        DKCoins.getInstance().getAccountManager().deleteAccountCredit(credit);
        this.credits.remove(credit);
    }

    @Override
    public void addLimitation(@Nullable AccountMember member, @Nullable AccountMemberRole role, Currency comparativeCurrency,
                       double amount, long interval) {
        AccountLimitation limitation = DKCoins.getInstance().getAccountManager()
                .addAccountLimitation(this, member, role, comparativeCurrency, amount, interval);
        this.limitations.add(limitation);
    }

    @Override
    public boolean removeLimitation(AccountLimitation limitation) {
        if(limitation == null) return false;
        DKCoins.getInstance().getAccountManager().removeAccountLimitation(limitation);
        this.limitations.remove(limitation);
        return true;
    }

    @Override
    public void addMember(DKCoinsUser user, AccountMember adder, AccountMemberRole role) {
        AccountMember member = DKCoins.getInstance().getAccountManager().addAccountMember(this, user, adder, role);
        this.members.add(member);
    }

    @Override
    public void removeMember(AccountMember member, AccountMember remover) {
        DKCoins.getInstance().getAccountManager().removeAccountMember(member, remover);
        this.members.remove(member);
    }

    @Override
    public AccountTransaction addTransaction(AccountCredit source, AccountMember sender, AccountCredit receiver, double amount,
                               String reason, String cause, Collection<AccountTransactionProperty> properties) {
        double exchangeRate = source.getCurrency().getExchangeRate(receiver.getCurrency()).getExchangeAmount();
        AccountTransaction transaction = DKCoins.getInstance().getAccountManager()
                .addAccountTransaction(source, sender, receiver, amount, exchangeRate, reason, cause, System.currentTimeMillis(), properties);
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
    public void addLoadedLimitation(AccountLimitation limitation) {
        this.limitations.add(limitation);
    }

    @Internal
    public void addLoadedMember(AccountMember member) {
        this.members.add(member);
    }

    @Override
    public void onUpdate(Document data) {
        for (DocumentEntry entry : data) {
            switch (entry.getKey()) {
                case "name": {
                    this.name = entry.toPrimitive().getAsString();
                    break;
                }
                case "disabled": {
                    this.disabled = entry.toPrimitive().getAsBoolean();
                    break;
                }
                case "newCredit": {
                    int id = entry.toPrimitive().getAsInt();
                    if(getCredit(id) == null) {
                        addLoadedAccountCredit(DKCoins.getInstance().getAccountManager().getAccountCredit(id));
                    }
                    break;
                }
                case "removeCredit": {
                    Iterators.removeOne(this.credits, credit -> credit.getId() == entry.toPrimitive().getAsInt());
                    break;
                }
                case "updateCreditAmount": {
                    DefaultAccountCredit credit = (DefaultAccountCredit) getCredit(entry.toDocument().getInt("creditId"));
                    credit.updateAmount(entry.toDocument().getDouble("amount"));
                    break;
                }
                case "newLimitation": {
                    addLoadedLimitation(DKCoins.getInstance().getAccountManager().getAccountLimitation(entry.toPrimitive().getAsInt()));
                    break;
                }
                case "removeLimitation": {
                    Iterators.removeOne(this.limitations, limitation -> limitation.getId() == entry.toPrimitive().getAsInt());
                    break;
                }
                case "newMember": {
                    addLoadedMember(DKCoins.getInstance().getAccountManager().getAccountMember(entry.toPrimitive().getAsInt()));
                    break;
                }
                case "removeMember": {
                    Iterators.removeOne(this.members, member -> member.getId() == entry.toPrimitive().getAsInt());
                }
                case "updateMemberRole": {
                    DefaultAccountMember member = (DefaultAccountMember) getMember(entry.toDocument().getInt("memberId"));
                    member.updateRole(AccountMemberRole.byId(entry.toDocument().getInt("roleId")));
                }
                default: {
                    DKCoins.getInstance().getLogger().warn("Account (id={}) update without action", getId());
                }
            }
        }
    }
}
