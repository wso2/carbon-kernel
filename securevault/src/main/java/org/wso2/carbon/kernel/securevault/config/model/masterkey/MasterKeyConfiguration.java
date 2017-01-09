package org.wso2.carbon.kernel.securevault.config.model.masterkey;

import java.util.Properties;

/**
 * MasterKeyConfiguration class holds static configuration parameters specified in the master-keys.yaml file.
 *
 * @since 5.2.0
 */
public class MasterKeyConfiguration {
    private boolean permanent = false;
    private Properties masterKeys = new Properties();
    private String relocation = "";

    public boolean isPermanent() {
        return permanent;
    }

    public Properties getMasterKeys() {
        return masterKeys;
    }

    public String getRelocation() {
        return relocation;
    }
}
