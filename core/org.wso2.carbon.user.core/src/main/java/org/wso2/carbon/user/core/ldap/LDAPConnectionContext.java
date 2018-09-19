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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.dto.CorrelationLogDTO;
import org.wso2.carbon.utils.Secret;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.UnsupportedSecretTypeException;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

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

    private static final Log correlationLog = LogFactory.getLog("CORRELATION_LOG");

    private static String initialContextFactoryClass = "com.sun.jndi.dns.DnsContextFactory";

    private static final String CORRELATION_LOG_TIME_TAKEN_KEY = "delta";
    private static final String CORRELATION_LOG_TIME_TAKEN_UNIT = " ms";
    private static final String CORRELATION_LOG_CALL_TYPE_KEY = "callType";
    private static final String CORRELATION_LOG_CALL_TYPE_VALUE = "ldap";
    private static final String CORRELATION_LOG_START_TIME_KEY = "startTime";
    private static final String CORRELATION_LOG_METHOD_NAME_KEY = "methodName";
    private static final String CORRELATION_LOG_INITIALIZATION_METHOD_NAME = "initialization";
    private static final String CORRELATION_LOG_INITIALIZATION_ARGS = "empty";
    private static final int CORRELATION_LOG_INITIALIZATION_ARGS_LENGTH = 0;
    private static final String CORRELATION_LOG_ARGS_KEY = "query";
    private static final String CORRELATION_LOG_ARGS_LENGTH_KEY = "query";
    private static final String CORRELATION_LOG_PROVIDER_URL_KEY = "providerUrl";
    private static final String CORRELATION_LOG_PRINCIPAL_KEY = "principal";
    private static final String CORRELATION_LOG_SEPARATOR = " | ";
    private static final String CORRELATION_LOG_SYSTEM_PROPERTY = "enableCorrelationLogs";

    static {
        String initialContextFactoryClassSystemProperty = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
        if (initialContextFactoryClassSystemProperty != null && initialContextFactoryClassSystemProperty.length() > 0) {
            initialContextFactoryClass = initialContextFactoryClassSystemProperty;
        }
    }

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
                environmentForDNS.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactoryClass);
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

        if (StringUtils.isNotEmpty(readTimeout)) {
            environment.put("com.sun.jndi.ldap.read.timeout", readTimeout);
        }
    }

    public DirContext getContext() throws UserStoreException {

        DirContext context = null;
        //if dcMap is not populated, it is not DNS case
        if (dcMap == null) {
            try {
                context = getDirContext(environment);

            } catch (NamingException e) {
                log.error("Error obtaining connection. " + e.getMessage(), e);
                log.error("Trying again to get connection.");

                try {
                    context = getDirContext(environment);
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
                context = getDirContext(environment);

            } catch (NamingException e) {
                log.error("Error obtaining connection to first Domain Controller." + e.getMessage(), e);
                log.info("Trying to connect with other Domain Controllers");

                for (Integer integer : dcMap.keySet()) {
                    try {
                        SRVRecord srv = dcMap.get(integer);
                        environment.put(Context.PROVIDER_URL, getLDAPURLFromSRVRecord(srv));
                        context = getDirContext(environment);
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
        return context;
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public void updateCredential(String connectionPassword) {
        /*
         * update the password otherwise it is not possible to connect again if admin password
         * changed
         */
        this.environment.put(Context.SECURITY_CREDENTIALS, connectionPassword);
    }

    /**
     * Updates the connection password
     *
     * @param connectionPassword
     */
    public void updateCredential(Object connectionPassword) throws UserStoreException {

        /*
         * update the password otherwise it is not possible to connect again if admin password
         * changed
         */
        Secret connectionPasswordObj;
        try {
            connectionPasswordObj = Secret.getSecret(connectionPassword);
        } catch (UnsupportedSecretTypeException e) {
            throw new UserStoreException("Unsupported credential type", e);
        }

        byte[] passwordBytes = connectionPasswordObj.getBytes();
        this.environment.put(Context.SECURITY_CREDENTIALS, Arrays.copyOf(passwordBytes, passwordBytes.length));

        connectionPasswordObj.clear();
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

    @Deprecated
    public LdapContext getContextWithCredentials(String userDN, String password)
            throws UserStoreException, NamingException, AuthenticationException {

        //create a temp env for this particular authentication session by copying the original env
        // following logic help to re use the connection pool in authentication
        Hashtable<String, String> tempEnv = new Hashtable<String, String>();
        for (Object key : environment.keySet()) {
            if (Context.SECURITY_PRINCIPAL.equals((String) key) || Context.SECURITY_CREDENTIALS.equals((String) key)
                    || Context.SECURITY_AUTHENTICATION.equals((String) key)) {
                // skip adding to environment
            } else {
                tempEnv.put((String) key, (String) environment.get(key));
            }
        }

        tempEnv.put(Context.SECURITY_AUTHENTICATION, "none");

        return getContextForEnvironmentVariables(tempEnv);
    }

    /**
     * Returns the LDAPContext for the given credentials
     *
     * @param userDN   user DN
     * @param password user password
     * @return returns The LdapContext instance if credentials are valid
     * @throws UserStoreException
     * @throws NamingException
     */
    public LdapContext getContextWithCredentials(String userDN, Object password)
            throws UserStoreException, NamingException {

        Secret credentialObj;
        try {
            credentialObj = Secret.getSecret(password);
        } catch (UnsupportedSecretTypeException e) {
            throw new UserStoreException("Unsupported credential type", e);
        }

        try {
            //create a temp env for this particular authentication session by copying the original env
            Hashtable<String, Object> tempEnv = new Hashtable<>();
            for (Object key : environment.keySet()) {
                tempEnv.put((String) key, environment.get(key));
            }
            //replace connection name and password with the passed credentials to this method
            tempEnv.put(Context.SECURITY_PRINCIPAL, userDN);
            tempEnv.put(Context.SECURITY_CREDENTIALS, credentialObj.getBytes());

            return getContextForEnvironmentVariables(tempEnv);
        } finally {
            credentialObj.clear();
        }
    }

    private LdapContext getContextForEnvironmentVariables(Hashtable<?, ?> environment)
            throws UserStoreException, NamingException {

        LdapContext context = null;

        Hashtable<Object, Object> tempEnv = new Hashtable<>();
        tempEnv.putAll(environment);
        //if dcMap is not populated, it is not DNS case
        if (dcMap == null) {
            //replace environment properties with these credentials
            context = getLdapContext(tempEnv, null);
        } else if (dcMap != null && dcMap.size() != 0) {
            try {
                //first try the first entry in dcMap, if it fails, try iteratively
                Integer firstKey = dcMap.firstKey();
                SRVRecord firstRecord = dcMap.get(firstKey);
                //compose the connection URL
                tempEnv.put(Context.PROVIDER_URL, getLDAPURLFromSRVRecord(firstRecord));
                context = getLdapContext(tempEnv, null);

            } catch (AuthenticationException e) {
                throw e;
            } catch (NamingException e) {
                log.error("Error obtaining connection to first Domain Controller.", e);
                log.info("Trying to connect with other Domain Controllers");

                for (Integer integer : dcMap.keySet()) {
                    try {
                        SRVRecord srv = dcMap.get(integer);
                        tempEnv.put(Context.PROVIDER_URL, getLDAPURLFromSRVRecord(srv));
                        context = getLdapContext(environment, null);
                        break;
                    } catch (AuthenticationException e1) {
                        throw e1;
                    } catch (NamingException e1) {
                        if (integer == (dcMap.lastKey())) {
                            throw new UserStoreException(
                                    "Error obtaining connection for all " + integer + " Domain Controllers.", e1);
                        }
                    }
                }
            }
        }
        return context;
    }

    /**
     * Creates the proxy for directory context and wrap the context.
     * Calculate the time taken for creation
     *
     * @param environment Contains all the environment details
     * @return The wrapped context
     * @throws NamingException
     */
    private DirContext getDirContext(Hashtable<?, ?> environment) throws NamingException {

        if (Boolean.parseBoolean(System.getProperty(CORRELATION_LOG_SYSTEM_PROPERTY))) {
            final Class[] proxyInterfaces = new Class[]{DirContext.class};
            long start = System.currentTimeMillis();

            DirContext context = new InitialDirContext(environment);

            Object proxy = Proxy.newProxyInstance(LDAPConnectionContext.class.getClassLoader(), proxyInterfaces,
                    new LdapContextInvocationHandler(context));

            long delta = System.currentTimeMillis() - start;

            CorrelationLogDTO correlationLogDTO = new CorrelationLogDTO();
            correlationLogDTO.setStartTime(start);
            correlationLogDTO.setDelta(delta);
            correlationLogDTO.setEnvironment(environment);
            correlationLogDTO.setMethodName(CORRELATION_LOG_INITIALIZATION_METHOD_NAME);
            correlationLogDTO.setArgsLength(CORRELATION_LOG_INITIALIZATION_ARGS_LENGTH);
            correlationLogDTO.setArgs(CORRELATION_LOG_INITIALIZATION_ARGS);
            logDetails(correlationLogDTO);
            return (DirContext) proxy;
        } else {
            return new InitialDirContext(environment);
        }
    }

    /**
     * Creates the proxy for LDAP context and wrap the context.
     * Calculate the time taken for creation
     *
     * @param environment        Contains all the environment details
     * @param connectionControls The wrapped context
     * @return ldap connection context
     * @throws NamingException
     */
    private LdapContext getLdapContext(Hashtable<?, ?> environment, Control[] connectionControls)
            throws NamingException {

        if (Boolean.parseBoolean(System.getProperty(CORRELATION_LOG_SYSTEM_PROPERTY))) {
            final Class[] proxyInterfaces = new Class[]{LdapContext.class};
            long start = System.currentTimeMillis();

            LdapContext context = new InitialLdapContext(environment, connectionControls);

            Object proxy = Proxy.newProxyInstance(LDAPConnectionContext.class.getClassLoader(), proxyInterfaces,
                    new LdapContextInvocationHandler(context));

            long delta = System.currentTimeMillis() - start;

            CorrelationLogDTO correlationLogDTO = new CorrelationLogDTO();
            correlationLogDTO.setStartTime(start);
            correlationLogDTO.setDelta(delta);
            correlationLogDTO.setEnvironment(environment);
            correlationLogDTO.setMethodName(CORRELATION_LOG_INITIALIZATION_METHOD_NAME);
            correlationLogDTO.setArgsLength(CORRELATION_LOG_INITIALIZATION_ARGS_LENGTH);
            correlationLogDTO.setArgs(CORRELATION_LOG_INITIALIZATION_ARGS);
            logDetails(correlationLogDTO);
            return (LdapContext) proxy;
        } else {
            return new InitialLdapContext(environment, connectionControls);
        }
    }

    /**
     * Proxy Class that is used to calculate and log the time taken for queries
     */
    private class LdapContextInvocationHandler implements InvocationHandler {

        private Object previousContext;

        public LdapContextInvocationHandler(Object previousContext) {

            this.previousContext = previousContext;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            long start = System.currentTimeMillis();
            Object result = method.invoke(this.previousContext, args);
            long delta = System.currentTimeMillis() - start;
            String methodName = method.getName();
            int argsLength = 0;

            if (args != null) {
                argsLength = args.length;
            }

            if (!StringUtils.equalsIgnoreCase("close", methodName)) {
                CorrelationLogDTO correlationLogDTO = new CorrelationLogDTO();
                correlationLogDTO.setStartTime(start);
                correlationLogDTO.setDelta(delta);
                correlationLogDTO.setEnvironment(((DirContext) this.previousContext).getEnvironment());
                correlationLogDTO.setMethodName(methodName);
                correlationLogDTO.setArgsLength(argsLength);
                correlationLogDTO.setArgs(stringify(args));
                logDetails(correlationLogDTO);
            }
            return result;
        }

        /**
         * Creates a argument string by appending the values in the array
         *
         * @param arr Arguments
         * @return Argument string
         */
        private String stringify(Object[] arr) {

            StringBuilder sb = new StringBuilder();
            if (arr == null) {
                sb.append("null");
            } else {
                sb.append(" ");
                for (int i = 0; i < arr.length; i++) {
                    Object o = arr[i];
                    sb.append(o.toString());
                    if (i < arr.length - 1) {
                        sb.append(",");
                    }
                }
            }
            return sb.toString();
        }
    }

    /**
     * Logs the details from the LDAP query
     *
     * @param correlationLogDTO Contains all details that should be to logged
     */
    private void logDetails(CorrelationLogDTO correlationLogDTO) {

        String providerUrl = " ";
        String principal = " ";

        if (correlationLogDTO.getEnvironment().containsKey("java.naming.provider.url")) {
            providerUrl = (String) environment.get("java.naming.provider.url");
        }

        if (environment.containsKey("java.naming.security.principal")) {
            principal = (String) environment.get("java.naming.security.principal");
        }

        if (correlationLog.isDebugEnabled()) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put(CORRELATION_LOG_TIME_TAKEN_KEY, Long.toString(correlationLogDTO.getDelta()) +
                    CORRELATION_LOG_TIME_TAKEN_UNIT);
            map.put(CORRELATION_LOG_CALL_TYPE_KEY, CORRELATION_LOG_CALL_TYPE_VALUE);
            map.put(CORRELATION_LOG_START_TIME_KEY, Long.toString(correlationLogDTO.getStartTime()));
            map.put(CORRELATION_LOG_METHOD_NAME_KEY, correlationLogDTO.getMethodName());
            map.put(CORRELATION_LOG_PROVIDER_URL_KEY, providerUrl);
            map.put(CORRELATION_LOG_PRINCIPAL_KEY, principal);
            map.put(CORRELATION_LOG_ARGS_LENGTH_KEY, Integer.toString(correlationLogDTO.getArgsLength()));
            map.put(CORRELATION_LOG_ARGS_KEY, correlationLogDTO.getArgs());
            correlationLog.debug(createLogFormat(map));
        }
    }

    /**
     * Creates the log line that should be printed
     *
     * @param map Contains the type and value that should be printed in the log
     * @return The log line
     */
    private String createLogFormat(Map<String, String> map) {

        StringBuilder sb = new StringBuilder();
        Object[] keys = map.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            sb.append(map.get(keys[i]));
            if (i < keys.length - 1) {
                sb.append(CORRELATION_LOG_SEPARATOR);
            }
        }
        return sb.toString();
    }
}
