package org.wso2.carbon.user.core.common;/*
 *  Copyright (c) 2015r, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class UserRenameEventListener implements UserOperationEventListener {


    @Override
    public int getExecutionOrderId() {
        return new Random().nextInt();
    }

    @Override
    public boolean doPreAuthenticate(String userName, Object credential, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostAuthenticate(String userName, boolean authenticated, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims, String profile, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    private String createUniqueID(String userName){

        return userName.hashCode()+"";

    }

    @Override
    public boolean doPostAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims, String profile, UserStoreManager userStoreManager) throws UserStoreException {
        try{
            AbstractUserStoreManager usm = (AbstractUserStoreManager) userStoreManager;
            RealmConfiguration realmConfig = usm.getRealmConfiguration();
            int tenantId = usm.getTenantId();
            Connection conn = null;

            java.util.Properties connProperties = new java.util.Properties();
            connProperties.setProperty("user","wso2carbon");
            connProperties.setProperty("password","wso2carbon");

            conn= DriverManager.getConnection("jdbc:h2:repository/database/WSO2CARBON_DB;DB_CLOSE_ON_EXIT=FALSE;LOCK_TIMEOUT=60000",connProperties);
            String addStatement = "INSERT INTO UM_UID_USER (UM_UID, UM_USERNAME, UM_STORE_DOMAIN, UM_TENANT)" +
                    " VALUES(?,?,?,?)";

            PreparedStatement stmt = conn.prepareStatement(addStatement);
            stmt.setString(1, createUniqueID(userName));
            stmt.setString(2,userName);
            stmt.setString(3,realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
            stmt.setString(4,tenantId+"");
            stmt.execute();
            conn.commit();

            conn.close();


        } catch (SQLException e){
            System.out.println("User '" + userName + "' was added, but something went wrong creating unique identifier for the user.");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean doPreUpdateCredential(String userName, Object newCredential, Object oldCredential, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostUpdateCredential(String userName, Object credential, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreUpdateCredentialByAdmin(String userName, Object newCredential, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostUpdateCredentialByAdmin(String userName, Object credential, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreDeleteUser(String userName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostDeleteUser(String userName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValue(String userName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims, String profileName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValues(String userName, Map<String, String> claims, String profileName, UserStoreManager userStoreManager) throws UserStoreException {
        Iterator<Map.Entry<String,String>> ite = claims.entrySet().iterator();

        while (ite.hasNext()){

            Map.Entry<String, String> entry = ite.next();
            String claimURI = entry.getKey();

            if(claimURI.equals("http://wso2.org/claims/identity/userName")) try {
                AbstractUserStoreManager usm = (AbstractUserStoreManager) userStoreManager;
                RealmConfiguration realmConfig = usm.getRealmConfiguration();
                int tenantId = usm.getTenantId();
                String newUserName = entry.getValue();
                Connection conn = null;

                java.util.Properties connProperties = new java.util.Properties();
                connProperties.setProperty("user","wso2carbon");
                connProperties.setProperty("password","wso2carbon");
                //TODO get from datasource rather than hardcode
                conn= DriverManager.getConnection("jdbc:h2:repository/database/WSO2CARBON_DB;DB_CLOSE_ON_EXIT=FALSE;LOCK_TIMEOUT=60000", connProperties);
                String updateStatement = "UPDATE UM_UID_USER SET UM_USERNAME=? WHERE UM_USERNAME=? AND UM_STORE_DOMAIN=? AND UM_TENANT=?";
                PreparedStatement stmt = conn.prepareStatement(updateStatement);
                stmt.setString(1, newUserName);
                stmt.setString(2, userName);
                stmt.setString(3, realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
                stmt.setString(4, tenantId + "");
                stmt.execute();

                //  PreparedStatement tst = conn.prepareStatement("create table TEST (t VARCHAR(255), TT VARCHAR(255));");
                // tst.execute();

                conn.commit();
                conn.close();
            } catch (SQLException e) {

                System.out.println("error while updating renamed user information");
                e.printStackTrace();

                return false;
            }

        }


        return true;    }

    @Override
    public boolean doPreDeleteUserClaimValues(String userName, String[] claims, String profileName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValues(String userName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValue(String userName, String claimURI, String profileName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValue(String userName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreAddRole(String roleName, String[] userList, Permission[] permissions, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostAddRole(String roleName, String[] userList, Permission[] permissions, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreDeleteRole(String roleName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostDeleteRole(String roleName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreUpdateRoleName(String roleName, String newRoleName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostUpdateRoleName(String roleName, String newRoleName, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPreUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    @Override
    public boolean doPostUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles, UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }
}
