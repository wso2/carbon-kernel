package org.wso2.carbon.security.keystore.dao;

import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.security.keystore.model.PubCertModel;

import java.util.Optional;

public abstract class PubCertDAO {

    private final int tenantId;

    public PubCertDAO(int tenantId) {
        this.tenantId = tenantId;
    }

    public abstract String addPubCert(PubCertModel pubCertModel) throws SecurityConfigException;

    public abstract Optional<PubCertModel> getPubCert(String uuid) throws SecurityConfigException;

    public int getTenantId() {

        return tenantId;
    }
}
