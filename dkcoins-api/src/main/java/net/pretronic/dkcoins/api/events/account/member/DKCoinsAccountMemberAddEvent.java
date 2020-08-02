/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 02.08.20, 20:48
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

package net.pretronic.dkcoins.api.events.account.member;

import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.events.account.DKCoinsAccountEvent;

public class DKCoinsAccountMemberAddEvent extends DKCoinsAccountEvent {

    private final AccountMember addedMember;
    private final AccountMember adder;

    public DKCoinsAccountMemberAddEvent(AccountMember addedMember, AccountMember adder) {
        super(addedMember.getAccount());
        this.addedMember = addedMember;
        this.adder = adder;
    }

    public AccountMember getAddedMember() {
        return addedMember;
    }

    public AccountMember getAdder() {
        return adder;
    }
}
