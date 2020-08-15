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

package net.pretronic.dkcoins.minecraft.config;

import net.pretronic.libraries.utility.Validate;

public class CreditAlias {

    private final String currency;
    private final String permission;
    private final String otherPermission;
    private final String[] commands;
    private final String[] disabledWorlds;

    public CreditAlias(String currency, String permission, String otherPermission, String[] commands, String[] disabledWorlds) {
        Validate.notNull(currency);
        this.currency = currency;
        this.permission = permission;
        this.otherPermission = otherPermission;
        this.commands = (commands == null ? new String[0] : commands);
        this.disabledWorlds = (disabledWorlds == null ? new String[0] : disabledWorlds);
    }

    public String getCurrency() {
        return currency;
    }

    public String getPermission() {
        return permission;
    }

    public String getOtherPermission() {
        return otherPermission;
    }

    public String[] getCommands() {
        return commands;
    }

    public String[] getDisabledWorlds() {
        return disabledWorlds;
    }
}