package org.wso2.carbon.user.core.model;

import org.wso2.carbon.user.api.User;

/**
 * Default implementation for the user.
 */
public class UserImpl implements User {

    private String username;
    private String userId;

    public UserImpl() {
        super();
    }

    public UserImpl(String id, String username) {
        this.userId = id;
        this.username = username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getId() {
        return userId;
    }

    @Override
    public String getName() {
        return username;
    }
}
