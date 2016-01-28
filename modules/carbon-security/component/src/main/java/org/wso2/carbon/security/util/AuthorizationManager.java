package org.wso2.carbon.security.util;

import org.wso2.carbon.security.jaas.CarbonPermission;

import java.util.HashMap;
import java.util.Map;

/**
 * In memory authorization manager.
 */
public class AuthorizationManager {

    private static Map<String, CarbonPermission[]> permissionStore = new HashMap<>();

    private static AuthorizationManager instance = new AuthorizationManager();

    private AuthorizationManager() {

        permissionStore.put("admin", new CarbonPermission[]{
                new CarbonPermission("/permission/stock/quote", "create, read")});
    }

    public static AuthorizationManager getInstance() {
        return instance;
    }

    /**
     * Checks whether a given principal is authorized against a provided permission.
     *
     * @param principalName Name of the principal.
     * @param requiredPermission Permission which the principal should be checked against.
     * @return
     */
    public boolean authorizePrincipal(String principalName, CarbonPermission requiredPermission) {

        if (principalName != null && !principalName.isEmpty()) {

            CarbonPermission[] carbonPermissions = permissionStore.get(principalName);
            if (carbonPermissions == null) {
                return false;
            }

            for (CarbonPermission carbonPermission : carbonPermissions) {
                if (carbonPermission.getName().equals(requiredPermission.getName())) {
                    return true;
                    //TODO actions are ignored temporally
                }
            }
        }

        return false;
    }

}
