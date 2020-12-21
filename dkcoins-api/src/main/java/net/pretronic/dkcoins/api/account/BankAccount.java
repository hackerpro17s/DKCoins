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

import net.pretronic.dkcoins.api.account.limitation.AccountLimitation;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitationCalculationType;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitationInterval;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.account.transferresult.TransferResult;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.libraries.utility.annonations.Nullable;

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

    AccountCredit addCredit(Currency currency, double amount);

    void deleteCredit(Currency currency);


    Collection<AccountLimitation> getLimitations();

    AccountLimitation getLimitation(int id);

    AccountLimitation getLimitation(@Nullable AccountMember member, @Nullable AccountMemberRole role, Currency comparativeCurrency,
                                    AccountLimitationCalculationType calculationType, double amount, AccountLimitationInterval interval);

    boolean hasLimitation(AccountMember member, Currency currency, double amount);

    AccountLimitation addLimitation(@Nullable AccountMember member, @Nullable AccountMemberRole role, Currency comparativeCurrency,
                                    AccountLimitationCalculationType calculationType, double amount, AccountLimitationInterval interval);

    boolean removeLimitation(AccountLimitation limitation);

    default boolean removeLimitation(@Nullable AccountMember member, @Nullable AccountMemberRole role, Currency comparativeCurrency,
                                     AccountLimitationCalculationType calculationType, double amount, AccountLimitationInterval interval) {
        return removeLimitation(getLimitation(member, role, comparativeCurrency, calculationType, amount, interval));
    }


    boolean isMember(DKCoinsUser user);

    Collection<AccountMember> getMembers();

    AccountMember getMember(DKCoinsUser user);

    AccountMember getMember(int id);

    AccountMember addMember(DKCoinsUser user, AccountMember adder, AccountMemberRole role, boolean receiveNotifications);

    boolean removeMember(AccountMember member, AccountMember remover);


    AccountTransaction addTransaction(AccountCredit source, AccountMember sender, AccountCredit receiver, double amount, String reason,
                                      String cause, Collection<AccountTransactionProperty> properties);

    TransferResult exchangeAccountCredit(AccountMember member, Currency from, Currency to, double amount, String reason, Collection<AccountTransactionProperty> properties);
}