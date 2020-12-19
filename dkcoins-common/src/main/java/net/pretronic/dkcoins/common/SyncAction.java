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

package net.pretronic.dkcoins.common;

public class SyncAction {

    public static final String ACCOUNT_UPDATE_NAME = "ACCOUNT_UPDATE_NAME";
    public static final String ACCOUNT_UPDATE_DISABLED = "ACCOUNT_UPDATE_DISABLED";

    public static final String ACCOUNT_CREDIT_NEW = "ACCOUNT_CREDIT_NEW";
    public static final String ACCOUNT_CREDIT_DELETE = "ACCOUNT_CREDIT_DELETE";
    public static final String ACCOUNT_CREDIT_AMOUNT_UPDATE = "ACCOUNT_CREDIT_AMOUNT_UPDATE";

    public static final String ACCOUNT_MEMBER_ADD = "ACCOUNT_MEMBER_ADD";
    public static final String ACCOUNT_MEMBER_REMOVE = "ACCOUNT_MEMBER_REMOVE";
    public static final String ACCOUNT_MEMBER_UPDATE_ROLE = "ACCOUNT_MEMBER_UPDATE_ROLE";
    public static final String ACCOUNT_MEMBER_UPDATE_RECEIVE_NOTIFICATIONS = "ACCOUNT_MEMBER_UPDATE_RECEIVE_NOTIFICATIONS";

    public static final String ACCOUNT_LIMITATION_ADD = "ACCOUNT_LIMITATION_ADD";
    public static final String ACCOUNT_LIMITATION_REMOVE = "ACCOUNT_LIMITATION_REMOVE";

    public static final String CURRENCY_UPDATE_NAME = "CURRENCY_UPDATE_NAME";
    public static final String CURRENCY_UPDATE_SYMBOL = "CURRENCY_UPDATE_SYMBOL";

    public static final String CURRENCY_EXCHANGE_RATE_NEW = "CURRENCY_EXCHANGE_RATE_NEW";
    public static final String CURRENCY_EXCHANGE_RATE_DELETE = "CURRENCY_EXCHANGE_RATE_DELETE";
    public static final String CURRENCY_EXCHANGE_RATE_UPDATE_AMOUNT = "CURRENCY_EXCHANGE_RATE_UPDATE_AMOUNT";
}
