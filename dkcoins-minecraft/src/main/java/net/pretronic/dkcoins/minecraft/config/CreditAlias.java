package net.pretronic.dkcoins.minecraft.config;

public class CreditAlias {

    private final String currency;
    private final String permission;
    private final String[] commands;
    private final String[] disabledWorlds;

    public CreditAlias(String currency, String permission, String[] commands, String[] disabledWorlds) {
        this.currency = currency;
        this.permission = permission;
        this.commands = commands;
        this.disabledWorlds = disabledWorlds;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPermission() {
        return permission;
    }

    public String[] getCommands() {
        return commands;
    }

    public String[] getDisabledWorlds() {
        return disabledWorlds;
    }
}