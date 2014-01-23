/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.core.persistence;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * The whole serviceGroup file format
 * <p/>
 * <br/>
 * <pre>
 * {@code <serviceGroup successfully.added="true/false" name="sgXXX" hash.value="xxx">
 *      <parameter name="yyy">xxx</parameter>
 *      <module name="addressing" version="1.1/undefined" type="int"/>
 *      <service name="aaa" documentation="aa aa" EXPOSED_ON_ALL_TRANSPORTS="true/false" DEPLOYED_TIME="$longValue">
 *          <operation name="bbb" documentation="" policy.uuid="xxx" message.in.policy.uuid="yyy" message.out.policy.uuid="zzz" >
 *              <parameter name="ccc">param value</parameter>
 *              <module name="" version="" type=""></module>
 *          </operation>
 *          <policies>
 *              <policy policy.type="2" version="xxx">
 *                  <policyUUID>dsdsds</policyUUID>
 *                  <wsp:Policy>
 *                  blah blah, and blah...
 *                  </wsp:Policy>
 *              </policy>
 *          </policies>
 *      //there should be some associations to modules here
 *
 *
 *          <bindings>
 *              <binding name="xxx">
 *              <operation name="yyy>
 *              </operation>
 *     </binding>
 *
 *      <association name="exposedTransports" type="" destinationPath="/_config/..."></association>
 *      </service>
 * </serviceGroup>
 * }
 * </pre>
 */
public class ServiceGroupPersistenceManager extends AbstractPersistenceManager {

    private static final Log log = LogFactory.getLog(ServiceGroupPersistenceManager.class);

    /**
     * Constructor gets the axis configutilssis and calls the super constructor.
     *
     * @param axisConfig - AxisConfiguration
     * @throws AxisFault - if the config registry is not found
     */
    public ServiceGroupPersistenceManager(AxisConfiguration axisConfig, PersistenceFactory pf) throws AxisFault {
        super(axisConfig, pf.getServiceGroupFilePM(), pf);
    }

    /**
     * Constructor gets the axis configutilssis and calls the super constructor.
     *
     * @param axisConfig - AxisConfiguration
     * @throws AxisFault - if the config registry is not found
     */
    public ServiceGroupPersistenceManager(AxisConfiguration axisConfig) throws AxisFault {
        super(axisConfig);
        try {
            if (this.pf == null) {
                this.pf = PersistenceFactory.getInstance(axisConfig);
            }
            this.fpm = this.pf.getServiceGroupFilePM();
        } catch (Exception e) {
            log.error("Error getting PersistenceFactory instance", e);
        }
    }

    /**
     * Returns the registry Resource for the specified Service group name
     *
     * @param serviceGroupId - Service Group name
     * @return - Service Group resource
     * @throws Exception - on registry transaction error
     */
    public OMElement getServiceGroup(String serviceGroupId) throws Exception {
        try {
            if (getServiceGroupFilePM().fileExists(serviceGroupId)) {
                OMElement sgElement = (OMElement) getServiceGroupFilePM().get(
                        serviceGroupId, Resources.ServiceGroupProperties.ROOT_XPATH);
                if (getServiceGroupFilePM().getAttribute(serviceGroupId, Resources.ServiceGroupProperties.ROOT_XPATH +
                        "@" + Resources.SUCCESSFULLY_ADDED) != null) {
                    return sgElement;
                }
            }
        } catch (Throwable e) {
            handleException("Could not get the Service Group resource", e);
        }
        return null;
    }

    /**
     * Deletes the registry resource of the specified service group
     *
     * @param serviceGroup - AxisServiceGroup instance
     * @throws Exception - on error
     */
    public void deleteServiceGroup(AxisServiceGroup serviceGroup) throws Exception {
        Iterator services = serviceGroup.getServices();
        Parameter param = serviceGroup.getParameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM);
        if (services.hasNext() && ((AxisService) services.next()).isClientSide()) {
            return;
        }
        try {
            if (param == null || !JavaUtils.isTrue(param.getValue().toString())) {
                removeResource(serviceGroup.getServiceGroupName(), Resources.ServiceGroupProperties.ROOT_XPATH);
                //a transaction gets started inside this.
                if (log.isDebugEnabled()) {
                    log.debug("Successfully deleted resource for " +
                            serviceGroup.getServiceGroupName() + " Service Group");
                }
            }
        } catch (Throwable e) {
            handleExceptionWithRollback(serviceGroup.getServiceGroupName(), "Could not delete Service Group " +
                    "resource from Config Registry", e);
        }
    }

    /**
     * Handle the engagement of the module to service group at the registry level
     *
     * @param module       - AxisModule instance
     * @param serviceGroup - AxisServiceGroup instance
     * @throws Exception - on error
     */
    public void engageModuleForServiceGroup(AxisModule module, AxisServiceGroup serviceGroup)
            throws Exception {
        try {
            handleModuleForAxisDescription(serviceGroup.getServiceGroupName(),
                    module, Resources.ServiceGroupProperties.ROOT_XPATH, true);
            if (log.isDebugEnabled()) {
                log.debug("Successfully engaged " + module.getName() + " module to " +
                        serviceGroup.getServiceGroupName() + " service group ");
            }
        } catch (Throwable e) {
            handleExceptionWithRollback(module.getName(), "Unable to engage " + module.getName() + " module to " +
                    serviceGroup.getServiceGroupName() + " service group ", e);
        }
    }

    /**
     * Handle the dis-engagement of the module to service group at the registry level
     *
     * @param module       - AxisModule instance
     * @param serviceGroup - AxisServiceGroup instance
     * @throws Exception - on error
     */
    public void disengageModuleForServiceGroup(AxisModule module, AxisServiceGroup serviceGroup)
            throws Exception {
        try {
            handleModuleForAxisDescription(serviceGroup.getServiceGroupName(),
                    module, Resources.ServiceGroupProperties.ROOT_XPATH, false);
            if (log.isDebugEnabled()) {
                log.debug("Successfully disengaged " + module.getName() + " module from " +
                        serviceGroup.getServiceGroupName() + " service group ");
            }
        } catch (Throwable e) {
            handleExceptionWithRollback(module.getName(), "Unable to disengage " + module.getName() +
                    " module from " + serviceGroup.getServiceGroupName() + " service group ", e);
        }
    }

    /**
     * Handle initialization of an already existing service group in registry. Loads all parameters
     * and engaged modules into the service group instance.
     *
     * @param serviceGroup - AxisServiceGroup instance
     * @throws Exception - on error
     */
    public void handleExistingServiceGroupInit(AxisServiceGroup serviceGroup) throws Exception {
        String serviceGroupId = serviceGroup.getServiceGroupName();
        try {
            boolean isTransactionStarted = getServiceGroupFilePM().isTransactionStarted(serviceGroupId);
            if(!isTransactionStarted) {
                getServiceGroupFilePM().beginTransaction(serviceGroupId);
            }
            // Add the Service Group Parameters
            loadParameters(serviceGroupId, serviceGroup, Resources.
                    ServiceGroupProperties.ROOT_XPATH + Resources.ParameterProperties.PARAMETER);

            // Disengage all the statically engaged modules (i.e. those module
            // engaged from the services.xml file)
            serviceGroup.getEngagedModules().clear();

            // Engage modules to service group
            List moduleList = getServiceGroupFilePM().getAll(serviceGroupId,
                    Resources.ServiceGroupProperties.ROOT_XPATH +
                            Resources.ModuleProperties.MODULE_XML_TAG);
            for (Object node : moduleList) {  //The top-level moduleList in serviceGroup file
                OMElement moduleElement = (OMElement) node;
                String name = moduleElement.getAttributeValue(new QName(Resources.NAME));
                String version = moduleElement.getAttributeValue(new QName(Resources.VERSION));
                AxisModule axisModule = getExistingAxisModule(name, version);
                if (!isGloballyEngaged(name, version)) {
                    serviceGroup.disengageModule(axisModule);
                    serviceGroup.engageModule(axisModule);
                }
            }
            if(!isTransactionStarted) {
                getServiceGroupFilePM().commitTransaction(serviceGroupId);
            }

            if (log.isDebugEnabled()) {
                log.debug("Initialized Service Group - " + serviceGroup.getServiceGroupName());
            }
        } catch (Throwable e) {
            log.error("unable init. " + getCurrentFPM().get(serviceGroupId), e);
            handleExceptionWithRollback(serviceGroupId, "Unable to handle service group init. Service group: " +
                    serviceGroupId, e);
        }
    }

    /**
     * Handle initialization of a new service group in regsitry. Writes all parameters
     * and engaged modules into the registry.
     * <p/>
     * {@code <serviceGroup successfully.added="true/false" name="sgXXX" hash.value="xxx">
     * <parameter name="yyy">xxx</parameter>
     * <module name="addressing"/>
     * <p/>
     * </serviceGroup>
     * }
     *
     * @param serviceGroup - AxisServiceGroup instance
     * @throws Exception - on error
     */
    public void handleNewServiceGroupAddition(AxisServiceGroup
                                                      serviceGroup) throws Exception {
        Iterator services = serviceGroup.getServices();
        if (services.hasNext() && ((AxisService) services.next()).isClientSide()) {
            return;
        }

        synchronized (WRITE_LOCK) {
            String sgName = serviceGroup.getServiceGroupName();
            try {
                getServiceGroupFilePM().beginTransaction(sgName);
                OMAttribute nameAttr = omFactory.createOMAttribute(Resources.NAME, null, sgName);
                getServiceGroupFilePM().put(sgName, nameAttr, Resources.ServiceGroupProperties.ROOT_XPATH);
                String hashValue = CarbonUtils.computeServiceHash(serviceGroup);
                if (hashValue != null) {
                    OMAttribute hashAttr = omFactory.createOMAttribute(
                            Resources.ServiceGroupProperties.HASH_VALUE, null, hashValue);
                    getServiceGroupFilePM().put(sgName, hashAttr, Resources.ServiceGroupProperties.ROOT_XPATH);
                }
                // Handle ServiceGroup-Module engagement
                for (Object o : serviceGroup.getEngagedModules()) {
                    AxisModule axisModule = (AxisModule) o;
                    if (!axisConfig.isEngaged(axisModule.getName())) {
                        String version = PersistenceUtils.getModuleVersion(axisModule);
                        OMElement module = PersistenceUtils.createModule(axisModule.getName(), version,
                                Resources.Associations.ENGAGED_MODULES);
                        //todo DEBUG apparently, this didn't work. CHECK IT
                        getServiceGroupFilePM().put(sgName, module, Resources.ServiceGroupProperties.ROOT_XPATH);
                    }
                }
                // Handle Service Group Parameters
                writeParameters(sgName, serviceGroup.getParameters(),
                        Resources.ServiceGroupProperties.ROOT_XPATH);
                getServiceGroupFilePM().put(sgName,
                        omFactory.createOMAttribute(Resources.SUCCESSFULLY_ADDED, null, "true"),
                        Resources.ServiceGroupProperties.ROOT_XPATH);

                getServiceGroupFilePM().commitTransaction(sgName);
            } catch (IOException ex) {
                log.error("unable to handle new service addition. ", ex);
            } catch (Throwable e) {
                log.error("unable to handle new service addition. " + serviceGroup.getServiceGroupName(), e);
                handleExceptionWithRollback(sgName, "Unable to handle new service group addition. " +
                        "Service group: " + serviceGroup.getServiceGroupName(), e);
            }
        }
    }

    /**
     * Set the given property to the service group resource in the registry
     *
     * @param serviceGroup  - AxisServiceGroup instance
     * @param propertyName  - name of the property to set
     * @param propertyValue - value to set
     * @throws Exception - on error
     */
    public void setServiceGroupProperty(AxisServiceGroup serviceGroup, String propertyName,
                                        String propertyValue) throws Exception {
        String sgId = serviceGroup.getServiceGroupName();
        try {
            OMAttribute prop = omFactory.createOMAttribute(propertyName, null, propertyValue);
            getServiceGroupFilePM().beginTransaction(sgId);
            if (getServiceGroupFilePM().fileExists(sgId)) {
                getServiceGroupFilePM().put(sgId, prop, Resources.ServiceGroupProperties.ROOT_XPATH);
            }
            getServiceGroupFilePM().commitTransaction(sgId);
        } catch (Throwable e) {
            handleExceptionWithRollback(sgId, "Unable to set property " + propertyName
                    + " to service group " + serviceGroup.getServiceGroupName(), e);
        }
    }

    /**
     * Persist the given service group parameter. If the parameter already exists in registry,
     * update it. Otherwise, create a new parameter.
     *
     * @param serviceGroup - AxisServiceGroup instance
     * @param parameter    - parameter to persist
     * @throws Exception - on registry call errors
     */
    public void updateServiceGroupParameter(AxisServiceGroup serviceGroup, Parameter parameter)
            throws Exception {
        try {
            updateParameter(serviceGroup.getServiceGroupName(), parameter, Resources.ServiceGroupProperties.ROOT_XPATH);
        } catch (Throwable e) {
            handleExceptionWithRollback(serviceGroup.getServiceGroupName(),
                    "Unable to update the service group parameter " + parameter.getName() + " of service group "
                            + serviceGroup.getServiceGroupName(), e);
        }
    }
}