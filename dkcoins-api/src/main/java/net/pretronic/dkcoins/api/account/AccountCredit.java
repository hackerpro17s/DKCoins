/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 14:24
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.account;

import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.account.transferresult.TransferResult;
import net.pretronic.dkcoins.api.currency.Currency;

import java.util.Collection;

public interface AccountCredit {

    int getId();

    BankAccount getAccount();

    Currency getCurrency();

    default String getName() {
        return getAccount().getName();
    }


    boolean hasAmount(double amount);


    String getFormattedAmount();

    double getAmount();

    AccountTransaction setAmount(AccountMember executor, double amount, String reason, String cause, Collection<AccountTransactionProperty> properties);

    default AccountTransaction setAmount(double amount, String cause, Collection<AccountTransactionProperty> properties) {
        return setAmount(null, amount, null, cause, properties);
    }

    AccountTransaction addAmount(AccountMember executor, double amount, String reason, String cause, Collection<AccountTransactionProperty> properties);

    AccountTransaction removeAmount(AccountMember executor, double amount, String reason, String cause, Collection<AccountTransactionProperty> properties);

    //Without transaction add
    void setAmount(double amount);

    //Without transaction add
    void addAmount(double amount);

    //Without transaction add
    void removeAmount(double amount);


    TransferResult canTransfer(AccountMember member, AccountCredit target, double amount);

    TransferResult deposit(AccountMember member, double amount, String reason,
                    Collection<AccountTransactionProperty> properties);

    //On member default account credit
    TransferResult withdraw(AccountMember member, double amount, String reason,
                     Collection<AccountTransactionProperty> properties);

    TransferResult transfer(AccountMember member, double amount, AccountCredit credit, String reason, String cause,
                     Collection<AccountTransactionProperty> properties);
}