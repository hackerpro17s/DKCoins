package net.pretronic.dkcoins.api.account;

import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.libraries.utility.map.caseintensive.CaseIntensiveMap;

public interface TransferResult {

    default boolean isSuccess() {
        return getFailCause() == null;
    }

    default boolean isFailed() {
        return getFailCause() != null;
    }

    FailCause getFailCause();

    AccountTransaction getTransaction();

    CaseIntensiveMap<Object> getProperties();

    Object getProperty(String key);

    <T> T getProperty(Class<T> propertyClass, String key);

    enum FailCause {

        NOT_ENOUGH_AMOUNT,
        NOT_ENOUGH_ACCESS_RIGHTS,
        MASTER_ACCOUNT_NOT_ENOUGH_AMOUNT,
        LIMIT,
        TRANSFER_DISABLED
    }
}
