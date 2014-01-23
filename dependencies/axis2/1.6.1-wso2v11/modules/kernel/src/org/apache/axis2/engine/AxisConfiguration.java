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

package org.apache.axis2.engine;

import java.io.File;
import java.net.URL;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.transaction.TransactionConfiguration;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.unknowncontent.UnknownContentBuilder;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.dataretrieval.AxisDataLocator;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.ModuleDeployer;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.ModuleConfiguration;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.Version;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.axis2.phaseresolver.PhaseResolver;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.util.TargetResolver;
import org.apache.axis2.util.Utils;
import org.apache.axis2.util.FaultyServiceData;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.securevault.SecretResolver;

/**
 * Class AxisConfiguration
 */
public class AxisConfiguration extends AxisDescription {

    private static final Log log = LogFactory.getLog(AxisConfiguration.class);
    /* 
     * To store configured data locators
     */
    private HashMap<String, AxisDataLocator> dataLocators = new HashMap<String, AxisDataLocator>();
    private HashMap<String, String> dataLocatorClassNames = new HashMap<String, String>();

    /**
     * Map of all available modules. The key is the archive name as defined by
     * {@link AxisModule#getArchiveName()}.
     */
    private final HashMap<String, AxisModule> allModules = new HashMap<String, AxisModule>();

    // To store mapping between default version and module name
    private final HashMap<String, String> nameToversionMap = new HashMap<String, String>();

    // private final HashMap serviceGroups = new HashMap();
    private final HashMap<String, TransportInDescription> transportsIn = new HashMap<String, TransportInDescription>();

    private final HashMap<String, TransportOutDescription> transportsOut = new HashMap<String, TransportOutDescription>();

    private final HashMap<String, List<AxisModule>> policySupportedModules = new HashMap<String, List<AxisModule>>();

    /**
     * Stores the QNames of local policy assertions
     */
    private final ArrayList<QName> localPolicyAssertions = new ArrayList<QName>();

    // to store AxisObserver Objects
    private Set<AxisObserver> observerSet = null;

    private URL axis2Repository = null;

    private Map<String, AxisService> allServices = new ConcurrentHashMap<String, AxisService>();
    private Map<String, AxisService> allEndpoints = new ConcurrentHashMap<String, AxisService>();

    /**
     * Stores the module specified in the server.xml at the document parsing time.
     */
    private List<String> globalModuleList;

    private Hashtable<String, String> faultyModules;

    /**
     * To store faulty services
     */
    private Hashtable<String, String> faultyServices;

    /**
     * To store services which have become faulty due to modules. Here key is the module name and
     * the corresponding value is an another map which holds the a Map of faulty services due a
     * single module (keyed by service name).
     */
    private final Map<String, Map<String, FaultyServiceData>> faultyServicesDueToModules =
            new HashMap<String, Map<String, FaultyServiceData>>();


    private List<Phase> inFaultPhases;

    private List<Phase> inPhasesUptoAndIncludingPostDispatch;

    private HashMap<String, MessageReceiver> messageReceivers;

    private HashMap<String, Builder> messageBuilders;

    private HashMap<String, MessageFormatter> messageFormatters;

    private ClassLoader moduleClassLoader;

    private HashMap<String, ModuleConfiguration> moduleConfigmap;

    private List<Phase> outFaultPhases;

    private List<Phase> outPhases;

    protected PhasesInfo phasesinfo;

    private ClassLoader serviceClassLoader;

    private ClassLoader systemClassLoader;

    //To keep track of whether the system has started or not
    private boolean start;

    private ArrayList<TargetResolver> targetResolvers;

    private ClusteringAgent clusteringAgent;

    private AxisConfigurator configurator;

    private TransactionConfiguration transactionConfiguration;

    /**
     * To secure the password in axis2.xml. SecretResolver must be initialized
     * When building axis configuration   
     */
    private SecretResolver secretResolver;

    /**
     * Constructor AxisConfiguration.
     */
    public AxisConfiguration() {
        moduleConfigmap = new HashMap<String, ModuleConfiguration>();
        globalModuleList = new ArrayList<String>();
        messageReceivers = new HashMap<String, MessageReceiver>();
        messageBuilders = new HashMap<String, Builder>();
        messageFormatters = new HashMap<String, MessageFormatter>();
        outPhases = new ArrayList<Phase>();
        inFaultPhases = new ArrayList<Phase>();
        outFaultPhases = new ArrayList<Phase>();
        faultyServices = new Hashtable<String, String>();
        faultyModules = new Hashtable<String, String>();
        observerSet = new CopyOnWriteArraySet<AxisObserver>();
        inPhasesUptoAndIncludingPostDispatch = new ArrayList<Phase>();
        systemClassLoader = org.apache.axis2.java.security.AccessController
                .doPrivileged(new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
        serviceClassLoader = systemClassLoader;
        moduleClassLoader = systemClassLoader;

        this.phasesinfo = new PhasesInfo();
        targetResolvers = new ArrayList<TargetResolver>();
    }

    public void addMessageReceiver(String mepURL,
                                   MessageReceiver messageReceiver) {
        messageReceivers.put(mepURL, messageReceiver);
    }

    /**
     * Register a messageBuilder implementation against a content type.
     * This is used by Axis2 to support different message formats.
     *
     * @param contentType    the relevant content-type (i.e. "text/xml")
     * @param messageBuilder a Builder implementation
     */
    public void addMessageBuilder(String contentType,
                                  Builder messageBuilder) {
        messageBuilders.put(contentType, messageBuilder);
    }

    /**
     * Register a messageFormatter implementation against a content type.
     * This is used by Axis2 to support serialization of messages to different
     * message formats. (Eg: JSON)
     *
     * @param contentType      the relevant content-type (i.e. "text/xml")
     * @param messageFormatter a MessageFormatter implementation
     */
    public void addMessageFormatter(String contentType,
                                    MessageFormatter messageFormatter) {
        messageFormatters.put(contentType, messageFormatter);
    }

    /**
     * Add an available Module to this configuration
     *
     * @param module an AxisModule
     * @throws AxisFault in case of error
     */
    public void addModule(AxisModule module) throws AxisFault {
        module.setParent(this);

        // check whether the module version paramter is there , if so set the module version as that
        Parameter versionParameter = module.getParameter(org.apache.axis2.Constants.MODULE_VERSION);
        if (versionParameter !=null ) {
            String version = (String) versionParameter.getValue();
            try {
                module.setVersion(new Version(version));
            } catch (ParseException ex) {
                throw new AxisFault("The version number '" + version + "' specified by the "
                        + org.apache.axis2.Constants.MODULE_VERSION + " parameter is invalid");
            }
        }

        allModules.put(module.getArchiveName(), module);
        notifyObservers(new AxisEvent(AxisEvent.MODULE_DEPLOY,null), module);

        // Registering the policy namespaces that the module understand
        registerModulePolicySupport(module);
        // Registering the policy assertions that are local to the system
        registerLocalPolicyAssertions(module);

    }

    public void deployModule(String moduleFileName) throws DeploymentException {
        File moduleFile = new File(moduleFileName);
        if (!moduleFile.exists()) {
            throw new DeploymentException("Module archive '" + moduleFileName + "' doesn't exist");
        }
        DeploymentFileData dfd = new DeploymentFileData(moduleFile, new ModuleDeployer(this));
        dfd.deploy();
    }

    /**
     * To remove a given module from the system
     *
     * @param module name of module to remove
     * @deprecated Use {@link #removeModule(String,String)}
     */
    public void removeModule(String module) {
        allModules.remove(module);
        // TODO disengage has to be done here
    }

    /**
     * Remove a module with moduleName & moduleVersion
     *
     * @param moduleName the name of the module to remove
     * @param moduleVersion the version of the module to remove
     */
    public void removeModule(String moduleName, String moduleVersion) {
        allModules.remove(Utils.getModuleName(moduleName, moduleVersion));
        // TODO disengage has to be done here
    }

    /**
     * Remove a module with moduleName & moduleVersion
     *
     * @param moduleName the name of the module to remove
     * @param moduleVersion the version of the module to remove
     */
    public void removeModule(String moduleName, Version moduleVersion) {
        String version = null;
        if (moduleVersion != null) {
            version = moduleVersion.toString();
        }
        removeModule(moduleName, version);
    }

    /**
     * Adds module configuration, if there is a moduleConfig tag in service.
     *
     * @param moduleConfiguration a ModuleConfiguration to remember
     */
    public void addModuleConfig(ModuleConfiguration moduleConfiguration) {
        moduleConfigmap.put(moduleConfiguration.getModuleName(),
                            moduleConfiguration);
    }

    /**
     * Register given AxisObserver object to the {@link AxisConfiguration}.
     * 
     * @param axisObserver the axis observer
     */
    public void addObservers(AxisObserver axisObserver) {
        observerSet.add(axisObserver);
    }
    
    /**
     * Unregister the given AxisObserver object if it is registered with
     * the {@link AxisConfiguration}. 
     * 
     * @param axisObserver the axis observer
     */
    public void removeObserver(AxisObserver axisObserver) {
    	observerSet.remove(axisObserver);
    }

    /**
     * Add an AxisService to our global configuration.  Since services must
     * be in ServiceGroups, we construct an anonymous AxisServiceGroup around
     * the passed AxisService and then call addServiceGroup().
     *
     * @param service an AxisService to deploy.
     * @throws AxisFault if something goes wrong (invalid service, service is
     *                   already deployed, etc...)
     */
    public synchronized void addService(AxisService service) throws AxisFault {
        AxisServiceGroup axisServiceGroup = new AxisServiceGroup();
        axisServiceGroup.setServiceGroupName(service.getName());
        axisServiceGroup.setParent(this);
        axisServiceGroup.addService(service);
        addServiceGroup(axisServiceGroup);
//        processEndpoints(service, service.getAxisConfiguration());
    }

    public synchronized void addServiceGroup(AxisServiceGroup axisServiceGroup)
            throws AxisFault {
        axisServiceGroup.setParent(this);
        notifyObservers(new AxisEvent(AxisEvent.SERVICE_DEPLOY, axisServiceGroup), axisServiceGroup);
        AxisService axisService;

        Iterator<AxisService> services = axisServiceGroup.getServices();
        while (services.hasNext()) {
            axisService = services.next();
            if (axisService.getSchemaTargetNamespace() == null) {
                axisService.setSchemaTargetNamespace(Java2WSDLConstants.AXIS2_XSD);
            }
        }
        services = axisServiceGroup.getServices();
        while (services.hasNext()) {
            axisService = services.next();
            if (axisService.isUseDefaultChains()) {
                Iterator<AxisOperation> operations = axisService.getOperations();
                while (operations.hasNext()) {
                    AxisOperation operation = operations.next();
                    phasesinfo.setOperationPhases(operation);
                }
            }
        }
        Iterator<AxisModule> enModule = getEngagedModules().iterator();
        while (enModule.hasNext()) {
            axisServiceGroup.engageModule(enModule.next());
        }
        services = axisServiceGroup.getServices();
        ArrayList<AxisService> servicesIAdded = new ArrayList<AxisService>();
        while (services.hasNext()) {
            axisService = services.next();
            processEndpoints(axisService, axisService.getAxisConfiguration());

            Map<String, AxisEndpoint> endpoints = axisService.getEndpoints();
            String serviceName = axisService.getName();
            try {
                addToAllServicesMap(axisService);
            } catch (AxisFault axisFault) {
                // Whoops, must have been a duplicate!  If we had a problem here, we have to
                // remove all the ones we added...
                for (AxisService service : servicesIAdded) {
                    allServices.remove(service.getName());
                }
                // And toss this in case anyone wants it?
                throw axisFault;
            }
            servicesIAdded.add(axisService);
            if (endpoints != null) {
                Iterator<String> endpointNameIter = endpoints.keySet().iterator();
                while (endpointNameIter.hasNext()) {
                    String endpointName = endpointNameIter.next();
                    if (log.isDebugEnabled()) {
                        log.debug("Adding service to allEndpoints map: ("
                                  + serviceName + "," + endpointName + ") ");
                    }

                    allEndpoints.put(serviceName + "." + endpointName, axisService);
                }
                if (log.isDebugEnabled()) {
                    log.debug("After adding to allEndpoints map, size is "
                              + allEndpoints.size());
                }
            }

            if (!axisService.isClientSide()) {
                notifyObservers(new AxisEvent(AxisEvent.SERVICE_DEPLOY ,axisService ), axisService);
            }
        }
        // serviceGroups.put(axisServiceGroup.getServiceGroupName(),
        // axisServiceGroup);
        addChild(axisServiceGroup);
    }

    /**
     * This method is used to add a service to an existing active service group in the axis configuration
     *
     * @param axisService service to be added to the existing service group provided
     * @param serviceGroupName name of the service group which should be existing in the axis configuration
     * @throws AxisFault in case of an error in adding the service to the group specified or if the group is not existing
     */
    public void addServiceToExistingServiceGroup(AxisService axisService,
                                                 String serviceGroupName) throws AxisFault {

        AxisServiceGroup serviceGroup = getServiceGroup(serviceGroupName);
        if (serviceGroup == null) {
            String message = "A ServiceGroup with the provided name "
                    + serviceGroupName + " is not existing";
            log.error(message);
            throw new AxisFault(message);
        }

        if (axisService.getSchemaTargetNamespace() == null) {
            axisService.setSchemaTargetNamespace(Java2WSDLConstants.AXIS2_XSD);
        }

        if (axisService.isUseDefaultChains()) {
            Iterator<AxisOperation> operations = axisService.getOperations();
            while (operations.hasNext()) {
                AxisOperation operation = operations.next();
                phasesinfo.setOperationPhases(operation);
            }
        }

        Map<String, AxisEndpoint> endpoints = axisService.getEndpoints();
        if (endpoints == null || endpoints.size() == 0) {
			org.apache.axis2.deployment.util.Utils.addEndpointsToService(
					axisService, axisService.getAxisConfiguration());
            endpoints = axisService.getEndpoints();
		}

        String serviceName = axisService.getName();
        addToAllServicesMap(axisService);

        if (endpoints != null) {
            Iterator<String> endpointNameIter = endpoints.keySet().iterator();
            while (endpointNameIter.hasNext()) {
                String endpointName = endpointNameIter.next();
                if (log.isDebugEnabled()) {
                    log.debug("Adding service to allEndpoints map: ("
                            + serviceName + "," + endpointName + ") ");
                }

                allEndpoints.put(serviceName + "." + endpointName, axisService);
            }
            if (log.isDebugEnabled()) {
                log.debug("After adding to allEndpoints map, size is "
                        + allEndpoints.size());
            }
        }
        
        serviceGroup.addService(axisService);

        if (!axisService.isClientSide()) {
            notifyObservers(new AxisEvent(AxisEvent.SERVICE_DEPLOY, axisService), axisService);
        }
    }

    public void addToAllServicesMap(AxisService axisService) throws AxisFault {
        String serviceName = axisService.getName();
        AxisService oldService = allServices.get(serviceName);
        if (oldService == null) {
            if (log.isDebugEnabled()) {
                log.debug("Adding service to allServices map: [" + serviceName + "] ");
            }
            allServices.put(serviceName, axisService);
            if (log.isTraceEnabled()) {
                //noinspection ThrowableInstanceNeverThrown
                log.trace("After adding to allServices map, size is "
                          + allServices.size() + " call stack is " + 
                          JavaUtils.stackToString(new Exception()));
            }

        } else {
            // If we were already there, that's fine.  If not, fault!
            if (oldService != axisService) {
                throw new AxisFault(Messages.getMessage("twoservicecannothavesamename",
                                                        axisService.getName() +
                                                        " [" + axisService.getFileName() + "]"));
            }
        }
    }

    public AxisServiceGroup removeServiceGroup(String serviceGroupName) throws AxisFault {
        AxisServiceGroup axisServiceGroup = (AxisServiceGroup) getChild(serviceGroupName);
        if (axisServiceGroup == null) {
            throw new AxisFault(Messages.getMessage("invalidservicegroupname",
                                                    serviceGroupName));
        }

        Iterator<AxisService> services = axisServiceGroup.getServices();
        boolean isClientSide = false;
        while (services.hasNext()) {
            AxisService axisService = services.next();
            allServices.remove(axisService.getName());
            if (!axisService.isClientSide()) {
                notifyObservers(new AxisEvent(AxisEvent.SERVICE_REMOVE , axisService), axisService);
            } else {
                isClientSide = true;
            }

            //removes the endpoints to this service
            String serviceName = axisService.getName();
            String key;

            for (String s : axisService.getEndpoints().keySet()) {
                key = serviceName + "." + s;
                this.allEndpoints.remove(key);
            }

        }
        removeChild(serviceGroupName);
        if (!isClientSide) {
            notifyObservers(new AxisEvent(AxisEvent.SERVICE_REMOVE, axisServiceGroup), axisServiceGroup);
        }

        return axisServiceGroup;
    }

    /**
     * Add an incoming transport description (i.e. receiver) to our configuration.
     *
     * @param transport TransportInDescription to add.
     * @throws AxisFault
     */
    public void addTransportIn(TransportInDescription transport) throws AxisFault {
        if (transport.getReceiver() == null) {
            throw new AxisFault(
                    "Transport Receiver can not be null for the transport "
                    + transport.getName());
        }
        transportsIn.put(transport.getName(), transport);
    }

    /**
     * Add an outgoing transport description (i.e. sender) to our configuration.
     *
     * @param transport TransportOutDescription to add.
     * @throws AxisFault
     */
    public void addTransportOut(TransportOutDescription transport)
            throws AxisFault {
        if (transport.getSender() == null) {
            throw new AxisFault(
                    "Transport sender can not be null for the transport "
                    + transport.getName());
        }
        transportsOut.put(transport.getName(), transport);
    }

    /**
     * Engages the default module version corresponding to the given module name,
     * or if the module name contains version number in it then it will engage
     * the correct module. Both the below cases are valid : -
     * 1. engageModule("addressing"); 2. engageModule("addressing-1.23");
     *
     * @param moduleref QName of module to engage
     * @throws AxisFault
     * @deprecated Please use the String version instead
     */
    public void engageModule(QName moduleref) throws AxisFault {
        engageModule(moduleref.getLocalPart());
    }

    /**
     * Engages the default module version corresponding to given module name ,
     * or if the module name contains version number in it then it will engage
     * the correct module. Both of the below two cases are valid 1.
     * engageModule("addressing"); 2. engageModule("addressing-1.23");
     *
     * @param moduleref name of module to engage
     * @throws AxisFault
     */
    public void engageModule(String moduleref) throws AxisFault {
        AxisModule module = getModule(moduleref);
        if (module != null) {
            engageModule(module);
        } else {
            throw new AxisFault(Messages.getMessage("modulenotavailble", moduleref));
        }
    }

    /**
     * Engages a module using given name and its version ID.
     *
     * @param moduleName name of module to engage
     * @param versionID version of module to engage
     * @throws AxisFault
     */
    public void engageModule(String moduleName, String versionID)
            throws AxisFault {
        String actualName = Utils.getModuleName(moduleName, versionID);
        AxisModule module = getModule(actualName);
        if (module != null) {
            engageModule(module);
        } else {
            // TODO : Should this be an NPE or InvalidArgumentException?
            throw new AxisFault(Messages.getMessage("refertoinvalidmodule"));
        }
    }

    public void onEngage(AxisModule module, AxisDescription engager) throws AxisFault {
        Iterator<AxisServiceGroup> servicegroups = getServiceGroups();
        while (servicegroups.hasNext()) {
            AxisServiceGroup serviceGroup = servicegroups.next();
            serviceGroup.engageModule(module, engager);
        }
    }

    /**
     * To dis-engage a module from the system. This will remove all the handlers
     * belonging to this module from all the handler chains.
     *
     * @param module module to disengage
     */
    public void onDisengage(AxisModule module) throws AxisFault {
        PhaseResolver phaseResolver = new PhaseResolver(this);
        phaseResolver.disengageModuleFromGlobalChains(module);

        Iterator<AxisServiceGroup> serviceGroups = getServiceGroups();
        while (serviceGroups.hasNext()) {
            AxisServiceGroup axisServiceGroup = serviceGroups.next();
            axisServiceGroup.disengageModule(module);
        }
    }

    public void notifyObservers(AxisEvent event, AxisService service) {
        if (service.isClientSide())
            return;

        for (AxisObserver observer : observerSet) {
            try {
                observer.serviceUpdate(event, service);
            } catch (Throwable e) {
                // No need to stop the system due to this, so log an error and ignore
                log.error("Error occured while notifying observer", e);
            }
        }
    }

    public void notifyObservers(AxisEvent event, AxisModule moule) {

        for (AxisObserver anObserversList : observerSet) {

            try {
                anObserversList.moduleUpdate(event, moule);
            } catch (Throwable e) {
                // No need to stop the system due to this, so log an error and ignore
                log.error("Error occured while notifying observer", e);
            }
        }
    }

    public void notifyObservers(AxisEvent event, AxisServiceGroup serviceGroup) {

        for (AxisObserver anObserversList : observerSet) {

            try {
                anObserversList.serviceGroupUpdate(event, serviceGroup);
            } catch (Throwable e) {
                // No need to stop the system due to this, so log an error and ignore
                log.error("Error occured while notifying observer", e);
            }
        }
    }

    /**
     * Remove a service.
     *
     * @param name name of service to remove
     * @throws AxisFault
     */
    public synchronized void removeService(String name) throws AxisFault {
        AxisService service = allServices.remove(name);
        if (service != null) {
            AxisServiceGroup serviceGroup = service.getAxisServiceGroup();
            serviceGroup.removeService(name);
            log.debug(Messages.getMessage("serviceremoved", name));
        }
    }

    /**
     * Add an AxisModule to the list of globally deployed modules.
     * <p/>
     * TODO: should this check for duplicate names?
     *
     * @param moduleName name of AxisModule to add to list.
     */
    public void addGlobalModuleRef(String moduleName) {
        globalModuleList.add(moduleName);
    }

    /**
     * Engage all the previously added global modules.
     *
     * @throws AxisFault if an individual engageModule() fails
     */
    public void engageGlobalModules() throws AxisFault {
        for (String aGlobalModuleList : globalModuleList) {
            engageModule(aGlobalModuleList);
        }
    }

    public Hashtable<String, String> getFaultyModules() {
        return faultyModules;
    }

    public Hashtable<String, String> getFaultyServices() {
        return faultyServices;
    }

    /**
     * Updates the map that keeps track of faulty services due to modules
     * @param moduleName This service has become faulty due this module.
     * @param faultyServiceData  Data that are required when recovering the faulty service.
     */
    public void addFaultyServiceDuetoModule(String moduleName, FaultyServiceData faultyServiceData) {
        Map<String, FaultyServiceData> faultyServicesMap;

        synchronized (faultyServicesDueToModules) {

            if (faultyServicesDueToModules.containsKey(moduleName)) {
                faultyServicesMap = faultyServicesDueToModules.get(moduleName);
                faultyServicesMap.put(faultyServiceData.getServiceGroupName(), faultyServiceData);

            } else {
                faultyServicesMap = new HashMap<String, FaultyServiceData>();
                faultyServicesMap.put(faultyServiceData.getServiceGroupName(), faultyServiceData);
                faultyServicesDueToModules.put(moduleName, faultyServicesMap);
            }

            //Adding the faulty service to default service map
            String serviceStatus = "Error:\n" + "Engaged module not found :" + moduleName;

            String serviceLocation = null;
            if(faultyServiceData.getCurrentDeploymentFile() != null){
                serviceLocation = faultyServiceData.getCurrentDeploymentFile().getFile().getAbsolutePath();
            }

            if(serviceLocation == null){
                serviceLocation = faultyServiceData.getServiceGroupName();
            }
            faultyServices.put(serviceLocation, serviceStatus);
        }
    }

    /**
     * Returns a map which contains the faulty services due a particular module.
     *
     * @param moduleName name of the module about which to inquire
     * @return a Map&lt;String, FaultyServiceData&gt; mapping service name to data
     */
    public Map<String, FaultyServiceData> getFaultyServicesDuetoModule(String moduleName) {
        if (faultyServicesDueToModules.containsKey(moduleName)) {
            return faultyServicesDueToModules.get(moduleName);

        }
        return new HashMap<String, FaultyServiceData>(1);
    }

    /**
     * Returns the map which keeps track of faulty services due to modules.
     * @return a Map keyed by module name, containing Maps keyed by service name containing
     *         FaultyServiceData for each faulty service.
     */
    public Map<String, Map<String, FaultyServiceData>> getFaultyServicesDuetoModules(){
        return faultyServicesDueToModules;
    }

    /**
     * Removes a faulty service for a given module from the internal map.
     *
     * TODO: Doesn't this actually remove a faulty service *group*? And should this even be public?
     * 
     * @param moduleName name of the module to look up
     * @param serviceGroupName name of the service group to remove
     */
    public void removeFaultyServiceDuetoModule(String moduleName, String serviceGroupName) {
        synchronized (faultyServicesDueToModules) {
            Map<String, FaultyServiceData> faultyServicesDueToModule = faultyServicesDueToModules.get(moduleName);
            FaultyServiceData faultyServiceData = faultyServicesDueToModule.get(serviceGroupName);

            if (faultyServicesDueToModule != null) {
                faultyServicesDueToModule.remove(serviceGroupName);

                //Removing from the default faulty services map.
                String serviceLocation = null;
                if (faultyServiceData.getCurrentDeploymentFile() != null) {
                    serviceLocation = faultyServiceData.getCurrentDeploymentFile().getFile().getAbsolutePath();
                }

                if (serviceLocation == null) {
                    serviceLocation = faultyServiceData.getServiceGroupName();
                }
                faultyServices.remove(serviceLocation);

                if (faultyServicesDueToModule.isEmpty()) {
                    faultyServicesDueToModules.remove(moduleName);
                }
            }
        }
    }

    public void removeFaultyService(String key) {
        Iterator<String> itr = faultyServices.keySet().iterator();
        while (itr.hasNext()) {
            String fullFileName = itr.next();
            if (fullFileName.indexOf(key) >= 0) {
                faultyServices.remove(fullFileName);
                return;
            }
        }
    }

    // to get the out flow correpodning to the global out flow;
    public List<Phase> getOutFlowPhases() {
        return this.outPhases;
    }

    /**
     * @return Returns ArrayList.
     */
    public List<Phase> getInFaultFlowPhases() {
        return inFaultPhases;
    }

    public List<Phase> getInFlowPhases() {
        return inPhasesUptoAndIncludingPostDispatch;
    }

    public MessageReceiver getMessageReceiver(String mepURL) {
        return messageReceivers.get(mepURL);
    }

    /**
     * Get a Builder for a particular content type.
     *
     * @param contentType the desired content type
     * @return the configured message builder implementation class name against
     *         the given content type.
     */
    public Builder getMessageBuilder(String contentType) {
        Builder builder = null;
        if (messageBuilders.isEmpty()) {
            return null;
        }
        if (contentType != null) {
            builder = messageBuilders.get(contentType);
            if (builder == null) {
                builder = messageBuilders.get(contentType.toLowerCase());
            }
            if (builder == null) {
                Iterator<Entry<String, Builder>> iterator = messageBuilders.entrySet().iterator();
                while (iterator.hasNext() && builder == null) {
                    Entry<String, Builder> entry = iterator.next();
                    String key = entry.getKey();
                    if (contentType.matches(key)) {
                        builder = entry.getValue();
                    }
                }
            }
        }
        return builder;
    }

    public Builder getMessageBuilder(String contentType, boolean defaultBuilder) {
        Builder builder = getMessageBuilder(contentType);
        if (builder == null && defaultBuilder){
            builder = new UnknownContentBuilder();
        }
        return builder;
    }

    /**
     * Get a MessageFormatter for the specified content type.
     *
     * @param contentType content type to look up
     * @return a MessageFormatter corresponding to the given content type, or null.
     */
    public MessageFormatter getMessageFormatter(String contentType) {
        MessageFormatter formatter = null;
        if (messageFormatters.isEmpty()) {
            return null;
        }

        if (contentType != null) {
            formatter = messageFormatters.get(contentType);
            if (formatter == null) {
                formatter = messageFormatters.get(contentType.toLowerCase());
            }

            if (formatter == null) {
                Iterator<Entry<String, MessageFormatter>> iterator =
                        messageFormatters.entrySet().iterator();
                while (iterator.hasNext() && formatter == null) {
                    Entry<String, MessageFormatter> entry = iterator.next();
                    String key = entry.getKey();
                    if (contentType.matches(key)) {
                        formatter = entry.getValue();
                    }
                }
            }
        }
        return formatter;
    }

//    /**
//     *
//     * @deprecate Please use String version instead
//     * @param qname
//     * @return
//     */
//    public AxisModule getModule(QName qname) {
//        return getModule(qname.getLocalPart());
//    }

    /**
     * Get an AxisModule by name.
     *
     * If the exact name passed (which might contain a version) is present, we'll return that,
     * otherwise we'll look for the default version *if* there is no version in the passed
     * name.
     *
     * @param name module name to look up
     * @return an AxisModule if found, or null
     */
    public AxisModule getModule(String name) {
        AxisModule module = allModules.get(name);
        if (module != null) {
            return module;
        }
        // checks whether the version string seperator is not there in the
        // module name
        String defaultModuleVersion = getDefaultModuleVersion(name);
        if (defaultModuleVersion != null) {
            module = allModules.get(Utils.getModuleName(name, defaultModuleVersion));
            if (module != null) {
                return module;
            }
        }
        return null;
    }

    /**
     * Return the module having name=moduleName & version=moduleVersion
     *
     * @param moduleName    The module name
     * @param moduleVersion The version of the module
     * @return The AxisModule having name=moduleName & version=moduleVersion
     */
    public AxisModule getModule(String moduleName, String moduleVersion) {
        if (moduleVersion == null || moduleVersion.trim().length() == 0) {
            moduleVersion = getDefaultModuleVersion(moduleName);
        }
        return allModules.get(Utils.getModuleName(moduleName, moduleVersion));
    }

    /**
     * Get the class loader that becomes the parent of all the modules
     *
     * @return a ClassLoader
     */
    public ClassLoader getModuleClassLoader() {
        return this.moduleClassLoader;
    }

    public ModuleConfiguration getModuleConfig(String moduleName) {
        return moduleConfigmap.get(moduleName);
    }

    /**
     * @return Returns HashMap.
     */
    public HashMap<String, AxisModule> getModules() {
        return allModules;
    }

    /**
     * Get a list of the global modules
     *
     * @return the global module list.  BE CAREFUL, this list is mutable.
     * @deprecated please use addGlobalModule()
     */
    public List<String> getGlobalModules() {
        return globalModuleList;
    }

    /**
     * @return Returns ArrayList.
     */
    public List<Phase> getOutFaultFlowPhases() {
        return outFaultPhases;
    }

    public PhasesInfo getPhasesInfo() {
        return phasesinfo;
    }

    public URL getRepository() {
        return axis2Repository;
    }

    /**
     * Method getService.
     *
     * @param name the name of the service to look up
     * @return an AxisService if found, or null
     */
    public AxisService getService(String name) throws AxisFault {
        AxisService axisService = allServices.get(name);
        if (axisService != null) {
            if (axisService.isActive()) {
                return axisService;
            } else {
                throw new AxisFault(Messages
                        .getMessage("serviceinactive", name));
            }
        } else {
            axisService = allEndpoints.get(name);
            if (axisService != null) {
                if (axisService.isActive()) {
                    return axisService;
                } else {
                    throw new AxisFault(Messages
                            .getMessage("serviceinactive", name));
                }
            }
        }
        return null;
    }

    /**
     * Service can start and stop, once stopped it cannot be accessed, so we
     * need a way to get the service even if service is not active.
     *
     * @param serviceName name to look up
     * @return AxisService
     */
    public AxisService getServiceForActivation(String serviceName) {
        AxisService axisService;
        axisService = allServices.get(serviceName);
        if (axisService != null) {
            return axisService;
        } else {
            axisService = allEndpoints.get(serviceName);
            return axisService;
        }
    }

    // The class loader that becomes the parent of all the services
    public ClassLoader getServiceClassLoader() {
        return this.serviceClassLoader;
    }

    public AxisServiceGroup getServiceGroup(String serviceNameAndGroupString) {
        // return (AxisServiceGroup)
        // serviceGroups.get(serviceNameAndGroupString);
        return (AxisServiceGroup) getChild(serviceNameAndGroupString);
    }

    public Iterator<AxisServiceGroup> getServiceGroups() {
        // return serviceGroups.values().iterator();
        return (Iterator<AxisServiceGroup>) getChildren();
    }

    // To get all the services in the system
    public HashMap<String, AxisService> getServices() {
        HashMap<String, AxisService> hashMap = new HashMap<String, AxisService>(this.allServices.size());
        String key;
        for (String s : this.allServices.keySet()) {
            key = s;
            hashMap.put(key, this.allServices.get(key));
        }
        return hashMap;
    }

    // The class loader which become the top most parent of all the modules and
    // services
    public ClassLoader getSystemClassLoader() {
        return this.systemClassLoader;
    }

    public TransportInDescription getTransportIn(String name) {
        return transportsIn.get(name);
    }

    public TransportOutDescription getTransportOut(String name) {
        return transportsOut.get(name);
    }

    public HashMap<String, TransportInDescription> getTransportsIn() {
        return transportsIn;
    }

    public HashMap<String, TransportOutDescription> getTransportsOut() {
        return transportsOut;
    }

    /**
     * Find out whether a given module is engaged.
     *
     * This method needs to remain for a few Axis2 releases to support
     * legacy apps still using it.  It will be disappearing in 1.6.
     *
     * @param qname QName of the module
     * @deprecated Use {@link #isEngaged(String)}
     * @return true if a module matching the passed QName is engaged globally
     */
    public boolean isEngaged(QName qname) {
        return isEngaged(qname.getLocalPart());
    }

    public boolean isEngaged(String moduleId) {
        AxisModule module = getModule(moduleId);
        if (module == null) {
            return false;
        }
        boolean isEngaged = super.isEngaged(module);
        if (!isEngaged) {
            AxisModule defaultModule = getDefaultModule(moduleId);
            isEngaged = engagedModules != null && engagedModules.values().contains(defaultModule);
        }
        return isEngaged;
    }

    public boolean isEngaged(AxisModule axisModule) {
        boolean isEngaged = super.isEngaged(axisModule);
        if (!isEngaged) {
            isEngaged = engagedModules != null &&
                        engagedModules.values().contains(axisModule);
        }
        return isEngaged;
    }

    public void setGlobalOutPhase(List<Phase> outPhases) {
        this.outPhases = outPhases;
    }

    /**
     * Set the inbound fault phases
     *
     * TODO: Passing mutable lists like this and using them directly is bad practice.
     *
     * @param list a List of Phases which will become our inbound fault flow
     */
    public void setInFaultPhases(List<Phase> list) {
        inFaultPhases = list;
    }

    public void setInPhasesUptoAndIncludingPostDispatch(
    		List<Phase> inPhasesUptoAndIncludingPostDispatch) {
        this.inPhasesUptoAndIncludingPostDispatch = inPhasesUptoAndIncludingPostDispatch;
    }

    public void setModuleClassLoader(ClassLoader classLoader) {
        this.moduleClassLoader = classLoader;
    }

    /**
     * Set the outbound fault phases
     *
     * TODO: Passing mutable lists like this and using them directly is bad practice.
     *
     * @param list a List of Phases which will become our outbound fault flow
     */
    public void setOutFaultPhases(List<Phase> list) {
        outFaultPhases = list;
    }

    public void setPhasesInfo(PhasesInfo phasesInfo) {
        this.phasesinfo = phasesInfo;
    }

    public void setRepository(URL axis2Repository) {
        this.axis2Repository = axis2Repository;
    }

    public void setServiceClassLoader(ClassLoader classLoader) {
        this.serviceClassLoader = classLoader;
    }

    public void setSystemClassLoader(ClassLoader classLoader) {
        this.systemClassLoader = classLoader;
    }

    /**
     * Adds a default module version, which can be done either programmatically
     * or by using axis2.xml. The default module version is important if user
     * asks to engage a module without given version ID, in which case,
     * the default version is engaged.
     *
     * TODO: This currently does NOTHING if a default has already been set. Should either overwrite or throw.
     *
     * @param moduleName the name of the module for which we're setting the default version
     * @param moduleVersion the default version
     */
    public void addDefaultModuleVersion(String moduleName, String moduleVersion) {
        if (nameToversionMap.get(moduleName) == null) {
            nameToversionMap.put(moduleName, moduleVersion);
        }
    }

    public String getDefaultModuleVersion(String moduleName) {
        return nameToversionMap.get(moduleName);
    }

    public AxisModule getDefaultModule(String moduleName) {
        String defaultModuleVersion = getDefaultModuleVersion(moduleName);
        if (defaultModuleVersion == null) {
            return allModules.get(moduleName);
        } else {
            return allModules.get(moduleName + "-" + defaultModuleVersion);
        }
    }

    public ClusteringAgent getClusteringAgent() {
        return clusteringAgent;
    }

    public void setClusteringAgent(ClusteringAgent clusteringAgent) {
        this.clusteringAgent = clusteringAgent;
    }

     public TransactionConfiguration getTransactionConfiguration() {
        return transactionConfiguration;
    }

    public void setTransactionConfig(TransactionConfiguration transactionConfiguration) {
        this.transactionConfiguration = transactionConfiguration;
    }

    public Object getKey() {
        return toString();
    }

    public void stopService(String serviceName) throws AxisFault {
        AxisService service = allServices.get(serviceName);
        if (service == null) {
            throw new AxisFault(Messages.getMessage("servicenamenotvalid",
                                                    serviceName));
        }
        service.setActive(false);
        notifyObservers(new AxisEvent(AxisEvent.SERVICE_STOP , service), service);
    }

    public void startService(String serviceName) throws AxisFault {
        AxisService service = allServices.get(serviceName);
        if (service == null) {
            throw new AxisFault(Messages.getMessage("servicenamenotvalid",
                                                    serviceName));
        }
        service.setActive(true);
        notifyObservers(new AxisEvent(AxisEvent.SERVICE_START , service), service);
    }

    public List<AxisModule> getModulesForPolicyNamesapce(String namesapce) {
        return policySupportedModules.get(namesapce);
    }

    public void registerModulePolicySupport(AxisModule axisModule) {
        String[] namespaces = axisModule.getSupportedPolicyNamespaces();

        if (namespaces == null) {
            return;
        }

        List<AxisModule> modulesList;

        for (String namespace : namespaces) {
            modulesList = policySupportedModules.get(namespace);

            if (modulesList != null) {
                modulesList.add(axisModule);
            } else {
                modulesList = new ArrayList<AxisModule>();
                modulesList.add(axisModule);
                policySupportedModules.put(namespace, modulesList);
            }
        }
    }

    public void registerLocalPolicyAssertions(AxisModule axisModule) {
        QName[] localPolicyAssertions = axisModule.getLocalPolicyAssertions();

        if (localPolicyAssertions == null) {
            return;
        }

        for (QName localPolicyAssertion : localPolicyAssertions) {
            addLocalPolicyAssertion(localPolicyAssertion);
        }
    }

    /**
     * This method returns read-only list of AxisObservers registered with this
     * {@link AxisConfiguration}.
     * 
     * @return ArrayList<AxisObserver>
     */
    public ArrayList<AxisObserver> getObserversList() {
    	AxisObserver[] array = observerSet.toArray(new AxisObserver[observerSet.size()]);
    	ArrayList<AxisObserver> observers = new ArrayList<AxisObserver>(array.length);
    	observers.addAll(Arrays.asList(array));
    	return observers;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    /**
     * getTargetResolverChain returns an instance of
     * TargetResolver which iterates over the registered
     * TargetResolvers, calling each one in turn when
     * resolveTarget is called.
     *
     * @return a TargetResolver which iterates over all registered TargetResolvers.
     */
    public TargetResolver getTargetResolverChain() {
        if (targetResolvers.isEmpty()) {
            return null;
        }
        return new TargetResolver() {
            public void resolveTarget(MessageContext messageContext) {
                Iterator<TargetResolver> iter = targetResolvers.iterator();
                while (iter.hasNext()) {
                    TargetResolver tr = iter.next();
                    tr.resolveTarget(messageContext);
                }
            }
        };
    }

    public void addTargetResolver(TargetResolver tr) {
        targetResolvers.add(tr);
    }

    public void addLocalPolicyAssertion(QName name) {
        this.localPolicyAssertions.add(name);
    }

    public List<QName> getLocalPolicyAssertions() {
        return this.localPolicyAssertions;
    }

    public void removeLocalPolicyAssertion(QName name) {
        this.localPolicyAssertions.remove(name);
    }

    public boolean isAssertionLocal(QName name) {
        return this.localPolicyAssertions.contains(name);
    }

    /**
     * Allows to define/configure Data Locator for specified dialect at Axis 2 Configuration.
     *
     * @param dialect-  an absolute URI represents the format and version of data
     * @param classname - class name of the Data Locator configured to support retrieval
     *                  for the specified dialect.
     */
    public void addDataLocatorClassNames(String dialect, String classname) {
        dataLocatorClassNames.put(dialect, classname);
    }

    /**
     * For internal used only! To store instance of DataLocator when it is first loaded. This allows to
     * reuse of the DataLocator after it is initially loaded.
     *
     * @param dialect-    an absolute URI represents the format and version of data
     * @param dataLocator - specified an DataLocator instance  to support retrieval
     *                    of the specified dialect.
     */
    public void addDataLocator(String dialect, AxisDataLocator dataLocator) {
        dataLocators.put(dialect, dataLocator);
    }

    /**
     * Return DataLocator instance for specified dialect.
     * @param dialect the dialect to look up
     * @return an AxisDataLocator, or null
     */
    public AxisDataLocator getDataLocator(String dialect) {
        return dataLocators.get(dialect);
    }


    /**
     * Return classname of DataLocator configured for specified dialect.
     * @param dialect the dialect to look up
     * @return a String containing a class name, or null
     */
    public String getDataLocatorClassName(String dialect) {
        return dataLocatorClassNames.get(dialect);
    }


    /**
     * Checks whether the system pre-defined phases
     * for all the flows, have been changed. If they have been changed, throws a DeploymentException.
     *
     * @throws org.apache.axis2.deployment.DeploymentException
     *
     */
    public void validateSystemPredefinedPhases() throws DeploymentException {
        PhasesInfo phasesInfo = getPhasesInfo();
        setInPhasesUptoAndIncludingPostDispatch(phasesInfo.getGlobalInflow());
        setInFaultPhases(phasesInfo.getGlobalInFaultPhases());
        setGlobalOutPhase(phasesInfo.getGlobalOutPhaseList());
        setOutFaultPhases(phasesInfo.getOUT_FaultPhases());
    }

    public AxisConfigurator getConfigurator() {
        return configurator;
    }

    public void setConfigurator(AxisConfigurator configurator) {
        this.configurator = configurator;
    }

    public void cleanup() {
        if (configurator != null) {
            configurator.cleanup();
        }
        if (clusteringAgent != null) {
            clusteringAgent.stop();
        }
        this.policySupportedModules.clear();
        this.moduleConfigmap.clear();
        this.allEndpoints.clear();
        this.allModules.clear();
        this.allServices.clear();
        this.outPhases.clear();
        this.messageReceivers.clear();
        this.targetResolvers.clear();
        if (this.engagedModules != null) {
            this.engagedModules.clear();
        }
        this.configurator = null;
    }

    /**
     * This method can be used to insert a phase at the runtime for a given location
     * And the relative location can be specified by beforePhase and afterPhase. Parameters
     * Either or both of them can be null , if both the parameters are null then the phase
     * will be added some where in the global phase. If one of them are null then the phase
     * will be added
     *  - If the beforePhase is null then the phase will be added after the afterPhase
     *  - If the after phase is null then the phase will be added before the beforePhase
     * Type of the flow will be specified by the parameter flow.
     *   1 - Inflow
     *   2 - out flow
     *   3 - fault in flow
     *   4 - fault out flow
     *
     * @param d the Deployable representing the Phase to deploy
     * @param flow the type of the flow
     * @throws org.apache.axis2.AxisFault : If something went wrong
     */
    public void insertPhase(Deployable d, int flow) throws AxisFault {

        switch (flow) {
            case PhaseMetadata.IN_FLOW : {
                List<Phase> phaseList = phasesinfo.getINPhases();
                phaseList = findAndInsertPhase(d, phaseList);
                if (phaseList != null) {
                    phasesinfo.setINPhases(phaseList);
                }
                break;
            }
            case PhaseMetadata.OUT_FLOW : {
            	List<Phase> phaseList = phasesinfo.getOUTPhases();
                phaseList = findAndInsertPhase(d, phaseList);
                if (phaseList != null) {
                    phasesinfo.setOUTPhases(phaseList);
                }
                break;
            }
            case PhaseMetadata.FAULT_OUT_FLOW : {
            	List<Phase> phaseList = phasesinfo.getOutFaultPhaseList();
                phaseList = findAndInsertPhase(d, phaseList);
                if (phaseList != null) {
                    phasesinfo.setOUT_FaultPhases(phaseList);
                }
                break;
            }
            case PhaseMetadata.FAULT_IN_FLOW : {
            	List<Phase> phaseList = phasesinfo.getIN_FaultPhases();
                phaseList = findAndInsertPhase(d, phaseList);
                if (phaseList != null) {
                    phasesinfo.setIN_FaultPhases(phaseList);
                }
                break;
            }
        }
    }

    /**
     * Insert a Phase into a list of Phases
     *
     * @param d a Deployable containing the Phase to insert
     * @param phaseList a list of Phases
     * @return a new List of Phases with the new one correctly deployed.
     * @throws AxisFault
     */
    private List<Phase> findAndInsertPhase(Deployable d, List<Phase> phaseList) throws AxisFault {
        DeployableChain<Phase> ec = new DeployableChain<Phase>();
        String last = null;
        for (Phase phase : phaseList) {
            String name = phase.getName();
            Deployable existing = new Deployable(name);
            existing.setTarget(phase);
            if (last != null) {
                // Set up explicit chain relationship for preexisting phases, for now.
                ec.addRelationship(last, name);
            }
            last = name;
            try {
                ec.deploy(existing);
            } catch (Exception e) {
                // This should never happen when building a simple list like the above
                throw AxisFault.makeFault(e);
            }
        }

        try {
            ec.deploy(d);

            if (d.getTarget() == null) {
                Phase phase = new Phase();
                phase.setName(d.getName());
                d.setTarget(phase);
            }

            ec.rebuild();
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }

        phaseList = ec.getChain();

        return phaseList;
    }
    
    private void processEndpoints(AxisService axisService,
    		AxisConfiguration axisConfiguration) throws AxisFault {
        Map<String, AxisEndpoint> enspoints = axisService.getEndpoints();
        if (enspoints == null || enspoints.size() == 0) {
			org.apache.axis2.deployment.util.Utils.addEndpointsToService(
					axisService, axisConfiguration);
		}
	}

    public boolean isChildFirstClassLoading(){
        boolean childFirstClassLoading = false;
        Parameter isChildFirstClassLoading = this.getParameter(Constants.Configuration.ENABLE_CHILD_FIRST_CLASS_LOADING);
        if (isChildFirstClassLoading != null){
             childFirstClassLoading = JavaUtils.isTrueExplicitly(isChildFirstClassLoading.getValue());
        }
        return childFirstClassLoading;
    }

    public SecretResolver getSecretResolver() {
        return secretResolver;
    }

    public void setSecretResolver(SecretResolver secretResolver) {
        this.secretResolver = secretResolver;
    }
}
