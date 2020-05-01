package net.pretronic.dkcoins.minecraft.account;

import net.pretronic.dkcoins.api.account.TransferResult;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.map.caseintensive.CaseIntensiveHashMap;
import net.pretronic.libraries.utility.map.caseintensive.CaseIntensiveMap;

public class DefaultTransferResult implements TransferResult {

    private AccountTransaction transaction;
    private final FailCause failCause;
    private final CaseIntensiveMap<Object> properties;

    public DefaultTransferResult(FailCause failCause, CaseIntensiveMap<Object> properties) {
        this.failCause = failCause;
        this.properties = properties;
    }

    public DefaultTransferResult(FailCause failCause) {
        this.failCause = failCause;
        this.properties = new CaseIntensiveHashMap<>();
    }

    @Override
    public FailCause getFailCause() {
        return this.failCause;
    }

    @Override
    public AccountTransaction getTransaction() {
        return this.transaction;
    }

    @Override
    public CaseIntensiveMap<Object> getProperties() {
        return this.properties;
    }

    @Override
    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(Class<T> propertyClass, String key) {
        return (T) getProperty(key);
    }

    public DefaultTransferResult addProperty(String key, Object value) {
        this.properties.put(key, value);
        return this;
    }

    @Internal
    public void setTransaction(AccountTransaction transaction) {
        this.transaction = transaction;
    }
}
