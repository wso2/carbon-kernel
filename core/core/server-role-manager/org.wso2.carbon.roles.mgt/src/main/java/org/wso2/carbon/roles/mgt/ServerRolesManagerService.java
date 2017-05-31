package org.wso2.carbon.roles.mgt;

public interface ServerRolesManagerService {

    /**
     * read server roles from registry
     *
     * @param serverRoleType - custom or default
     * @return server roles - server roles list
     * @throws Exception - if operation fails
     */
    String[] readServerRoles(String serverRoleType) throws Exception;

    /**
     * delete server role from the registry
     *
     * @param serverRoles    - names of the server roles
     * @param serverRoleType - custom or default
     * @return true is successful
     * @throws Exception - if operation fails
     */
    boolean removeServerRoles(String[] serverRoles, String serverRoleType) throws Exception;

    /**
     * put server role to the registry
     *
     * @param serverRoles    - names of the server role
     * @param serverRoleType - custom or default
     * @return true if successful
     * @throws Exception - if operation fails
     */
    boolean addServerRoles(String[] serverRoles, String serverRoleType) throws Exception;
}
