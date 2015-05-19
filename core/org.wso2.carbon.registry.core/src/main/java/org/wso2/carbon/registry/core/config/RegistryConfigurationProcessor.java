/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.core.config;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.Aspect;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.CustomEditManager;
import org.wso2.carbon.registry.core.jdbc.handlers.EditProcessor;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerLifecycleManager;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.Filter;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Builds the registry configuration from xml document. Configuration has to be given as an input
 * stream. Registry configuration consists of details of data sources, handlers and aspects. These
 * information is extracted from the configuration populates the necessary components.
 */
@SuppressWarnings("unused")
public class RegistryConfigurationProcessor {

    private static final Log log = LogFactory.getLog(RegistryConfigurationProcessor.class);

    /**
     * Read XML configuration from the passed InputStream, or from the classpath.
     *
     * @param in              an InputStream containing XML data, or null.
     * @param registryContext the RegistryContext to populate
     *
     * @throws RegistryException if there's a problem
     */
    public static void populateRegistryConfig(InputStream in, RegistryContext registryContext)
            throws RegistryException {
        if (in == null) {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "org/wso2/carbon/registry/core/servlet/registry.xml");
            if (in == null) {
                return;
            }
        }

        try {
            StAXOMBuilder builder = new StAXOMBuilder(
                    CarbonUtils.replaceSystemVariablesInXml(in));
            OMElement configElement = builder.getDocumentElement();
            if (configElement != null) {

                OMElement registryRootEle =
                        configElement.getFirstChildWithName(new QName("registryRoot"));
                if (registryRootEle != null) {
                    String registryRoot = registryRootEle.getText();
                    if (registryRoot != null && !registryRoot.equals(RegistryConstants.ROOT_PATH)) {
                        if (registryRoot.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                            registryRoot = registryRoot.substring(0, registryRoot.length() - 1);
                        } else if (!registryRoot.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                            registryRoot = RegistryConstants.ROOT_PATH + registryRoot;
                        }
                    } else {
                        registryRoot = null;
                    }
                    registryContext.setRegistryRoot(registryRoot);
                }

                OMElement readOnlyEle =
                        configElement.getFirstChildWithName(new QName("readOnly"));
                if (readOnlyEle != null) {
                    registryContext.setReadOnly(CarbonUtils.isReadOnlyNode() ||
                            "true".equals(readOnlyEle.getText()));
                }

                OMElement enableCachingEle =
                        configElement.getFirstChildWithName(new QName("enableCache"));
                if (enableCachingEle != null) {
                    registryContext.setCacheEnabled("true".equals(enableCachingEle.getText()));
                }

                SecretResolver secretResolver = SecretResolverFactory.create(configElement, false);
                Iterator dbConfigs = configElement.getChildrenWithName(new QName("dbConfig"));
                // Read Database configurations
                while (dbConfigs.hasNext()) {
                    OMElement dbConfig = (OMElement) dbConfigs.next();
                    DataBaseConfiguration dataBaseConfiguration = new DataBaseConfiguration();

                    dataBaseConfiguration.setPasswordManager(secretResolver);
                    String dbName = dbConfig.getAttributeValue(new QName("name"));
                    if (dbName == null) {
                        throw new RegistryException("The database configuration name cannot be " +
                                "null.");
                    }
                    dataBaseConfiguration.setConfigName(dbName);
                    OMElement dataSource = dbConfig.getFirstChildWithName(new QName("dataSource"));
                    if (dataSource != null) {
                        String dataSourceName = dataSource.getText();
                        dataBaseConfiguration.setDataSourceName(dataSourceName);
                        try {
                            Context context = new InitialContext();
                            Connection connection = null;
                            try {
                                connection = ((DataSource) context.lookup(
                                        dataSourceName)).getConnection();
                                DatabaseMetaData metaData = connection.getMetaData();

                                // We need to obtain the connection URL and the username, which is
                                // required for building the cache key.
                                dataBaseConfiguration.setDbUrl(metaData.getURL());
                                dataBaseConfiguration.setUserName(metaData.getUserName());
                            } finally {
                                if (connection != null) {
                                    connection.close();
                                }
                            }
                        } catch (NamingException ignored) {
                            log.warn("Unable to look-up JNDI name " + dataSourceName);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            throw new RegistryException("Unable to connect to Data Source", e);
                        }
                    } else {
                        OMElement userName = dbConfig.getFirstChildWithName(new QName("userName"));
                        if (userName != null) {
                            dataBaseConfiguration.setUserName(userName.getText());
                        }

                        OMElement password = dbConfig.getFirstChildWithName(new QName("password"));
                        if (password != null) {
                            dataBaseConfiguration.setPassWord(password.getText());
                        }

                        OMElement url = dbConfig.getFirstChildWithName(new QName("url"));
                        String dbUrl = url.getText();
                        if (dbUrl != null) {
                            // If the connection URL contains ${carbon.home}, replace it with the
                            // corresponding value.
                            if (dbUrl.contains(CarbonConstants.CARBON_HOME_PARAMETER)) {
                                File carbonHomeDir;
                                carbonHomeDir = new File(CarbonUtils.getCarbonHome());
                                String path = carbonHomeDir.getPath();
                                path = path.replaceAll(Pattern.quote("\\"), "/");
                                if (carbonHomeDir.exists() && carbonHomeDir.isDirectory()) {
                                    dbUrl = dbUrl.replaceAll(
                                            Pattern.quote(CarbonConstants.CARBON_HOME_PARAMETER),
                                            path);
                                } else {
                                    log.warn("carbon home invalid");
                                    String[] tempStrings1 = dbUrl.split(
                                            Pattern.quote(CarbonConstants.CARBON_HOME_PARAMETER));
                                    String tempUrl = tempStrings1[1];
                                    String[] tempStrings2 = tempUrl.split("/");
                                    for (int i = 0; i < tempStrings2.length - 1; i++) {
                                        dbUrl = tempStrings1[0] + tempStrings2[i] + "/";
                                    }
                                    dbUrl = dbUrl + tempStrings2[tempStrings2.length - 1];
                                }

                                url.setText(dbUrl);
                            }
                        }
                        dataBaseConfiguration.setDbUrl(url.getText());


                        OMElement driverName =
                                dbConfig.getFirstChildWithName(new QName("driverName"));
                        if (driverName != null) {
                            dataBaseConfiguration.setDriverName(driverName.getText());
                        }

                        OMElement maxWait = dbConfig.getFirstChildWithName(new QName("maxWait"));
                        if (maxWait != null) {
                            dataBaseConfiguration.setMaxWait(maxWait.getText());
                        }

						OMElement testWhileIdle = dbConfig
								.getFirstChildWithName(new QName(
										"testWhileIdle"));
						if (testWhileIdle != null) {
							dataBaseConfiguration
									.setTestWhileIdle(testWhileIdle
											.getText());
						}
						
						OMElement timeBetweenEvictionRunsMillis = dbConfig
								.getFirstChildWithName(new QName(
										"timeBetweenEvictionRunsMillis"));
						if (timeBetweenEvictionRunsMillis != null) {
							dataBaseConfiguration
									.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis
											.getText());
						}
						
						OMElement minEvictableIdleTimeMillis = dbConfig
								.getFirstChildWithName(new QName(
										"minEvictableIdleTimeMillis"));
						if (minEvictableIdleTimeMillis != null) {
							dataBaseConfiguration
									.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis
											.getText());
						}
						
						OMElement numTestsPerEvictionRun = dbConfig
								.getFirstChildWithName(new QName(
										"numTestsPerEvictionRun"));
						if (numTestsPerEvictionRun != null) {
							dataBaseConfiguration
									.setNumTestsPerEvictionRun(numTestsPerEvictionRun
											.getText());
						}
						
                        OMElement maxActive =
                                dbConfig.getFirstChildWithName(new QName("maxActive"));
                        if (maxActive != null) {
                            dataBaseConfiguration.setMaxActive(maxActive.getText());
                        }

                        OMElement maxIdle = dbConfig.getFirstChildWithName(new QName("maxIdle"));
                        if (maxIdle != null) {
                            dataBaseConfiguration.setMaxIdle(maxIdle.getText());
                        }

                        OMElement minIdle = dbConfig.getFirstChildWithName(new QName("minIdle"));
                        if (minIdle != null) {
                            dataBaseConfiguration.setMinIdle(minIdle.getText());
                        }

                        OMElement validationQuery =
                                dbConfig.getFirstChildWithName(new QName("validationQuery"));
                        if (validationQuery != null) {
                            dataBaseConfiguration.setValidationQuery(validationQuery.getText());
                        }
                    }
                    registryContext.addDBConfig(dbName, dataBaseConfiguration);
                }

                // reading the cache configurations, if the config element is not present default values will be used
                OMElement cacheConfig = configElement.getFirstChildWithName(new QName("cacheConfig"));
                if (cacheConfig != null) {
                    String lastAccessedExpirationMillis = cacheConfig
                            .getFirstChildWithName(new QName("lastAccessedExpirationMillis")).getText();
                    String lastModifiedExpirationMillis = cacheConfig
                            .getFirstChildWithName(new QName("lastModifiedExpirationMillis")).getText();
                    registryContext.setLastAccessedExpirationMillis(Long.parseLong(lastAccessedExpirationMillis));
                    registryContext.setLastModifiedExpirationMillis(Long.parseLong(lastModifiedExpirationMillis));
                }

                // loading one-time start-up configurations
                OMElement staticConfigElement =
                        configElement.getFirstChildWithName(new QName("staticConfiguration"));
                if (staticConfigElement != null) {
                    Iterator staticConfigs = staticConfigElement.getChildElements();
                    while (staticConfigs.hasNext()) {
                        OMElement staticConfig = (OMElement) staticConfigs.next();

                        if (staticConfig.getLocalName().equals("versioningProperties")) {
                            String versioningProperties = staticConfig.getText();
                            StaticConfiguration
                                    .setVersioningProperties(versioningProperties.equals("true"));
                        } else if (staticConfig.getLocalName().equals("versioningComments")) {
                            String versioningComments = staticConfig.getText();
                            StaticConfiguration
                                    .setVersioningComments(versioningComments.equals("true"));
                        } else if (staticConfig.getLocalName().equals("versioningTags")) {
                            String versioningTags = staticConfig.getText();
                            StaticConfiguration.setVersioningTags(versioningTags.equals("true"));
                        } else if (staticConfig.getLocalName().equals("versioningRatings")) {
                            String versioningRatings = staticConfig.getText();
                            StaticConfiguration
                                    .setVersioningRatings(versioningRatings.equals("true"));
                        } else if (staticConfig.getLocalName().equals("versioningAssociations")) {
                            String versioningAssociations = staticConfig.getText();
                            StaticConfiguration.setVersioningAssociations(
                                    versioningAssociations.equals("true"));
                        } else if (staticConfig.getLocalName().equals("profilesPath")) {
                            String profilesPath = staticConfig.getText();
                            if (!profilesPath.startsWith(
                                    RegistryConstants.PATH_SEPARATOR)) {
                                //if user give the path like test or test/
                                profilesPath = RegistryConstants.PATH_SEPARATOR + profilesPath;
                            }
                            if (profilesPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                                profilesPath = profilesPath.substring(0, (profilesPath.length() -
                                        1)); //if user give the path like this /test/
                            }

                            if (profilesPath != null) {
                                if (profilesPath.startsWith(
                                        RegistryConstants.CONFIG_REGISTRY_BASE_PATH)) {
                                    registryContext.setProfilesPath(profilesPath);
                                } else {
                                    registryContext.setProfilesPath(
                                            RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                                                    profilesPath);
                                }
                            }
                        } else if (staticConfig.getLocalName().equals("servicePath")) {
                            String servicePath = staticConfig.getText();
                            if (!servicePath.startsWith(
                                    RegistryConstants.PATH_SEPARATOR)) {
                                //if user give the path like test or test/
                                servicePath = RegistryConstants.PATH_SEPARATOR + servicePath;
                            }
                            if (servicePath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                                servicePath = servicePath.substring(0, (servicePath.length() -
                                        1)); //if user give the path like this /test/
                            }

                            if (servicePath != null) {
                                if (servicePath.startsWith(
                                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)) {
                                    registryContext.setServicePath(servicePath);
                                } else {
                                    registryContext.setServicePath(
                                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                                    servicePath);
                                }
                            }
                        }
                    }
                }

                OMElement currentConfigElement =
                        configElement.getFirstChildWithName(new QName("currentDBConfig"));
                if (currentConfigElement == null) {
                    throw new RegistryException("The current database configuration is not " +
                            "defined.");
                }

                String currentConfigName = currentConfigElement.getText();
                readRemoteInstances(configElement, registryContext, secretResolver);
                readMounts(configElement, registryContext);
                DataBaseConfiguration dbConfiguration =
                        registryContext.selectDBConfig(currentConfigName);
                registryContext.setDefaultDataBaseConfiguration(dbConfiguration);

                OMElement versionConfig =
                        configElement.getFirstChildWithName(new QName("versionResourcesOnChange"));
                if (versionConfig != null && "true".equals(versionConfig.getText())) {
                    registryContext.setVersionOnChange(true);
                } else {
                registryContext.setVersionOnChange(false);              }
                initializeHandlers(configElement, registryContext);

                // process query processor config
                Iterator queryProcessors = configElement.
                        getChildrenWithName(new QName("queryProcessor"));
                while (queryProcessors.hasNext()) {

                    QueryProcessorConfiguration queryProcessorConfiguration =
                            new QueryProcessorConfiguration();

                    OMElement queryProcessorElement = (OMElement) queryProcessors.next();
                    OMElement queryType = queryProcessorElement.
                            getFirstChildWithName(new QName("queryType"));
                    if (queryType != null) {
                        queryProcessorConfiguration.setQueryType(queryType.getText());
                    }

                    OMElement processorName = queryProcessorElement.
                            getFirstChildWithName(new QName("processor"));
                    if (processorName != null) {
                        queryProcessorConfiguration.
                                setProcessorClassName(processorName.getText());
                    }

                    registryContext.addQueryProcessor(queryProcessorConfiguration);
                }

                initializeAspects(configElement, registryContext);

            }

        } catch (XMLStreamException e) {
            throw new RegistryException(e.getMessage());
        } catch (CarbonException e) {
            log.error("An error occurred during system variable replacement", e);
        }
    }

    /**
     * Obtains the registry configuration as XML element.
     *
     * @param registryContext the Registry Context used by this registry instance.
     *
     * @return AXIOM element containing registry configuration.
     */
    public static OMElement getRegistryConfigAsXML(RegistryContext registryContext) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement root = factory.createOMElement("wso2registry", null);

        if (registryContext.getDefaultDataBaseConfiguration() != null) {
            OMElement currentConfigElement = factory.createOMElement("currentConfig", null);
            currentConfigElement
                    .setText(registryContext.getDefaultDataBaseConfiguration().getConfigName());
            root.addChild(currentConfigElement);
        }

        Iterator values = registryContext.getDBConfigNames();
        while (values.hasNext()) {
            DataBaseConfiguration dataBaseConfiguration = (DataBaseConfiguration) values.next();
            OMElement config = factory.createOMElement("dbConfig", null);
            OMElement url = factory.createOMElement("url", null);
            url.setText(dataBaseConfiguration.getDbUrl());
            config.addChild(url);
            OMElement userName = factory.createOMElement("userName", null);
            userName.setText(dataBaseConfiguration.getUserName());
            config.addChild(userName);
            OMElement password = factory.createOMElement("password", null);
            password.setText(dataBaseConfiguration.getResolvedPassword());
            config.addChild(password);
            OMElement driverName = factory.createOMElement("driverName", null);
            driverName.setText(dataBaseConfiguration.getDriverName());
            config.addChild(driverName);
            config.addAttribute("name", dataBaseConfiguration.getConfigName(), null);
            root.addChild(config);
        }
        return root;
    }

    /**
     * Creates and initializes an aspect.
     *
     * @param configElement   the aspect configuration element.
     * @param registryContext the Registry Context used by this registry instance.
     *
     * @throws RegistryException if anything goes wrong.
     */
    public static void initializeAspects(OMElement configElement, RegistryContext registryContext)
            throws RegistryException {
        Iterator aspectElement = configElement.
                getChildrenWithName(new QName("aspect"));
        if (aspectElement != null) {
            while (aspectElement.hasNext()) {
                OMElement aspect = (OMElement) aspectElement.next();
                String name = aspect.getAttributeValue(new QName("name"));
//                Replacing  the hardcoded value with the constant
                registryContext.addAspect(name, buildAspect(aspect, name), MultitenantConstants.SUPER_TENANT_ID);
            }
        }
    }

    // common method to build an aspect
    private static Aspect buildAspect(OMElement aspect, String name) throws RegistryException {
        String clazz = aspect.getAttributeValue(new QName("class"));
        Aspect aspectInstance = null;
        try {
            if (name == null || clazz == null) {
                throw new RegistryException("Invalid aspect element , required " +
                        "values are missing " + aspect.toString());
            }
            Class handlerClass = RegistryUtils.loadClass(clazz);
            if (aspect.getChildElements().hasNext()) {
                try {
                    Constructor constructor =
                            handlerClass.getConstructor(OMElement.class);
                    try {
                        aspectInstance = (Aspect) constructor.newInstance(aspect);
                    } catch (Exception e) {
                        throw new RegistryException("Couldn't instantiate", e);
                    }
                } catch (NoSuchMethodException e) {
                    // Throw error because the specified config won't be used?
                }
            }

            if (aspectInstance == null) {
                aspectInstance = (Aspect) handlerClass.newInstance();
            }
            return aspectInstance;
        } catch (Exception e) {
            String msg = "Could not initialize custom aspects. Caused by: " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    /**
     * Updates an aspect based on given configuration.
     *
     * @param configElement the aspect configuration element.
     *
     * @return Created aspect
     * @throws RegistryException if anything goes wrong.
     */
    public static Aspect updateAspects(OMElement configElement) throws RegistryException {
        Iterator aspectElement = configElement.
                getChildrenWithName(new QName("aspect"));
        if (aspectElement != null) {
            OMElement aspect = (OMElement) aspectElement.next();
            String name = aspect.getAttributeValue(new QName("name"));
            return buildAspect(aspect, name);
        }
        return null;
    }

    // Creates and initializes a handler
    private static void initializeHandlers(OMElement configElement, RegistryContext registryContext)
            throws RegistryException {
        // process handler configurations
        CustomEditManager customEditManager = registryContext.getCustomEditManager();
        try {
            @SuppressWarnings("unchecked")
            Iterator<OMElement> handlerConfigs =
                    configElement.getChildrenWithName(new QName("handler"));
            String currentProfile = System.getProperty("profile", "default");
            while (handlerConfigs.hasNext()) {
                OMElement handlerConfigElement = handlerConfigs.next();
                String profileStr = handlerConfigElement.getAttributeValue(new QName("profiles"));
                if (profileStr != null){
                    String[] profiles = profileStr.split(",");
                    for (String profile : profiles) {
                        if (profile.trim().equals(currentProfile)) {
                            buildHandler(registryContext, customEditManager, handlerConfigElement, null);
                        }
                    }
                } else {
                    buildHandler(registryContext, customEditManager, handlerConfigElement, null);
                }
            }
        } catch (Exception e) {
            String msg = "Could not initialize custom handlers. Caused by: " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    /**
     * Updates a handler based on given configuration.
     *
     * @param configElement   the handler configuration element.
     * @param lifecyclePhase  the lifecycle phase to which this handler belongs. The possible values
     *                        are "default", "reporting" and "user".
     * @param registryContext the Registry Context used by this registry instance.
     *
     * @return Created handler
     * @throws RegistryException if anything goes wrong.
     */
    public static boolean updateHandler(OMElement configElement, RegistryContext registryContext,
                                        String lifecyclePhase)
            throws RegistryException {
        try {
            Iterator handlerConfigs =
                    configElement.getChildrenWithName(new QName("handler"));
            if (handlerConfigs != null) {
                OMElement handlerConfigElement = (OMElement) handlerConfigs.next();
                // We won't be adding custom edit processors for handlers inserted through the UI.
                // This is because the CustomEditManager is not MT aware.
                return buildHandler(registryContext, null, handlerConfigElement, lifecyclePhase);
            }
            return false;
        } catch (Exception e) {
            String msg = "Could not create custom handler. Caused by: " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    // common method to build a handler
    private static boolean buildHandler(RegistryContext registryContext,
                                        CustomEditManager customEditManager,
                                        OMElement handlerConfigElement,
                                        String lifecyclePhase)
            throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, UserStoreException {
        HandlerDefinitionObject handlerDefinitionObject =
                new HandlerDefinitionObject(customEditManager, handlerConfigElement).invoke();
        String[] methods = handlerDefinitionObject.getMethods();
        Filter filter = handlerDefinitionObject.getFilter();
        Handler handler = handlerDefinitionObject.getHandler();
        if (filter == null || handler == null) {
            return false;
        }
        if (lifecyclePhase != null) {
            if (handlerDefinitionObject.getTenantId() != MultitenantConstants.INVALID_TENANT_ID &&
                    !HandlerLifecycleManager.DEFAULT_SYSTEM_HANDLER_PHASE.equals(lifecyclePhase) &&
                    !HandlerLifecycleManager.USER_DEFINED_SYSTEM_HANDLER_PHASE.equals(
                            lifecyclePhase)) {
                CurrentSession.setCallerTenantId(handlerDefinitionObject.getTenantId());
                try {
                    // We need to swap the tenant id for this call, if the handler overrides the
                    // default value.
                    registryContext.getHandlerManager().addHandler(methods, filter,
                            handler, lifecyclePhase);
                } finally {
                    CurrentSession.removeCallerTenantId();
                }
            } else {
                registryContext.getHandlerManager().addHandler(methods, filter,
                        handler, lifecyclePhase);
            }
        } else {
            registryContext.getHandlerManager().addHandler(methods, filter, handler,
                    HandlerLifecycleManager.USER_DEFINED_SYSTEM_HANDLER_PHASE);
        }
        return true;
    }

    // reads remote instances from the configuration
    private static void readRemoteInstances(OMElement configElement,
                                            RegistryContext registryContext,
                                            SecretResolver secretResolver) throws RegistryException {
        try {
            @SuppressWarnings("unchecked")
            Iterator<OMElement> remoteConfigs =
                    configElement.getChildrenWithName(new QName("remoteInstance"));
            List<String> idList = new ArrayList<String>();

            while (remoteConfigs.hasNext()) {
                OMElement remoteConfigElement = remoteConfigs.next();

                String url = remoteConfigElement.getAttributeValue(new QName("url"));
                String id = remoteConfigElement.getFirstChildWithName(new QName("id")).getText();

                if (idList.contains(id)) {
                    String msg = "Two remote instances can't have the same id.";
                    log.error(msg);
                    throw new RegistryException(msg);
                }
                idList.add(id);

                String trustedUser = null;
                if (remoteConfigElement.getFirstChildWithName(new QName("username")) != null) {
                    trustedUser =
                            remoteConfigElement.getFirstChildWithName(new QName("username"))
                                    .getText();
                }
                String trustedPwd = null;
                if (remoteConfigElement.getFirstChildWithName(new QName("password")) != null) {
                    trustedPwd =
                            remoteConfigElement.getFirstChildWithName(new QName("password"))
                                    .getText();
                }
                String type = null;
                if (remoteConfigElement.getFirstChildWithName(new QName("type")) != null) {
                    type =
                            remoteConfigElement.getFirstChildWithName(new QName("type"))
                                    .getText();
                }
                String dbConfig = null;
                if (remoteConfigElement.getFirstChildWithName(new QName("dbConfig")) != null) {
                    dbConfig =
                            remoteConfigElement.getFirstChildWithName(new QName("dbConfig"))
                                    .getText();
                }
                String readOnly = null;
                if (remoteConfigElement.getFirstChildWithName(new QName("readOnly")) != null) {
                    readOnly =
                            remoteConfigElement.getFirstChildWithName(new QName("readOnly"))
                                    .getText();
                }
                String enableCache = null;
                if (remoteConfigElement.getFirstChildWithName(new QName("enableCache")) != null) {
                    enableCache =
                            remoteConfigElement.getFirstChildWithName(new QName("enableCache"))
                                    .getText();
                }
                String cacheId = null;
                if (remoteConfigElement.getFirstChildWithName(new QName("cacheId")) != null) {
                    cacheId =
                            remoteConfigElement.getFirstChildWithName(new QName("cacheId"))
                                    .getText();
                }
                String registryRoot = null;
                if (remoteConfigElement.getFirstChildWithName(new QName("registryRoot")) != null) {
                    registryRoot =
                            remoteConfigElement.getFirstChildWithName(new QName("registryRoot"))
                                    .getText();
                }

                RemoteConfiguration remoteConfiguration = new RemoteConfiguration();
                remoteConfiguration.setPasswordManager(secretResolver);
                remoteConfiguration.setId(id);
                remoteConfiguration.setUrl(url);
                remoteConfiguration.setTrustedUser(trustedUser);
                remoteConfiguration.setTrustedPwd(trustedPwd);
                remoteConfiguration.setType(type);
                remoteConfiguration.setDbConfig(dbConfig);
                remoteConfiguration.setReadOnly(readOnly);
                remoteConfiguration.setCacheEnabled(enableCache);
                remoteConfiguration.setCacheId(cacheId);
                remoteConfiguration.setRegistryRoot(registryRoot);

                registryContext.getRemoteInstances().add(remoteConfiguration);

            }
        } catch (Exception e) {
            String msg =
                    "Could not read remote instance configuration. Caused by: " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

    }

    // read mounts from configuration
    private static void readMounts(OMElement configElement,
                                   RegistryContext registryContext) throws RegistryException {
        try {
            @SuppressWarnings("unchecked")
            Iterator<OMElement> mounts =
                    configElement.getChildrenWithName(new QName("mount"));
            List<String> pathList = new ArrayList<String>();

            while (mounts.hasNext()) {
                OMElement mountElement = mounts.next();

                String path = mountElement.getAttributeValue(new QName("path"));
                if (path == null) {
                    String msg = "The path attribute was not specified for remote mount. " +
                            "Skipping creation of remote mount. " +
                            "Element: " + mountElement.toString();
                    log.warn(msg);
                    continue;    
                }
                if (pathList.contains(path)) {
                    String msg = "Two remote instances can't have the same path.";
                    log.error(msg);
                    throw new RegistryException(msg);
                }
                OMElement instanceIdElement = mountElement.getFirstChildWithName(
                        new QName("instanceId"));
                if (instanceIdElement == null) {
                    String msg = "The instance identifier was not specified for the mount: " + path;
                    log.warn(msg);
                    continue;
                }
                OMElement targetPathElement = mountElement.getFirstChildWithName(
                        new QName("targetPath"));
                if (targetPathElement == null) {
                    String msg = "The target path was not specified for the mount: " + path;
                    log.warn(msg);
                    continue;
                }
                pathList.add(path);
                String overwriteStr = mountElement.getAttributeValue(new QName("overwrite"));
                boolean overwrite = false;
                boolean virtual = false;
                if (overwriteStr != null) {
                    overwrite = Boolean.toString(true).equalsIgnoreCase(overwriteStr);
                    if (!overwrite) {
                        virtual = "virtual".equalsIgnoreCase(overwriteStr);
                    }
                }

                String resolveLinksElement = mountElement.getAttributeValue(new QName("resolveLinks"));
                boolean isExecuteQueryAllowed = true;
                if (resolveLinksElement != null && Boolean.toString(false).equalsIgnoreCase(resolveLinksElement)) {
                    isExecuteQueryAllowed = false;
                }

                String instanceId = instanceIdElement.getText();
                String targetPath = targetPathElement.getText();

                Mount mount = new Mount();
                mount.setPath(path);
                mount.setOverwrite(overwrite);
                mount.setVirtual(virtual);
                mount.setInstanceId(instanceId);
                mount.setTargetPath(targetPath);
                mount.setExecuteQueryAllowed(isExecuteQueryAllowed);

                registryContext.getMounts().add(mount);

            }
        } catch (Exception e) {
            String msg =
                    "Could not read remote instance configuration. Caused by: " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

    }

    // utility method to get setter name for a given property.
    private static String getSetterName(String varName) {

        String setterName;

        if (varName.length() == 1) {
            setterName = "set" + varName.substring(0, 1).toUpperCase();
        } else {
            setterName = "set" +
                    varName.substring(0, 1).toUpperCase() + varName.substring(1, varName.length());
        }

        return setterName;
    }

    /**
     * Object to store a handler definition
     */
    public static class HandlerDefinitionObject {

        private CustomEditManager customEditManager;
        private OMElement handlerConfigElement;
        private List<String> methods;
        private Handler handler;
        private Filter filter;
        private int tenantId;

        /**
         * Constructor accepting a handler configuration and the custom edit manager to use.
         *
         * @param customEditManager    the custom edit manager to use.
         * @param handlerConfigElement the handler configuration element.
         */
        public HandlerDefinitionObject(CustomEditManager customEditManager,
                                       OMElement handlerConfigElement) {
            this.customEditManager = customEditManager;
            this.handlerConfigElement = handlerConfigElement;
        }

        /**
         * Constructor accepting a handler configuration.
         *
         * @param handlerConfigElement the handler configuration element.
         */
        public HandlerDefinitionObject(OMElement handlerConfigElement) {
            this.customEditManager = null;
            this.handlerConfigElement = handlerConfigElement;
        }

        /**
         * Get methods to which this handler is engaged.
         *
         * @return array of methods
         */
        public String[] getMethods() {
            if (methods == null) {
                return null;
            }
            return methods.toArray(new String[methods.size()]);
        }

        /**
         * Gets the handler instance.
         *
         * @return the handler instance.
         */
        public Handler getHandler() {
            return handler;
        }

        /**
         * Gets the tenant identifier
         *
         * @return tenant id
         */
        public int getTenantId() {
            return tenantId;
        }

        /**
         * Gets the filter instance.
         *
         * @return the filter instance.
         */
        public Filter getFilter() {
            return filter;
        }

        /**
         * Builds a handler definition object from XML configuration
         *
         * @return the definition object
         * @throws InstantiationException    for errors in creating classes
         * @throws IllegalAccessException    for exceptions due to invisibility of methods
         * @throws NoSuchMethodException     for errors due to accessing non-existing methods.
         * @throws InvocationTargetException for errors in invoking methods or constructors.
         * @throws UserStoreException        if an error occurs in user management related
         *                                   operations.
         */
        public HandlerDefinitionObject invoke()
                throws InstantiationException, IllegalAccessException,
                NoSuchMethodException, InvocationTargetException, UserStoreException {
            String handlerClassName = handlerConfigElement.getAttributeValue(new QName("class"));
            String methodsValue = handlerConfigElement.getAttributeValue(new QName("methods"));
            String tenantIdString = handlerConfigElement.getAttributeValue(new QName("tenant"));
            tenantId = MultitenantConstants.INVALID_TENANT_ID;
            int tempTenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            // if the tenant id was found from the carbon context, it will be greater than -1. If not, it will be equal
            // to -1. Therefore, we need to check whether the carbon context had a tenant id and use it if it did.
            if (tempTenantId != MultitenantConstants.INVALID_TENANT_ID) {
                tenantId = tempTenantId;
            } else if (tenantIdString != null) {
                try {
                    tenantId = Integer.parseInt(tenantIdString);
                } catch (NumberFormatException ignore) {
                    RegistryContext context = RegistryContext.getBaseInstance();
                    if (context != null && context.getRealmService() != null) {
                        try {
                            tenantId = context.getRealmService().getTenantManager().getTenantId(
                                    tenantIdString);
                        } catch (org.wso2.carbon.user.api.UserStoreException e) {
                            throw new UserStoreException(e);
                        }
                    }
                }
            }

            String[] methods;
            if (methodsValue != null) {
                methods = methodsValue.split(",");
                for (int i = 0; i < methods.length; i++) {
                    methods[i] = methods[i].trim();
                }
                this.methods = Arrays.asList(methods);
            }

            Class handlerClass;
            try {
                handlerClass = RegistryUtils.loadClass(handlerClassName);
            } catch (ClassNotFoundException e) {
                String msg = "Could not find the handler class " + handlerClassName +
                        ". This handler will not be registered. All handler and " +
                        "filter classes should be in the class path of the Registry.";
                log.warn(msg);
                return this;
            }
            handler = (Handler) handlerClass.newInstance();

            // set configured properties of the handler object
            @SuppressWarnings("unchecked")
            Iterator<OMElement> handlerProps =
                    handlerConfigElement.getChildrenWithName(new QName("property"));
            while (handlerProps.hasNext()) {
                OMElement propElement = handlerProps.next();

                String propName = propElement.getAttributeValue(new QName("name"));
                String propType = propElement.getAttributeValue(new QName("type"));

                if (propType != null && "xml".equals(propType)) {

                    String setterName = getSetterName(propName);
                    Method setter = handlerClass.getMethod(setterName, OMElement.class);
                    setter.invoke(handler, propElement);

                } else {

                    String setterName = getSetterName(propName);
                    Method setter = handlerClass.getMethod(setterName, String.class);
                    String propValue = propElement.getText();
                    setter.invoke(handler, propValue);
                }
            }

            // initialize and configure the filter for this handler
            OMElement filterElement =
                    handlerConfigElement.getFirstChildWithName(new QName("filter"));
            String filterClassName = filterElement.getAttributeValue(new QName("class"));

            Class filterClass;
            try {
                filterClass = RegistryUtils.loadClass(filterClassName);
            } catch (ClassNotFoundException e) {
                String msg = "Could not find the filter class " +
                        filterClassName + ". " + handlerClassName +
                        " will not be registered. All configured handler, filter and " +
                        "edit processor classes should be in the class " +
                        "path of the Registry.";
                log.warn(msg);
                return this;
            }
            filter = (Filter) filterClass.newInstance();

            // set configured properties of the filter object
            @SuppressWarnings("unchecked")
            Iterator<OMElement> filterProps =
                    filterElement.getChildrenWithName(new QName("property"));
            while (filterProps.hasNext()) {
                OMElement propElement = filterProps.next();

                String propName = propElement.getAttributeValue(new QName("name"));
                String propValue = propElement.getText();

                String setterName = getSetterName(propName);
                Method setter = filterClass.getMethod(setterName, String.class);
                setter.invoke(filter, propValue);
            }
            if (customEditManager != null) {
                OMElement editElement =
                        handlerConfigElement.getFirstChildWithName(new QName("edit"));
                if (editElement != null) {
                    String processorKey = editElement.getAttributeValue(new QName("processor"));
                    String processorClassName = editElement.getText();

                    Class editProcessorClass;
                    try {
                        editProcessorClass = RegistryUtils.loadClass(processorClassName);
                    } catch (ClassNotFoundException e) {
                        String msg = "Could not find the edit processor class " +
                                processorClassName + ". " + handlerClassName +
                                " will not be registered. All configured handler, filter and " +
                                "edit processor classes should be in the class " +
                                "path of the Registry.";
                        log.warn(msg);
                        return this;
                    }
                    EditProcessor editProcessor = (EditProcessor) editProcessorClass.newInstance();

                    customEditManager.addProcessor(processorKey, editProcessor);
                }
            }
            return this;
        }
    }
}
