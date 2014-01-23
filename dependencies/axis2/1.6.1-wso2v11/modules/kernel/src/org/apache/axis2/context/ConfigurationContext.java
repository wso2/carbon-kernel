/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
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


package org.apache.axis2.context;

import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.management.NodeManager;
import org.apache.axis2.clustering.state.StateManager;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.modules.Module;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.OnDemandLogger;
import org.apache.axis2.util.threadpool.ThreadFactory;
import org.apache.axis2.util.threadpool.ThreadPool;

import java.io.File;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Axis2 states are held in two information models, called description hierarchy and context
 * hierarchy. Description hierarchy hold deployment configuration and it's values does not change
 * unless deployment configuration change occurs where Context hierarchy hold run time information.
 * Both hierarchies consists four levels, Global, Service Group, Operation and Message. Please look
 * at "Information Model" section  of "Axis2 Architecture Guide" for more information.</p>
 * <p/>
 * <p>Configuration Context hold Global level run-time information. This allows same configurations
 * to be used by two Axis2 instances and most Axis2 wide configurations can changed by setting name
 * value pairs of the configurationContext. This hold all OperationContexts, ServiceGroups,
 * Sessions, and ListenerManager.
 */
public class ConfigurationContext extends AbstractContext {

    private static final OnDemandLogger log = new OnDemandLogger(ConfigurationContext.class);
    /** Map containing <code>MessageID</code> to <code>OperationContext</code> mapping. */
    private final ConcurrentHashMap<String, OperationContext> operationContextMap = new ConcurrentHashMap<String, OperationContext>();
    private final Hashtable<String, ServiceGroupContext> serviceGroupContextMap = new Hashtable<String, ServiceGroupContext>();
    private Hashtable<String, ServiceGroupContext> applicationSessionServiceGroupContexts = new Hashtable<String, ServiceGroupContext>();
    private AxisConfiguration axisConfiguration;
    private ThreadFactory threadPool;
    //To keep TransportManager instance
    private ListenerManager listenerManager;

    // current time out interval is 30 secs. Need to make this configurable
    private long serviceGroupContextTimeoutInterval = 30 * 1000;

    //To specify url mapping for services
    private String contextRoot;
    private String servicePath;

    private String cachedServicePath = null;
    protected List<ContextListener> contextListeners;
    private boolean stopped = false;
    
    /**
     * Constructor
     *
     * @param axisConfiguration - AxisConfiguration for which to create a context
     */
    public ConfigurationContext(AxisConfiguration axisConfiguration) {
        super(null);
        this.axisConfiguration = axisConfiguration;
        initConfigContextTimeout(axisConfiguration);
    }

    private void initConfigContextTimeout(AxisConfiguration axisConfiguration) {
        Parameter parameter = axisConfiguration
                .getParameter(Constants.Configuration.CONFIG_CONTEXT_TIMEOUT_INTERVAL);
        if (parameter != null) {
            Object value = parameter.getValue();
            if (value != null && value instanceof String) {
                serviceGroupContextTimeoutInterval = Integer.parseInt((String)value);
            }
        }
    }

    /**
     * Initializes the ClusterManager for this ConfigurationContext
     *
     * @throws AxisFault
     */
    public void initCluster() throws AxisFault {
        ClusteringAgent clusteringAgent = axisConfiguration.getClusteringAgent();
        if (clusteringAgent != null) {
            StateManager stateManaget = clusteringAgent.getStateManager();
            if (stateManaget != null) {
                stateManaget.setConfigurationContext(this);
            }
            NodeManager nodeManager = clusteringAgent.getNodeManager();
            if (nodeManager != null) {
                nodeManager.setConfigurationContext(this);
            }
            if (shouldClusterBeInitiated(clusteringAgent)) {
                clusteringAgent.setConfigurationContext(this);
                clusteringAgent.init();
            }
        }
    }

    /**
     * @param clusteringAgent The ClusterManager implementation
     * @return true, if the cluster needs to be automatically initialized by the framework; false,
     *         otherwise
     */
    private static boolean shouldClusterBeInitiated(ClusteringAgent clusteringAgent) {
        Parameter param =
                clusteringAgent.getParameter(ClusteringConstants.Parameters.AVOID_INITIATION);
        return !(param != null && JavaUtils.isTrueExplicitly(param.getValue()));
    }

    /**
     * Inform any listeners of a new context being created
     *
     * @param context the just-created subcontext
     */
    void contextCreated(AbstractContext context) {
        if (contextListeners == null) {
            return;
        }
        for (Object contextListener : contextListeners) {
            ContextListener listener = (ContextListener)contextListener;
            listener.contextCreated(context);
        }
    }

    /**
     * Inform any listeners of a context being removed
     *
     * @param context the just-created subcontext
     */
    void contextRemoved(AbstractContext context) {
        if (contextListeners == null) {
            return;
        }
        for (Object contextListener : contextListeners) {
            ContextListener listener = (ContextListener)contextListener;
            listener.contextRemoved(context);
        }
    }

    /**
     * Register a {@link ContextListener} to be notified of all sub-context events.
     *
     * @param contextListener A ContextListener
     * @see #removeContextListener
     */
    public void addContextListener(ContextListener contextListener) {
        if (contextListeners == null) {
            contextListeners = new ArrayList<ContextListener>();
        }
        contextListeners.add(contextListener);
    }

    /**
     * Remove an already registered {@link ContextListener}
     *
     * @param contextListener A ContextListener
     * @see #addContextListener
     */
    public void removeContextListener(ContextListener contextListener) {
        if (contextListeners != null) {
            contextListeners.remove(contextListener);
        }
    }

    /**
     * Searches for a ServiceGroupContext in the map with given id as the key.
     * <pre>
     * If(key != null && found)
     * check for a service context for the intended service.
     * if (!found)
     * create one and hook up to ServiceGroupContext
     * else
     * create new ServiceGroupContext with the given key or if key is null with a new key
     * create a new service context for the service
     * </pre>
     *
     * @param messageContext : MessageContext
     * @throws AxisFault : If something goes wrong
     */
    public void fillServiceContextAndServiceGroupContext(MessageContext messageContext)
            throws AxisFault {
        // by this time service group context id must have a value. Either from transport or from addressing
        ServiceGroupContext serviceGroupContext;
        ServiceContext serviceContext = messageContext.getServiceContext();

        AxisService axisService = messageContext.getAxisService();

        if (serviceContext == null) {
            String scope = axisService.getScope();
            if (Constants.SCOPE_APPLICATION.equals(scope)) {
                String serviceGroupName = axisService.getAxisServiceGroup().getServiceGroupName();
                serviceGroupContext =applicationSessionServiceGroupContexts.get(
                                serviceGroupName);
                if (serviceGroupContext == null) {
                    AxisServiceGroup axisServiceGroup = messageContext.getAxisServiceGroup();
                    if (axisServiceGroup == null) {
                        axisServiceGroup = axisService.getAxisServiceGroup();
                        messageContext.setAxisServiceGroup(axisServiceGroup);
                    }
                    ConfigurationContext cfgCtx = messageContext.getConfigurationContext();
                    serviceGroupContext = cfgCtx.createServiceGroupContext(axisServiceGroup);
                    applicationSessionServiceGroupContexts
                            .put(serviceGroupName, serviceGroupContext);
                }
                messageContext.setServiceGroupContext(serviceGroupContext);
                messageContext
                        .setServiceContext(serviceGroupContext.getServiceContext(axisService));
            } else if (Constants.SCOPE_SOAP_SESSION.equals(scope)) {
                //cleaning the session
                cleanupServiceGroupContexts();
                String serviceGroupContextId = messageContext.getServiceGroupContextId();
                if (serviceGroupContextId != null) {
                    serviceGroupContext =
                            getServiceGroupContextFromSoapSessionTable(serviceGroupContextId,
                                                                       messageContext);

                } else {
                    AxisServiceGroup axisServiceGroup = axisService.getAxisServiceGroup();
                    serviceGroupContext = createServiceGroupContext(axisServiceGroup);
                    serviceContext = serviceGroupContext.getServiceContext(axisService);
                    // set the serviceGroupContextID
                    serviceGroupContextId = UIDGenerator.generateURNString();
                    serviceGroupContext.setId(serviceGroupContextId);
                    messageContext.setServiceGroupContextId(serviceGroupContextId);
                    addServiceGroupContextIntoSoapSessionTable(serviceGroupContext);
                }
                messageContext.setServiceGroupContext(serviceGroupContext);
                messageContext
                        .setServiceContext(serviceGroupContext.getServiceContext(axisService));
            } else if (Constants.SCOPE_REQUEST.equals(scope)) {
                AxisServiceGroup axisServiceGroup = axisService.getAxisServiceGroup();
                serviceGroupContext = createServiceGroupContext(axisServiceGroup);
                messageContext.setServiceGroupContext(serviceGroupContext);
                serviceContext = serviceGroupContext.getServiceContext(axisService);
                messageContext.setServiceContext(serviceContext);
            }
        }
        if (messageContext.getOperationContext() != null) {
            messageContext.getOperationContext().setParent(serviceContext);
        }
    }

    /**
     * Registers a OperationContext with a given message ID. If the given message id already has a
     * registered operation context, no change is made and the method returns false.
     *
     * @param messageID        the message ID of the request message in the MEP
     * @param operationContext the OperationContext
     * @return true if we registered this context, false if there was already one for that ID
     */
    public boolean registerOperationContext(String messageID, OperationContext operationContext) {
        return registerOperationContext(messageID, operationContext, false);
    }

    /**
     * Registers a OperationContext with a given message ID. If the given message id already has a
     * registered operation context, no change is made unless the override flag is set.
     *
     * @param messageID  the message ID of the request message in the MEP
     * @param mepContext the OperationContext
     * @param override   true if we should overwrite any existing OperationContext
     * @return true if we registered the passed OperationContext, false if not
     */
    public boolean registerOperationContext(String messageID,
                                            OperationContext mepContext,
                                            boolean override) {

        if (messageID == null) {
            if (log.isDebugEnabled()) {
                log.debug("messageID is null. Returning false");
            }
            return false;
        }

        boolean alreadyInMap = false;
        mepContext.setKey(messageID);

        if (override) {
            operationContextMap.put(messageID, mepContext);
        } else {
            Object previous = operationContextMap.putIfAbsent(messageID, mepContext);
            alreadyInMap = (previous != null);
        }
        if (log.isDebugEnabled()) {
            log.debug("registerOperationContext (" + override + "): " +
                      mepContext + " with key: " + messageID);
            HashMap<String, MessageContext> msgContextMap = mepContext.getMessageContexts();
            Iterator<MessageContext> msgContextIterator = msgContextMap.values().iterator();
            while (msgContextIterator.hasNext()) {
                MessageContext msgContext = msgContextIterator.next();
                log.debug("msgContext: " + msgContext + " action: " + msgContext.getWSAAction());
            }
        }
        return (!alreadyInMap || override);
    }

    /**
     * Unregisters the operation context associated with the given messageID
     *
     * @param messageID the messageID to remove
     */
    public void unregisterOperationContext(String messageID) {
        if (messageID == null) {
            if (log.isDebugEnabled()) {
                log.debug("messageID is null.");
            }
        } else {
            OperationContext opCtx = operationContextMap.remove(messageID);
            contextRemoved(opCtx);
        }
    }

    public boolean isAnyOperationContextRegistered() {
        return !operationContextMap.isEmpty();
    }

    /**
     * Adds the given ServiceGroupContext into the SOAP session table
     *
     * @param serviceGroupContext ServiceGroup Context to add
     */
    public void addServiceGroupContextIntoSoapSessionTable(
            ServiceGroupContext serviceGroupContext) {
        String id = serviceGroupContext.getId();
        serviceGroupContextMap.put(id, serviceGroupContext);
        serviceGroupContext.touch();
        serviceGroupContext.setParent(this);
        // this is the best time to clean up the SGCtxts since are not being used anymore
        cleanupServiceGroupContexts();
    }

    /**
     * Adds the given ServiceGroupContext into the Application Scope table
     *
     * @param serviceGroupContext The Service Group Context to add
     */
    public void addServiceGroupContextIntoApplicationScopeTable
            (ServiceGroupContext serviceGroupContext) {
        if (applicationSessionServiceGroupContexts == null) {
            applicationSessionServiceGroupContexts = new Hashtable<String, ServiceGroupContext>();
        }
        applicationSessionServiceGroupContexts.put(
                serviceGroupContext.getDescription().getServiceGroupName(), serviceGroupContext);
    }

    /**
     * Deploy a service to the embedded AxisConfiguration, and initialize it.
     *
     * @param service service to deploy
     * @throws AxisFault if there's a problem
     */
    public void deployService(AxisService service) throws AxisFault {
        axisConfiguration.addService(service);
        if (Constants.SCOPE_APPLICATION.equals(service.getScope())) {
            ServiceGroupContext sgc = createServiceGroupContext(service.getAxisServiceGroup());
            DependencyManager.initService(sgc);
        }
    }

    /**
     * Returns the AxisConfiguration
     *
     * @return Returns AxisConfiguration
     */
    public AxisConfiguration getAxisConfiguration() {
        return axisConfiguration;
    }

    /**
     * Gets a OperationContext given a Message ID.
     *
     * @param messageID the message ID of an active OperationContext
     * @return an active OperationContext, or null
     */
    public OperationContext getOperationContext(String messageID) {
        return this.operationContextMap.get(messageID);
    }

    /**
     * Finds the OperationContext given the Operation name, Service Name, and ServiceGroupName
     *
     * @param operationName    - OperationName to find
     * @param serviceName      - ServiceName to find
     * @param serviceGroupName - ServiceGroupName to find
     * @return Returns OperationContext <code>OperationContext<code>
     */
    public OperationContext findOperationContext(String operationName, String serviceName,
                                                 String serviceGroupName) {
        if (operationName == null) {
            return null;
        }

        if (serviceName == null) {
            return null;
        }

        // group name is not necessarily a prereq
        // but if the group name is non-null, then it has to match

        Iterator<OperationContext> it = operationContextMap.values().iterator();

        while (it.hasNext()) {
            OperationContext value = it.next();

            String valueOperationName;
            String valueServiceName;
            String valueServiceGroupName;

            if (value != null) {
                valueOperationName = value.getOperationName();
                valueServiceName = value.getServiceName();
                valueServiceGroupName = value.getServiceGroupName();

                if ((valueOperationName != null) && (valueOperationName.equals(operationName))) {
                    if ((valueServiceName != null) && (valueServiceName.equals(serviceName))) {
                        if ((valueServiceGroupName != null) && (serviceGroupName != null)
                            && (valueServiceGroupName.equals(serviceGroupName))) {
                            // match
                            return value;
                        }

                        // or, both need to be null
                        if ((valueServiceGroupName == null) && (serviceGroupName == null)) {
                            // match
                            return value;
                        }
                    }
                }
            }
        }

        // if we got here, we did not find an operation context
        // that fits the criteria
        return null;
    }

    /**
     * Create a MessageContext, and notify any registered ContextListener.
     *
     * @return a new MessageContext
     */
    public MessageContext createMessageContext() {
        MessageContext msgCtx = new MessageContext(this);
        contextCreated(msgCtx);
        return msgCtx;
    }

    /**
     * Create a ServiceGroupContext for the specified service group, and notify any registered
     * ContextListener.
     *
     * @param serviceGroup an AxisServiceGroup
     * @return a new ServiceGroupContext
     */
    public ServiceGroupContext createServiceGroupContext(AxisServiceGroup serviceGroup) {
        ServiceGroupContext sgCtx = new ServiceGroupContext(this, serviceGroup);
        contextCreated(sgCtx);
        return sgCtx;
    }

    /**
     * Allows users to resolve the path relative to the root directory.
     *
     * @param path a relative path
     * @return a File for the given path relative to the current repository, or null if no repo
     */
    public File getRealPath(String path) {
        URL repository = axisConfiguration.getRepository();
        if (repository != null) {
            File repo = new File(repository.getFile());
            return new File(repo, path);
        }
        return null;
    }

    /**
     * Retrieve the ServiceGroupContext from the SOAP session table
     *
     * @param serviceGroupContextId Service Group Context ID to search on
     * @param msgContext            Message Context to search on
     * @return Returns a ServiceGroupContext
     * @throws AxisFault if ServiceGroupContext cannot be found
     */
    public ServiceGroupContext getServiceGroupContextFromSoapSessionTable(
            String serviceGroupContextId,
            MessageContext msgContext) throws AxisFault {
        ServiceGroupContext serviceGroupContext =
                serviceGroupContextMap.get(serviceGroupContextId);

        if (serviceGroupContext != null) {
            serviceGroupContext.touch();
            return serviceGroupContext;
        } else {
            throw new AxisFault("Unable to find corresponding context" +
                                " for the serviceGroupId: " + serviceGroupContextId);
        }
    }


    /**
     * Returns a ServiceGroupContext object associated with the specified ID from the internal
     * table.
     *
     * @param serviceGroupCtxId The ID string associated with the ServiceGroupContext object
     * @return The ServiceGroupContext object, or null if not found
     */
    public ServiceGroupContext getServiceGroupContext(String serviceGroupCtxId) {

        if (serviceGroupCtxId == null) {
            // Hashtables require non-null key-value pairs
            return null;
        }

        ServiceGroupContext serviceGroupContext = null;

        if (serviceGroupContextMap != null) {
            serviceGroupContext =serviceGroupContextMap.get(serviceGroupCtxId);
            if (serviceGroupContext != null) {
                serviceGroupContext.touch();
            } else {
                serviceGroupContext =applicationSessionServiceGroupContexts
                                .get(serviceGroupCtxId);
                if (serviceGroupContext != null) {
                    serviceGroupContext.touch();
                }
            }
        }


        return serviceGroupContext;
    }

    /**
     * Gets all service groups in the system.
     *
     * @return Returns hashmap of ServiceGroupContexts.
     */
    public String[] getServiceGroupContextIDs() {
        String[] ids = new String[serviceGroupContextMap.size() +
                                  applicationSessionServiceGroupContexts.size()];
        int index = 0;
        for (Object o : serviceGroupContextMap.keySet()) {
            ids[index] = (String)o;
            index++;
        }
        for (Object o : applicationSessionServiceGroupContexts.keySet()) {
            ids[index] = (String)o;
            index++;
        }
        return ids;
    }

    /**
     * @return The ServiceGroupContexts
     * @deprecated Use {@link #getServiceGroupContextIDs} & {@link #getServiceGroupContext(String)}
     */
    public Hashtable<String, ServiceGroupContext> getServiceGroupContexts() {
        return serviceGroupContextMap;
    }

    /**
     * Returns the thread factory.
     *
     * @return Returns configuration specific thread pool
     */
    public ThreadFactory getThreadPool() {
        if (threadPool == null) {
            threadPool = new ThreadPool();
        }

        return threadPool;
    }

    /**
     * Set the AxisConfiguration to the specified configuration
     *
     * @param configuration an AxisConfiguration
     */
    public void setAxisConfiguration(AxisConfiguration configuration) {
        axisConfiguration = configuration;
    }

    /**
     * Sets the thread factory.
     *
     * @param pool The thread pool
     * @throws AxisFault If a thread pool has already been set
     */
    public void setThreadPool(ThreadFactory pool) throws AxisFault {
        if (threadPool == null) {
            threadPool = pool;
        } else {
            throw new AxisFault(Messages.getMessage("threadpoolset"));
        }
    }

    /**
     * Remove a ServiceGroupContext
     *
     * @param serviceGroupContextId The ID of the ServiceGroupContext
     */
    public void removeServiceGroupContext(String serviceGroupContextId) {
        if (serviceGroupContextMap == null) {
            return;
        }
        ServiceGroupContext serviceGroupContext =serviceGroupContextMap.get(serviceGroupContextId);
        serviceGroupContextMap.remove(serviceGroupContextId);
        cleanupServiceContexts(serviceGroupContext);
    }

    private void cleanupServiceGroupContexts() {
        if (serviceGroupContextMap == null) {
            return;
        }
        long currentTime = new Date().getTime();

        synchronized (serviceGroupContextMap) {
            for (Iterator<String> sgCtxtMapKeyIter = serviceGroupContextMap.keySet().iterator();
                 sgCtxtMapKeyIter.hasNext();) {
                String sgCtxtId = sgCtxtMapKeyIter.next();
                ServiceGroupContext serviceGroupContext =serviceGroupContextMap.get(sgCtxtId);
                if ((currentTime - serviceGroupContext.getLastTouchedTime()) >
                    getServiceGroupContextTimeoutInterval()) {
                    sgCtxtMapKeyIter.remove();
                    cleanupServiceContexts(serviceGroupContext);
                    contextRemoved(serviceGroupContext);
                }
            }
        }
    }

    /**
     * Retrieve the ListenerManager
     *
     * @return Returns the ListenerManager
     */
    public ListenerManager getListenerManager() {
        return listenerManager;
    }

    /**
     * Set the TransportManager to the given ListenerManager
     *
     * @param listenerManager The ListenerManager for which to set the TransportManager
     */
    public void setTransportManager(ListenerManager listenerManager) {
        this.listenerManager = listenerManager;
    }

    private void cleanupServiceContexts(ServiceGroupContext serviceGroupContext) {
        if (serviceGroupContext == null) {
            return;
        }
        Iterator<ServiceContext> serviceContextIter = serviceGroupContext.getServiceContexts();
        if (serviceContextIter == null) {
            return;
        }
        while (serviceContextIter.hasNext()) {
            ServiceContext serviceContext = serviceContextIter.next();
            DependencyManager.destroyServiceObject(serviceContext);
        }
    }

    /** Called during shutdown to clean up all Contexts */
    public void cleanupContexts() {
        if ((applicationSessionServiceGroupContexts != null) &&
            (applicationSessionServiceGroupContexts.size() > 0)) {
            for (Object o : applicationSessionServiceGroupContexts.values()) {
                ServiceGroupContext serviceGroupContext =
                        (ServiceGroupContext)o;
                cleanupServiceContexts(serviceGroupContext);
            }
            applicationSessionServiceGroupContexts.clear();
        }
        if ((serviceGroupContextMap != null) && (serviceGroupContextMap.size() > 0)) {
            for (Object o : serviceGroupContextMap.values()) {
                ServiceGroupContext serviceGroupContext =
                        (ServiceGroupContext)o;
                cleanupServiceContexts(serviceGroupContext);
            }
            serviceGroupContextMap.clear();
        }
    }
    /**
     * Called during shutdown to clean up all Contexts
     */
    public void shutdownModulesAndServices() throws AxisFault{
        if(stopped){
            if (log.isDebugEnabled()) {
                log.debug("ConfigurationContext is stopped, modules and services not being shut down");
            }
            return;
        }
        /*Shut down the modules*/
        if(log.isDebugEnabled()){
            log.debug("Invoke modules shutdown.");
        }
        if(axisConfiguration!=null){
            HashMap modules = axisConfiguration.getModules();
            if (log.isDebugEnabled()) {
                log.debug("Modules to be shutdown from axisConfiguration: " + modules);
            }
            if (modules != null) {
                Iterator moduleitr = modules.values().iterator();
                while (moduleitr.hasNext()) {
                    AxisModule axisModule = (AxisModule) moduleitr.next();
                    Module module = axisModule.getModule();
                    if (module != null) {
                        try {
                            module.shutdown(this);
                        } catch (Exception e) {
                            log.warn("Could not shutdown module " + module.getClass().getName(), e);
                        }
                    }
                }
            }
        }
        cleanupContexts();
        /*Shut down the services*/
        if(log.isDebugEnabled()){
            log.debug("Invoke services shutdown.");
        }
        if(axisConfiguration!=null){
            for (Iterator services = axisConfiguration.getServices().values().iterator();
            services.hasNext();) {
                AxisService axisService = (AxisService) services.next();
                ServiceLifeCycle serviceLifeCycle = axisService.getServiceLifeCycle();
                if (serviceLifeCycle != null) {
                    try {
                        serviceLifeCycle.shutDown(this, axisService);
                    } catch (Exception e) {
                        log.warn("Could not shutdown service " + axisService.getName(), e);
                    }
                }
            }
        }
        stopped = true;
    }
    /**
     * Invoked during shutdown to stop the ListenerManager and perform configuration cleanup
     *
     * @throws AxisFault
     */
    public void terminate() throws AxisFault {
        shutdownModulesAndServices();
        if (listenerManager != null) {
            listenerManager.destroy();
        }
        if (axisConfiguration != null) {
            axisConfiguration.cleanup();
            cleanupTemp();
            this.axisConfiguration = null;
        }
    }

    /**
     * This include all the major changes we have done from 1.2 release to 1.3 release. This will
     * include API changes , class deprecating etc etc.
     */
    private void cleanupTemp() {
        File tempFile = (File)axisConfiguration.getParameterValue(
                Constants.Configuration.ARTIFACTS_TEMP_DIR);
        if (tempFile == null) {
            String property = AccessController.doPrivileged(
                    new PrivilegedAction<String>() {
                        public String run() {
                            return System.getProperty("java.io.tmpdir");
                        }
                    }
            );
            tempFile = new File(property, "_axis2");
        }
        deleteTempFiles(tempFile);
    }

    private void deleteTempFiles(final File dir) {
        Boolean isDir = AccessController.doPrivileged(
                new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        return dir.isDirectory();
                    }
                }
        );
        if (isDir) {
            String[] children = AccessController.doPrivileged(
                    new PrivilegedAction<String[]>() {
                        public String[] run() {
                            return dir.list();
                        }
                    }
            );
            for (int i = 0; children != null && i < children.length; i++) {
                deleteTempFiles(new File(dir, children[i]));
            }
        }
        AccessController.doPrivileged(
                new PrivilegedAction<Object>() {
                    public Object run() {
                        dir.delete();
                        return null;
                    }
                }
        );
    }

    /**
     * Retrieves the ServiceContext path
     *
     * @return path to the ServiceContext
     */
    public String getServiceContextPath() {
        if (cachedServicePath == null) {
            cachedServicePath = internalGetServiceContextPath();
        }
        return cachedServicePath;
    }

    private String internalGetServiceContextPath() {
        String ctxRoot = getContextRoot();
        String path = "/";
        if (ctxRoot != null) {
            if (!ctxRoot.equals("/")) {
                path = ctxRoot + "/";
            }
            if (servicePath == null || servicePath.trim().length() == 0) {
                throw new IllegalArgumentException("service path cannot be null or empty");
            } else {
                path += servicePath.trim();
            }
        }
        return path;
    }

    /**
     * Retrieves the ServicePath
     *
     * @return The path to the Service
     */
    public String getServicePath() {
        if (servicePath == null || servicePath.trim().length() == 0) {
            throw new IllegalArgumentException("service path cannot be null or empty");
        }
        return servicePath.trim();
    }

    /**
     * Sets the ServicePath to the given string
     *
     * @param servicePath The service path for which to set
     */
    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    /**
     * Retrieves the ContextRoot
     *
     * @return The ContextRoot
     */
    public String getContextRoot() {
        if (contextRoot == null) {
            contextRoot = (String) axisConfiguration.getParameter("contextRoot").getValue();
        }
        return contextRoot;
    }

    /**
     * Sets the context root to the given string
     *
     * @param contextRoot The context root for which to set
     */
    public void setContextRoot(String contextRoot) {
        if (contextRoot != null) {
            this.contextRoot = contextRoot.trim();  // Trim before storing away for good hygiene
            cachedServicePath = internalGetServiceContextPath();
        }
    }

    /**
     * @deprecated MISSPELLING - Please use getServiceGroupContextTimeoutInterval()
     * @return the service group context timeout interval
     */
    public long getServiceGroupContextTimoutInterval() {
        return getServiceGroupContextTimeoutInterval();
    }

    /**
     * This will be used to fetch the serviceGroupContextTimoutInterval from any place available.
     *
     * @return the service group context timeout interval (in milliseconds)
     */
    public long getServiceGroupContextTimeoutInterval() {
        Integer serviceGroupContextTimoutIntervalParam =
                (Integer)getProperty(Constants.Configuration.CONFIG_CONTEXT_TIMEOUT_INTERVAL);
        if (serviceGroupContextTimoutIntervalParam != null) {
            // TODO: This seems wrong - setting a field inside a getter??
            serviceGroupContextTimeoutInterval = serviceGroupContextTimoutIntervalParam;
        }
        return serviceGroupContextTimeoutInterval;
    }

    /**
     * Removes the given ServiceGroup from the ServiceGroup context
     *
     * @param serviceGroup the AxisServiceGroup to remove
     */
    public void removeServiceGroupContext(AxisServiceGroup serviceGroup) {
        if (serviceGroup == null) return;

        String groupName = serviceGroup.getServiceGroupName();
        Object obj = applicationSessionServiceGroupContexts.get(groupName);
        if (obj != null) {
            applicationSessionServiceGroupContexts.remove(serviceGroup.getServiceGroupName());
            return;
        }

        ArrayList<String> toBeRemovedList = new ArrayList<String>();
        Iterator<ServiceGroupContext> serviceGroupContexts = serviceGroupContextMap.values().iterator();
        while (serviceGroupContexts.hasNext()) {
            ServiceGroupContext serviceGroupContext =serviceGroupContexts.next();
            if (serviceGroupContext.getDescription().equals(serviceGroup)) {
                toBeRemovedList.add(serviceGroupContext.getId());
            }
        }
        for (Object aToBeRemovedList : toBeRemovedList) {
            String s = (String)aToBeRemovedList;
            serviceGroupContextMap.remove(s);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.context.AbstractContext#getRootContext()
     */
    public ConfigurationContext getRootContext() {
        return this;
    }
}