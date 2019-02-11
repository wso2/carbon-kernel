package org.wso2.carbon.roles.mgt;

import org.wso2.carbon.base.ServerConfiguration;

import java.util.*;

public final class ServerRoleUtils {

    private ServerRoleUtils() {
    }

    /**
     * First checks for the "serverRoles" system property. If null, reads the ServerRoles property
     * from carbon.xml.
     *
     * @return server roles List, null if there are no roles defined/
     */
    public static List<String> readProductServerRoles() {
        String[] serverRoles;
        // read the system property
        String temp = System.getProperty(ServerRoleConstants.SERVER_ROLES_CMD_OPTION);
        if (temp != null) {
            serverRoles = temp.split(",");
        } else {
            // now try to read from carbon.xml
            ServerConfiguration serverConfig = ServerConfiguration.getInstance();
            serverRoles = serverConfig.getProperties(ServerRoleConstants.CARBON_SERVER_ROLE);
        }
        return ServerRoleUtils.arrayToList(serverRoles);
    }

    public static String[] listToArray(List<String> list) {
        if ((list != null && !list.isEmpty())) {
            String[] array = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = list.get(i);
            }
            return array;
        } else {
            return null;
        }
    }

    public static List<String> arrayToList(String[] array) {
        if ((array != null) && (array.length != 0)) {
            return Arrays.asList(array);
        } else {
            return null;
        }
    }

    /*public static List<String> removeDuplicates(List<String> targetList, List<String> itemsList) {
        List<String> returnList = null;

        if (targetList != null && !targetList.isEmpty()) {
            if ((itemsList != null) && (!itemsList.isEmpty())) {

                Set<String> targetSet = new HashSet<String>(targetList);
                targetSet.removeAll(itemsList);
                returnList = new ArrayList<String>(targetSet);
            } else {
                returnList = targetList;
            }
        }
        return returnList;
    }*/

    public static List<String> mergeLists(List<String> targetList, List<String> itemsList) {
        List<String> returnList = null;

        if (itemsList != null && !itemsList.isEmpty()) {
            if (targetList != null && !targetList.isEmpty()) {
                Set<String> targetSet = new HashSet<String>(targetList);
                targetSet.addAll(itemsList);
                returnList = new ArrayList<String>(targetSet);
            } else {
                returnList = itemsList;
            }
        } else if (targetList != null && !targetList.isEmpty()) {
            returnList = targetList;
        }
        return returnList;
    }

    public static String getRegistryPath(String serverRoleType) {
        if (ServerRoleConstants.DEFAULT_ROLES_ID.equals(serverRoleType)) {
            return ServerRoleConstants.DEFAULT_ROLES_PATH;
        } else if (ServerRoleConstants.CUSTOM_ROLES_ID.equals(serverRoleType)) {
            return ServerRoleConstants.CUSTOM_ROLES_PATH;
        } else {
            return null;
        }
    }
}

