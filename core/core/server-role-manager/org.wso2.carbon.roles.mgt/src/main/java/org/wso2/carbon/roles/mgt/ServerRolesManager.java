package org.wso2.carbon.roles.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import java.util.List;

public class ServerRolesManager extends AbstractAdmin implements ServerRolesManagerService {

    private static final Log log = LogFactory.getLog(ServerRolesManager.class);

    //todo - if did an add or remove before a read, product server-roles won't get read

    /**
     * read server roles from registry
     *
     * @param serverRoleType - custom or default
     * @return server roles - server roles list
     * @throws ServerRolesException - if operation fails
     */
    public String[] readServerRoles(String serverRoleType) throws ServerRolesException {
        log.debug("Reading " + serverRoleType + " Server-Roles from Registry.");

        Registry configReg = getConfigSystemRegistry();
        String regPath = this.getRegistryPath(serverRoleType);
        List<String> serverRolesList;
        Resource resource = this.getResourceFromRegistry(configReg, regPath);

        if (ServerRoleConstants.DEFAULT_ROLES_ID.equals(serverRoleType)) {
            String modified;
            List<String> productServerRolesList = ServerRoleUtils.readProductServerRoles();

            if (resource == null) {
                try {
                    resource = configReg.newResource();
                    resource.setProperty(serverRoleType, productServerRolesList);
                    resource.setProperty(ServerRoleConstants.MODIFIED_TAG,
                            ServerRoleConstants.MODIFIED_TAG_FALSE);
                    this.putResourceToRegistry(configReg, resource, regPath);
                } catch (RegistryException e) {
                    this.handleException(e.getMessage(), e);
                }
            } else {
                modified = resource.getProperty(ServerRoleConstants.MODIFIED_TAG);
                if (modified == null || ServerRoleConstants.MODIFIED_TAG_FALSE.equals(modified)) {
                    resource.setProperty(serverRoleType, productServerRolesList);
                    resource.setProperty(ServerRoleConstants.MODIFIED_TAG,
                            ServerRoleConstants.MODIFIED_TAG_FALSE);
                    this.putResourceToRegistry(configReg, resource, regPath);
                }
            }
        }

        String[] serverRolesArray = null;
        if (resource != null) {
            serverRolesList = resource.getPropertyValues(serverRoleType);
            serverRolesArray = ServerRoleUtils.listToArray(serverRolesList);
        }
        if (serverRolesArray == null) {
            serverRolesArray = new String[0];
        }

        return serverRolesArray;
    }

    /**
     * delete server role from the registry
     *
     * @param serverRolesArray - names of the server roles
     * @param serverRoleType   - custom or default
     * @return true is successful
     * @throws ServerRolesException - if operation fails
     */
    public boolean removeServerRoles(String[] serverRolesArray, String serverRoleType)
            throws ServerRolesException {
        log.debug("Removing " + serverRoleType + " Server-Roles from Registry.");
        boolean status = false;
        Registry configReg = getConfigSystemRegistry();
        String regPath = this.getRegistryPath(serverRoleType);

        List<String> serverRolesListToRemove = ServerRoleUtils.arrayToList(serverRolesArray);
        if ((serverRolesArray != null) && (serverRolesArray.length != 0)) {
            Resource resource = this.getResourceFromRegistry(configReg, regPath);

            List<String> serverRolesList = resource.getPropertyValues(serverRoleType);

            if ((serverRolesList != null) && !serverRolesList.isEmpty()) {
                boolean isRemoved = serverRolesList.removeAll(serverRolesListToRemove);

                if (isRemoved) {
                    resource.setProperty(serverRoleType, serverRolesList);

                    if (ServerRoleConstants.DEFAULT_ROLES_ID.equals(serverRoleType)) {
                        resource.setProperty(ServerRoleConstants.MODIFIED_TAG,
                                ServerRoleConstants.MODIFIED_TAG_TRUE);
                    }
                    this.putResourceToRegistry(configReg, resource, regPath);
                    status = true;
                }
            }
        }
        return status;
    }

    /**
     * put server role to the registry
     *
     * @param serverRolesArray - names of the server role
     * @param serverRoleType   - custom or default TODO: this is always custom, refactor
     * @return true if successful
     * @throws ServerRolesException - if operation fails
     */
    public boolean addServerRoles(String[] serverRolesArray, String serverRoleType)
            throws ServerRolesException {
        log.debug("Adding " + serverRoleType + " Server-Roles to Registry.");

        boolean status = false;
        Registry configReg = getConfigSystemRegistry();
        String regPath = this.getRegistryPath(serverRoleType);

        if (serverRolesArray != null && serverRolesArray.length != 0) {
            List<String> serverRolesListToAdd = ServerRoleUtils.arrayToList(serverRolesArray);

            Resource resource = this.getResourceFromRegistry(configReg, regPath);
            if (resource == null) {
                try {
                    resource = configReg.newResource();
                    resource.setProperty(serverRoleType, serverRolesListToAdd);
                } catch (RegistryException e) {
                    this.handleException(e.getMessage(), e);
                }
            } else {
                List<String> serverRolesList = resource.getPropertyValues(serverRoleType);
                //todo manage duplicates - done(used Sets)
                serverRolesList = ServerRoleUtils.mergeLists(serverRolesList, serverRolesListToAdd);
                resource.setProperty(serverRoleType, serverRolesList);
                status = true;
            }
            putResourceToRegistry(configReg, resource, regPath);

            // We have to update the modified flag in any case.. serverRoleType above
            // is always custom.
            String defaultPath = getRegistryPath(ServerRoleConstants.DEFAULT_ROLES_ID);
            Resource defaultResource = getResourceFromRegistry(configReg, defaultPath);
            if (defaultResource != null) {
                defaultResource.setProperty(ServerRoleConstants.MODIFIED_TAG,
                        ServerRoleConstants.MODIFIED_TAG_TRUE);
            }
            putResourceToRegistry(configReg, defaultResource, defaultPath);
        }
        return status;
    }

    /**
     * @param configReg       Configuration Registry Instance
     * @param serverRolesPath - path to resource
     * @return List of server roles, will return null if the resource is empty.
     * @throws ServerRolesException - Error doing the registry.get
     */
    private Resource getResourceFromRegistry(Registry configReg, String serverRolesPath)
            throws ServerRolesException {
        Resource serverRolesResource = null;
        try {
            if (configReg.resourceExists(serverRolesPath)) {
                serverRolesResource = configReg.get(serverRolesPath);
            }
        } catch (RegistryException e) {
            this.handleException(e.getMessage(), e);
        }
        return serverRolesResource;
    }

    private void putResourceToRegistry(Registry configReg, Resource serverRolesResource,
                                       String serverRolesPath) throws ServerRolesException {
        try {
            if (configReg.resourceExists(serverRolesPath)) {
                configReg.delete(serverRolesPath);
            }
            configReg.put(serverRolesPath, serverRolesResource);
        } catch (RegistryException e) {
            this.handleException(e.getMessage(), e);
        }
    }

    private void handleException(String message, Exception e) throws ServerRolesException {
        log.error(message, e);
        throw new ServerRolesException(message, e);
    }

    private void handleException(String message) throws ServerRolesException {
        log.error(message);
        throw new ServerRolesException(message);
    }

    private String getRegistryPath(String serverRoleType) throws ServerRolesException {
        String path = ServerRoleUtils.getRegistryPath(serverRoleType);
        if (path == null) {
            this.handleException("Undefined Server Roles Type.");
        }
        return path;
    }
}
