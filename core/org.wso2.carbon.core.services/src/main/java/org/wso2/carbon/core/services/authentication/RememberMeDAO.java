package org.wso2.carbon.core.services.authentication;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;

public class RememberMeDAO {

    private static final Log log = LogFactory.getLog(RememberMeDAO.class);

    private Registry registry;

    public RememberMeDAO(Registry registry) {
        this.registry = registry;
    }

    public void updateToken(String userName, String value) throws Exception {
        Collection userResource = null;
        boolean transactionStarted = Transaction.isStarted();

        try {
            if (!registry.resourceExists(RegistryConstants.PROFILES_PATH + userName)) {
                userResource = registry.newCollection();
                registry.put(RegistryConstants.PROFILES_PATH + userName, userResource);
            } else {
                userResource = (Collection) registry
                        .get(RegistryConstants.PROFILES_PATH + userName);
            }

            if (!transactionStarted) {
                registry.beginTransaction();
            }

            userResource.removeProperty("RememberMeToken");
            userResource.setProperty("RememberMeToken", value);
            registry.put(RegistryConstants.PROFILES_PATH + userName, userResource);

            if (!transactionStarted) {
                registry.commitTransaction();
            }
        } catch (Exception ex) {
            if (!transactionStarted) {
                try {
                    registry.rollbackTransaction();
                } catch (RegistryException e) {
                    // log.error("Error occured while updating remember me token",
                    // e);
                    throw new Exception("Error occured while updating remember me token", e);
                }
            }
        }
    }

    public String getToken(String userName) throws Exception {
        Collection userResource = null;
        String value = null;

        try {
            if (!registry.resourceExists(RegistryConstants.PROFILES_PATH + userName)) {
                return null;
            } else {
                userResource = (Collection) registry
                        .get(RegistryConstants.PROFILES_PATH + userName);
            }

            value = userResource.getProperty("RememberMeToken");

        } catch (Exception e) {
            log.error("Error occured while updating remember me token", e);
            throw new RegistryException("Error occured while updating remember me token", e);
        }

        return value;
    }

}
