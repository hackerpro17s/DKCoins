/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 14:37
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.account;

import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.libraries.utility.annonations.Nullable;

public interface AccountLimitation {

    int getId();

    BankAccount getAccount();

    @Nullable
    AccountMember getMember();

    @Nullable
    AccountMemberRole getMemberRole();

    Currency getComparativeCurrency();

    CalculationType getCalculationType();

    double getAmount();

    String getFormattedAmount();

    Interval getInterval();


    enum CalculationType {

        GLOBAL,
        USER_BASED;

        public static CalculationType parse(String value) {
            for (CalculationType calculationType : values()) {
                if(calculationType.name().equalsIgnoreCase(value)) return calculationType;
            }
            return null;
        }
    }

    enum Interval {

        DAILY,
        WEEKLY,
        MONTHLY;

        public static Interval parse(String value) {
            for (Interval interval : values()) {
                if(interval.name().equalsIgnoreCase(value)) return interval;
            }
            return null;
        }
    }
}
