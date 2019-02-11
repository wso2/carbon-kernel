package org.wso2.carbon.roles.mgt;

public class ServerRolesException extends Exception {
    public ServerRolesException(String message, Exception e) {
        super(message, e);
    }

    public ServerRolesException(Exception e) {
        super(e);
    }

    public ServerRolesException(String message) {
        super(message);
    }
}
