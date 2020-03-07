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
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.currency.Currency;

import java.util.Collection;

public interface AccountCredit {

    int getId();

    BankAccount getAccount();

    Currency getCurrency();


    boolean hasAmount(double amount);

    double getAmount();

    void setAmount(AccountMember member, double amount, String reason, String cause, Collection<AccountTransactionProperty> properties);

    default void addAmount(AccountMember member, double amount, String reason, String cause, Collection<AccountTransactionProperty> properties) {
        setAmount(member, getAmount()+amount, reason, cause, properties);
    }

    default void removeAmount(AccountMember member, double amount, String reason, String cause, Collection<AccountTransactionProperty> properties) {
        setAmount(member, getAmount()-amount, reason, cause, properties);
    }

    void setAmount(double amount);

    default void addAmount(double amount) {
        setAmount(getAmount()+amount);
    }

    default void removeAmount(double amount) {
        setAmount(getAmount()-amount);
    }

    TransferResult canTransfer(AccountMember member, AccountCredit target, double amount);

    TransferResult deposit(AccountMember member, double amount, String reason,
                    Collection<AccountTransactionProperty> properties);

    //On member default account credit
    TransferResult withdraw(AccountMember member, double amount, String reason,
                     Collection<AccountTransactionProperty> properties);

    TransferResult transfer(AccountMember member, double amount, AccountCredit credit, String reason, String cause,
                     Collection<AccountTransactionProperty> properties);
}