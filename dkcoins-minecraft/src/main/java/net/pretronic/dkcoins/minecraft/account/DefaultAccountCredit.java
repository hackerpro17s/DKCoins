/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 24.11.19, 15:08
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.account;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.currency.Currency;

import java.util.Collection;

public class DefaultAccountCredit implements AccountCredit {

    private final int id;
    private final BankAccount account;
    private final Currency currency;
    private double amount;

    public DefaultAccountCredit(int id, BankAccount account, Currency currency, double amount) {
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
    public double getAmount() {
        return this.amount;
    }

    @Override
    public void setAmount(double amount) {
        DKCoins.getInstance().getAccountManager().setAccountCreditAmount(this, amount);
        this.amount = amount;
    }

    @Override
    public void addAmount(double amount) {
        double amount0 = this.amount+amount;
        setAmount(amount0);
    }

    @Override
    public void removeAmount(double amount) {
        double amount0 = this.amount-amount;
        setAmount(amount0);
    }

    @Override
    public boolean canTransfer(AccountMember member, double amount) {
        return getAmount() >= amount
                && member.getRole() != AccountMemberRole.GUEST
                && (!getAccount().isMasterAccount() || getAccount().asMasterAccount().getCredit(getCurrency()).getAmount() >= amount)
                && !member.hasLimitation(getCurrency(), amount);
    }

    @Override
    public boolean deposit(AccountMember member, double amount, String reason, Collection<AccountTransactionProperty> properties) {
        BankAccount account = member.getUser().getDefaultAccount();
        AccountMember accountMember = account.getMember(member.getUser());
        return account.getCredit(getCurrency()).transfer(accountMember, amount, this, reason, TransferCause.DEPOSIT, properties);
    }

    @Override
    public boolean withdraw(AccountMember member, double amount, String reason, Collection<AccountTransactionProperty> properties) {
        return transfer(member, amount, member.getUser().getDefaultAccount()
                .getCredit(getCurrency()), reason, TransferCause.WITHDRAW, properties);
    }

    @Override
    public boolean transfer(AccountMember member, double amount, AccountCredit credit, String reason, String cause,
                            Collection<AccountTransactionProperty> properties) {
        if(canTransfer(member, amount)) {
            credit.addAmount(amount);
            removeAmount(amount);
            member.getAccount().addTransaction(this, member, credit, amount, reason, cause, properties);
            return true;
        }
        return false;
    }
}
