/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.core.multitenancy;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.AxisConfigBuilder;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.ModuleDeployer;
import org.apache.axis2.deployment.RepositoryListener;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.engine.AxisObserver;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.CarbonAxisConfigurator;
import org.wso2.carbon.core.CarbonThreadFactory;
import org.wso2.carbon.core.deployment.CarbonDeploymentSchedulerTask;
import org.wso2.carbon.core.deployment.DeploymentInterceptor;
import org.wso2.carbon.core.deployment.RegistryBasedRepository;
import org.wso2.carbon.core.deployment.RegistryBasedRepositoryUpdater;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.core.util.ParameterUtil;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.PreAxisConfigurationPopulationObserver;
import org.wso2.carbon.utils.WSO2Constants;
import org.wso2.carbon.utils.component.xml.config.DeployerConfig;
import org.wso2.carbon.utils.deployment.Axis2DeployerProvider;
import org.wso2.carbon.utils.deployment.Axis2DeployerRegistry;
import org.wso2.carbon.utils.deployment.Axis2ModuleRegistry;
import org.wso2.carbon.utils.deployment.GhostArtifactRepository;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.axis2.transport.TransportListener.HOST_ADDRESS;

/**
 * The tenant specific AxisConfigurator
 */
public class TenantAxisConfigurator extends DeploymentEngine implements AxisConfigurator {


    private static Log log = LogFactory.getLog(TenantAxisConfigurator.class);

    private Collection globallyEngagedModules = new ArrayList();

    private final AxisConfiguration mainAxisConfig;
    private final String tenantDomain;
    private final int tenantId;
    private final String repoLocation;
    private final UserRegistry registry;
    private final BundleContext bundleContext;
    private final Bundle[] moduleBundles;
    private final Bundle[] deployerBundles;
    private File repositoryDir;

    private ScheduledExecutorService scheduler;
    private CarbonDeploymentSchedulerTask schedulerTask;

    @Deprecated
    public TenantAxisConfigurator(AxisConfiguration mainAxisConfig,
                                  String tenantDomain,
                                  int tenantId,
                                  UserRegistry registry) throws AxisFault {
        this.tenantDomain = tenantDomain;
        this.tenantId = tenantId;
        this.mainAxisConfig = mainAxisConfig;
        this.registry = registry;
        this.bundleContext = CarbonCoreDataHolder.getInstance().getBundleContext();
        this.moduleBundles =
                ((CarbonAxisConfigurator) mainAxisConfig.getConfigurator()).
                        getConfigItemHolder().getModuleBundles();
        this.deployerBundles =
                ((CarbonAxisConfigurator) mainAxisConfig.getConfigurator()).
                        getConfigItemHolder().getDeployerBundles();
        File tenantDir = new File(CarbonUtils.getCarbonTenantsDirPath() + File.separator + tenantId);
        if (!tenantDir.exists() && !tenantDir.mkdirs()) {
            log.warn("Could not create directory " + tenantDir.getAbsolutePath());
        }
        this.repoLocation = tenantDir.getAbsolutePath();

        // Use registry based deployer if necessary
        if (CarbonUtils.useRegistryBasedRepository()) {
            String registryPath = "/repository/deployment";
            new RegistryBasedRepository(registry,
                                        registryPath,
                                        repoLocation).updateFileSystemFromRegistry();
            RegistryBasedRepositoryUpdater.scheduleAtFixedRate(registry,
                                                               registryPath,
                                                               repoLocation, 0, 10);
        }
    }

    // New constructor is introduced to set the config and local registry separately

    public TenantAxisConfigurator(AxisConfiguration mainAxisConfig,
                                  String tenantDomain,
                                  int tenantId,
                                  UserRegistry configRegistry,
                                  UserRegistry localRegistry) throws AxisFault {
        this.tenantDomain = tenantDomain;
        this.tenantId = tenantId;
        this.mainAxisConfig = mainAxisConfig;
        this.registry = configRegistry;
        this.bundleContext = CarbonCoreDataHolder.getInstance().getBundleContext();
        this.moduleBundles =
                ((CarbonAxisConfigurator) mainAxisConfig.getConfigurator()).
                        getConfigItemHolder().getModuleBundles();
        this.deployerBundles =
                ((CarbonAxisConfigurator) mainAxisConfig.getConfigurator()).
                        getConfigItemHolder().getDeployerBundles();
        String filePath = MultitenantUtils.getAxis2RepositoryPath(tenantId);
        File tenantDir = new File(filePath);
        if (!tenantDir.exists() && !tenantDir.mkdirs()) {
            log.warn("Could not create directory " + tenantDir.getAbsolutePath());
        }
        this.repoLocation = filePath;

        // Use registry based deployer if necessary
        if (CarbonUtils.useRegistryBasedRepository()) {
            String registryPath = "/repository/deployment";
            new RegistryBasedRepository(localRegistry,
                                        registryPath,
                                        repoLocation).updateFileSystemFromRegistry();
            RegistryBasedRepositoryUpdater.scheduleAtFixedRate(localRegistry,
                                                               registryPath,
                                                               repoLocation, 0, 10);
        }
    }

    private String getTenantString(String tenantDomain, int tenantId) {
        return tenantDomain + "[" + tenantId + "]";
    }

    /**
     * This over-ridden method is same as super method except
     * this does a axisConfig.getRepository() == null before setting the repo.
     * This is done because with Stratos, we can not set the repo twice.
     *
     * @param repoDir
     * @throws DeploymentException
     */
    @Override
    public void loadRepository(String repoDir) throws DeploymentException {
        File axisRepo = new File(repoDir);
        if (!axisRepo.exists()) {
            throw new DeploymentException(
                    Messages.getMessage("cannotfindrepo", repoDir));
        }
        setDeploymentFeatures();
        prepareRepository(repoDir);
        // setting the CLs
        setClassLoaders(repoDir);
        repoListener = new RepositoryListener(this, false);
        org.apache.axis2.util.Utils
                .calculateDefaultModuleVersion(axisConfig.getModules(), axisConfig);
        try {
            try {
                if (axisConfig.getRepository() == null) {
                //we set
                    axisConfig.setRepository(axisRepo.toURL());
                }
            } catch (MalformedURLException e) {
                log.info(e.getMessage());
            }
            axisConfig.validateSystemPredefinedPhases();
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
        }
    }

     /**
     * Overriding this method because we want to override the service dir path. Ghost deployer will
     * set the proper services path.
     *
     * @param repositoryName - path to repository
     */
    @Override
    protected void prepareRepository(String repositoryName) {
        repositoryDir = new File(repositoryName);
        // set a fake services dir which is not there
        if (servicesPath != null && !GhostDeployerUtils.isGhostOn()) {
            servicesDir = new File(servicesPath);
            if (!servicesDir.exists()) {
                servicesDir = new File(repositoryDir, servicesPath);
            }
        } else {
            servicesDir = new File(repositoryDir, DeploymentConstants.SERVICE_PATH);
        }
        if (modulesPath != null) {
            modulesDir = new File(modulesPath);
            if (!modulesDir.exists()) {
                modulesDir = new File(repositoryDir, modulesPath);
            }
        } else {
            modulesDir = new File(repositoryDir, DeploymentConstants.MODULE_PATH);
        }
        if (!modulesDir.exists()) {
            log.info(Messages.getMessage("nomoduledirfound", getRepositoryPath(repositoryDir)));
        }
    }

    public File getRepositoryDir() {
        return repositoryDir;
    }

    /**
     * First create a Deployment engine, use that to create an AxisConfiguration
     *
     * @return Axis Configuration
     * @throws AxisFault
     */
    public AxisConfiguration getAxisConfiguration() throws AxisFault {
        //ClassLoader origTccl = Thread.currentThread().getContextClassLoader();
        //Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        log.info("Creating tenant AxisConfiguration for tenant: " +
                 getTenantString(tenantDomain, tenantId));
        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(tenantId);
            carbonContext.setTenantDomain(tenantDomain);

            //TODO Refactor the code use only one mechanism to populate CarbonContext.
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

            if (log.isDebugEnabled()) {
                log.debug("Axis2 repo: " + repoLocation);
            }
            populateAxisConfig();
            addDeployer(new ModuleDeployer(), repoLocation + File.separator +
                                              "axis2modules", "mar");
            axisConfig.setConfigurator(this);

            //TODO: May need to set certain parameters into the AxisConfig

            // Hot deployment & update should be turned on for tenants
            axisConfig.addParameter(new Parameter(DeploymentConstants.TAG_HOT_DEPLOYMENT,
                                                  "true"));
            axisConfig.addParameter(new Parameter(DeploymentConstants.TAG_HOT_UPDATE,
                                                  "true"));

            globallyEngagedModules = axisConfig.getEngagedModules();
            loadRepository(repoLocation);

            // set the service class loader in the axisConfig, this is needed due to ghost deployer
            // as the service deployer is no longer treated as a special case, we have to do this
            File axis2ServicesDir = new File(repoLocation,
                    CarbonUtils.getAxis2ServicesDir(axisConfig));
            if (axis2ServicesDir.exists()) {
                axisConfig.setServiceClassLoader(
                        Utils.getClassLoader(axisConfig.getSystemClassLoader(), axis2ServicesDir,
                                axisConfig.isChildFirstClassLoading()));
            }

            for (Object globallyEngagedModule : globallyEngagedModules) {
                AxisModule module = (AxisModule) globallyEngagedModule;
                if (log.isDebugEnabled()) {
                    log.debug("Globally engaging module: " + module.getName());
                }
            }

            // Remove all the transports made available in the tenant's axis2.xml
            axisConfig.getTransportsOut().clear();

            // Remove all in-transports made available in the tenant's axis2.xml
            axisConfig.getTransportsIn().clear();

            // Add the transports that are made available in the main axis2.xml file
            TenantAxisUtils.setTenantTransports(mainAxisConfig, tenantDomain, axisConfig);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
            //Thread.currentThread().setContextClassLoader(origTccl);
        }
        return axisConfig;
    }

    private void populateAxisConfig() throws DeploymentException {
        InputStream axis2xmlStream = null;
        try {
            File tenantAxis2XML = new File(CarbonUtils.getCarbonConfigDirPath()+File.separator
                                           +"axis2"+File.separator + "tenant-axis2.xml");
            axis2xmlStream = new FileInputStream(tenantAxis2XML);
            axisConfig = populateAxisConfiguration(axis2xmlStream);
        } catch (FileNotFoundException e) {
            String msg = "Cannot read tenant-axis2.xml";
            log.error(msg, e);
            throw new DeploymentException(msg, e);
        } finally {
            try {
                if (axis2xmlStream != null) {
                    axis2xmlStream.close();
                }
            } catch (IOException e) {
                log.error("Could not close input stream to " +
                          DeploymentConstants.AXIS2_CONFIGURATION_RESOURCE, e);
            }
        }
    }

    public boolean isGlobalyEngaged(AxisModule axisModule) {
        String modName = axisModule.getName();
        for (Object globallyEngagedModule : globallyEngagedModules) {
            AxisModule module = (AxisModule) globallyEngagedModule;
            if (modName.startsWith(module.getName())) {
                return true;
            }
        }
        return false;
    }

    public void engageGlobalModules() throws AxisFault {
        engageModules();
    }

    public AxisConfiguration populateAxisConfiguration(InputStream in) throws DeploymentException {
        axisConfig = TenantAxisConfiguration.createInstance();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantId(tenantId);
        carbonContext.setTenantDomain(tenantDomain);

        boolean isUrlRepo = CarbonUtils.isURL(repoLocation);
        if (repoLocation != null && repoLocation.trim().length() != 0) {
            try {
                if (isUrlRepo) {
                    URL axis2Repository = new URL(repoLocation);
                    axisConfig.setRepository(axis2Repository);
                } else {
                    axisConfig.setRepository(new URL("file://" + repoLocation));
                }
            } catch (MalformedURLException e) {
                throw new DeploymentException("Invalid URL " + repoLocation, e);
            }
        }

        // Notify all observers
        if (bundleContext != null) {
            ServiceTracker tracker =
                    new ServiceTracker(bundleContext,
                                       PreAxisConfigurationPopulationObserver.class.getName(), null);
            tracker.open();
            Object[] services = tracker.getServices();
            if (services != null) {
                for (Object service : services) {
                    ((PreAxisConfigurationPopulationObserver) service).createdAxisConfiguration(axisConfig);
                }
            }
            tracker.close();
        }
        try {
            // Add the relevant AxisObservers to the tenantAxisConfig
            if (bundleContext != null) {
                ServiceTracker tracker =
                        new ServiceTracker(bundleContext, AxisObserver.class.getName(), null);
                tracker.open();
                ServiceReference[] serviceRefs = tracker.getServiceReferences();
                if (serviceRefs != null) {
                    for (ServiceReference serviceRef : serviceRefs) {
                        if (serviceRef.getProperty(MultitenantConstants.TENANT_ID) != null &&
                            tenantId == (Integer) serviceRef.getProperty(MultitenantConstants.TENANT_ID)) {
                            axisConfig.addObservers((AxisObserver) bundleContext.getService(serviceRef));
                        }
                    }
                }
                tracker.close();
            }

            // Set services dir
            File servicesDir = new File(repoLocation + File.separator +
                    CarbonUtils.getAxis2ServicesDir(axisConfig));
            if (!servicesDir.exists() && !servicesDir.mkdirs()) {
                throw new DeploymentException("Could not create services directory " +
                                              servicesDir.getAbsolutePath());
            }

            // Set modules dir
            String modulesDirName = "axis2modules";
            File modulesDir = new File(repoLocation + File.separator + modulesDirName);
            if(!modulesDir.exists() && !modulesDir.mkdirs()){
                throw new DeploymentException("Could not create modules directory " +
                                              modulesDir.getAbsolutePath());
            }
            axisConfig.addParameter(new Parameter(DeploymentConstants.MODULE_DRI_PATH,
                                                  modulesDirName));
        } catch (AxisFault e) {
            String msg =
                    "Cannot add DeploymentConstants.SERVICE_DIR_PATH or " +
                    "DeploymentConstants.MODULE_DIR_PATH parameters";
            log.error(msg, e);
            throw new DeploymentException(msg, e);
        }

        carbonContext.setRegistry(RegistryType.SYSTEM_CONFIGURATION, registry);
        try {
            // TODO: The governance system registry should be passed into the tenant axis
            // configurator like the config system registry - Senaka.
            carbonContext.setRegistry(RegistryType.SYSTEM_GOVERNANCE,
                    CarbonCoreDataHolder.getInstance().getRegistryService()
                            .getGovernanceSystemRegistry(tenantId));
            carbonContext.setRegistry(RegistryType.LOCAL_REPOSITORY,
                    CarbonCoreDataHolder.getInstance().getRegistryService().
                            getLocalRepository(tenantId));
        } catch (Exception ignored) {
            // We are not worried about the exception in here.
        }

        // The following two lines of code are kept for backward compatibility. Remove this once we
        // are certain that this is not required. -- Senaka.
        // Please also note that we no longer need to set the user realm to the configuration
        // explicitly.
        setRegistry();
        setUserRealm();

        // Add the DeploymentInterceptor for the tenant AxisConfigurations
        DeploymentInterceptor interceptor = new DeploymentInterceptor();
        interceptor.setRegistry(registry);
        interceptor.init(axisConfig);
        axisConfig.addObservers(interceptor);

        setHostName(axisConfig);

        //TCCL will be based on OSGi
        AxisConfigBuilder builder = new AxisConfigBuilder(in, axisConfig, this);
        builder.populateConfig();
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            String msg = "error in closing input stream";
            log.error(msg, e);
        }
        axisConfig.setConfigurator(this);
        Parameter disableArtifactLoading = axisConfig.getParameter("DisableArtifactLoading");
        if (disableArtifactLoading == null || "false".equals(disableArtifactLoading.getValue())) {
            moduleDeployer = new ModuleDeployer(axisConfig);
            new Axis2ModuleRegistry(axisConfig).register(moduleBundles);
            ServiceTracker deployerServiceTracker = null;
            Axis2DeployerProvider[] axis2DeployerProviderList;
            try {
                deployerServiceTracker = new ServiceTracker(bundleContext,
                        Axis2DeployerProvider.class.getName(), null);
                deployerServiceTracker.open();
                if (deployerServiceTracker.getServices() == null) {
                    axis2DeployerProviderList = new Axis2DeployerProvider[]{};
                } else {
                    axis2DeployerProviderList = Arrays.copyOf(deployerServiceTracker.getServices(),
                            deployerServiceTracker.getServices().length, Axis2DeployerProvider[].class);
                }
            } finally {
                if (deployerServiceTracker != null) {
                    deployerServiceTracker.close();
                }
            }
            List<DeployerConfig> deployerConfigs = readDeployerConfigs(axis2DeployerProviderList);
            if (GhostDeployerUtils.isGhostOn()) {
                GhostArtifactRepository ghostArtifactRepository = new GhostArtifactRepository(axisConfig);
                GhostDeployerUtils.setGhostArtifactRepository(ghostArtifactRepository, axisConfig);
            }

            // Adding deployers from vhosts and deployers which come inside bundles
            new Axis2DeployerRegistry(axisConfig).register(deployerBundles,
                    deployerConfigs);
        }
        return axisConfig;
    }

    private List<DeployerConfig> readDeployerConfigs(Axis2DeployerProvider[] axis2DeployerProviders) {
        List<DeployerConfig> allDeployerConfig = new ArrayList<DeployerConfig>();
        for (Axis2DeployerProvider axis2DeployerProvider : axis2DeployerProviders) {
            allDeployerConfig.addAll(axis2DeployerProvider.getDeployerConfigs());
        }
        return allDeployerConfig;
    }

    public synchronized void runDeployment(){
        schedulerTask.runAxisDeployment();
    }

    public void setRepoUpdateFailed(){
        schedulerTask.setRepoUpdateFailed();
    }

    @Override
    protected void startSearch(RepositoryListener listener) {
        schedulerTask = new CarbonDeploymentSchedulerTask(listener, axisConfig,
                                                          tenantId, tenantDomain);
        scheduler = Executors
                .newScheduledThreadPool(1, new CarbonThreadFactory(new ThreadGroup("TenantDeploymentSchedulerThread")));
        String deploymentInterval =
                CarbonCoreDataHolder.getInstance().
                        getServerConfigurationService().getFirstProperty("Axis2Config.DeploymentUpdateInterval");
        int deploymentIntervalInt = 15;
        if(deploymentInterval != null) {
           deploymentIntervalInt = Integer.parseInt(deploymentInterval); 
        }
        scheduler.scheduleWithFixedDelay(schedulerTask, 0, deploymentIntervalInt, TimeUnit.SECONDS);
    }

    @Override
    public void cleanup() {
        scheduler.shutdown();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantId(tenantId);
            cc.setTenantDomain(tenantDomain);
            super.cleanup();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        if (CarbonUtils.useRegistryBasedRepository()) {
            RegistryBasedRepositoryUpdater.cancelTask(repoLocation);
        }
    }

    private void setRegistry() throws DeploymentException {
        Parameter param = new Parameter(WSO2Constants.CONFIG_SYSTEM_REGISTRY_INSTANCE, registry);
        try {
            axisConfig.addParameter(param);
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault.getMessage(), axisFault);
        }
    }

    private void setUserRealm() throws DeploymentException {
        Parameter param = new Parameter(WSO2Constants.USER_REALM_INSTANCE, registry.getUserRealm());
        try {
            axisConfig.addParameter(param);
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault.getMessage(), axisFault);
        }
    }

    private static void setHostName(AxisConfiguration axisConfig) throws DeploymentException {
        try {
            String hostName =
                    CarbonCoreDataHolder.getInstance().
                            getServerConfigurationService().getFirstProperty("HostName");
            if (hostName != null) {
                Parameter param = ParameterUtil.createParameter(HOST_ADDRESS, hostName);
                axisConfig.addParameter(param);
            }
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault.getMessage(), axisFault);
        }
    }

    @Override
    public void loadServices() {
        //We don't deploy any artifacts at this time, TenantAxisUtils will take care of
        //deployment in later stage of server startup (Refer CARBON-14977 ).

    }

    public  void deployServices() {
        super.loadServices();
    }
}
