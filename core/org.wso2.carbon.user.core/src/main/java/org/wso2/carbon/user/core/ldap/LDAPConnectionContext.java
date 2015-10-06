/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.user.core.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;
import java.util.SortedMap;
import java.util.TreeMap;

public class LDAPConnectionContext {

    private static Log log = LogFactory.getLog(LDAPConnectionContext.class);
    @SuppressWarnings("rawtypes")
    private Hashtable environment;
    private SortedMap<Integer, SRVRecord> dcMap;

    private Hashtable environmentForDNS;

    private String DNSDomainName;

    private boolean readOnly = false;

    private static final String CONNECTION_TIME_OUT = "LDAPConnectionTimeout";

    private static final String READ_TIME_OUT = "ReadTimeout";

    @SuppressWarnings({"rawtypes", "unchecked"})
    public LDAPConnectionContext(RealmConfiguration realmConfig) throws UserStoreException {

        //if DNS is enabled, populate DC Map
        String DNSUrl = realmConfig.getUserStoreProperty(LDAPConstants.DNS_URL);
        if (DNSUrl != null) {
            DNSDomainName = realmConfig.getUserStoreProperty(LDAPConstants.DNS_DOMAIN_NAME);
            if (DNSDomainName == null) {
                throw new UserStoreException("DNS is enabled, but DNS domain name not provided.");
            } else {
                environmentForDNS = new Hashtable();
                environmentForDNS.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
                environmentForDNS.put("java.naming.provider.url", DNSUrl);
                populateDCMap();
            }
            //need to keep track of if the user store config is read only
            String readOnlyString = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_READ_ONLY);
            if (readOnlyString != null) {
                readOnly = Boolean.parseBoolean(readOnlyString);
            }
        }

        String rawConnectionURL = realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_URL);
        String connectionURL = null;
        //if DNS enabled in AD case, this can be null
        if (rawConnectionURL != null) {
            String portInfo = rawConnectionURL.split(":")[2];

            String port = null;

            // if the port contains a template string that refers to carbon.xml
            if ((portInfo.contains("${")) && (portInfo.contains("}"))) {
                port = Integer.toString(CarbonUtils.getPortFromServerConfig(portInfo));
            }

            if (port != null) {
                connectionURL = rawConnectionURL.replace(portInfo, port);
            } else {
                // if embedded-ldap is not enabled,
                connectionURL = realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_URL);
            }
        }

        String connectionName = realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_NAME);
        String connectionPassword = realmConfig
                .getUserStoreProperty(LDAPConstants.CONNECTION_PASSWORD);

        if (log.isDebugEnabled()) {
            log.debug("Connection Name :: " + connectionName + ", Connection URL :: " + connectionURL);
        }

        environment = new Hashtable();

        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.SECURITY_AUTHENTICATION, "simple");

        /**
         * In carbon JNDI context we need to by pass specific tenant context and we need the base
         * context for LDAP operations.
         */
        environment.put(CarbonConstants.REQUEST_BASE_CONTEXT, "true");

        if (connectionName != null) {
            environment.put(Context.SECURITY_PRINCIPAL, connectionName);
        }

        if (connectionPassword != null) {
            environment.put(Context.SECURITY_CREDENTIALS, connectionPassword);
        }

        if (connectionURL != null) {
            environment.put(Context.PROVIDER_URL, connectionURL);
        }

        // Enable connection pooling if property is set in user-mgt.xml
        boolean isLDAPConnectionPoolingEnabled = false;
        String value = realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_POOLING_ENABLED);

        if (value != null && !value.trim().isEmpty()) {
            isLDAPConnectionPoolingEnabled = Boolean.parseBoolean(value);
        }

        environment.put("com.sun.jndi.ldap.connect.pool", isLDAPConnectionPoolingEnabled ? "true" : "false");

        // set referral status if provided in configuration.
        if (realmConfig.getUserStoreProperty(LDAPConstants.PROPERTY_REFERRAL) != null) {
            environment.put("java.naming.referral",
                    realmConfig.getUserStoreProperty(LDAPConstants.PROPERTY_REFERRAL));
        }

        String binaryAttribute = realmConfig.getUserStoreProperty(LDAPConstants.LDAP_ATTRIBUTES_BINARY);

        if (binaryAttribute != null) {
            environment.put(LDAPConstants.LDAP_ATTRIBUTES_BINARY, binaryAttribute);
        }

        //Set connect timeout if provided in configuration. Otherwise set default value
        String connectTimeout = realmConfig.getUserStoreProperty(CONNECTION_TIME_OUT);
        String readTimeout = realmConfig.getUserStoreProperty(READ_TIME_OUT);
        if (connectTimeout != null && !connectTimeout.trim().isEmpty()) {
            environment.put("com.sun.jndi.ldap.connect.timeout", connectTimeout);
        } else {
            environment.put("com.sun.jndi.ldap.connect.timeout", "5000");
        }

        if(readTimeout != null && !readTimeout.trim().isEmpty()){
            environment.put("com.sun.jndi.ldap.read.timeout",readTimeout);
        }
    }

    public DirContext getContext() throws UserStoreException {
        DirContext context = null;
        //if dcMap is not populated, it is not DNS case
        if (dcMap == null) {
            try {
                context = new InitialDirContext(environment);

            } catch (NamingException e) {
                log.error("Error obtaining connection. " + e.getMessage(), e);
                log.error("Trying again to get connection.");

                try {
                    context = new InitialDirContext(environment);
                } catch (Exception e1) {
                    log.error("Error obtaining connection for the second time" + e.getMessage(), e);
                    throw new UserStoreException("Error obtaining connection. " + e.getMessage(), e);
                }

            }
        } else if (dcMap != null && dcMap.size() != 0) {
            try {
                //first try the first entry in dcMap, if it fails, try iteratively
                Integer firstKey = dcMap.firstKey();
                SRVRecord firstRecord = dcMap.get(firstKey);
                //compose the connection URL
                environment.put(Context.PROVIDER_URL, getLDAPURLFromSRVRecord(firstRecord));
                context = new InitialDirContext(environment);

            } catch (NamingException e) {
                log.error("Error obtaining connection to first Domain Controller." + e.getMessage(), e);
                log.info("Trying to connect with other Domain Controllers");

                for (Integer integer : dcMap.keySet()) {
                    try {
                        SRVRecord srv = dcMap.get(integer);
                        environment.put(Context.PROVIDER_URL, getLDAPURLFromSRVRecord(srv));
                        context = new InitialDirContext(environment);
                        break;
                    } catch (NamingException e1) {
                        if (integer == (dcMap.lastKey())) {
                            log.error("Error obtaining connection for all " + integer + " Domain Controllers."
                                    + e.getMessage(), e);
                            throw new UserStoreException("Error obtaining connection. " + e.getMessage(), e);
                        }
                    }
                }
            }
        }
        return (context);

    }

    @SuppressWarnings("unchecked")
    public void updateCredential(String connectionPassword) {
        /*
         * update the password otherwise it is not possible to connect again if admin password
         * changed
         */
        this.environment.put(Context.SECURITY_CREDENTIALS, connectionPassword);
    }

    private void populateDCMap() throws UserStoreException {
        try {
            //get the directory context for DNS
            DirContext dnsContext = new InitialDirContext(environmentForDNS);
            //compose the DNS service to be queried
            String DNSServiceName = LDAPConstants.ACTIVE_DIRECTORY_DOMAIN_CONTROLLER_SERVICE + DNSDomainName;
            //query the DNS
            Attributes attributes = dnsContext.getAttributes(DNSServiceName, new String[]{LDAPConstants.SRV_ATTRIBUTE_NAME});
            Attribute srvRecords = attributes.get(LDAPConstants.SRV_ATTRIBUTE_NAME);
            //there can be multiple records with same domain name - get them all
            NamingEnumeration srvValues = srvRecords.getAll();
            dcMap = new TreeMap<Integer, SRVRecord>();
            //extract all SRV Records for _ldap._tcp service under the specified domain and populate dcMap
            //int forcedPriority = 0;
            while (srvValues.hasMore()) {
                String value = srvValues.next().toString();
                SRVRecord srvRecord = new SRVRecord();
                String valueItems[] = value.split(" ");
                String priority = valueItems[0];
                if (priority != null) {
                    int priorityInt = Integer.parseInt(priority);

                    /*if ((priorityInt == forcedPriority) || (priorityInt < forcedPriority)) {
                        forcedPriority++;
                        priorityInt = forcedPriority;
                    }*/
                    srvRecord.setPriority(priorityInt);
                }/* else {
                    forcedPriority++;
                    srvRecord.setPriority(forcedPriority);
                }*/
                String weight = valueItems[1];
                if (weight != null) {
                    srvRecord.setWeight(Integer.parseInt(weight));
                }
                String port = valueItems[2];
                if (port != null) {
                    srvRecord.setPort(Integer.parseInt(port));
                }
                String host = valueItems[3];
                if (host != null) {
                    srvRecord.setHostName(host);
                }
                //we index dcMap on priority basis, therefore, priorities must be different
                dcMap.put(srvRecord.getPriority(), srvRecord);
            }
            //iterate over the SRVRecords for Active Directory Domain Controllers and figure out the
            //host records for that
            for (SRVRecord srvRecord : dcMap.values()) {
                Attributes hostAttributes = dnsContext.getAttributes(
                        srvRecord.getHostName(), new String[]{LDAPConstants.A_RECORD_ATTRIBUTE_NAME});
                Attribute hostRecord = hostAttributes.get(LDAPConstants.A_RECORD_ATTRIBUTE_NAME);
                //we know there is only one IP value for a given host. So we do just get, not getAll
                srvRecord.setHostIP((String) hostRecord.get());
            }
        } catch (NamingException e) {
            log.error("Error obtaining information from DNS Server" + e.getMessage(), e);
            throw new UserStoreException("Error obtaining information from DNS Server " + e.getMessage(), e);
        }
    }

    private String getLDAPURLFromSRVRecord(SRVRecord srvRecord) {
        String ldapURL = null;
        if (readOnly) {
            ldapURL = "ldap://" + srvRecord.getHostIP() + ":" + srvRecord.getPort();
        } else {
            ldapURL = "ldaps://" + srvRecord.getHostIP() + ":" + srvRecord.getPort();
        }
        return ldapURL;
    }

    public LdapContext getContextWithCredentials(String userDN, String password)
            throws UserStoreException, NamingException, AuthenticationException {
        LdapContext context = null;

        //create a temp env for this particular authentication session by copying the original env
        Hashtable<String, String> tempEnv = new Hashtable<String, String>();
        for (Object key : environment.keySet()) {
            tempEnv.put((String) key, (String) environment.get(key));
        }
        //replace connection name and password with the passed credentials to this method
        tempEnv.put(Context.SECURITY_PRINCIPAL, userDN);
        tempEnv.put(Context.SECURITY_CREDENTIALS, password);

        //if dcMap is not populated, it is not DNS case
        if (dcMap == null) {

            //replace environment properties with these credentials
            context = new InitialLdapContext(tempEnv, null);


        } else if (dcMap != null && dcMap.size() != 0) {
            try {
                //first try the first entry in dcMap, if it fails, try iteratively
                Integer firstKey = dcMap.firstKey();
                SRVRecord firstRecord = dcMap.get(firstKey);
                //compose the connection URL
                tempEnv.put(Context.PROVIDER_URL, getLDAPURLFromSRVRecord(firstRecord));
                context = new InitialLdapContext(tempEnv, null);

            } catch (AuthenticationException e) {
                throw e;

            } catch (NamingException e) {
                log.error("Error obtaining connection to first Domain Controller." + e.getMessage(), e);
                log.info("Trying to connect with other Domain Controllers");

                for (Integer integer : dcMap.keySet()) {
                    try {
                        SRVRecord srv = dcMap.get(integer);
                        environment.put(Context.PROVIDER_URL, getLDAPURLFromSRVRecord(srv));
                        context = new InitialLdapContext(environment, null);
                        break;
                    } catch (AuthenticationException e2) {
                        throw e2;
                    } catch (NamingException e1) {
                        if (integer == (dcMap.lastKey())) {
                            log.error("Error obtaining connection for all " + integer + " Domain Controllers."
                                    + e1.getMessage(), e1);
                            throw new UserStoreException("Error obtaining connection. " + e1.getMessage(), e1);
                        }
                    }
                }
            }
        }
        return (context);
    }

}
