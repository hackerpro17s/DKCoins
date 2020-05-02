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