/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 02.08.20, 20:44
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

package net.pretronic.dkcoins.minecraft.listener;

import net.pretronic.dkcoins.api.events.account.credit.DKCoinsAccountCreditPreCreateEvent;
import net.pretronic.dkcoins.api.events.currency.DKCoinsCurrencyEditEvent;
import net.pretronic.dkcoins.minecraft.DKCoinsPlugin;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.event.Listener;

public class InternalListener {

    @Listener
    public void onAccountCreditPreCreate(DKCoinsAccountCreditPreCreateEvent event) {
        event.getAccountCredit().setAmount(DKCoinsConfig.getAccountTypeStartAmount(event.getAccount().getType()));
    }

    @Listener
    public void onCurrencyEdit(DKCoinsCurrencyEditEvent event) {
        if(event.getOperation() == DKCoinsCurrencyEditEvent.Operation.CHANGED_NAME) {
            if(DKCoinsConfig.CURRENCY_DEFAULT.equals(event.getCurrency())) {
                DKCoinsPlugin.getInstance().getConfiguration().set("currency.default", event.getCurrency().getName());
            }
        }
    }
}
