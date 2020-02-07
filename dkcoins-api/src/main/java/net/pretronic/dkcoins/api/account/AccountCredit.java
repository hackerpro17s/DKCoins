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

    void setAmount(double amount);

    void addAmount(double amount);

    void removeAmount(double amount);


    boolean canTransfer(AccountMember member, double amount);

    boolean deposit(AccountMember member, double amount, String reason,
                    Collection<AccountTransactionProperty> properties);

    //On member default account credit
    boolean withdraw(AccountMember member, double amount, String reason,
                     Collection<AccountTransactionProperty> properties);

    boolean transfer(AccountMember member, double amount, AccountCredit credit, String reason, String cause,
                     Collection<AccountTransactionProperty> properties);
}