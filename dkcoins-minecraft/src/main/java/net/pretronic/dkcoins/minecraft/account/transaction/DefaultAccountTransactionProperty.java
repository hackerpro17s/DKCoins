package net.pretronic.dkcoins.minecraft.account.transaction;

import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.libraries.utility.Convert;

public class DefaultAccountTransactionProperty implements AccountTransactionProperty {

    private final String key;
    private final Object value;

    public DefaultAccountTransactionProperty(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public Object asObject() {
        return this.value;
    }

    @Override
    public String asString() {
        return Convert.toString(value);
    }

    @Override
    public int asInt() {
        return Convert.toInteger(value);
    }

    @Override
    public long asLong() {
        return Convert.toLong(value);
    }

    @Override
    public double asDouble() {
        return Convert.toDouble(value);
    }

    @Override
    public float asFloat() {
        return Convert.toFloat(value);
    }

    @Override
    public byte asByte() {
        return Convert.toByte(value);
    }
}
