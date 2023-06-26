package org.wso2.carbon.security.keystore.dao;

import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.security.keystore.model.KeyStoreModel;

import java.util.List;
import java.util.Optional;

public abstract class KeyStoreDAO {

    // TODO: check whether converting this to a protected variable is better.
    private final int tenantId;

    public KeyStoreDAO(int tenantId) {
        this.tenantId = tenantId;
    }

    public abstract void addKeyStore(KeyStoreModel keyStoreModel) throws
            SecurityConfigException;

    // TODO: think whether we need a method to see existence of a key store.

    public abstract List<KeyStoreModel> getKeyStores() throws SecurityConfigException;

    public abstract Optional<KeyStoreModel> getKeyStore(String fileName) throws SecurityConfigException;

    public abstract void deleteKeyStore(String fileName) throws SecurityConfigException;

    public abstract void updateKeyStore(KeyStoreModel keyStoreModel) throws SecurityConfigException;

    public abstract void addPubCertIdToKeyStore(String fileName, String pubCertId) throws SecurityConfigException;

    public abstract Optional<String> getPubCertIdFromKeyStore(String fileName) throws SecurityConfigException;

    public int getTenantId() {

        return tenantId;
    }


}
