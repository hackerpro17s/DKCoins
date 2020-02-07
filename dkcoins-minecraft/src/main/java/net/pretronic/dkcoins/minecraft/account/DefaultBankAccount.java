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

import net.prematic.libraries.document.Document;
import net.prematic.libraries.utility.Iterators;
import net.prematic.libraries.utility.Validate;
import net.prematic.libraries.utility.annonations.Internal;
import net.prematic.libraries.utility.annonations.Nullable;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.*;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.MasterBankAccount;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

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
    private final List<AccountTransaction> transactions;

    public DefaultBankAccount(int id, String name, AccountType type, boolean disabled, MasterBankAccount parent) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.disabled = disabled;
        this.parent = parent;
        this.limitations = new ArrayList<>();
        this.credits = new ArrayList<>();
        this.members = new ArrayList<>();
        this.transactions = new ArrayList<>();
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
        return Iterators.findOne(this.credits, credit -> credit.getCurrency().equals(currency));
    }

    @Override
    public Collection<AccountLimitation> getLimitations() {
        return this.limitations;
    }

    //@Todo caching
    @Override
    public boolean hasLimitation(AccountMemberRole memberRole, Currency currency, double amount) {
        return DKCoins.getInstance().getAccountManager().hasLimitation(this, currency, amount);
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
    public AccountMember getMember(DKCoinsUser user) {
        AccountMember accountMember = Iterators.findOne(this.members, member -> member.getUser().equals(user));
        if(accountMember == null) {
            accountMember = DKCoins.getInstance().getAccountManager().getAccountMember(user, this);
        }
        if(accountMember != null) this.members.add(accountMember);
        return accountMember;
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
    public void addCredit(Currency currency, double amount) {
        AccountCredit accountCredit = DKCoins.getInstance().getAccountManager().addAccountCredit(this, currency, amount);
        this.credits.add(accountCredit);
    }

    @Override
    public void addLimitation(@Nullable AccountMember member, @Nullable AccountMemberRole role, Currency comparativeCurrency,
                       double amount, long interval) {
        AccountLimitation limitation = DKCoins.getInstance().getAccountManager()
                .addAccountLimitation(this, member, role, comparativeCurrency, amount, interval);
        this.limitations.add(limitation);
    }

    @Override
    public void deleteLimitation(AccountLimitation limitation) {
        DKCoins.getInstance().getAccountManager().deleteAccountLimitation(limitation);
        this.limitations.remove(limitation);
    }

    @Override
    public void addMember(DKCoinsUser user, AccountMemberRole role) {
        AccountMember member = DKCoins.getInstance().getAccountManager().addAccountMember(this, user, role);
        this.members.add(member);
    }

    @Override
    public void removeMember(AccountMember member) {
        DKCoins.getInstance().getAccountManager().deleteAccountMember(member);
        this.members.remove(member);
    }

    @Override
    public void addTransaction(AccountCredit source, AccountMember sender, AccountCredit receiver, double amount,
                               String reason, String cause, Collection<AccountTransactionProperty> properties) {
        double exchangeRate = DKCoins.getInstance().getCurrencyManager().getCurrencyExchangeRate(source.getCurrency(),
                receiver.getCurrency()).getExchangeAmount();
        AccountTransaction transaction = DKCoins.getInstance().getAccountManager()
                .addAccountTransaction(source, sender, receiver, amount, exchangeRate, reason, cause, System.currentTimeMillis(), properties);
        this.transactions.add(transaction);
    }

    @Override
    public boolean exchangeAccountCredit(AccountMember member, Currency from, Currency to, double amount,
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
}
