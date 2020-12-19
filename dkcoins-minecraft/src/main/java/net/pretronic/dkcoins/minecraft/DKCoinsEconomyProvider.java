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

package net.pretronic.dkcoins.minecraft;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.common.account.TransferCause;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.utility.Validate;
import org.mcnative.common.player.MinecraftPlayer;
import org.mcnative.common.serviceprovider.economy.EconomyProvider;
import org.mcnative.common.serviceprovider.economy.EconomyResponse;

import java.util.Collection;
import java.util.Collections;

public class DKCoinsEconomyProvider implements EconomyProvider {

    @Override
    public String getName() {
        return "DKCoins";
    }

    @Override
    public boolean hasBankSupport() {
        return true;
    }

    @Override
    public String getCurrencyName() {
        return DKCoinsConfig.ECONOMY_PROVIDER_CURRENCY.getName();
    }

    @Override
    public String getCurrencySingularName() {
        return null;
    }

    @Override
    public String getCurrencyPluralName() {
        return null;
    }

    @Override
    public String formatBalance(double balance) {
        return DKCoinsConfig.formatCurrencyAmount(balance);
    }

    @Override
    public boolean hasAccount(MinecraftPlayer player) {
        Validate.notNull(player);
        return DKCoins.getInstance().getAccountManager().searchAccount(player.getName()) != null;
    }

    @Override
    public double getPlayerBalance(MinecraftPlayer player) {
        Validate.notNull(player);
        AccountCredit credit = getDefaultAccountCredit(player);
        Validate.notNull(credit);
        return credit.getAmount();
    }

    @Override
    public boolean hasPlayerBalance(MinecraftPlayer player, double amount) {
        Validate.notNull(player);
        return getPlayerBalance(player) >= amount;
    }

    @Override
    public EconomyResponse setPlayerBalance(MinecraftPlayer player, double amount) {
        Validate.notNull(player);
        AccountCredit credit = getDefaultAccountCredit(player);
        credit.setAmount(amount, TransferCause.PROVIDER,
                DKCoins.getInstance().getTransactionPropertyBuilder().build(credit.getAccount().getMember(player.getAs(DKCoinsUser.class))));
        return new EconomyResponse(true, null, amount, credit.getAmount());
    }

    @Override
    public EconomyResponse withdrawPlayerBalance(MinecraftPlayer player, double amount) {
        Validate.notNull(player);
        return setPlayerBalance(player, getDefaultAccountCredit(player).getAmount()-amount);
    }

    @Override
    public EconomyResponse depositPlayerBalance(MinecraftPlayer player, double amount) {
        Validate.notNull(player);
        return setPlayerBalance(player, getDefaultAccountCredit(player).getAmount()+amount);
    }

    @Override
    public Collection<String> getBanks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean doesBankExist(String name) {
        Validate.notNull(name);
        return DKCoins.getInstance().getAccountManager().searchAccount(name) != null;
    }

    @Override
    public boolean createBank(String name) {
        Validate.notNull(name);
        DKCoins.getInstance().getAccountManager().createAccount(name, DKCoins.getInstance().getAccountManager().searchAccountType("Bank"),
                false, null, null);
        return false;
    }

    @Override
    public boolean createBank(String name, MinecraftPlayer owner) {
        Validate.notNull(name, owner);
        DKCoins.getInstance().getAccountManager().createAccount(name, DKCoins.getInstance().getAccountManager().searchAccountType("Bank"),
                false, null, owner.getAs(DKCoinsUser.class));
        return true;
    }

    @Override
    public boolean deleteBank(String name) {
        Validate.notNull(name);
        DKCoins.getInstance().getAccountManager().deleteAccount(DKCoins.getInstance().getAccountManager().searchAccount(name), null);
        return true;
    }

    @Override
    public double getBankBalance(String name) {
        Validate.notNull(name);
        return DKCoins.getInstance().getAccountManager().searchAccount(name).getCredit(DKCoinsConfig.ECONOMY_PROVIDER_CURRENCY).getAmount();
    }

    @Override
    public boolean hasBankBalance(String name, double amount) {
        Validate.notNull(name);
        return getBankBalance(name) >= amount;
    }

    @SuppressWarnings("unchecked")
    @Override
    public EconomyResponse setBankBalance(String name, double amount) {
        Validate.notNull(name);
        AccountCredit credit = getBankAccountCredit(name);
        credit.setAmount(amount, TransferCause.PROVIDER, Collections.EMPTY_LIST);
        return new EconomyResponse(true, null, amount, credit.getAmount());
    }

    @Override
    public EconomyResponse withdrawBankBalance(String name, double amount) {
        Validate.notNull(name);
        return setBankBalance(name, getBankBalance(name)-amount);
    }

    @Override
    public EconomyResponse depositBankBalance(String name, double amount) {
        Validate.notNull(name);
        return setBankBalance(name, getBankBalance(name)+amount);
    }

    @Override
    public boolean isBankOwner(String name, MinecraftPlayer owner) {
        Validate.notNull(name, owner);
        return DKCoins.getInstance().getAccountManager().searchAccount(name).getMember(owner.getAs(DKCoinsUser.class)).getRole() == AccountMemberRole.OWNER;
    }

    @Override
    public boolean isBankMember(String name, MinecraftPlayer member) {
        Validate.notNull(name, member);
        return DKCoins.getInstance().getAccountManager().searchAccount(name).isMember(member.getAs(DKCoinsUser.class));
    }

    private AccountCredit getDefaultAccountCredit(MinecraftPlayer player) {
        Validate.notNull(player);
        BankAccount account = player.getAs(DKCoinsUser.class).getDefaultAccount();
        Validate.notNull(account);
        return account.getCredit(DKCoinsConfig.ECONOMY_PROVIDER_CURRENCY);
    }

    private AccountCredit getBankAccountCredit(String name) {
        Validate.notNull(name);
        return DKCoins.getInstance().getAccountManager().searchAccount(name).getCredit(DKCoinsConfig.ECONOMY_PROVIDER_CURRENCY);
    }
}
