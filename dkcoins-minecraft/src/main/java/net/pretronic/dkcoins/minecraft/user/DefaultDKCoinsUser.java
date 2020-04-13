/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 18.11.19, 21:19
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.user;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import org.mcnative.common.McNative;
import org.mcnative.common.player.MinecraftPlayer;

import java.util.Collection;
import java.util.UUID;

public class DefaultDKCoinsUser implements DKCoinsUser {

    private final UUID uniqueId;
    private boolean userAccountsLoaded;

    public DefaultDKCoinsUser(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.userAccountsLoaded = false;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public String getName() {
        MinecraftPlayer player = getAsPlayer();
        if(player == null) {
            DKCoins.getInstance().getLogger().warn("Player with uuid [{}] is not in McNative registered", uniqueId);
            return null;
        }
        return player.getName();
    }

    @Override
    public String getDisplayName() {
        MinecraftPlayer player = getAsPlayer();
        if(player == null) {
            DKCoins.getInstance().getLogger().warn("Player with uuid [{}] is not in McNative registered", uniqueId);
            return null;
        }
        return player.getDisplayName();
    }

    @Override
    public Collection<BankAccount> getAccounts() {
        return DKCoins.getInstance().getAccountManager().getAccounts(this);
    }

    @Override
    public BankAccount getDefaultAccount() {
        MinecraftPlayer player = getAsPlayer();
        if(player == null) {
            DKCoins.getInstance().getLogger().warn("Player with uuid [{}] is not in McNative registered", uniqueId);
            return null;
        }
        AccountType accountType = DKCoins.getInstance().getAccountManager().searchAccountType("User");
        return DKCoins.getInstance().getAccountManager().getAccount(player.getName(), accountType);
    }

    @Override
    public AccountMember getAsMember(BankAccount account) {
        return account.getMember(this);
    }

    public boolean isUserAccountsLoaded() {
        return userAccountsLoaded;
    }

    public DefaultDKCoinsUser setUserAccountsLoaded(boolean userAccountsLoaded) {
        this.userAccountsLoaded = userAccountsLoaded;
        return this;
    }

    private MinecraftPlayer getAsPlayer() {
        return McNative.getInstance().getPlayerManager().getPlayer(getUniqueId());
    }

    public BankAccount initAccount() {
        MinecraftPlayer player = getAsPlayer();
        if(player == null) {
            DKCoins.getInstance().getLogger().warn("Player with uuid [{}] is not in McNative registered", uniqueId);
            return null;
        }

        AccountType accountType = DKCoins.getInstance().getAccountManager().searchAccountType("User");
        BankAccount account = DKCoins.getInstance().getAccountManager().getAccount(player.getName(), accountType);
        if(account == null) {
            account = DKCoins.getInstance().getAccountManager().createAccount(player.getName(),
                    accountType, false, null, this);
        }
        return account;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DKCoinsUser && ((DKCoinsUser)obj).getUniqueId().equals(getUniqueId());
    }
}
