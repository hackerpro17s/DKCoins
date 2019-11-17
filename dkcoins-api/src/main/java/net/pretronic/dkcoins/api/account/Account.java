/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 14:19
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.account;

import net.prematic.libraries.utility.annonations.Nullable;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

import java.util.Collection;

public interface Account {

    int getId();

    String getName();

    AccountType getType();

    boolean isLocked();

    boolean hasParent();

    Account getParent();

    Collection<AccountCredit> getCredits();

    AccountCredit getCredit(Currency currency);

    AccountCredit getCredit(String currencyName);

    boolean hasLimitation();

    Collection<AccountLimitation> getLimitations();

    @Nullable
    AccountLimitation getLimitation();

    boolean isMember(DKCoinsUser user);

    Collection<AccountMember> getMembers();

    AccountMember getMember(DKCoinsUser user);

    void setName();

    void setLocked(boolean blocked);

    void addCredit(AccountCredit credit);

    void setLimitation(AccountLimitation limitation);

    void deleteLimitation();

    void addMember(AccountMember member);

    void removeMember(AccountMember member);
}