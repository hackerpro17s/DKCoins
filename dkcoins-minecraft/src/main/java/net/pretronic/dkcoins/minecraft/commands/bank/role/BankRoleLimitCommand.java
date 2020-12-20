/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 05.08.20, 15:18
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

package net.pretronic.dkcoins.minecraft.commands.bank.role;

import net.pretronic.dkcoins.api.account.limitation.AccountLimitation;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitationCalculationType;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitationInterval;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.bank.AbstractBankLimitCommand;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.map.Triple;

import java.util.Collection;

public class BankRoleLimitCommand extends AbstractBankLimitCommand<AccountMemberRole> {

    public BankRoleLimitCommand(ObjectOwner owner) {
        super(owner, Messages.COMMAND_BANK_ROLE_LIMIT_HELP);
    }

    @Override
    protected Collection<AccountLimitation> getLimitations(Triple<BankAccount, AccountMemberRole, AccountMemberRole> target) {
        return Iterators.filter(target.getFirst().getLimitations(), limitation ->
                limitation.getMemberRole() != null && limitation.getMemberRole() == target.getThird());
    }

    @Override
    protected AccountLimitation getLimitation(Triple<BankAccount, AccountMemberRole, AccountMemberRole> target, Currency currency, AccountLimitationCalculationType calculationType, double amount, AccountLimitationInterval interval) {
        return target.getFirst().getLimitation(null, target.getSecond(), currency, calculationType, amount, interval);
    }

    @Override
    protected AccountLimitation addLimitation(Triple<BankAccount, AccountMemberRole, AccountMemberRole> target, Currency currency, AccountLimitationCalculationType calculationType, double amount, AccountLimitationInterval interval) {
        return target.getFirst().addLimitation(null, target.getSecond(), currency, calculationType, amount, interval);
    }
}
