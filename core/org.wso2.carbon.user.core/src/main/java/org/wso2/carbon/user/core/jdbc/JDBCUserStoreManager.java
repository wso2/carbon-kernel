/*
z * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS"//also need to clear role authorization
        userRealm.getAuthorizationManager().cle BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.user.core.jdbc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.sql.DataSource;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.RoleContext;
import org.wso2.carbon.user.core.dto.RoleDTO;
import org.wso2.carbon.user.core.hybrid.HybridJDBCConstants;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.user.core.util.JDBCRealmUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class JDBCUserStoreManager extends AbstractUserStoreManager {

	protected DataSource jdbcds = null;
	protected Random random = new Random();
	// private boolean useOnlyInternalRoles;
	private static Log log = LogFactory.getLog(JDBCUserStoreManager.class);
	
	// Loading JDBC data store on demand.
    private DataSource getJDBCDataSource() throws UserStoreException {
        if (jdbcds == null) {
            jdbcds = loadUserStoreSpacificDataSoruce();
        }
        return jdbcds;
    }

    public JDBCUserStoreManager() {

    }

	/**
	 * 
	 * @param realmConfig
	 * @param tenantId
	 * @throws UserStoreException 
	 */
	public JDBCUserStoreManager(RealmConfiguration realmConfig, int tenantId) throws UserStoreException {
		this.realmConfig = realmConfig;
		this.tenantId = tenantId;
		realmConfig.setUserStoreProperties(JDBCRealmUtil.getSQL(realmConfig
				.getUserStoreProperties()));
		
//		if (isReadOnly() && realmConfig.isPrimary()) {
//			String adminRoleName = UserCoreUtil.removeDomainFromName(realmConfig.getAdminRoleName());
//			realmConfig.setAdminRoleName(UserCoreUtil.addInternalDomainName(adminRoleName));
//		}

		// new properties after carbon core 4.0.7 release.
		if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED) != null) {
			readGroupsEnabled = Boolean.parseBoolean(realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED));
		}

		if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED) != null) {
			writeGroupsEnabled = Boolean.parseBoolean(realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED));
		} else {
			if (!isReadOnly()){
				writeGroupsEnabled = true;
			}
		}

		// This property is now deprecated
		if (realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_INTERNAL_ROLES_ONLY) != null) {
			boolean internalRolesOnly = Boolean
					.parseBoolean(realmConfig
							.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_INTERNAL_ROLES_ONLY));
			if (internalRolesOnly) {
				readGroupsEnabled = false;
				writeGroupsEnabled = false;
			} else {
				readGroupsEnabled = true;
				writeGroupsEnabled = true;
			}
		}


		if (writeGroupsEnabled) {
			readGroupsEnabled = true;
		}

		/* Initialize user roles cache as implemented in AbstractUserStoreManager */
		initUserRolesCache();
	}

	/**
	 * This constructor is used by the support IS
	 * 
	 * @param ds
	 * @param realmConfig
	 * @param tenantId
	 * @param addInitData
	 * @param tenantId
	 */
	public JDBCUserStoreManager(DataSource ds, RealmConfiguration realmConfig, int tenantId,
			boolean addInitData) throws UserStoreException {

		this(realmConfig, tenantId);
		if (log.isDebugEnabled()) {
			log.debug("Started " + System.currentTimeMillis());
		}
		realmConfig.setUserStoreProperties(JDBCRealmUtil.getSQL(realmConfig
				.getUserStoreProperties()));
		this.jdbcds = ds;
		this.dataSource = ds;

		if (dataSource == null) {
			dataSource = DatabaseUtil.getRealmDataSource(realmConfig);
		}
		if (dataSource == null) {
			throw new UserStoreException("User Management Data Source is null");
		}
        doInitialSetup();
        this.persistDomain();
        if (realmConfig.isPrimary()) {
            addInitialAdminData(Boolean.parseBoolean(realmConfig.getAddAdmin()),
                    !isInitSetupDone());
        }

		if (log.isDebugEnabled()) {
			log.debug("Ended " + System.currentTimeMillis());
		}
	}

	/**
	 * This constructor to accommodate PasswordUpdater called from chpasswd script
	 * 
	 * @param ds
	 * @param realmConfig
	 * @throws UserStoreException
	 */
	public JDBCUserStoreManager(DataSource ds, RealmConfiguration realmConfig)
			throws UserStoreException {

		this(realmConfig, MultitenantConstants.SUPER_TENANT_ID);
		realmConfig.setUserStoreProperties(JDBCRealmUtil.getSQL(realmConfig
				.getUserStoreProperties()));
		this.jdbcds = ds;
	}

	/**
	 * 
	 * @param realmConfig
	 * @param properties
	 * @param claimManager
	 * @param profileManager
	 * @param realm
	 * @param tenantId
	 * @throws UserStoreException
	 */
	public JDBCUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
			ClaimManager claimManager, ProfileConfigurationManager profileManager, UserRealm realm,
			Integer tenantId) throws UserStoreException {
		this(realmConfig, properties, claimManager, profileManager, realm, tenantId, false);
	}

	/**
	 * 
	 * @param realmConfig
	 * @param properties
	 * @param claimManager
	 * @param profileManager
	 * @param realm
	 * @param tenantId
	 * @param skipInitData
	 * @throws UserStoreException
	 */
	public JDBCUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
			ClaimManager claimManager, ProfileConfigurationManager profileManager, UserRealm realm,
			Integer tenantId, boolean skipInitData) throws UserStoreException {
		this(realmConfig, tenantId);
		if (log.isDebugEnabled()) {
			log.debug("Started " + System.currentTimeMillis());
		}
		this.claimManager = claimManager;
		this.userRealm = realm;

        try {
            jdbcds = loadUserStoreSpacificDataSoruce();

            if (jdbcds == null) {
                jdbcds = (DataSource) properties.get(UserCoreConstants.DATA_SOURCE);
            }
            if (jdbcds == null) {
                jdbcds = DatabaseUtil.getRealmDataSource(realmConfig);
                properties.put(UserCoreConstants.DATA_SOURCE, jdbcds);
            }

            if (log.isDebugEnabled()) {
                log.debug("The jdbcDataSource being used by JDBCUserStoreManager :: "
                        + jdbcds.hashCode());
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()){
                log.debug("Loading JDBC datasource failed",e );
            }
        }

		dataSource = (DataSource) properties.get(UserCoreConstants.DATA_SOURCE);
		if (dataSource == null) {
			dataSource = DatabaseUtil.getRealmDataSource(realmConfig);
		}
		if (dataSource == null) {
			throw new UserStoreException("User Management Data Source is null");
		}

		properties.put(UserCoreConstants.DATA_SOURCE, dataSource);

		
		realmConfig.setUserStoreProperties(JDBCRealmUtil.getSQL(realmConfig
				.getUserStoreProperties()));

        this.persistDomain();
        doInitialSetup();
        if (realmConfig.isPrimary()) {
            addInitialAdminData(Boolean.parseBoolean(realmConfig.getAddAdmin()),
                    !isInitSetupDone());
        }

        initUserRolesCache();

		if (log.isDebugEnabled()) {
			log.debug("Ended " + System.currentTimeMillis());
		}
		/* Initialize user roles cache as implemented in AbstractUserStoreManager */

	}

	/**
	 * 
	 */
	public String[] doListUsers(String filter, int maxItemLimit) throws UserStoreException {

		String[] users = new String[0];
		Connection dbConnection = null;
		String sqlStmt = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;

		if (maxItemLimit == 0) {
			return new String[0];
		}

		int givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;

		int searchTime = UserCoreConstants.MAX_SEARCH_TIME;

		try {
			givenMax = Integer.parseInt(realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST));
		} catch (Exception e) {
			givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
		}

		try {
			searchTime = Integer.parseInt(realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
		} catch (Exception e) {
			searchTime = UserCoreConstants.MAX_SEARCH_TIME;
		}

		if (maxItemLimit < 0 || maxItemLimit > givenMax) {
			maxItemLimit = givenMax;
		}

		try {

			if (filter != null && filter.trim().length() != 0) {
				filter = filter.trim();
				filter = filter.replace("*", "%");
				filter = filter.replace("?", "_");
			} else {
				filter = "%";
			}

			List<String> lst = new LinkedList<String>();

			dbConnection = getDBConnection();

			if (dbConnection == null) {
				throw new UserStoreException("null connection");
			}

			sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USER_FILTER);

			prepStmt = dbConnection.prepareStatement(sqlStmt);
			prepStmt.setString(1, filter);
			if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				prepStmt.setInt(2, tenantId);
			}
			prepStmt.setMaxRows(maxItemLimit);
			try {
				prepStmt.setQueryTimeout(searchTime);
			} catch (Exception e) {
				// this can be ignored since timeout method is not implemented
				log.debug(e);
			}

			try {
				rs = prepStmt.executeQuery();
			} catch (SQLException e) {
				// may be due time out, therefore ignore this
				log.error(e);
				return users;
			}

			while (rs.next()) {

				String name = rs.getString(1);
				if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(name)) {
					continue;
				}
				// append the domain if exist
				String domain = realmConfig
						.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
				name = UserCoreUtil.addDomainToName(name, domain);
				lst.add(name);
			}
			rs.close();

			if (lst.size() > 0) {
				users = lst.toArray(new String[lst.size()]);
			}

			Arrays.sort(users);

		} catch (SQLException e) {
			log.error("Using sql : " + sqlStmt);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
		}
		return users;

	}


    @Override
    public boolean doCheckIsUserInRole(String userName, String roleName) throws UserStoreException {
        // TODO
        String[] roles = doGetExternalRoleListOfUser(userName, "*");
        if(roles != null){
            for(String role : roles){
                if(role.equalsIgnoreCase(roleName)){
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected String[] doGetDisplayNamesForInternalRole(String[] userNames) throws UserStoreException {
        return userNames;
    }

    /**
	 * 
	 */
	public String[] doGetRoleNames(String filter, int maxItemLimit) throws UserStoreException {

		String[] roles = new String[0];
		Connection dbConnection = null;
		String sqlStmt = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;

		if (maxItemLimit == 0) {
			return roles;
		}

		try {

			if (filter != null && filter.trim().length() != 0) {
				filter = filter.trim();
				filter = filter.replace("*", "%");
				filter = filter.replace("?", "_");
			} else {
				filter = "%";
			}

			List<String> lst = new LinkedList<String>();

			dbConnection = getDBConnection();

			if (dbConnection == null) {
				throw new UserStoreException("null connection");
			}

			sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_ROLE_LIST); // TODO
			
			prepStmt = dbConnection.prepareStatement(sqlStmt);
			//prepStmt.setString(1, filter);
			byte count = 0;
			prepStmt.setString(++count, filter);
			if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				prepStmt.setInt(++count, tenantId);
			}
			setPSRestrictions(prepStmt, maxItemLimit);
			try {
				rs = prepStmt.executeQuery();
			} catch (SQLException e) {
				log.error("Error while retrieving roles from JDBC user store", e);
				// may be due time out, therefore ignore this exception
			}

			//Expected columns UM_ROLE_NAME, UM_TENANT_ID, UM_SHARED_ROLE
			if (rs != null) {
				while (rs.next()) {
					String name = rs.getString(1);
					// append the domain if exist
					String domain =
					                realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
					name = UserCoreUtil.addDomainToName(name, domain);
					lst.add(name);
				}
			}
//
//			if (isSharedGroupEnabled()) {
//				lst.addAll(Arrays.asList(doGetSharedRoleNames(null, filter, maxItemLimit)));
//			}
//
			if (lst.size() > 0) {
				roles = lst.toArray(new String[lst.size()]);
			}
			
		} catch (SQLException e) {
			log.error("Using sql : " + sqlStmt);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
		}
		return roles;

	}
	
	private void setPSRestrictions(PreparedStatement ps, int maxItemLimit) throws SQLException {

		int givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;

		int searchTime = UserCoreConstants.MAX_SEARCH_TIME;

		try {
			givenMax =
			           Integer.parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST));
		} catch (Exception e) {
			givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
		}

		try {
			searchTime =
			             Integer.parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
		} catch (Exception e) {
			searchTime = UserCoreConstants.MAX_SEARCH_TIME;
		}

		if (maxItemLimit < 0 || maxItemLimit > givenMax) {
			maxItemLimit = givenMax;
		}

		ps.setMaxRows(maxItemLimit);
		try {
			ps.setQueryTimeout(searchTime);
		} catch (Exception e) {
			// this can be ignored since timeout method is not implemented
			log.debug(e);
		}
	}
	

	@Override
    protected String[] doGetSharedRoleNames(String tenantDomain, String filter, int maxItemLimit)
                                                                        throws UserStoreException {
		String[] roles = new String[0];
		Connection dbConnection = null;
		String sqlStmt = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		
		if(maxItemLimit == 0){
			return roles;
		}

		try {
			
			if (!isSharedGroupEnabled()) {
				return roles;
			}
			
			if (filter != null && filter.trim().length() != 0) {
				filter = filter.trim();
				filter = filter.replace("*", "%");
				filter = filter.replace("?", "_");
			} else {
				filter = "%";
			}

			List<String> lst = new LinkedList<String>();

			dbConnection = getDBConnection();

			if (dbConnection == null) {
				throw new UserStoreException("null connection");
			}

			sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_SHARED_ROLE_LIST);

			prepStmt = dbConnection.prepareStatement(sqlStmt);
			byte count = 0;
			prepStmt.setString(++count, filter);
			setPSRestrictions(prepStmt, maxItemLimit);
			try {
				rs = prepStmt.executeQuery();
			} catch (SQLException e) {
				log.error("Error while retrieving roles from JDBC user store", e);
				// may be due time out, therefore ignore this exception
			}

			// Expected columns UM_ROLE_NAME, UM_TENANT_ID, UM_SHARED_ROLE
			if (rs != null) {
				while (rs.next()) {
					String name = rs.getString(1);
					int roleTenantId = rs.getInt(2);
					// append the domain if exist
					String domain =
					                realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
					name = UserCoreUtil.addDomainToName(name, domain);
					name = UserCoreUtil.addTenantDomainToEntry(name, String.valueOf(roleTenantId));
					lst.add(name);
				}
			}

			if (lst.size() > 0) {
				roles = lst.toArray(new String[lst.size()]);
			}
		} catch (SQLException e) {
			log.error("Using sql : " + sqlStmt);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
		}
		return roles;
	}
    
    
    public String[] doGetUserListOfRole(String roleName, String filter) throws UserStoreException {
        
        RoleContext roleContext = createRoleContext(roleName);
        return getUserListOfJDBCRole(roleContext, filter);
    }
	/**
	 * 
	 */
	public String[] getUserListOfJDBCRole(RoleContext ctx, String filter) throws UserStoreException {

        String roleName = ctx.getRoleName();
		String[] names = null;
		String sqlStmt = null;
		if (!ctx.isShared()) {
			sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USERS_IN_ROLE);
			if (sqlStmt == null) {
				throw new UserStoreException("The sql statement for retrieving user roles is null");
			}
			if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				names =
				        getStringValuesFromDatabase(sqlStmt, roleName, tenantId, tenantId, tenantId);
			} else {
				names = getStringValuesFromDatabase(sqlStmt, roleName);
			}
		} else if (ctx.isShared()) {
			sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE);
			names = getStringValuesFromDatabase(sqlStmt, roleName);
		}

		List<String> userList = new ArrayList<String>();

		String domainName =
		                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

		if (names != null) {
			for (String user : names) {
				user = UserCoreUtil.addDomainToName(user, domainName);
				userList.add(user);
			}

			names = userList.toArray(new String[userList.size()]);
		}
		log.debug("Roles are not defined for the role name " + roleName);

		return names;
	}

	/**
	 * 
	 */
	public boolean doCheckExistingRole(String roleName) throws UserStoreException {

        RoleContext roleContext = createRoleContext(roleName);  // TODO if role Name with Shared Role?
        return isExistingJDBCRole(roleContext);             
	}

    protected boolean isExistingJDBCRole(RoleContext context) throws UserStoreException{

        boolean isExisting;
        String roleName = context.getRoleName();

        String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_IS_ROLE_EXISTING);
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for is role existing role null");
        }

        if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
            isExisting =
                    isValueExisting(sqlStmt, null, roleName, ((JDBCRoleContext) context).getTenantId());
        } else {
            isExisting = isValueExisting(sqlStmt, null, roleName);
        }

        return isExisting;        
    }
	/**
	 * 
	 */
	public String[] getAllProfileNames() throws UserStoreException {
		String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_PROFILE_NAMES);
		if (sqlStmt == null) {
			throw new UserStoreException("The sql statement for retrieving profile names is null");
		}
		String[] names;
		if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
			names = getStringValuesFromDatabase(sqlStmt, tenantId);
		} else {
			names = getStringValuesFromDatabase(sqlStmt);
		}

		return names;
	}

	/**
	 * 
	 */
	public String[] getProfileNames(String userName) throws UserStoreException {

        userName = UserCoreUtil.removeDomainFromName(userName);
		String sqlStmt = realmConfig
				.getUserStoreProperty(JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER);
		if (sqlStmt == null) {
			throw new UserStoreException("The sql statement for retrieving  is null");
		}
		String[] names;
		if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
			names = getStringValuesFromDatabase(sqlStmt, userName, tenantId, tenantId);
		} else {
			names = getStringValuesFromDatabase(sqlStmt, userName);
		}
		if (names.length == 0) {
			names = new String[] { UserCoreConstants.DEFAULT_PROFILE };
		} else {
			Arrays.sort(names);
			if (Arrays.binarySearch(names, UserCoreConstants.DEFAULT_PROFILE) < 0) {
				// we have to add the default profile
				String[] newNames = new String[names.length + 1];
				int i = 0;
				for (i = 0; i < names.length; i++) {
					newNames[i] = names[i];
				}
				newNames[i] = UserCoreConstants.DEFAULT_PROFILE;
				names = newNames;
			}
		}

		return names;
	}

	/**
	 * 
	 */
	public int getUserId(String username) throws UserStoreException {
		String sqlStmt = realmConfig
				.getUserStoreProperty(JDBCRealmConstants.GET_USERID_FROM_USERNAME);
		if (sqlStmt == null) {
			throw new UserStoreException("The sql statement for retrieving ID is null");
		}
		int id = -1;
		Connection dbConnection = null;
		try {
			dbConnection = getDBConnection();
			if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				id = DatabaseUtil.getIntegerValueFromDatabase(dbConnection, sqlStmt, username,
						tenantId);
			} else {
				id = DatabaseUtil.getIntegerValueFromDatabase(dbConnection, sqlStmt, username);
			}
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
		return id;
	}

	/**
	 * 
	 * @param tenantId tenant id
	 * @return array of users of the tenant.
	 * @throws UserStoreException throws user store exception
	 */
	public String[] getUserNames(int tenantId) throws UserStoreException {
		String sqlStmt = realmConfig
				.getUserStoreProperty(JDBCRealmConstants.GET_USERNAME_FROM_TENANT_ID);
		if (sqlStmt == null) {
			throw new UserStoreException("The sql statement for retrieving user names is null");
		}
		String[] userNames;
		Connection dbConnection = null;
		try {
			dbConnection = getDBConnection();
			userNames = DatabaseUtil.getStringValuesFromDatabase(dbConnection, sqlStmt, tenantId);
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
		return userNames;
	}

	/**
	 * @deprecated
	 * 
	 *             Returns the admin users for the given tenant.
	 * @return the admin user. // TODO remove this method
	 * @throws org.wso2.carbon.user.core.UserStoreException from the getUserNames()
	 */
	public String getAdminUser() throws UserStoreException {
		String[] users = getUserListOfRole(this.realmConfig.getAdminRoleName());
		if (users != null && users.length > 0) {
			return users[0];
		}
		return null;
	}

	/**
	 * 
	 */
	public int getTenantId() throws UserStoreException {
		return this.tenantId;
	}

	/**
	 * 
	 */
	public Map<String, String> getProperties(org.wso2.carbon.user.api.Tenant tenant)
			throws org.wso2.carbon.user.api.UserStoreException {
		return getProperties((Tenant) tenant);
	}

	/**
	 * 
	 */
	public int getTenantId(String username) throws UserStoreException {
		if (this.tenantId != MultitenantConstants.SUPER_TENANT_ID) {
			throw new UserStoreException("Not allowed to perform this operation");
		}
		String sqlStmt = realmConfig
				.getUserStoreProperty(JDBCRealmConstants.GET_TENANT_ID_FROM_USERNAME);
		if (sqlStmt == null) {
			throw new UserStoreException("The sql statement for retrieving ID is null");
		}
		int id = -1;
		Connection dbConnection = null;
		try {
			dbConnection = getDBConnection();
			id = DatabaseUtil.getIntegerValueFromDatabase(dbConnection, sqlStmt, username);
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
		return id;
	}

	/**
	 * 
	 */
	public Map<String, String> getUserPropertyValues(String userName, String[] propertyNames,
			String profileName) throws UserStoreException {

		if (profileName == null) {
			profileName = UserCoreConstants.DEFAULT_PROFILE;
		}
		Connection dbConnection = null;
		String sqlStmt = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		String[] propertyNamesSorted = propertyNames.clone();
		Arrays.sort(propertyNamesSorted);
		Map<String, String> map = new HashMap<String, String>();
		try {
			dbConnection = getDBConnection();
			sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_PROPS_FOR_PROFILE);
			prepStmt = dbConnection.prepareStatement(sqlStmt);
			prepStmt.setString(1, userName);
			prepStmt.setString(2, profileName);
			if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				prepStmt.setInt(3, tenantId);
				prepStmt.setInt(4, tenantId);
			}
			rs = prepStmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString(1);
				String value = rs.getString(2);
				if (Arrays.binarySearch(propertyNamesSorted, name) < 0) {
					continue;
				}
				map.put(name, value);
			}

			return map;
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
		}
	}

	/**
	 * 
	 * @param sqlStmt
	 * @param params
	 * @return
	 * @throws UserStoreException
	 */
	private String[] getStringValuesFromDatabase(String sqlStmt, Object... params)
			throws UserStoreException {
		
		if(log.isDebugEnabled()) {
			log.debug("Execuring Query: " + sqlStmt);
			for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                log.debug("Input value: " + param);
            }
		}
		
		String[] values = new String[0];
		Connection dbConnection = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			dbConnection = getDBConnection();
			values = DatabaseUtil.getStringValuesFromDatabase(dbConnection, sqlStmt, params);
		} catch (SQLException e) {
			log.error("Using sql : " + sqlStmt);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
		}
		return values;
	}
	
	private String[] getRoleNamesWithDomain(String sqlStmt, String userName, int tenantId,
	                                        boolean appendDn) throws UserStoreException {

		Connection dbConnection = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		List<String> roles = new ArrayList<String>();
		try {
			dbConnection = getDBConnection();
			prepStmt = dbConnection.prepareStatement(sqlStmt);
			byte count = 0;
			prepStmt.setString(++count, userName);
			prepStmt.setInt(++count, tenantId);

			rs = prepStmt.executeQuery();
			// append the domain if exist
			String domain =
			                realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

			while (rs.next()) {
				String name = rs.getString(1);
				int tenant = rs.getInt(2);
				// boolean shared = rs.getBoolean(3);

				String role = name;
				if (appendDn) {
					name = UserCoreUtil.addTenantDomainToEntry(name, String.valueOf(tenant));
				}
				roles.add(role);
			}

		} catch (SQLException e) {
			log.error("Using sql : " + sqlStmt);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
		}
		return roles.toArray(new String[roles.size()]);
	}


	/**
	 * 
	 * @return
	 * @throws SQLException
	 * @throws UserStoreException 
	 */
	protected Connection getDBConnection() throws SQLException, UserStoreException {
		Connection dbConnection = getJDBCDataSource().getConnection();
		dbConnection.setAutoCommit(false);
        if(dbConnection.getTransactionIsolation() != Connection.TRANSACTION_READ_COMMITTED){
            dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        }
		return dbConnection;
	}

	/**
	 * 
	 * @param sqlStmt
	 * @param dbConnection
	 * @param params
	 * @return
	 * @throws UserStoreException
	 */
	protected boolean isValueExisting(String sqlStmt, Connection dbConnection, Object... params)
			throws UserStoreException {
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		boolean isExisting = false;
		boolean doClose = false;
		try {
			if (dbConnection == null) {
				dbConnection = getDBConnection();
				doClose = true; // because we created it
			}
			if (DatabaseUtil.getIntegerValueFromDatabase(dbConnection, sqlStmt, params) > -1) {
				isExisting = true;
			}
			return isExisting;
		} catch (SQLException e) {
			log.error("Using sql : " + sqlStmt);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			if (doClose) {
				DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
			}
		}
	}

	/**
	 * 
	 */
	public boolean doCheckExistingUser(String userName) throws UserStoreException {

		String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_IS_USER_EXISTING);
		if (sqlStmt == null) {
			throw new UserStoreException("The sql statement for is user existing null");
		}
		boolean isExisting = false;

		String isUnique = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USERNAME_UNIQUE);
		if ("true".equals(isUnique)
				&& !CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userName)) {
			String uniquenesSql = realmConfig
					.getUserStoreProperty(JDBCRealmConstants.USER_NAME_UNIQUE);
			isExisting = isValueExisting(uniquenesSql, null, userName);
			if (log.isDebugEnabled()) {
				log.debug("The username should be unique across tenants.");
			}
		} else {
			if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				isExisting = isValueExisting(sqlStmt, null, userName, tenantId);
			} else {
				isExisting = isValueExisting(sqlStmt, null, userName);
			}
		}

		return isExisting;
	}

	/**
	 * 
	 */
	public boolean doAuthenticate(String userName, Object credential) throws UserStoreException {

		if (!checkUserNameValid(userName)) {
			return false;
		}

		if (!checkUserPasswordValid(credential)) {
			return false;
		}

		if (UserCoreUtil.isRegistryAnnonymousUser(userName)) {
			log.error("Anonnymous user trying to login");
			return false;
		}

		Connection dbConnection = null;
		ResultSet rs = null;
		PreparedStatement prepStmt = null;
		String sqlstmt = null;
		String password = (String) credential;
		boolean isAuthed = false;

		try {
			dbConnection = getDBConnection();
			dbConnection.setAutoCommit(false);

			sqlstmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.SELECT_USER);

			if (log.isDebugEnabled()) {
				log.debug(sqlstmt);
			}

			prepStmt = dbConnection.prepareStatement(sqlstmt);
			prepStmt.setString(1, userName);
			if (sqlstmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				prepStmt.setInt(2, tenantId);
			}

			rs = prepStmt.executeQuery();

			if (rs.next() == true) {
				String storedPassword = rs.getString(3);
				String saltValue = null;
				if ("true".equalsIgnoreCase(realmConfig
						.getUserStoreProperty(JDBCRealmConstants.STORE_SALTED_PASSWORDS))) {
					saltValue = rs.getString(4);
				}

				boolean requireChange = rs.getBoolean(5);
				Timestamp changedTime = rs.getTimestamp(6);

				GregorianCalendar gc = new GregorianCalendar();
				gc.add(GregorianCalendar.HOUR, -24);
				Date date = gc.getTime();

				if (requireChange == true && changedTime.before(date)) {
					isAuthed = false;
				} else {
					password = this.preparePassword(password, saltValue);
					if ((storedPassword != null) && (storedPassword.equals(password))) {
						isAuthed = true;
					}
				}
			}
		} catch (SQLException e) {
			log.error("Using sql : " + sqlstmt);
			throw new UserStoreException("Authentication Failure");
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
		}

		if (log.isDebugEnabled()) {
			log.debug("User " + userName + " login attempt. Login success :: " + isAuthed);
		}

		return isAuthed;
	}

	/**
	 * 
	 */
	public boolean isReadOnly() throws UserStoreException {
		if ("true".equalsIgnoreCase(realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_READ_ONLY))) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 */
	public void doAddUser(String userName, Object credential, String[] roleList,
			Map<String, String> claims, String profileName, boolean requirePasswordChange)
			throws UserStoreException {

		// persist the user info. in the database.
		persistUser(userName, credential, roleList, claims, profileName, requirePasswordChange);

	}

	/*
	 * This method persists the user information in the database.
	 */
	protected void persistUser(String userName, Object credential, String[] roleList,
			Map<String, String> claims, String profileName, boolean requirePasswordChange)
 throws UserStoreException {

		Connection dbConnection = null;
		String password = (String) credential;
		try {
			dbConnection = getDBConnection();
			String sqlStmt1 = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_USER);

			String saltValue = null;

			if ("true".equalsIgnoreCase(realmConfig.getUserStoreProperties()
			                                       .get(JDBCRealmConstants.STORE_SALTED_PASSWORDS))) {
				byte[] bytes = new byte[16];
				random.nextBytes(bytes);
				saltValue = Base64.encode(bytes);
			}

			password = this.preparePassword(password, saltValue);

			// do all 4 possibilities
			if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN) && (saltValue == null)) {
				this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userName, password, "",
				                                  requirePasswordChange, new Date(), tenantId);
			} else if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN) && (saltValue != null)) {
				this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userName, password,
				                                  saltValue, requirePasswordChange, new Date(),
				                                  tenantId);
			} else if (!sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN) &&
			           (saltValue == null)) {
				this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userName, password, "",
				                                  requirePasswordChange, new Date());
			} else {
				this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userName, password, saltValue,
				                                  requirePasswordChange, new Date());
			}

			if (roleList != null && roleList.length > 0) {

				RoleBreakdown breakdown = getSharedRoleBreakdown(roleList);
				String[] roles = breakdown.getRoles();
				// Integer[] tenantIds = breakdown.getTenantIds();

				String[] sharedRoles = breakdown.getSharedRoles();
				Integer[] sharedTenantIds = breakdown.getSharedTenantids();

				String sqlStmt2 = null;
				String type = DatabaseCreator.getDatabaseType(dbConnection);
				if (roles.length > 0) {
					// Adding user to the non shared roles
					sqlStmt2 =
					           realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_ROLE_TO_USER +
					                                            "-" + type);
					if (sqlStmt2 == null) {
						sqlStmt2 =
						           realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_ROLE_TO_USER);
					}

					if (sqlStmt2.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
						if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
							DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2,
							                                              tenantId, roles,
							                                              tenantId, userName,
							                                              tenantId);
						} else {
							DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2,
							                                              roles, tenantId,
							                                              userName, tenantId,
							                                              tenantId);
						}
					} else {
						DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, roleList, userName);
					}

				}
				if (sharedRoles.length > 0) {
					// Adding user to the shared roles
					sqlStmt2 =
					           realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER);
					DatabaseUtil.udpateUserRoleMappingWithExactParams(dbConnection, sqlStmt2,
					                                              sharedRoles, userName,
					                                              sharedTenantIds, tenantId);
				}

			}

			if (claims != null) {
				// add the properties
				if (profileName == null) {
					profileName = UserCoreConstants.DEFAULT_PROFILE;
				}

				Iterator<Map.Entry<String, String>> ite = claims.entrySet().iterator();
				while (ite.hasNext()) {
					Map.Entry<String, String> entry = ite.next();
					String claimURI = entry.getKey();
					String propName = getClaimAtrribute(claimURI, userName, null);
					String propValue = entry.getValue();
					addProperty(dbConnection, userName, propName, propValue, profileName);
				}
			}

			dbConnection.commit();
		} catch (Throwable e) {
			try {
				dbConnection.rollback();
			} catch (SQLException e1) {
				throw new UserStoreException("Error rollbacking add user operation", e1);
			}
            log.error("Error while persisting user : " + userName );
			throw new UserStoreException("Error while persisting user : " + userName , e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}

	/**
	 * 
	 */
	public void doAddRole(String roleName, String[] userList, boolean shared) throws UserStoreException {


        if(shared && isSharedGroupEnabled()){
            doAddSharedRole(roleName, userList);
        }

		Connection dbConnection = null;

		try {
			dbConnection = getDBConnection();
			String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_ROLE);
			if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				this.updateStringValuesToDatabase(dbConnection, sqlStmt, roleName, tenantId);
			} else {
				this.updateStringValuesToDatabase(dbConnection, sqlStmt, roleName);
			}
			if (userList != null) {
				// add role to user
				String type = DatabaseCreator.getDatabaseType(dbConnection);
				String sqlStmt2 = realmConfig
						.getUserStoreProperty(JDBCRealmConstants.ADD_USER_TO_ROLE + "-" + type);
				if (sqlStmt2 == null) {
					sqlStmt2 = realmConfig
							.getUserStoreProperty(JDBCRealmConstants.ADD_USER_TO_ROLE);
				}
				if (sqlStmt2.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
					if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
						DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2,
								tenantId, userList, tenantId, roleName, tenantId);
					} else {
						DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2,
								userList, tenantId, roleName, tenantId, tenantId);
					}
				} else {
					DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, userList, roleName);
				}

			}
			dbConnection.commit();
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} catch (Exception e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}

	/**
	 * 
	 */
	public void doUpdateRoleName(String roleName, String newRoleName) throws UserStoreException {
		
		JDBCRoleContext ctx = (JDBCRoleContext) createRoleContext(roleName);
        
		if (isExistingRole(newRoleName)) {
			throw new UserStoreException("Role name: " + newRoleName
					+ " in the system. Please pick another role name.");
		}
		String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.UPDATE_ROLE_NAME);
		if (sqlStmt == null) {
			throw new UserStoreException("The sql statement for update role name is null");
		}
		Connection dbConnection = null;
		try {

			roleName = ctx.getRoleName();
			dbConnection = getDBConnection();
			if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				this.updateStringValuesToDatabase(dbConnection, sqlStmt, newRoleName, roleName,
						tenantId);
			} else {
				this.updateStringValuesToDatabase(dbConnection, sqlStmt, newRoleName, roleName);
			}
			dbConnection.commit();
		} catch (SQLException e) {
			log.error("Using sql : " + sqlStmt);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}
	

	@Override
	public boolean isSharedRole(String roleName, String roleNameBase) {
		if (roleNameBase != null && roleNameBase.indexOf(TRUE_VALUE) > -1) {	
			return true;
		}
		return false;
	}
	
	private int getTenantIdFromRole(String roleBase) {
		int tenantId = MultitenantConstants.SUPER_TENANT_ID;
		String[] postfix = roleBase.split(UserCoreConstants.SHARED_ROLE_TENANT_COMBINER);
		if (postfix.length > 1) {
			try {
				tenantId = Integer.parseInt(postfix[1]);
			} catch (NumberFormatException e) {
				log.error(e);
				tenantId = MultitenantConstants.SUPER_TENANT_ID;
			}
		}

		return tenantId;
	}

	/**
	 * JDBC User store supports bulk import.
	 * 
	 * @return Always <code>true<code>.
	 */
	public boolean isBulkImportSupported() {
		return true;
	}

	public RealmConfiguration getRealmConfiguration() {
		return this.realmConfig;
	}

	/**
	 * User of this? TODO remove
	 * 
	 * @param noHybridRoles
	 * @return
	 * @throws UserStoreException
	 */
	public RoleDTO[] getRoleNamesWithDomain(boolean noHybridRoles) throws UserStoreException {

		String[] names = null;
		String domain = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

		String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_ROLE_LIST);
		if (sqlStmt == null) {
			throw new UserStoreException("The sql statement for retrieving role name is null");
		}
		names = getStringValuesFromDatabase(sqlStmt, tenantId);
		if (isReadOnly() && !noHybridRoles) {
			String[] hybrids = hybridRoleManager.getHybridRoles("*");
			names = UserCoreUtil.combineArrays(names, hybrids);
		}

		List<RoleDTO> roleDTOs = new ArrayList<RoleDTO>();
		if (names != null && names.length != 0) {
			roleDTOs.addAll(Arrays.asList(UserCoreUtil.convertRoleNamesToRoleDTO(names, domain)));
		}

		RoleDTO[] secondaryRoleDTOs = getAllSecondaryRoleDTOs();
		if (secondaryRoleDTOs != null && secondaryRoleDTOs.length != 0) {
			roleDTOs.addAll(Arrays.asList(secondaryRoleDTOs));
		}

		return roleDTOs.toArray(new RoleDTO[roleDTOs.size()]);
	}

	/**
	 * This method is to check whether multiple profiles are allowed with a particular user-store.
	 * For an example, currently, JDBC user store supports multiple profiles and where as ApacheDS
	 * does not allow. Currently, JDBC user store allows multiple profiles. Hence return true.
	 * 
	 * @return boolean
	 */
	public boolean isMultipleProfilesAllowed() {
		return true;
	}

	/**
	 * 
	 */
	public void doDeleteRole(String roleName) throws UserStoreException {

		String sqlStmt1 = realmConfig
				.getUserStoreProperty(JDBCRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE);
		if (sqlStmt1 == null) {
			throw new UserStoreException("The sql statement for delete user-role mapping is null");
		}

		String sqlStmt2 = realmConfig.getUserStoreProperty(JDBCRealmConstants.DELETE_ROLE);
		if (sqlStmt2 == null) {
			throw new UserStoreException("The sql statement for delete role is null");
		}

		Connection dbConnection = null;
		try {
			dbConnection = getDBConnection();
			if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				this.updateStringValuesToDatabase(dbConnection, sqlStmt1, roleName, tenantId,
						tenantId);
				this.updateStringValuesToDatabase(dbConnection, sqlStmt2, roleName, tenantId);
			} else {
				this.updateStringValuesToDatabase(dbConnection, sqlStmt1, roleName);
				this.updateStringValuesToDatabase(dbConnection, sqlStmt2, roleName);
			}
			//this.userRealm.getAuthorizationManager().clearRoleAuthorization(roleName);
			dbConnection.commit();
		} catch (SQLException e) {
			log.error("Using sql : " + sqlStmt1);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}

	/**
	 * 
	 */
	public void doDeleteUser(String userName) throws UserStoreException {

		String sqlStmt1 = realmConfig
				.getUserStoreProperty(JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE);
		if (sqlStmt1 == null) {
			throw new UserStoreException("The sql statement for delete user-role mapping is null");
		}

		String sqlStmt2 = realmConfig
				.getUserStoreProperty(JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE);
		if (sqlStmt2 == null) {
			throw new UserStoreException("The sql statement for delete user attribute is null");
		}

		String sqlStmt3 = realmConfig.getUserStoreProperty(JDBCRealmConstants.DELETE_USER);
		if (sqlStmt3 == null) {
			throw new UserStoreException("The sql statement for delete user is null");
		}

		Connection dbConnection = null;
		try {
			dbConnection = getDBConnection();
			if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userName, tenantId,
						tenantId);
				this.updateStringValuesToDatabase(dbConnection, sqlStmt2, userName, tenantId,
						tenantId);
				this.updateStringValuesToDatabase(dbConnection, sqlStmt3, userName, tenantId);
			} else {
				this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userName);
				this.updateStringValuesToDatabase(dbConnection, sqlStmt2, userName);
				this.updateStringValuesToDatabase(dbConnection, sqlStmt3, userName);
			}
			dbConnection.commit();
		} catch (SQLException e) {
			log.error("Using sql : " + sqlStmt1 + " :: " + sqlStmt2);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}
	
	/**
	 * 
	 */
	public void doUpdateUserListOfRole(String roleName, String deletedUsers[], String[] newUsers)
			throws UserStoreException {
		
		JDBCRoleContext ctx = (JDBCRoleContext) createRoleContext(roleName);
		roleName = ctx.getRoleName();
		int roleTenantId = ctx.getTenantId();
		boolean isShared = ctx.isShared();

		String sqlStmt1 = realmConfig
				.getUserStoreProperty(isShared ? JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE:
                        JDBCRealmConstants.REMOVE_USER_FROM_ROLE);
		if (sqlStmt1 == null) {
			throw new UserStoreException("The sql statement for remove user from role is null");
		}

		Connection dbConnection = null;
		try {
			dbConnection = getDBConnection();
			String type = DatabaseCreator.getDatabaseType(dbConnection);
			String sqlStmt2 = null;
			if (!isShared) {
				sqlStmt2 =
				           realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_USER_TO_ROLE +
				                                            "-" + type);
				if (sqlStmt2 == null) {
					sqlStmt2 =
					           realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_USER_TO_ROLE);
				}
			} else {
				sqlStmt2 =
				           realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER);
			}
			if (sqlStmt2 == null) {
				throw new UserStoreException("The sql statement for add user to role is null");
			}
			if (deletedUsers != null) {
				if (isShared) {
					DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt1,
					                                              roleName, tenantId,
					                                              deletedUsers, tenantId, tenantId, roleTenantId);
				} else {
					if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
						DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt1,
						                                              deletedUsers, tenantId,
						                                              roleName, tenantId, tenantId);
					} else {
						DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt1,
						                                              deletedUsers, roleName);
					}
				}
			}
			if (newUsers != null) {
				if (isShared) {
					DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, roleName,
					                                              roleTenantId, newUsers, tenantId,
					                                              tenantId, roleTenantId);

				} else {
					if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
						if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
							DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2,
							                                              tenantId, newUsers,
							                                              tenantId, roleName,
							                                              tenantId);
						} else {
							DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2,
							                                              newUsers, tenantId,
							                                              roleName, tenantId,
							                                              tenantId);
						}
					} else {
						DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2,
						                                              newUsers, roleName);
					}
				}
			}
			dbConnection.commit();
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} catch (Exception e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}

	}
	
	/**
	 * Break the provided role list based on whether roles are shared or not
	 * @param rolesList
	 * @return
	 */
	private RoleBreakdown getSharedRoleBreakdown(String[] rolesList) {
		List<String> roles = new ArrayList<String>();
		List<Integer> tenantIds = new ArrayList<Integer>();

		List<String> sharedRoles = new ArrayList<String>();
		List<Integer> sharedTenantIds = new ArrayList<Integer>();

		for (String role : rolesList) {

			String[] deletedRoleNames = role.split(CarbonConstants.DOMAIN_SEPARATOR);
			if (deletedRoleNames.length > 1) {
				role = deletedRoleNames[1];
			}

			JDBCRoleContext ctx = (JDBCRoleContext) createRoleContext(role);
			role = ctx.getRoleName();
			int roleTenantId = ctx.getTenantId();
			boolean isShared = ctx.isShared();

			if (isShared) {
				sharedRoles.add(role);
				sharedTenantIds.add(roleTenantId);
			} else {
				roles.add(role);
				tenantIds.add(roleTenantId);
			}

		}

		RoleBreakdown breakdown = new RoleBreakdown();

		// Non shared roles and tenant ids
		breakdown.setRoles(roles.toArray(new String[roles.size()]));
		breakdown.setTenantIds(tenantIds.toArray(new Integer[tenantIds.size()]));

		// Shared roles and tenant ids
		breakdown.setSharedRoles(sharedRoles.toArray(new String[sharedRoles.size()]));
		breakdown.setSharedTenantids(sharedTenantIds.toArray(new Integer[sharedTenantIds.size()]));

		return breakdown;

	}

	/**
	 * 
	 */
	public void doUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
			throws UserStoreException {

		Connection dbConnection = null;
		try {
			dbConnection = getDBConnection();
			String type = DatabaseCreator.getDatabaseType(dbConnection);
			String sqlStmt2 = null;
			// if user name and role names are prefixed with domain name, remove the domain name
			String[] userNames = userName.split(CarbonConstants.DOMAIN_SEPARATOR);
			if (userNames.length > 1) {
				userName = userNames[1];
			}
			if (deletedRoles != null && deletedRoles.length > 0) {
				// if user name and role names are prefixed with domain name,
				// remove the domain name
				RoleBreakdown breakdown = getSharedRoleBreakdown(deletedRoles);
				String[] roles = breakdown.getRoles();
				// Integer[] tenantIds = breakdown.getTenantIds();

				String[] sharedRoles = breakdown.getSharedRoles();
				Integer[] sharedTenantIds = breakdown.getSharedTenantids();

				String sqlStmt1 = null;

				if (roles.length > 0) {
					sqlStmt1 =
					           realmConfig.getUserStoreProperty(JDBCRealmConstants.REMOVE_ROLE_FROM_USER);
					if (sqlStmt1 == null) {
						throw new UserStoreException(
						                             "The sql statement for remove user from role is null");
					}
					if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
						DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt1,
						                                              roles, tenantId, userName,
						                                              tenantId, tenantId);
					} else {
						DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt1, roles, userName);
					}
				}
				if (sharedRoles.length > 0) {
					sqlStmt1 =
					           realmConfig.getUserStoreProperty(JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE);
					if (sqlStmt1 == null) {
						throw new UserStoreException(
						                             "The sql statement for remove user from role is null");
					}

					DatabaseUtil.udpateUserRoleMappingWithExactParams(dbConnection, sqlStmt1,
					                                              sharedRoles, userName,
					                                              sharedTenantIds, tenantId);
				}
			}

			if (newRoles != null && newRoles.length > 0) {
				// if user name and role names are prefixed with domain name,
				// remove the domain name

				RoleBreakdown breakdown = getSharedRoleBreakdown(newRoles);
				String[] roles = breakdown.getRoles();
				// Integer[] tenantIds = breakdown.getTenantIds();

				String[] sharedRoles = breakdown.getSharedRoles();
				Integer[] sharedTenantIds = breakdown.getSharedTenantids();

				if (roles.length > 0) {

					realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_ROLE_TO_USER + "-" +
					                                 type);
					if (sqlStmt2 == null) {
						sqlStmt2 =
						           realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_ROLE_TO_USER);
					}
					if (sqlStmt2 == null) {
						throw new UserStoreException(
						                             "The sql statement for add user to role is null");
					}
					if (sqlStmt2.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
						if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
							DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2,
							                                              tenantId, roles,
							                                              tenantId, userName,
							                                              tenantId);
						} else {
							DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2,
							                                              roles, tenantId,
							                                              userName, tenantId,
							                                              tenantId);
						}
					} else {
						DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, newRoles, userName);
					}
				}
				if (sharedRoles.length > 0) {
					sqlStmt2 =
					           realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER);
					if (sqlStmt2 == null) {
						throw new UserStoreException(
						                             "The sql statement for remove user from role is null");
					}

					DatabaseUtil.udpateUserRoleMappingWithExactParams(dbConnection, sqlStmt2,
					                                                  sharedRoles, userName,
					                                                  sharedTenantIds, tenantId);

				}
			}
			dbConnection.commit();
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} catch (Exception e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}

	}

	/**
	 * 
	 */
	public void doSetUserClaimValue(String userName, String claimURI, String claimValue,
			String profileName) throws UserStoreException {
		if (profileName == null) {
			profileName = UserCoreConstants.DEFAULT_PROFILE;
		}
		if (claimValue == null) {
			throw new UserStoreException("Cannot set null values.");
		}
		Connection dbConnection = null;
		try {
			dbConnection = getDBConnection();
			String property = getClaimAtrribute(claimURI, userName, null);
			String value = getProperty(dbConnection, userName, property, profileName);
			if (value == null) {
				addProperty(dbConnection, userName, property, claimValue, profileName);
			} else {
				updateProperty(dbConnection, userName, property, claimValue, profileName);
			}
			dbConnection.commit();
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} catch (org.wso2.carbon.user.api.UserStoreException e) {
			throw new UserStoreException(e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}

	/**
	 * 
	 */
	public void doSetUserClaimValues(String userName, Map<String, String> claims, String profileName)
			throws UserStoreException {
		Connection dbConnection = null;
		if (profileName == null) {
			profileName = UserCoreConstants.DEFAULT_PROFILE;
		}

		if (claims.get(UserCoreConstants.PROFILE_CONFIGURATION) == null) {
			claims.put(UserCoreConstants.PROFILE_CONFIGURATION,
					UserCoreConstants.DEFAULT_PROFILE_CONFIGURATION);
		}

		try {
			dbConnection = getDBConnection();
			Iterator<Map.Entry<String, String>> ite = claims.entrySet().iterator();

			while (ite.hasNext()) {
				Map.Entry<String, String> entry = ite.next();
				String claimURI = entry.getKey();
				
				String property = getClaimAtrribute(claimURI, userName, null);
				String value = entry.getValue();
				String existingValue = getProperty(dbConnection, userName, property, profileName);
				if (existingValue == null) {
					addProperty(dbConnection, userName, property, value, profileName);
				} else {
					updateProperty(dbConnection, userName, property, value, profileName);
				}
			}
			dbConnection.commit();
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} catch (org.wso2.carbon.user.api.UserStoreException e) {
			throw new UserStoreException(e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}

	/**
	 * 
	 */
	public void doDeleteUserClaimValue(String userName, String claimURI, String profileName)
			throws UserStoreException {
		Connection dbConnection = null;
		if (profileName == null) {
			profileName = UserCoreConstants.DEFAULT_PROFILE;
		}
		try {
			String property = null;
			if (UserCoreConstants.PROFILE_CONFIGURATION.equals(claimURI)) {
				property = UserCoreConstants.PROFILE_CONFIGURATION;
			} else {
				property = getClaimAtrribute(claimURI, userName, null);
			}

			dbConnection = getDBConnection();
			this.deleteProperty(dbConnection, userName, property, profileName);
			dbConnection.commit();
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} catch (org.wso2.carbon.user.api.UserStoreException e) {
			throw new UserStoreException(e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}

	/**
	 * 
	 */
	public void doDeleteUserClaimValues(String userName, String[] claims, String profileName)
			throws UserStoreException {
		Connection dbConnection = null;
		if (profileName == null) {
			profileName = UserCoreConstants.DEFAULT_PROFILE;
		}
		try {
			dbConnection = getDBConnection();
			for (String claimURI : claims) {
				String property = getClaimAtrribute(claimURI, userName, null);
				this.deleteProperty(dbConnection, userName, property, profileName);
			}
			dbConnection.commit();
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} catch (org.wso2.carbon.user.api.UserStoreException e) {
			throw new UserStoreException(e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}

	/**
	 * 
	 */
	public void doUpdateCredential(String userName, Object newCredential, Object oldCredential)
			throws UserStoreException {
		// no need to check old password here because we already authenticate in super class
		// if (this.authenticate(userName, oldCredential)) {
		this.doUpdateCredentialByAdmin(userName, newCredential);
		/*
		 * } else { log.error("Wrong username/password provided"); throw new
		 * UserStoreException("Wrong username/password provided"); }
		 */
	}

	/**
	 * 
	 */
	public void doUpdateCredentialByAdmin(String userName, Object newCredential)
			throws UserStoreException {

		if (!checkUserPasswordValid(newCredential)) {
			throw new UserStoreException(
					"Credential not valid. Credential must be a non null string with following format, "
							+ realmConfig
									.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX));

		}

		String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.UPDATE_USER_PASSWORD);
		if (sqlStmt == null) {
			throw new UserStoreException("The sql statement for delete user claim value is null");
		}
		String saltValue = null;
		if ("true".equalsIgnoreCase(realmConfig.getUserStoreProperties().get(
				JDBCRealmConstants.STORE_SALTED_PASSWORDS))) {
			byte[] bytes = new byte[16];
			random.nextBytes(bytes);
			saltValue = Base64.encode(bytes);
		}

		String password = this.preparePassword((String) newCredential, saltValue);

		if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN) && saltValue == null) {
			updateStringValuesToDatabase(null, sqlStmt, password, false, new Date(), userName,
					tenantId);
		} else if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN) && saltValue != null) {
			updateStringValuesToDatabase(null, sqlStmt, password, saltValue, false, new Date(),
					userName, tenantId);
		} else if (!sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN) && saltValue == null) {
			updateStringValuesToDatabase(null, sqlStmt, password, false, new Date(), userName);
		} else {
			updateStringValuesToDatabase(null, sqlStmt, password, saltValue, false, new Date(),
					userName);
		}
	}

	/**
	 * 
	 */
	public Date getPasswordExpirationTime(String userName) throws UserStoreException {
		Connection dbConnection = null;
		ResultSet rs = null;
		PreparedStatement prepStmt = null;
		String sqlstmt = null;
		Date date = null;

		try {
			dbConnection = getDBConnection();
			dbConnection.setAutoCommit(false);

			sqlstmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.SELECT_USER);

			if (log.isDebugEnabled()) {
				log.debug(sqlstmt);
			}

			prepStmt = dbConnection.prepareStatement(sqlstmt);
			prepStmt.setString(1, userName);
			if (sqlstmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				prepStmt.setInt(2, tenantId);
			}

			rs = prepStmt.executeQuery();

			if (rs.next() == true) {
				boolean requireChange = rs.getBoolean(5);
				Timestamp changedTime = rs.getTimestamp(6);
				if (requireChange) {
					GregorianCalendar gc = new GregorianCalendar();
					gc.setTime(changedTime);
					gc.add(GregorianCalendar.HOUR, 24);
					date = gc.getTime();
				}
			}
		} catch (SQLException e) {
			log.error("Using sql : " + sqlstmt);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
		}
		return date;
	}

	/**
	 * 
	 * @param dbConnection
	 * @param sqlStmt
	 * @param params
	 * @throws UserStoreException
	 */
	private void updateStringValuesToDatabase(Connection dbConnection, String sqlStmt,
			Object... params) throws UserStoreException {
		PreparedStatement prepStmt = null;
		boolean localConnection = false;
		try {
			if (dbConnection == null) {
				localConnection = true;
				dbConnection = getDBConnection();
			}
			prepStmt = dbConnection.prepareStatement(sqlStmt);
			if (params != null && params.length > 0) {
				for (int i = 0; i < params.length; i++) {
					Object param = params[i];
					if (param == null) {
						throw new UserStoreException("Invalid data provided");
					} else if (param instanceof String) {
						prepStmt.setString(i + 1, (String) param);
					} else if (param instanceof Integer) {
						prepStmt.setInt(i + 1, (Integer) param);
					} else if (param instanceof Date) {
						// Timestamp timestamp = new Timestamp(((Date) param).getTime());
						// prepStmt.setTimestamp(i + 1, timestamp);
						prepStmt.setTimestamp(i + 1, new Timestamp(System.currentTimeMillis()));
					} else if (param instanceof Boolean) {
						prepStmt.setBoolean(i + 1, (Boolean) param);
					}
				}
			}
			int count = prepStmt.executeUpdate();

			if (log.isDebugEnabled()) {
				if (count == 0) {
					log.debug("No rows were updated");
				}
				log.debug("Executed querry is " + sqlStmt + " and number of updated rows :: "
						+ count);
			}

			if (localConnection) {
				dbConnection.commit();
			}
		} catch (SQLException e) {
			log.error("Using sql : " + sqlStmt);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			if (localConnection) {
				DatabaseUtil.closeAllConnections(dbConnection);
			}
			DatabaseUtil.closeAllConnections(null, prepStmt);
		}
	}

	/**
	 * 
	 * @param dbConnection
	 * @param userName
	 * @param propertyName
	 * @param value
	 * @param profileName
	 * @throws UserStoreException
	 */
	public void addProperty(Connection dbConnection, String userName, String propertyName,
			String value, String profileName) throws UserStoreException {
		try {
			String type = DatabaseCreator.getDatabaseType(dbConnection);
			String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_USER_PROPERTY
					+ "-" + type);
			if (sqlStmt == null) {
				sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_USER_PROPERTY);
			}
			if (sqlStmt == null) {
				throw new UserStoreException("The sql statement for add user property sql is null");
			}

            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                    updateStringValuesToDatabase(dbConnection, sqlStmt, propertyName, value,
                            profileName, tenantId, userName, tenantId);
                } else {
                    updateStringValuesToDatabase(dbConnection, sqlStmt, userName, tenantId,
                            propertyName, value, profileName, tenantId);
                }
            } else {
                updateStringValuesToDatabase(dbConnection, sqlStmt, userName, propertyName, value, profileName);
            }
		} catch (UserStoreException e) {
			throw e;
		} catch (Exception e) {
			throw new UserStoreException(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param dbConnection
	 * @param userName
	 * @param propertyName
	 * @param value
	 * @param profileName
	 * @throws UserStoreException
	 */
	protected void updateProperty(Connection dbConnection, String userName, String propertyName,
			String value, String profileName) throws UserStoreException {
		String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.UPDATE_USER_PROPERTY);
		if (sqlStmt == null) {
			throw new UserStoreException("The sql statement for add user property sql is null");
		}

        if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
            updateStringValuesToDatabase(dbConnection, sqlStmt, value, userName, tenantId,
                    propertyName, profileName, tenantId);
        } else {
            updateStringValuesToDatabase(dbConnection, sqlStmt, value, userName, propertyName, profileName);
        }

	}

	/**
	 * 
	 * @param dbConnection
	 * @param userName
	 * @param propertyName
	 * @param profileName
	 * @throws UserStoreException
	 */
	protected void deleteProperty(Connection dbConnection, String userName, String propertyName,
			String profileName) throws UserStoreException {
		String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.DELETE_USER_PROPERTY);
		if (sqlStmt == null) {
			throw new UserStoreException("The sql statement for add user property sql is null");
		}

        if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
            updateStringValuesToDatabase(dbConnection, sqlStmt, userName, tenantId, propertyName,
                    profileName, tenantId);
        } else {
            updateStringValuesToDatabase(dbConnection, sqlStmt, userName, propertyName, profileName);
        }
	}

	/**
	 * 
	 * @param dbConnection
	 * @param userName
	 * @param propertyName
	 * @param profileName
	 * @return
	 * @throws UserStoreException
	 */
	protected String getProperty(Connection dbConnection, String userName, String propertyName,
			String profileName) throws UserStoreException {
		String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_PROP_FOR_PROFILE);
		if (sqlStmt == null) {
			throw new UserStoreException("The sql statement for add user property sql is null");
		}
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		String value = null;
		try {
			prepStmt = dbConnection.prepareStatement(sqlStmt);
			prepStmt.setString(1, userName);
			prepStmt.setString(2, propertyName);
			prepStmt.setString(3, profileName);
			if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				prepStmt.setInt(4, tenantId);
				prepStmt.setInt(5, tenantId);
			}

			rs = prepStmt.executeQuery();
			while (rs.next()) {
				value = rs.getString(1);
			}
			return value;
		} catch (SQLException e) {
			log.error("Using sql : " + sqlStmt);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(null, rs, prepStmt);
		}
	}

	/**
	 * 
	 * @param password
	 * @param saltValue
	 * @return
	 * @throws UserStoreException
	 */
	protected String preparePassword(String password, String saltValue) throws UserStoreException {
		try {
			String digestInput = password;
			if (saltValue != null) {
				digestInput = password + saltValue;
			}
			String digsestFunction = realmConfig.getUserStoreProperties().get(
					JDBCRealmConstants.DIGEST_FUNCTION);
			if (digsestFunction != null) {

				if (digsestFunction
						.equals(UserCoreConstants.RealmConfig.PASSWORD_HASH_METHOD_PLAIN_TEXT)) {
					return password;
				}

				MessageDigest dgst = MessageDigest.getInstance(digsestFunction);
				byte[] byteValue = dgst.digest(digestInput.getBytes());
				password = Base64.encode(byteValue);
			}
			return password;
		} catch (NoSuchAlgorithmException e) {
			throw new UserStoreException(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @return
	 * @throws UserStoreException
	 */
	private DataSource loadUserStoreSpacificDataSoruce() throws UserStoreException {
		return DatabaseUtil.createUserStoreDataSource(realmConfig);
	}

	/**
	 * 
	 */
	public Map<String, String> getProperties(Tenant tenant) throws UserStoreException {
		return this.realmConfig.getUserStoreProperties();
	}

	/**
	 * 
	 */
	public void addRememberMe(String userName, String token)
			throws org.wso2.carbon.user.api.UserStoreException {
		Connection dbConnection = null;
		try {
			dbConnection = getDBConnection();
			String[] values = DatabaseUtil.getStringValuesFromDatabase(dbConnection,
					HybridJDBCConstants.GET_REMEMBERME_VALUE_SQL, userName, tenantId);
			Date createdTime = Calendar.getInstance().getTime();
			if (values != null && values.length > 0 && values[0].length() > 0) {
				// udpate
				DatabaseUtil.updateDatabase(dbConnection,
						HybridJDBCConstants.UPDATE_REMEMBERME_VALUE_SQL, token, createdTime,
						userName, tenantId);
			} else {
				// add
				DatabaseUtil.updateDatabase(dbConnection,
						HybridJDBCConstants.ADD_REMEMBERME_VALUE_SQL, userName, token, createdTime,
						tenantId);
			}
			dbConnection.commit();
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} catch (Exception e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}

	/**
	 * Checks whether the token is existing or not.
	 * 
	 * @param userName
	 * @param token
	 * @return
	 * @throws org.wso2.carbon.user.api.UserStoreException
	 */
	public boolean isExistingRememberMeToken(String userName, String token)
			throws org.wso2.carbon.user.api.UserStoreException {
		boolean isValid = false;
		Connection dbConnection = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		String value = null;
		Date createdTime = null;
		try {
			dbConnection = getDBConnection();
			prepStmt = dbConnection.prepareStatement(HybridJDBCConstants.GET_REMEMBERME_VALUE_SQL);
			prepStmt.setString(1, userName);
			prepStmt.setInt(2, tenantId);
			rs = prepStmt.executeQuery();
			while (rs.next()) {
				value = rs.getString(1);
				createdTime = rs.getTimestamp(2);
			}
		} catch (SQLException e) {
			log.error("Using sql : " + HybridJDBCConstants.GET_REMEMBERME_VALUE_SQL);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
		}

		if (value != null && createdTime != null) {
			Calendar calendar = Calendar.getInstance();
			Date nowDate = calendar.getTime();
			calendar.setTime(createdTime);
			calendar.add(Calendar.SECOND, CarbonConstants.REMEMBER_ME_COOKIE_TTL);
			Date expDate = calendar.getTime();
			if (expDate.before(nowDate)) {
				// Do nothing remember me expired.
				// Return the user gracefully
				log.debug("Remember me token has expired !!");
			} else {

				// We also need to compare the token
				if (value.equals(token)) {
					isValid = true;
				} else {
					log.debug("Remember me token in DB and token in request are different !!");
					isValid = false;
				}
			}
		}

		return isValid;
	}

	/**
	 * 
	 */
	public boolean isValidRememberMeToken(String userName, String token)
			throws org.wso2.carbon.user.api.UserStoreException {
		try {
			if (isExistingUser(userName)) {
				return isExistingRememberMeToken(userName, token);
			}
		} catch (Exception e) {
			log.error("Validating remember me token failed for" + userName);
			// not throwing exception.
			// because we need to seamlessly direct them to login uis
		}

		return false;
	}

	@Override
	public String[] getUserListFromProperties(String property, String value, String profileName)
			throws UserStoreException {

		if (profileName == null) {
			profileName = UserCoreConstants.DEFAULT_PROFILE;
		}

        if(value.contains("*")){
            if((value.startsWith("*") && !value.substring(1).contains("*"))||
                    value.endsWith("*")&& !value.substring(0,value.length()-1).contains("*")){
                value = value.replace('*','%');
            }
        }

		String[] users = new String[0];
		Connection dbConnection = null;
		String sqlStmt = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;

		List<String> list = new ArrayList<String>();
		try {
			dbConnection = getDBConnection();
			sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USERS_FOR_PROP);
			prepStmt = dbConnection.prepareStatement(sqlStmt);
			prepStmt.setString(1, property);
			prepStmt.setString(2, value);
			prepStmt.setString(3, profileName);
			if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				prepStmt.setInt(4, tenantId);
				prepStmt.setInt(5, tenantId);
			}
			rs = prepStmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString(1);
				list.add(name);
			}

			if (list.size() > 0) {
				users = list.toArray(new String[list.size()]);
			}

		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
		}

		return users;
	}

	@Override
	public String[] doGetExternalRoleListOfUser(String userName, String filter) throws UserStoreException {
		
		if(log.isDebugEnabled()) {
			log.debug("Getting roles of user: " + userName + " with filter: " + filter);
		}

		String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USER_ROLE);
		List<String> roles = new ArrayList<String>();
		String[] names;
		if (sqlStmt == null) {
			throw new UserStoreException("The sql statement for retrieving user roles is null");
		}
		if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
			names = getStringValuesFromDatabase(sqlStmt, userName, tenantId, tenantId, tenantId);
		} else {
			names = getStringValuesFromDatabase(sqlStmt, userName);
		}
		
		if(log.isDebugEnabled()) {
			if (names != null) {
				for (String name : names) {
					log.debug("Found role: " + name);
				}
			} else {
				log.debug("No external role found for the user: " + userName);
			}
		}

		Collections.addAll(roles, names);
		return roles.toArray(new String[roles.size()]);
	}

    @Override
    public org.wso2.carbon.user.api.Properties getDefaultUserStoreProperties() {
        Properties properties = new Properties();
        properties.setMandatoryProperties(JDBCUserStoreConstants.JDBC_UM_MANDATORY_PROPERTIES.toArray
                (new Property[JDBCUserStoreConstants.JDBC_UM_MANDATORY_PROPERTIES.size()]));
        properties.setOptionalProperties(JDBCUserStoreConstants.JDBC_UM_OPTIONAL_PROPERTIES.toArray
                (new Property[JDBCUserStoreConstants.JDBC_UM_OPTIONAL_PROPERTIES.size()]));
        properties.setAdvancedProperties(JDBCUserStoreConstants.JDBC_UM_ADVANCED_PROPERTIES.toArray
                (new Property[JDBCUserStoreConstants.JDBC_UM_ADVANCED_PROPERTIES.size()]));
        return properties;
    }

	protected void doAddSharedRole(String roleName, String[] userList) throws UserStoreException {

		Connection dbConnection = null;

		try {
			dbConnection = getDBConnection();
			String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_SHARED_ROLE);
			if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				this.updateStringValuesToDatabase(dbConnection, sqlStmt, true, roleName, tenantId);
			} else {
				this.updateStringValuesToDatabase(dbConnection, sqlStmt, true, roleName);
			}
			if (userList != null) {
				// add role to user
				int roleTenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
				sqlStmt =
				          realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER);
				DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt, roleName,
				                                              roleTenantId, userList, tenantId,
				                                              tenantId, roleTenantId);
			}
			dbConnection.commit();
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} catch (Exception e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}
	
	public class RoleBreakdown {
		private String[] roles;
		private Integer[] tenantIds;

		private String[] sharedRoles;
		private Integer[] sharedTenantids;

		public String[] getRoles() {
			return roles;
		}

		public void setRoles(String[] roles) {
			this.roles = roles;
		}

		public Integer[] getTenantIds() {
			return tenantIds;
		}

		public void setTenantIds(Integer[] tenantIds) {
			this.tenantIds = tenantIds;
		}

		public String[] getSharedRoles() {
			return sharedRoles;
		}

		public void setSharedRoles(String[] sharedRoles) {
			this.sharedRoles = sharedRoles;
		}

		public Integer[] getSharedTenantids() {
			return sharedTenantids;
		}

		public void setSharedTenantids(Integer[] sharedTenantids) {
			this.sharedTenantids = sharedTenantids;
		}

	}

	@Override
	protected String[] doGetSharedRoleListOfUser(String userName,
                                     String tenantDomain, String filter) throws UserStoreException {
		
		if(log.isDebugEnabled()) {
			log.debug("Looking for shared roles for user: " + userName + " for teanant: " + tenantDomain);
		}

		if (isSharedGroupEnabled()) {
			// shared roles
			String sqlStmt =
			                 realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER);
			String[] sharedNames = getRoleNamesWithDomain(sqlStmt, userName, tenantId, true);

			return sharedNames;
		}
		return new String[0];
	}

	@Override
	protected RoleContext createRoleContext(String roleName) {

		JDBCRoleContext searchCtx = new JDBCRoleContext();
		String[] roleNameParts = roleName.split(UserCoreConstants.TENANT_DOMAIN_COMBINER);
		if(roleNameParts.length > 1 && (roleNameParts[1] == null || roleNameParts[1].equals("null"))){
			roleNameParts = new String[]{roleNameParts[0]};
		}
		int tenantId = -1;
		if (roleNameParts.length > 1) {
			tenantId = Integer.parseInt(roleNameParts[1]);
			searchCtx.setTenantId(tenantId);
		} else {
			tenantId = this.tenantId;
			searchCtx.setTenantId(tenantId);
		}

		if (tenantId != this.tenantId) {
			searchCtx.setShared(true);
		}

		searchCtx.setRoleName(roleNameParts[0]);
		return searchCtx;
	}
}
