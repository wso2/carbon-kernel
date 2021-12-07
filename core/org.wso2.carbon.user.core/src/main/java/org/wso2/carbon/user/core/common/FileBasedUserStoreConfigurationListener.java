package org.wso2.carbon.user.core.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.listener.UserStoreConfigurationListener;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileBasedUserStoreConfigurationListener implements UserStoreConfigurationListener {

    private static final Log LOG = LogFactory.getLog(FileBasedUserStoreConfigurationListener.class);

    @Override
    public int getExecutionOrderId() {

        return 1;
    }

    @Override
    public boolean canExecutable() {

        return true;
    }

    @Override
    public RealmConfiguration[] getSecondaryUserStoreRealmConfigurations(int tenantId) {

        String configPath = CarbonUtils.getCarbonTenantsDirPath() +
                File.separator + tenantId + File.separator + "userstores";
        File userStores = new File(configPath);
        UserStoreDeploymentManager userStoreDeploymentManager = new UserStoreDeploymentManager();
        List<RealmConfiguration> realmConfigurations = new ArrayList<>();

        File[] files = userStores.listFiles((userStores1, name) -> name.toLowerCase().endsWith(".xml"));
        if (files != null) {
            for (File file : files) {
                try {
                    RealmConfiguration newRealmConfig = userStoreDeploymentManager.
                            getUserStoreConfiguration(file.getAbsolutePath());
                    if (newRealmConfig != null) {
                        realmConfigurations.add(newRealmConfig);
                    } else {
                        LOG.error("Error while creating realm configuration from file " + file.getAbsolutePath());
                    }
                } catch (UserStoreException e) {
                    LOG.error("Error while creating realm configuration from file " + file.getAbsolutePath(), e);
                }
            }
        }
        return realmConfigurations.toArray(new RealmConfiguration[0]);
    }
}
