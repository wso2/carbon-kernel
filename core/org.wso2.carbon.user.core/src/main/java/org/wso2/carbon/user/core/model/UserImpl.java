package org.wso2.carbon.user.core.model;

import org.wso2.carbon.user.api.User;

/**
 * Default implementation for the user.
 */
public class UserImpl implements User {

    private String username;
    private String userId;

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
