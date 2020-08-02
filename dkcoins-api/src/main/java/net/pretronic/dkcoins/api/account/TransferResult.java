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

package net.pretronic.dkcoins.api.account;

import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.libraries.utility.map.caseintensive.CaseIntensiveMap;

public interface TransferResult {

    default boolean isSuccess() {
        return getFailCause() == null;
    }

    default boolean isFailed() {
        return getFailCause() != null;
    }

    FailCause getFailCause();

    AccountTransaction getTransaction();

    CaseIntensiveMap<Object> getProperties();

    Object getProperty(String key);

    <T> T getProperty(Class<T> propertyClass, String key);

    enum FailCause {

        NOT_ENOUGH_AMOUNT,
        NOT_ENOUGH_ACCESS_RIGHTS,
        MASTER_ACCOUNT_NOT_ENOUGH_AMOUNT,
        LIMIT,
        TRANSFER_DISABLED
    }
}
