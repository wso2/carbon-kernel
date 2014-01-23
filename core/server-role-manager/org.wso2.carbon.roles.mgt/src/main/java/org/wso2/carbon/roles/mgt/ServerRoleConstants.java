package org.wso2.carbon.roles.mgt;

public final class ServerRoleConstants {
    private ServerRoleConstants(){}

    public static final String REG_BASE_PATH = "serverroles/";

    public static final String DEFAULT_ROLES_ID = "Default";
    public static final String CUSTOM_ROLES_ID = "Custom";
    public static final String DEFAULT_ROLES_PATH = REG_BASE_PATH.concat("default/");

    public static final String CUSTOM_ROLES_PATH = REG_BASE_PATH.concat("custom/");
    public static final java.lang.String CARBON_SERVER_ROLE = "ServerRoles.Role";

    public static final java.lang.String SERVER_ROLES_CMD_OPTION = "serverRoles";

    public static final String MODIFIED_TAG = "modified";
    public static final String MODIFIED_TAG_TRUE = "true";
    public static final String MODIFIED_TAG_FALSE = "false";
}
