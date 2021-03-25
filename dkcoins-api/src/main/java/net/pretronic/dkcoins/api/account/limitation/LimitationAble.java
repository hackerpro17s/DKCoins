package net.pretronic.dkcoins.api.account.limitation;

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.currency.Currency;

import java.util.Collection;

public interface LimitationAble {

    BankAccount getAccount();

    Collection<AccountLimitation> getLimitations();

    AccountLimitation getLimitation(Currency comparativeCurrency, AccountLimitationCalculationType calculationType, double amount, AccountLimitationInterval interval);

    boolean hasLimitation(Currency currency, double amount);

    AccountLimitation addLimitation(Currency comparativeCurrency, AccountLimitationCalculationType calculationType, double amount, AccountLimitationInterval interval);

    boolean removeLimitation(AccountLimitation limitation);

    default boolean removeLimitation(Currency comparativeCurrency, AccountLimitationCalculationType calculationType, double amount, AccountLimitationInterval interval) {
        return removeLimitation(getLimitation(comparativeCurrency, calculationType, amount, interval));
    }
}
