/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 29.01.20, 16:35
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.config;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.DKCoinsPlugin;
import net.pretronic.dkcoins.minecraft.commands.user.UserBankCommand;
import net.pretronic.dkcoins.minecraft.integration.labymod.LabyModServiceListener;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.configuration.DefaultCommandConfigurationBuilder;
import net.pretronic.libraries.document.annotations.DocumentIgnored;
import net.pretronic.libraries.document.annotations.DocumentKey;
import net.pretronic.libraries.plugin.service.ServicePriority;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.map.Maps;
import net.pretronic.libraries.utility.map.Pair;
import org.mcnative.runtime.api.McNative;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DKCoinsConfig {

    @DocumentKey("currency.default")
    public static String CURRENCY_DEFAULT0 = "Coins";

    @DocumentIgnored
    public static Currency CURRENCY_DEFAULT = null;

    @DocumentKey("currency.format.pattern")//currency.format.separator: ###,###.##
    public static String CURRENCY_FORMAT0 = "###,###.##";

    @DocumentIgnored
    private static DecimalFormat CURRENCY_FORMAT = null;

    @DocumentKey("currency.format.separator.grouping")
    public static char CURRENCY_FORMAT_SEPARATOR_GROUPING = '\'';

    @DocumentKey("currency.format.separator.decimal")
    public static char CURRENCY_FORMAT_SEPARATOR_DECIMAL = '.';


    @DocumentKey("date.format.pattern")
    public static String DATE_FORMAT0 = "dd-MM-yyyy HH:mm";

    @DocumentIgnored
    public static SimpleDateFormat DATE_FORMAT = null;


    @DocumentKey("account.type.startAmount")
    public static Map<String, Integer> ACCOUNT_TYPE_START_AMOUNT0 = Maps.ofValues(new Pair<>("User", 1000), new Pair<>("Bank", 0));

    @DocumentIgnored
    public static Map<AccountType, Integer> ACCOUNT_TYPE_START_AMOUNT = new HashMap<>();

    @DocumentKey("account.user.creditAliases")
    public static CreditAlias[] ACCOUNT_USER_CREDIT_ALIASES = new CreditAlias[]{new CreditAlias("Coins",
            "dkcoins.command.coins",
            "dkcoins.command.coins.other",
            new String[]{"coins", "money"},
            new String[0])};

    @DocumentKey("economyProvider.enabled")
    public static boolean ECONOMY_PROVIDER_ENABLED = true;

    @DocumentKey("economyProvider.priority")
    public static byte ECONOMY_PROVIDER_PRIORITY = ServicePriority.HIGHEST;

    @DocumentKey("economyProvider.currency")
    public static String ECONOMY_PROVIDER_CURRENCY0 = CURRENCY_DEFAULT0;

    @DocumentIgnored
    public static Currency ECONOMY_PROVIDER_CURRENCY = null;


    @DocumentKey("account.payment.minimum.user")
    public static double MINIMUM_PAYMENT_USER = 0.01;

    @DocumentKey("account.payment.minimum.all")
    public static double MINIMUM_PAYMENT_ALL = 1;

    public static String[] PAYMENT_ALL_ALIASES = new String[]{"*"};


    @DocumentKey("command.top.entriesPerPage")
    public static int TOP_LIMIT_ENTRIES_PER_PAGE = 5;


    public static boolean LABYMOD_BALANCE_CASH_ENABLED = true;
    @DocumentKey("labymod.balance.cash.currency")
    private static String LABYMOD_BALANCE_CASH_CURRENCY0 = CURRENCY_DEFAULT0;
    @DocumentIgnored
    public static Currency LABYMOD_BALANCE_CASH_CURRENCY = null;

    public static boolean LABYMOD_BALANCE_BANK_ENABLED = false;
    @DocumentKey("labymod.balance.bank.currency")
    public static String LABYMOD_BALANCE_BANK_CURRENCY0 = CURRENCY_DEFAULT0;
    @DocumentIgnored
    public static Currency LABYMOD_BALANCE_BANK_CURRENCY = null;

    @DocumentKey("command.bank")
    public static CommandConfiguration COMMAND_BANK = CommandConfiguration.newBuilder()
            .name("bank")
            .enabled(McNative.getInstance().getPlatform().isService())
            .permission("dkcoins.command.bank")
            .create();

    @DocumentKey("command.currency")
    public static CommandConfiguration COMMAND_CURRENCY = CommandConfiguration.newBuilder()
            .name("currency")
            .enabled(McNative.getInstance().getPlatform().isService())
            .permission("dkcoins.command.currency")
            .create();

    @DocumentKey("command.pay")
    public static CommandConfiguration COMMAND_PAY = CommandConfiguration.newBuilder()
            .name("pay")
            .aliases("transfer")
            .enabled(McNative.getInstance().getPlatform().isService())
            .create();


    public static String PERMISSIONS_ADMIN = "dkcoins.admin";

    public static void init() {
        CURRENCY_DEFAULT = DKCoins.getInstance().getCurrencyManager().searchCurrency(CURRENCY_DEFAULT0);
        if(CURRENCY_DEFAULT == null) throw new IllegalArgumentException("Can't match default currency " + CURRENCY_DEFAULT0 + ". It may not exists");
        CURRENCY_FORMAT = new DecimalFormat(CURRENCY_FORMAT0);

        DecimalFormatSymbols formatSymbols = CURRENCY_FORMAT.getDecimalFormatSymbols();
        formatSymbols.setGroupingSeparator(CURRENCY_FORMAT_SEPARATOR_GROUPING);
        formatSymbols.setDecimalSeparator(CURRENCY_FORMAT_SEPARATOR_DECIMAL);
        CURRENCY_FORMAT.setDecimalFormatSymbols(formatSymbols);

        ACCOUNT_TYPE_START_AMOUNT0.forEach((type, startAmount) ->
                ACCOUNT_TYPE_START_AMOUNT.put(DKCoins.getInstance().getAccountManager().searchAccountType(type), startAmount));

        for (CreditAlias creditAlias : ACCOUNT_USER_CREDIT_ALIASES) {
            if(creditAlias.getCommands().length == 0) {
                McNative.getInstance().getLogger().warn("Credit command can't be registered. Commands list is empty.");
                continue;
            }
            DefaultCommandConfigurationBuilder commandConfigurationBuilder = CommandConfiguration.newBuilder();
            commandConfigurationBuilder.name(creditAlias.getCommands()[0]);
            if(creditAlias.getCommands().length > 1) {
                commandConfigurationBuilder.aliases(Arrays.copyOfRange(creditAlias.getCommands(), 1, creditAlias.getCommands().length));
            }
            commandConfigurationBuilder.permission(creditAlias.getPermission());

            McNative.getInstance().getLocal().getCommandManager().registerCommand(new UserBankCommand(DKCoinsPlugin.getInstance(),
                    commandConfigurationBuilder.create(), creditAlias));
        }

        ECONOMY_PROVIDER_CURRENCY = DKCoins.getInstance().getCurrencyManager().searchCurrency(ECONOMY_PROVIDER_CURRENCY0);
        if(ECONOMY_PROVIDER_CURRENCY == null){
            DKCoins.getInstance().getLogger().error(ECONOMY_PROVIDER_CURRENCY0+" is not available as currency");
        }

        DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT0);

        LABYMOD_BALANCE_BANK_CURRENCY = DKCoins.getInstance().getCurrencyManager().getCurrency(LABYMOD_BALANCE_BANK_CURRENCY0);
        LABYMOD_BALANCE_CASH_CURRENCY = DKCoins.getInstance().getCurrencyManager().getCurrency(LABYMOD_BALANCE_CASH_CURRENCY0);

        if(LABYMOD_BALANCE_BANK_ENABLED || LABYMOD_BALANCE_CASH_ENABLED) {
            McNative.getInstance().getLocal().getEventBus().subscribe(DKCoinsPlugin.getInstance(), new LabyModServiceListener());
        }
    }

    public static String formatCurrencyAmount(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatTime(long time) {
        return DATE_FORMAT.format(time);
    }

    public static double getAccountTypeStartAmount(AccountType type) {
        Validate.notNull(type);
        for (Map.Entry<AccountType, Integer> entry : ACCOUNT_TYPE_START_AMOUNT.entrySet()) {
            if(entry.getKey().equals(type)) return entry.getValue();
        }
        return 0.0D;
    }

    public static boolean isPaymentAllAlias(String value) {
        for (String alias : PAYMENT_ALL_ALIASES) {
            if(alias.equalsIgnoreCase(value)) return true;
        }
        return false;
    }
}
