package net.pretronic.dkcoins.minecraft;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.libraries.utility.GeneralUtil;
import org.mcnative.common.player.MinecraftPlayer;
import org.mcnative.common.serviceprovider.placeholder.PlaceholderHook;

public class DKCoinsPlaceholderHook implements PlaceholderHook {

    /*
    Currency: Sonst default

    Direct player:
    dkcoins_balance_[currency]

    Specific player:
    dkcoins_player_(player)_balance_[currency]

    Top:
    dkcoins_top_(rank)_name_[currency]
    dkcoins_top_(rank)_balance_[currency]

    Bank:
    dkcoins_bank_(bank)_balance_[currency]
     */

    @Override
    public Object onRequest(MinecraftPlayer player, String parameter) {
        String[] parameters = parameter.toLowerCase().split("_");
        switch (parameters[0].toLowerCase()) {
            case "balance": {
                Currency currency = parseCurrency(parameters,1);
                DKCoinsUser user = player.getAs(DKCoinsUser.class);
                return user.getDefaultAccount().getCredit(currency).getAmount();
            }
            case "player": {
                DKCoinsUser user;
                if(parameter.length() > 2) {
                    user = DKCoins.getInstance().getUserManager().getUser(parameters[1]);
                    if(user != null) {
                        switch (parameters[2].toLowerCase()) {
                            case "balance": {
                                Currency currency = parseCurrency(parameters, 3);
                                return user.getDefaultAccount().getCredit(currency).getAmount();
                            }
                        }
                    }
                }
                break;
            }
            case "top:": {
                if(parameters.length > 2) {
                    String rank0 = parameters[1];
                    if(GeneralUtil.isNaturalNumber(rank0)) {
                        int rank = Integer.parseInt(rank0);
                        Currency currency = parseCurrency(parameters, 3);
                        switch (parameters[2].toLowerCase()) {
                            case "name": {
                                BankAccount account = DKCoins.getInstance().getAccountManager().getAccountByRank(currency, rank);
                                return account.getName();
                            }
                            case "balance": {
                                BankAccount account = DKCoins.getInstance().getAccountManager().getAccountByRank(currency, rank);
                                return account.getCredit(currency).getAmount();
                            }
                        }
                    }
                }
                break;
            }
            case "bank": {
                if(parameters.length > 2) {
                    String bank0 = parameters[1];
                    BankAccount account = DKCoins.getInstance().getAccountManager().searchAccount(bank0);
                    if(account != null) {
                        switch (parameters[2].toLowerCase()) {
                            case "balance": {
                                Currency currency = parseCurrency(parameters, 3);
                                return account.getCredit(currency).getAmount();
                            }
                        }
                    }
                }
                break;
            }
        }
        return null;
    }

    private Currency parseCurrency(String[] parameters, int currencyPlace) {
        Currency currency;
        if(parameters.length > currencyPlace) {
            String currency0 = parameters[currencyPlace];
            currency = DKCoins.getInstance().getCurrencyManager().searchCurrency(currency0);
        } else {
            currency = DKCoinsConfig.CURRENCY_DEFAULT;
        }
        return currency;
    }
}
