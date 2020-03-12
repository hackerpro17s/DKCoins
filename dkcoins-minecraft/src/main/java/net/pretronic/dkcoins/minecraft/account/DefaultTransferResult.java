package net.pretronic.dkcoins.minecraft.account;

import net.pretronic.libraries.utility.map.caseintensive.CaseIntensiveHashMap;
import net.pretronic.libraries.utility.map.caseintensive.CaseIntensiveMap;
import net.pretronic.dkcoins.api.account.TransferResult;

public class DefaultTransferResult implements TransferResult {

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
}
