/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.user.core.tenant;

import java.util.Locale;
import java.util.Map;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.sql.DataSource;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.TenantMgtConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.ldap.LDAPConnectionContext;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.util.JNDIUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;

/**
 * This class is the tenant manager for any external LDAP and based on the "ou" partitioning
 * per tenant under one DIT.
 */
public class CommonHybridLDAPTenantManager extends JDBCTenantManager {
    private static Log logger = LogFactory.getLog(CommonHybridLDAPTenantManager.class);
    private LDAPConnectionContext ldapConnectionSource;
    //TODO move the following configurations and constants to relevant files.

    private TenantMgtConfiguration tenantMgtConfig = null;
    private RealmConfiguration realmConfig = null;
    //constants
    private static final String USER_PASSWORD_ATTRIBUTE_NAME = "userPassword";
    private static final String EMAIL_ATTRIBUTE_NAME = "mail";
    private static final String SN_ATTRIBUTE_NAME = "sn";
    private static final String CN_ATTRIBUTE_NAME = "cn";

    public CommonHybridLDAPTenantManager(OMElement omElement, Map<String, Object> properties)
            throws Exception {
        super(omElement, properties);

        tenantMgtConfig = (TenantMgtConfiguration) properties.get(
                UserCoreConstants.TENANT_MGT_CONFIGURATION);

        realmConfig = (RealmConfiguration) properties.get(UserCoreConstants.REALM_CONFIGURATION);
        if (realmConfig == null) {
            throw new UserStoreException("Tenant Manager can not function without a bootstrap realm config");
        }
        
        if (ldapConnectionSource == null) {
        	ldapConnectionSource = new LDAPConnectionContext(realmConfig);
        }

    }

    public CommonHybridLDAPTenantManager(DataSource dataSource, String superTenantDomain) {
        super(dataSource, superTenantDomain);
    }

    /**
     * Do necessary things in LDAP when adding a tenant.
     *
     * @param tenant Tenant to be added.
     * @return The tenant id.
     * @throws UserStoreException If an error occurred while creating the tenant.
     */
    @Override
    public int addTenant(org.wso2.carbon.user.api.Tenant tenant) throws UserStoreException {
        int tenantID = super.addTenant(tenant);
        tenant.setId(tenantID);

        DirContext initialDirContext = null;
        try {
            initialDirContext = this.ldapConnectionSource.getContext();
            //create per tenant context and its user store and group store with admin related entries.
            if (!isOrganizationalUnitCreated(tenant.getDomain(), initialDirContext)) {
                createOrganizationalUnit(tenant.getDomain(), (Tenant) tenant, initialDirContext);
                addSharedGroupForTenant((Tenant) tenant, initialDirContext);
            }else {               
                logger.warn("Organizational unit for tenant domain:"+tenant.getDomain() +" is already created.");              
            }
        } finally {
            closeContext(initialDirContext);
        }


        return tenantID;
    }

    /**
     * Check if organizational unit is created in tenant.
     *
     * @param orgName           Organization name.
     * @param initialDirContext The directory connection.
     * @throws UserStoreException If an error occurred while searching.
     */
    protected boolean isOrganizationalUnitCreated(String orgName, DirContext initialDirContext)
            throws UserStoreException {

        //construct search filter,eg. (&(objectClass=organizationalUnit)(ou=wso2.com))
        String partitionDN = tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ROOT_PARTITION);
        String organizationalObjectClass = tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ORGANIZATIONAL_OBJECT_CLASS);
        String organizationalAttribute = tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ORGANIZATIONAL_ATTRIBUTE);
        String searchFilter = "(&(objectClass=" + organizationalObjectClass + ")(" + organizationalAttribute + "=" + orgName + "))";

        SearchControls userSearchControl = new SearchControls();
        userSearchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<SearchResult> userSearchResults = null;

        try {
            userSearchResults = initialDirContext.search(partitionDN, searchFilter, userSearchControl);
            return userSearchResults.hasMore();
        } catch (NamingException e) {
            String errorMessage = "Error occurred while searching in root partition.";
            throw new UserStoreException(errorMessage, e);
        }
    }

/**
     * Create a space for tenant in LDAP.
     *
     * @param orgName           Organization name.
     * @param tenant            The tenant
     * @param initialDirContext The directory connection.
     * @throws UserStoreException If an error occurred while creating.
     */
    protected void createOrganizationalUnit(String orgName, Tenant tenant,
                                            DirContext initialDirContext)
            throws UserStoreException {
        //e.g: ou=wso2.com
        String partitionDN = tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ROOT_PARTITION);
        createOrganizationalContext(partitionDN, orgName, initialDirContext);

        //create user store
        String organizationNameAttribute = tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ORGANIZATIONAL_ATTRIBUTE);
        //eg:o=cse.org,dc=wso2,dc=com
        String dnOfOrganizationalContext = organizationNameAttribute + "=" + orgName + "," +
                                           partitionDN;
        if (tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_USER_CONTEXT_VALUE) != null) {
            createOrganizationalSubContext(dnOfOrganizationalContext, tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_USER_CONTEXT_VALUE), initialDirContext);
        } else {
            //if property value is not set use default value
            createOrganizationalSubContext(dnOfOrganizationalContext, LDAPConstants.USER_CONTEXT_NAME, initialDirContext);
        }

        //create group store
        if (tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_GROUP_CONTEXT_VALUE) != null) {
            createOrganizationalSubContext(dnOfOrganizationalContext, tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_GROUP_CONTEXT_VALUE), initialDirContext);
        } else {
            //if property value is not set use default value
            createOrganizationalSubContext(dnOfOrganizationalContext, LDAPConstants.GROUP_CONTEXT_NAME, initialDirContext);
        }

        //create admin entry
        String orgSubContextAttribute = tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);
        //eg: ou=users,o=cse.org,dc=wso2,dc=com
        String dnOfUserContext;
        if (tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_USER_CONTEXT_VALUE) != null) {
            dnOfUserContext = orgSubContextAttribute + "=" + tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_USER_CONTEXT_VALUE)
                    + "," + dnOfOrganizationalContext;
        } else {
            //if property value is not set use default value
            dnOfUserContext = orgSubContextAttribute + "=" + LDAPConstants.USER_CONTEXT_NAME
                    + "," + dnOfOrganizationalContext;
        }
        String dnOfUserEntry = createAdminEntry(dnOfUserContext, tenant, initialDirContext);

        //create admin group if write ldap group is enabled
        if (("true").equals(realmConfig.getUserStoreProperty(
                UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED))) {
            //construct dn of group context: eg:ou=groups,o=cse.org,dc=wso2,dc=com
            String dnOfGroupContext;
            if (tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_GROUP_CONTEXT_VALUE) != null) {
                dnOfGroupContext = orgSubContextAttribute + "=" + tenantMgtConfig.getTenantStoreProperties().get(
                        UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_GROUP_CONTEXT_VALUE) + "," +
                        dnOfOrganizationalContext;
            } else {
                //if property value is not set use default value
                dnOfGroupContext = orgSubContextAttribute + "=" + LDAPConstants.GROUP_CONTEXT_NAME + "," +
                        dnOfOrganizationalContext;
            }
            createAdminGroup(dnOfGroupContext, dnOfUserEntry, initialDirContext);
        }
    }

    /**
     * Create main context corresponding to tenant.
     *
     * @param rootDN            Root domain name.
     * @param orgName           Organization name
     * @param initialDirContext The directory connection.
     * @throws UserStoreException If an error occurred while creating context.
     */
    protected void createOrganizationalContext(String rootDN, String orgName,
                                               DirContext initialDirContext)
            throws UserStoreException {

        DirContext subContext = null;
        DirContext organizationalContext = null;
        try {

            //get the connection context for rootDN
            subContext = (DirContext) initialDirContext.lookup(rootDN);

            Attributes contextAttributes = new BasicAttributes(true);
            //create organizational object class attribute
            Attribute objectClass = new BasicAttribute(LDAPConstants.OBJECT_CLASS_NAME);
            objectClass.add(
                    tenantMgtConfig.getTenantStoreProperties().get(
                            UserCoreConstants.TenantMgtConfig.PROPERTY_ORGANIZATIONAL_OBJECT_CLASS));
            contextAttributes.put(objectClass);
            //create organizational name attribute
            String organizationalNameAttribute = tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORGANIZATIONAL_ATTRIBUTE);
            Attribute organization =
                    new BasicAttribute(organizationalNameAttribute);
            organization.add(orgName);
            contextAttributes.put(organization);
            //construct organization rdn.
            String rdnOfOrganizationalContext = organizationalNameAttribute + "=" + orgName;
            if (logger.isDebugEnabled()) {
                logger.debug("Adding sub context: " + rdnOfOrganizationalContext + " under " +
                             rootDN + " ...");
            }
            //create organization sub context
            organizationalContext = subContext.createSubcontext(rdnOfOrganizationalContext, contextAttributes);
            if (logger.isDebugEnabled()) {
                logger.debug("Sub context: " + rdnOfOrganizationalContext + " was added under "
                             + rootDN + " successfully.");
            }

        } catch (NamingException e) {
            String errorMsg = "Error occurred while adding the organizational unit " +
                              "sub context.";
            logger.error(errorMsg, e);
            throw new UserStoreException(errorMsg, e);
        } finally {
            closeContext(organizationalContext);
            closeContext(subContext);
        }
    }

    protected void closeContext(DirContext ldapContext) {
        if (ldapContext != null) {
            try {
                ldapContext.close();
            } catch (NamingException e) {
                logger.error("Error closing sub context.", e);
            }
        }
    }

    /**
     * Create sub contexts under the tenant's main context.
     *
     * @param dnOfParentContext    domain name of the parent context.
     * @param nameOfCurrentContext name of the current context.
     * @param initialDirContext    The directory connection.
     * @throws UserStoreException if an error occurs while creating context.
     */
    protected void createOrganizationalSubContext(String dnOfParentContext,
                                                  String nameOfCurrentContext,
                                                  DirContext initialDirContext)
            throws UserStoreException {

        DirContext subContext = null;
        DirContext organizationalContext = null;

        try {
            //get the connection for tenant's main context
            subContext = (DirContext) initialDirContext.lookup(dnOfParentContext);

            Attributes contextAttributes = new BasicAttributes(true);
            //create sub unit object class attribute
            Attribute objectClass = new BasicAttribute(LDAPConstants.OBJECT_CLASS_NAME);
            objectClass.add(tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_OBJ_CLASS));
            contextAttributes.put(objectClass);

            //create org sub unit name attribute
            String orgSubUnitAttributeName = tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);
            Attribute organizationSubUnit = new BasicAttribute(orgSubUnitAttributeName);
            organizationSubUnit.add(nameOfCurrentContext);
            contextAttributes.put(organizationSubUnit);

            //construct the rdn of org sub context
            String rdnOfOrganizationalContext = orgSubUnitAttributeName + "=" +
                                                nameOfCurrentContext;
            if (logger.isDebugEnabled()) {
                logger.debug("Adding sub context: " + rdnOfOrganizationalContext + " under " +
                             dnOfParentContext + " ...");
            }
            //create sub context
            organizationalContext = subContext.createSubcontext(rdnOfOrganizationalContext, contextAttributes);
            if (logger.isDebugEnabled()) {
                logger.debug("Sub context: " + rdnOfOrganizationalContext + " was added under "
                             + dnOfParentContext + " successfully.");
            }

        } catch (NamingException e) {
            String errorMsg = "Error occurred while adding the organizational unit " +
                              "sub context.";
            logger.error(errorMsg, e);
            throw new UserStoreException(errorMsg, e);
        } finally {
            closeContext(organizationalContext);
            closeContext(subContext);
        }
    }

    protected String createAdminEntry(String dnOfUserContext, Tenant tenant,
                                      DirContext initialDirContext)
            throws UserStoreException {
        String userDN = null;
        DirContext organizationalUsersContext = null;
        try {
            //get connection to tenant's user context
            organizationalUsersContext = (DirContext) initialDirContext.lookup(
                    dnOfUserContext);
            Attributes userAttributes = new BasicAttributes(true);

            //create person object class attribute
            Attribute objClass = new BasicAttribute(LDAPConstants.OBJECT_CLASS_NAME);
            objClass.add(realmConfig.getUserStoreProperty(LDAPConstants.USER_ENTRY_OBJECT_CLASS));
            if (UserCoreUtil.isKdcEnabled(realmConfig)) {
    			// Add Kerberos specific object classes
            	objClass.add("krb5principal");
            	objClass.add("krb5kdcentry");
            	objClass.add("subschema");
            	
                String principal = tenant.getAdminName() + UserCoreConstants.PRINCIPAL_USERNAME_SEPARATOR + tenant.getDomain() + UserCoreConstants.TENANT_DOMAIN_COMBINER + getRealmName();
                Attribute kerberosPrincipalName = new BasicAttribute("krb5PrincipalName");
                kerberosPrincipalName.add(principal);
                
                Attribute keyVersionNumber = new BasicAttribute("krb5KeyVersionNumber");
                keyVersionNumber.add("0");
                
                userAttributes.put(kerberosPrincipalName);
                userAttributes.put(keyVersionNumber);
    		}
            userAttributes.put(objClass);
            
           
            //create user password attribute
            Attribute password = new BasicAttribute(USER_PASSWORD_ATTRIBUTE_NAME);
            String passwordHashMethod = realmConfig.getUserStoreProperty(LDAPConstants.PASSWORD_HASH_METHOD);
            if(passwordHashMethod == null)  {
                passwordHashMethod =  realmConfig.getUserStoreProperty("passwordHashMethod");
            }
            String passwordToStore = UserCoreUtil.getPasswordToStore(
                    tenant.getAdminPassword(), passwordHashMethod, isKDCEnabled());
            password.add(passwordToStore);
            userAttributes.put(password);

            //create mail attribute
            Attribute adminEmail = new BasicAttribute(EMAIL_ATTRIBUTE_NAME);
            adminEmail.add(tenant.getEmail());
            userAttributes.put(adminEmail);
			
            //create compulsory attribute: sn-last name
            Attribute lastName = new BasicAttribute(SN_ATTRIBUTE_NAME);
            lastName.add(tenant.getAdminLastName());
            userAttributes.put(lastName);

            //read user name attribute in user-mgt.xml
            String userNameAttribute = realmConfig.getUserStoreProperty(
                    LDAPConstants.USER_NAME_ATTRIBUTE);

            //if user name attribute is not cn, add it to attribute list
            if (!(CN_ATTRIBUTE_NAME.equals(userNameAttribute))) {
                Attribute firstName = new BasicAttribute(CN_ATTRIBUTE_NAME);
                firstName.add(tenant.getAdminFirstName());
                userAttributes.put(firstName);
            }
            String userRDN = userNameAttribute + "=" + tenant.getAdminName();
            organizationalUsersContext.bind(userRDN, null, userAttributes);
            userDN = userRDN + "," + dnOfUserContext;
            //return (userRDN + dnOfUserContext);
        } catch (NamingException e) {
            String errorMsg = "Error occurred while creating Admin entry";
            logger.error(errorMsg, e);
            throw new UserStoreException(errorMsg, e);
        } finally {
            closeContext(organizationalUsersContext);
        }

        return userDN;
    }

    protected void createAdminGroup(String dnOfGroupContext, String adminUserDN,
                                    DirContext initialDirContext)
            throws UserStoreException {
        //create set of attributes required to create admin group
        Attributes adminGroupAttributes = new BasicAttributes(true);
        //admin entry object class
        Attribute objectClassAttribute = new BasicAttribute(LDAPConstants.OBJECT_CLASS_NAME);
        objectClassAttribute.add(realmConfig.getUserStoreProperty(
                LDAPConstants.GROUP_ENTRY_OBJECT_CLASS));
        adminGroupAttributes.put(objectClassAttribute);

        //group name attribute
        String groupNameAttributeName = realmConfig.getUserStoreProperty(
                LDAPConstants.GROUP_NAME_ATTRIBUTE);
        Attribute groupNameAttribute = new BasicAttribute(groupNameAttributeName);
        String adminRoleName = realmConfig.getAdminRoleName();
        groupNameAttribute.add(UserCoreUtil.removeDomainFromName(adminRoleName));
        adminGroupAttributes.put(groupNameAttribute);

        //membership attribute
        Attribute membershipAttribute = new BasicAttribute(realmConfig.getUserStoreProperty(
                LDAPConstants.MEMBERSHIP_ATTRIBUTE));
        membershipAttribute.add(adminUserDN);
        adminGroupAttributes.put(membershipAttribute);

        DirContext groupContext = null;
        try {
            groupContext = (DirContext) initialDirContext.lookup(dnOfGroupContext);
            String rdnOfAdminGroup = groupNameAttributeName + "=" + UserCoreUtil.removeDomainFromName(adminRoleName);
            groupContext.bind(rdnOfAdminGroup, null, adminGroupAttributes);

        } catch (NamingException e) {
            String errorMessage = "Error occurred while creating the admin group.";
            logger.error(errorMessage);
            throw new UserStoreException(errorMessage, e);
        } finally {
            closeContext(groupContext);
        }
    }

    private boolean isKDCEnabled() {
        return UserCoreUtil.isKdcEnabled(realmConfig);
    }

	public boolean isSharedGroupEnabled() {
		String value = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.SHARED_GROUPS_ENABLED);
		return realmConfig.isPrimary() && "true".equalsIgnoreCase(value);
	}

	public void addSharedGroupForTenant(Tenant tenant, DirContext mainDirContext) throws UserStoreException {

		if(!isSharedGroupEnabled()){
			return;
		}
		Attributes groupAttributes = new BasicAttributes(true);

		String domainName = tenant.getDomain();
		// create ou attribute
		String groupNameAttributeName =
                        realmConfig.getUserStoreProperty(LDAPConstants.SHARED_TENANT_NAME_ATTRIBUTE);

		// create group entry's object class attribute
		Attribute objectClassAttribute = new BasicAttribute(LDAPConstants.OBJECT_CLASS_NAME);
		objectClassAttribute.add(realmConfig.getUserStoreProperty(LDAPConstants.SHARED_TENANT_OBJECT_CLASS));
		groupAttributes.put(objectClassAttribute);

		DirContext groupContext = null;

		String searchBase =
		                    realmConfig.getUserStoreProperties()
		                               .get(LDAPConstants.SHARED_GROUP_SEARCH_BASE);

		try {
			groupContext = (DirContext) mainDirContext.lookup(searchBase);
			NameParser ldapParser = groupContext.getNameParser("");
			Name compoundGroupName = ldapParser.parse(groupNameAttributeName + "=" + domainName);
			groupContext.bind(compoundGroupName, null, groupAttributes);

		} catch (Exception e) {
			String errorMsg = "Shared tenant: " + domainName + "could not be added.";
			throw new UserStoreException(errorMsg, e);
		} finally {
			JNDIUtil.closeContext(groupContext);
		}

	}
    
	/**
	 * 
	 * @return
	 */
	protected String getRealmName() {

		// First check whether realm name is defined in the configuration
		String defaultRealmName = this.realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.DEFAULT_REALM_NAME);

		if (defaultRealmName != null) {
			return defaultRealmName;
		}

		// If not build the realm name from the search base.
		// Here the realm name will be a concatenation of dc components in the
		// search base.
		String searchBase = this.realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);

		String[] domainComponents = searchBase.split("dc=");

		StringBuilder builder = new StringBuilder();

		for (String dc : domainComponents) {
			if (!dc.contains("=")) {
				String trimmedDc = dc.trim();
				if (trimmedDc.endsWith(",")) {
					builder.append(trimmedDc.replace(',', '.'));
				} else {
					builder.append(trimmedDc);
				}
			}
		}

		return builder.toString().toUpperCase(Locale.ENGLISH);
	}
    
}
