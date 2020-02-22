/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 13.02.20, 16:30
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.account.transaction;

import net.pretronic.dkcoins.api.account.member.AccountMember;

import java.util.Collection;

public interface TransactionPropertyBuilder {

    Collection<AccountTransactionProperty> build(AccountMember member);
}
