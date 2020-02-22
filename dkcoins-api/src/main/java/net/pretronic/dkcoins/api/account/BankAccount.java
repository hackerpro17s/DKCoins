/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 03.02.20, 00:12
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.account;

import net.prematic.libraries.utility.annonations.Nullable;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

import java.util.Collection;

public interface BankAccount {

    int getId();


    boolean isMasterAccount();

    MasterBankAccount asMasterAccount();


    String getName();

    void setName(String name);


    AccountType getType();


    boolean isDisabled();

    void setDisabled(boolean disabled);


    boolean hasParent();

    @Nullable
    MasterBankAccount getParent();


    Collection<AccountCredit> getCredits();

    AccountCredit getCredit(int id);

    AccountCredit getCredit(Currency currency);

    void addCredit(Currency currency, double amount);


    Collection<AccountLimitation> getLimitations();

    boolean hasLimitation(AccountMemberRole memberRole, Currency currency, double amount);

    boolean hasLimitation(AccountMember member, Currency currency, double amount);

    void addLimitation(@Nullable AccountMember member, @Nullable AccountMemberRole role, Currency comparativeCurrency,
                       double amount, long interval);

    void deleteLimitation(AccountLimitation limitation);


    boolean isMember(DKCoinsUser user);

    AccountMember getMember(DKCoinsUser user);

    void addMember(DKCoinsUser user, AccountMemberRole role);

    void removeMember(AccountMember member);


    void addTransaction(AccountCredit source, AccountMember sender, AccountCredit receiver, double amount, String reason,
                        String cause, Collection<AccountTransactionProperty> properties);

    boolean exchangeAccountCredit(AccountMember member, Currency from, Currency to, double amount, String reason, Collection<AccountTransactionProperty> properties);
}