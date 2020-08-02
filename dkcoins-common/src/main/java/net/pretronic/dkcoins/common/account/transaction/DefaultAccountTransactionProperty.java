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

package net.pretronic.dkcoins.common.account.transaction;

import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.libraries.utility.Convert;

public class DefaultAccountTransactionProperty implements AccountTransactionProperty {

    private final String key;
    private final Object value;

    public DefaultAccountTransactionProperty(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public Object asObject() {
        return this.value;
    }

    @Override
    public String asString() {
        return Convert.toString(value);
    }

    @Override
    public int asInt() {
        return Convert.toInteger(value);
    }

    @Override
    public long asLong() {
        return Convert.toLong(value);
    }

    @Override
    public double asDouble() {
        return Convert.toDouble(value);
    }

    @Override
    public float asFloat() {
        return Convert.toFloat(value);
    }

    @Override
    public byte asByte() {
        return Convert.toByte(value);
    }
}
