/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 05.08.20, 15:25
 * @web %web%
 *
 * The DKCoins Project is under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package net.pretronic.dkcoins.minecraft.commands.bank;

import net.pretronic.dkcoins.api.account.AccountLimitation;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.map.Triple;

import java.util.Collection;

public class BankLimitCommand extends AbstractBankLimitCommand<BankAccount> {

    public BankLimitCommand(ObjectOwner owner) {
        super(owner, Messages.COMMAND_BANK_LIMIT_HELP);
    }

    @Override
    protected Collection<AccountLimitation> getLimitations(Triple<BankAccount, AccountMemberRole, BankAccount> target) {
        return Iterators.filter(target.getFirst().getLimitations(), limitation -> limitation.getMemberRole() == null && limitation.getMember() == null);
    }

    @Override
    protected AccountLimitation getLimitation(Triple<BankAccount, AccountMemberRole, BankAccount> target, Currency currency, AccountLimitation.CalculationType calculationType, double amount, AccountLimitation.Interval interval) {
        return target.getFirst().getLimitation(null, null, currency, calculationType, amount, interval);
    }

    @Override
    protected AccountLimitation addLimitation(Triple<BankAccount, AccountMemberRole, BankAccount> target, Currency currency, AccountLimitation.CalculationType calculationType, double amount, AccountLimitation.Interval interval) {
        return target.getFirst().addLimitation(null, null, currency, calculationType, amount, interval);
    }
}
