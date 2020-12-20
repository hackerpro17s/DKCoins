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

package net.pretronic.dkcoins.common.account;

import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transferresult.TransferResult;
import net.pretronic.dkcoins.api.account.transferresult.TransferResultFailCause;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.map.caseintensive.CaseIntensiveHashMap;
import net.pretronic.libraries.utility.map.caseintensive.CaseIntensiveMap;

public class DefaultTransferResult implements TransferResult {

    private AccountTransaction transaction;
    private final TransferResultFailCause failCause;
    private final CaseIntensiveMap<Object> properties;

    public DefaultTransferResult(TransferResultFailCause failCause, CaseIntensiveMap<Object> properties) {
        this.failCause = failCause;
        this.properties = properties;
    }

    public DefaultTransferResult(TransferResultFailCause failCause) {
        this.failCause = failCause;
        this.properties = new CaseIntensiveHashMap<>();
    }

    @Override
    public TransferResultFailCause getFailCause() {
        return this.failCause;
    }

    @Override
    public AccountTransaction getTransaction() {
        return this.transaction;
    }

    @Override
    public CaseIntensiveMap<Object> getProperties() {
        return this.properties;
    }

    @Override
    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(Class<T> propertyClass, String key) {
        return (T) getProperty(key);
    }

    public DefaultTransferResult addProperty(String key, Object value) {
        this.properties.put(key, value);
        return this;
    }

    @Internal
    public void setTransaction(AccountTransaction transaction) {
        this.transaction = transaction;
    }
}
