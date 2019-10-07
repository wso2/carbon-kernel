package org.wso2.carbon.user.core.model;

import org.wso2.carbon.user.core.config.UserStorePreferenceOrderSupplier;

import java.util.List;

/**
 * This is a model class which uses to keep the context information of user management.
 */
public class UserMgtContext {

    private UserStorePreferenceOrderSupplier<List<String>> userStorePreferenceOrderSupplier;

    public UserStorePreferenceOrderSupplier<List<String>> getUserStorePreferenceOrderSupplier() {

        return userStorePreferenceOrderSupplier;
    }

    public void setUserStorePreferenceOrderSupplier(UserStorePreferenceOrderSupplier<List<String>> userStorePreferenceOrderSupplier) {

        this.userStorePreferenceOrderSupplier = userStorePreferenceOrderSupplier;
    }
}
