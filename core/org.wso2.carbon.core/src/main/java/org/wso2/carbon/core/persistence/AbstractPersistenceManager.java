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
package org.wso2.carbon.core.persistence;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.XMLPrettyPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.jaxen.JaxenException;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.persistence.file.AbstractFilePersistenceManager;
import org.wso2.carbon.core.persistence.file.ModuleFilePersistenceManager;
import org.wso2.carbon.core.persistence.file.ServiceGroupFilePersistenceManager;
import org.wso2.carbon.core.util.ParameterUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides common logic for all extending PersistenceManager classes.
 */
public abstract class AbstractPersistenceManager {

    private static final Log log = LogFactory.getLog(AbstractPersistenceManager.class);

    /**
     * The lock object used to synchronize database write locks.
     */
    // TODO: See whether a cluster-wide lock needs to be created in here - Senaka.
    protected static final Object WRITE_LOCK = new Object();

    /**
     * The Configuration registry instance in which all configuration data are stored.
     */
    protected Registry configRegistry;

    protected AxisConfiguration axisConfig;

    protected PersistenceFactory pf;

    protected AbstractFilePersistenceManager fpm;

//    protected File metafilesDir;

    protected OMFactory omFactory = OMAbstractFactory.getOMFactory();

    /**
     * Constructor gets the axisconfig and create reference to the config registry instances.
     *
     * @param axisConfig - AxisConfiguration
     * @param pf         pf
     * @param fpm        fpm
     * @throws AxisFault - if the config registry is not found
     */
    protected AbstractPersistenceManager(AxisConfiguration axisConfig, AbstractFilePersistenceManager fpm,
                                         PersistenceFactory pf) throws AxisFault {
        this.axisConfig = axisConfig;
        this.pf = pf;
        this.fpm = fpm;
        try {
            configRegistry =
                    (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext().  //needed for TransportPM
                            getRegistry(RegistryType.SYSTEM_CONFIGURATION);
        } catch (Exception e) {
            log.error("Error while retrieving config registry from Axis configuration", e);
        }
        if (configRegistry == null) {
            throw new AxisFault("Configuration Registry is not available");
        }
    }

    /**
     * Constructor gets the axisconfig and create reference to the config registry instances.
     *
     * @param axisConfig - AxisConfiguration
     * @throws AxisFault - if the config registry is not found
     */
    protected AbstractPersistenceManager(AxisConfiguration axisConfig) throws AxisFault {
        this.axisConfig = axisConfig;
        try {
            configRegistry =
                    (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext().  //needed for TransportPM
                            getRegistry(RegistryType.SYSTEM_CONFIGURATION);
        } catch (Exception e) {
            log.error("Error while retrieving config registry from Axis configuration", e);
        }
        if (configRegistry == null) {
            throw new AxisFault("Configuration Registry is not available");
        }
    }

    /**
     * If the parameter already exists, persist new value. Otherwise create a parameter resource
     * and set the value
     * <p/>
     * If possible, use the extended classes' method - update.*Parameter
     *
     * @param resourceId       - resource id/name
     * @param serviceParameter        - parameter instance
     * @param xpathStrOfParent xpathStrOfParent
     * @throws java.io.IOException
     */
    public void updateParameter(String resourceId, Parameter serviceParameter, String xpathStrOfParent) throws
            XMLStreamException, IOException, PersistenceException, JaxenException {
        String paramName = serviceParameter.getName();
        int paramType = serviceParameter.getParameterType();
        boolean locked = serviceParameter.isLocked();
        if (paramName != null && paramName.trim().length() != 0) {
            if (serviceParameter.getParameterElement() == null && serviceParameter.getValue() != null
                    && serviceParameter.getValue() instanceof String) {
                try {
                    serviceParameter = ParameterUtil.createParameter(paramName.trim(),
                            (String) serviceParameter.getValue(), locked);
                } catch (AxisFault ignore) {
                }
            }

            if (serviceParameter.getParameterElement() != null) {
                boolean isTransactionStarted = getCurrentFPM().isTransactionStarted(resourceId);
                if (!isTransactionStarted) {
                    getCurrentFPM().beginTransaction(resourceId);
                }
                String paramXPath;
                if (xpathStrOfParent.equals("/")) {
                    paramXPath = xpathStrOfParent + Resources.ParameterProperties.PARAMETER +
                            PersistenceUtils.getXPathAttrPredicate(Resources.NAME, paramName);
                } else {
                    paramXPath = xpathStrOfParent + "/" + Resources.ParameterProperties.PARAMETER +
                            PersistenceUtils.getXPathAttrPredicate(Resources.NAME, paramName);
                }
                OMElement paramElementFromMeta;
                paramElementFromMeta = (OMElement) getCurrentFPM().get(resourceId, paramXPath);
                // If the existing parameter is identical to what we are going to store, return
                if (paramElementFromMeta == null) {
                    paramElementFromMeta = serviceParameter.getParameterElement().cloneOMElement();
                    Iterator itr = paramElementFromMeta.getAllDeclaredNamespaces();
                    while (itr.hasNext()) {
                        itr.next();
                        itr.remove();
                    }
                } else {
                    String name = paramElementFromMeta.getAttributeValue(new QName(Resources.ParameterProperties.NAME));
                    String type = paramElementFromMeta.getAttributeValue(new QName(Resources.ParameterProperties.TYPE));
                    String content = paramElementFromMeta.toString();

                    if (name != null && name.equals(paramName) && type != null &&
                            type.equals(Integer.toString(paramType))) {
                        boolean isContentEqual;
                        if (type.equals(Integer.toString(Parameter.OM_PARAMETER))) {     //compare OM
                            isContentEqual = serviceParameter.getParameterElement() != null &&
                                    prettyPrintXml(paramElementFromMeta).equals(
                                    prettyPrintXml(serviceParameter.getParameterElement()));
                        } else {
                            isContentEqual = content != null &&
                                    paramElementFromMeta.getText().equals(serviceParameter.getValue().toString());
                        }

                        if (isContentEqual) {
                            if(!isTransactionStarted) {
                                getCurrentFPM().rollbackTransaction(resourceId);
                            }
                            return;
                        }
                    }
                    if (serviceParameter.getParameterType() == Parameter.TEXT_PARAMETER) {
                        paramElementFromMeta.setText(serviceParameter.getValue().toString());
                    } else if (serviceParameter.getParameterType() == Parameter.OM_PARAMETER) {
                        paramElementFromMeta = serviceParameter.getParameterElement();
                    } else if (serviceParameter.getValue() instanceof OMNode) {
                        paramElementFromMeta.addChild((OMNode) serviceParameter.getValue());
                    } else {
                        log.error("Not persisting the parameter because parameter is not recognized " +
                                paramName + paramType);
//                        throw new PersistenceException("The type of the parameter value is not recognized!");
                    }
                }

                paramElementFromMeta.addAttribute(Resources.ParameterProperties.NAME, paramName, null);
                paramElementFromMeta.addAttribute(Resources.ParameterProperties.TYPE, Integer.
                        toString(paramType), null);
                if (serviceParameter.isLocked()) {
                    paramElementFromMeta.addAttribute(Resources.ParameterProperties.LOCKED, Boolean.TRUE.toString(), null);
                }

                getCurrentFPM().deleteAll(resourceId, paramXPath);
                getCurrentFPM().put(resourceId, paramElementFromMeta, xpathStrOfParent);

                if (!isTransactionStarted) {
                    getCurrentFPM().commitTransaction(resourceId);
                }
            }
        }
    }

    protected static String convertStreamToString(InputStream is) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            // we assume that there is only one line..
            return reader.readLine();
        } catch (IOException e) {
            log.error("Error while reading Input Stream");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Remove a parameter at the given path from the registry
     *
     * @param resourceId sg name of module name
     * @param paramName - parameter name
     * @param xpathStr the xpath expression string
     * @throws Exception - on error
     */
    protected void removeParameter(String resourceId, String paramName, String xpathStr) throws Exception {
        //call #removeResource with xpath
        removeResource(resourceId, xpathStr + "/" + Resources.ParameterProperties.PARAMETER +
                PersistenceUtils.getXPathAttrPredicate(Resources.NAME, paramName));
    }

    /**
     * Remove a resource at the given path
     *
     * @param resourceId SG name or module name
     * @param xpathStr   xpath to element
     * @throws Exception - on registry error
     */
    protected void removeResource(String resourceId, String xpathStr) throws Exception {
        try {
            getCurrentFPM().beginTransaction(resourceId);
            if (getCurrentFPM().elementExists(resourceId, xpathStr)) {
                getCurrentFPM().delete(resourceId, xpathStr);
            }
            getCurrentFPM().commitTransaction(resourceId);
        } catch (Throwable e) {
            handleExceptionWithRollback(resourceId, "Unable to remove the resource " + resourceId, e);
        }
    }

    /**
     * Engage or disengage module at the given resource path.
     *
     * @param serviceGroupId serviceGroupId
     * @param module         - AxisModule instance
     * @param xpathStr       - registry path of a service group, service, operation etc
     * @param engage         - engage or disengage
     * @throws Exception - on registry transaction errors
     */
    protected void handleModuleForAxisDescription(String serviceGroupId, AxisModule module, String xpathStr,
                                                  boolean engage) throws Exception {
        boolean isStarted = getServiceGroupFilePM().isTransactionStarted(serviceGroupId);
        if (!isStarted) {
            getServiceGroupFilePM().beginTransaction(serviceGroupId);
        }
        String version = PersistenceUtils.getModuleVersion(module);
        if (engage) {
            OMElement moduleElement = PersistenceUtils.createModule(module.getName(),
                    version,
                    Resources.Associations.ENGAGED_MODULES);
            getServiceGroupFilePM().put(serviceGroupId, moduleElement, xpathStr);
        } else {
            OMElement moduleElement = (OMElement) getServiceGroupFilePM().get(serviceGroupId,
                    xpathStr + "/" + Resources.ModuleProperties.MODULE_XML_TAG +
                            PersistenceUtils.getXPathAttrPredicate(Resources.NAME, module.getName()) +
                            PersistenceUtils.getXPathAttrPredicate(Resources.VERSION, version));
            moduleElement.detach();
            getServiceGroupFilePM().setMetaFileModification(serviceGroupId);
        }
        if (!isStarted) {
            getServiceGroupFilePM().commitTransaction(serviceGroupId);
        }
    }

    /**
     * Load parameters for the given AxisDescription instance from registry
     *
     * @param resourceId          SG name or module name
     * @param ad                  - AxisDescription instance
     * @param xpathStrOfParameter Provide the xpath to parameters. ex. /service[@name="HelloService"]/parameter
     * @throws Exception - on error
     */
    protected void loadParameters(String resourceId, AxisDescription ad, String xpathStrOfParameter) throws Exception {
        if (getCurrentFPM().fileExists(resourceId)) {
//            String xpathString = "Resources.ServiceGroupProperties.ROOT_XPATH/parameter";
            AXIOMXPath xpathExpression = new AXIOMXPath(xpathStrOfParameter);
            List matchedNodes = xpathExpression.selectNodes(getCurrentFPM().get(resourceId));

            for (Object node : matchedNodes) {
                OMElement paramEl = (OMElement) node;
                Parameter parameter = ParameterUtil.createParameter(paramEl);
                Parameter p = ad.getParameter(paramEl.getAttributeValue(new QName(Resources.NAME)));
                // don't override the param if it already exists and locked..
                if (!(p != null && p.isLocked())) {
                    ad.addParameter(parameter);
                }
            }
        }
    }

    /**
     * Write a list of parameters to the registry
     * Parameter element looks liks
     * {@code <parameter name="param-name">val</parameter> }
     *
     * @param resourceId       SG name or module name
     * @param paramList        - Parameter list
     * @param xpathStrOfParent xpath to parent element
     * @throws Exception - on error
     */
    protected void writeParameters(String resourceId, ArrayList<Parameter> paramList, String xpathStrOfParent) throws
            Exception {
        for (Object o : paramList) {
            Parameter parameter = (Parameter) o;
            String paramName = parameter.getName();
            if (paramName != null && paramName.trim().length() != 0) {
                if (parameter.getParameterElement() == null && parameter.getValue() != null
                        && parameter.getValue() instanceof String) {
                    parameter = ParameterUtil.createParameter(paramName.trim(),
                            (String) parameter.getValue());
                }
                if (parameter.getParameterElement() != null) {
                    //todo do we need to get the param ns by parameter.getParameterElement().getNamespace() ? - kasung
                    OMElement paramElement = parameter.getParameterElement().cloneOMElement();
                    Iterator itr = paramElement.getAllDeclaredNamespaces();
                    while (itr.hasNext()) {
                        itr.next();
                        itr.remove();
                    }
//                    OMElement paramElement = omFactory.createOMElement(Resources.ParameterProperties.PARAMETER, null);
                    paramElement.setText(parameter.getParameterElement().getText());
                    paramElement.addAttribute(Resources.ParameterProperties.NAME, parameter.getName(), null);

                    getCurrentFPM().put(resourceId, paramElement, xpathStrOfParent);
                }
            }
        }
    }

    /**
     * Writes any AxisDescription instance with its documentation and name as
     * properties.
     * current usage is that this is used to write operation info of services and bindings.
     * For service-level operations - Resources.ServiceGroupProperties.ROOT_XPATH/service[@name="xxx"]
     * For binding ops - Resources.ServiceGroupProperties.ROOT_XPATH/bindings/binding[@name="yyy"]
     * <p/>
     * Transactions should be handled by an upper-layer
     *
     * @param serviceGroupId SG name
     * @param ad             - AxisDescription instance
     * @param nameProperty   - name to be set
     * @param xpathStr       - xpathStr to store AxisDescription
     * @throws PersistenceException error in persisting data
     * @deprecated Encourage to use this#createOperation and do the operations manually
     */
    protected void writeAxisDescription(String serviceGroupId, AxisDescription ad, String nameProperty,
                                        String xpathStr) throws PersistenceException {
        OMElement opParent = (OMElement) getServiceGroupFilePM().get(serviceGroupId, xpathStr);

        OMElement operation = omFactory.createOMElement(Resources.OPERATION, null);
        String doc = ad.getDocumentation();
        if (doc != null) {
            operation.addAttribute(Resources.ServiceProperties.DOCUMENTATION, doc, null);
        }
        operation.addAttribute(Resources.NAME, nameProperty, null);
        opParent.addChild(operation);
//        getServiceGroupFilePM().put(serviceGroupId, operation, xpathStr);
    }

    /**
     * Get all the values against a property of a given resource path
     *
     * @param resourcePath - resource path
     * @param property     - property name
     * @return - list of values
     * @throws RegistryException - on registry error
     * @deprecated why we need store multiple property values with the same property name specially for POLICY_UUID?
     */
    protected List getPropertyValues(String resourcePath, String property) throws RegistryException {
//        Resource resource = configRegistry.get(resourcePath);
//        List values = resource.getPropertyValues(property);
//        resource.discard();
//        return values;
        return null;
    }

    /**
     * Load the documentation of an AxisDescription instance
     *
     * @param serviceGroupId SG name
     * @param ad             - AxisDescription instance to set the documentation
     * @param resourceXPath  - resource path of the AxisDescription
     * @throws PersistenceDataNotFoundException
     *          error in persisting data
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          reg ex
     */
    protected void loadDocumentation(String serviceGroupId, AxisDescription ad, String resourceXPath)
            throws RegistryException, PersistenceDataNotFoundException {
        OMAttribute docAttr = getServiceGroupFilePM().getAttribute(serviceGroupId,
                resourceXPath + "/@" + Resources.ServiceProperties.DOCUMENTATION);
        if (docAttr != null) {
            String documentation = docAttr.getAttributeValue();
            try {
//                tries to build an OMNode from the documentation
                ad.setDocumentation(AXIOMUtil.stringToOM(documentation));
            } catch (Exception e) {
                ad.setDocumentation(documentation);
            }
        }
    }

    /**
     * Load policies from the file system and attach to the AxisDescription instance.
     *
     * @param serviceGroupId SG name
     * @param ad             - AxisDescription instance
     * @param policyIdList   - list of policy UUIDs
     * @param serviceXPath   - all policies are stored at service level. Therefore, fetch the
     *                       actual policy from service level
     * @throws RegistryException - registry transaction errors
     * @throws PersistenceDataNotFoundException
     *                           ex
     */
    protected void loadPolicies(String serviceGroupId, AxisDescription ad, List policyIdList,
                                String serviceXPath) throws RegistryException, PersistenceDataNotFoundException {
        // if AxisDescription is null, return
        if (ad == null) {
            return;
        }

        ad.getPolicySubject().clear();

        if (policyIdList != null) {
            for (Object node : policyIdList) {
                OMElement servicePolicyEl = (OMElement) node;
                String currentPolicyUUID = servicePolicyEl.getText();
                String policyResourcePath = serviceXPath +
                        "/" + Resources.POLICIES +
                        "/" + Resources.POLICY +
                        PersistenceUtils.getXPathTextPredicate(
                                Resources.ServiceProperties.POLICY_UUID, currentPolicyUUID);
                OMElement policyElement = (OMElement) getServiceGroupFilePM().get(serviceGroupId, policyResourcePath);
                if (policyElement != null) {
                    Policy policy = PolicyEngine.getPolicy(policyElement.getFirstChildWithName(
                            new QName(Resources.WS_POLICY_NAMESPACE, "Policy")));  //note that P is capital
                    ad.getPolicySubject().attachPolicy(policy);
                } else {
                    log.error("Failed to load Policy with ID " + currentPolicyUUID
                            + ". The Policy does not exist. " + serviceGroupId);
                }
            }
        }
    }
    /**
     * Checks whether the provided module is globally engaged. This is done using a property in
     * the module resource
     *
     * @param moduleName    module Name
     * @param moduleVersion module Version
     * @return - true if globally engaged, else false
     */
    protected boolean isGloballyEngaged(String moduleName, String moduleVersion) {
//        System.out.println("module name n version " + moduleName + moduleVersion);
        if (getModuleFilePM().fileExists(moduleName)) {
            if (moduleVersion == null) {
                moduleVersion = Resources.ModuleProperties.UNDEFINED;
            }
            try {
                OMElement module = (OMElement) getModuleFilePM().get(moduleName,
                        Resources.ModuleProperties.VERSION_XPATH +
                                PersistenceUtils.getXPathAttrPredicate(
                                        Resources.ModuleProperties.VERSION_ID, moduleVersion));
                if(log.isDebugEnabled()) {
                    log.debug("module " + moduleName  + "\n" +  module);
                }
                if (module == null || "".equals(module)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Module element is null or empty for module : " + moduleName + " , version : " +
                                moduleVersion);
                    }
                    return false;
                }
                return Boolean.parseBoolean(module.getAttributeValue(
                        new QName(Resources.ModuleProperties.GLOBALLY_ENGAGED)));
            } catch (PersistenceDataNotFoundException e) {
                log.error("Couldn't retrieve data for " + moduleName + moduleVersion, e);
                return false;
            }
        }
        return false;
    }

    /**
     * Finds the existing AxisModule instance for a particular module. If the given module path
     * exists in the registry, we just check the AxisConfig for that module. If it is not found,
     * we search for a different version of the same module. If any of the versions exists in the
     * system, return the found AxisModule instance.
     *
     * @param moduleName    module Name
     * @param moduleVersion module Version
     * @return - AxisModule instance
     * @throws Exception - on errors while accessing registry or on module not found
     */
    protected AxisModule getExistingAxisModule(String moduleName, String moduleVersion) throws Exception {

        AxisModule existingModule = null;
        ModuleFilePersistenceManager mpm = pf.getModuleFilePM();
        if (moduleVersion == null) {
            moduleVersion = Resources.ModuleProperties.UNDEFINED;
        }
        if (mpm.fileExists(moduleName)) {
            //get the relevant version of the module
            OMElement module = (OMElement) mpm.get(moduleName, Resources.ModuleProperties.VERSION_XPATH +
                    PersistenceUtils.getXPathAttrPredicate(Resources.ModuleProperties.VERSION_ID, moduleVersion));
            existingModule = getAxisModule(moduleName, module);
        }
        // if the existingModule is null, check whether there are new versions..
        if (existingModule == null) {
            // we iterate through all the version of the module and finds on which currently
            // exists in the system
            List moduleVersionsList = mpm.getAll(moduleName, Resources.ModuleProperties.VERSION_XPATH);
            for (Object node : moduleVersionsList) {
                OMElement module = (OMElement) node;
                existingModule = getAxisModule(moduleName, module);
                if (existingModule != null) {
                    break;
                }
            }
        }
        if (existingModule == null) {
            throw new CarbonException("Axis Module not found for : " + moduleName + "-" + moduleVersion);
        }
        return existingModule;
    }

    /**
     * Handles exception and rollbacks an already started transaction. Don't use this method if
     * you haven't already started a registry transaction
     * <p/>
     * For a serviceGroup or service or operation we need the resourceId of serviceGroup.
     * For modules, we need the module id.
     *
     * @param resourceId The id/name of resource
     * @param msg        - Message to log
     * @param e          - original exception
     * @throws PersistenceException ex
     */
    protected void handleExceptionWithRollback(String resourceId, String msg, Throwable e) throws PersistenceException {
        log.error(msg, e);
        getCurrentFPM().rollbackTransaction(resourceId);
        throw new PersistenceException(msg, e);
    }

    protected void handleException(String msg, Throwable e) throws PersistenceException {
        log.error(msg, e);
        throw new PersistenceException(msg, e);
    }

    protected void handleException(String msg) throws PersistenceException {
        log.error(msg);
        throw new PersistenceException(msg);
    }

    /**
     * null if module not found
     *
     * @param moduleName    module Name
     * @param moduleElement module OMElement
     * @return the axis module
     */
    private AxisModule getAxisModule(String moduleName, OMElement moduleElement) {
        String moduleVersion = null;
        //return null if the moduleElement is null.
        if(moduleElement == null) {
            return null;
        }
        String tmp = moduleElement.getAttributeValue(new QName(Resources.VERSION));
        if (!Resources.ModuleProperties.UNDEFINED.equals(tmp)) {
            moduleVersion = tmp;
        }

        return axisConfig.getModule(moduleName, moduleVersion);
    }

    /**
     * Returns the attribute value
     *
     * @param resource omelement
     * @param xpathStr xpath to attribute/property
     * @return the attribute value
     */
    public String getProperty(OMElement resource, String xpathStr) {
        try {
            AXIOMXPath xpathExpression = new AXIOMXPath(xpathStr);
            OMAttribute attr = (OMAttribute) xpathExpression.selectSingleNode(resource);
            return attr.getAttributeValue();
        } catch (JaxenException e) {
            log.error("XPath evaluation failed for " + xpathStr, e);
        }
        return null;
    }

    public AbstractFilePersistenceManager getCurrentFPM() {
        return fpm;
    }

    public ServiceGroupFilePersistenceManager getServiceGroupFilePM() {
        if (this instanceof ServiceGroupPersistenceManager ||
                this instanceof ServicePersistenceManager ||
                this instanceof OperationPersistenceManager) {
            return (ServiceGroupFilePersistenceManager) fpm;
        }
        return pf.getServiceGroupFilePM();
    }

    public ModuleFilePersistenceManager getModuleFilePM() {
        if (this instanceof ModulePersistenceManager) {
            return (ModuleFilePersistenceManager) fpm;
        }
        return pf.getModuleFilePM();
    }


    /**
     * Persists the given <code>Policy</code> object under policies associated with the
     * <code>servicePath</code> in the registry.
     *
     * @param policy      the <code>Policy</code> instance to be persisted
     * @param policyType  Policy Type
     * @param servicePath - path in the registry to persist policy
     * @throws RegistryException  if saving data to the registry is unsuccessful
     * @throws XMLStreamException if serializing the <code>Policy<code> object is unsuccessful
     */
    public void persistPolicyToRegistry(Policy policy, String policyType, String servicePath)
            throws RegistryException, XMLStreamException {
        if (log.isDebugEnabled()) {
            log.debug("Persisting caching policy in the registry");
        }

        Resource policyResource = PersistenceUtils.createPolicyResource(
                configRegistry, policy, policy.getId(), policyType);
        String policyResourcePath = servicePath + RegistryResources.POLICIES
                + policy.getId();
        try {
            configRegistry.put(policyResourcePath, policyResource);
        } catch (Exception e) {
            String msg = "Error persisting caching policy in the configRegistry.";
            log.error(msg, e);
            configRegistry.rollbackTransaction();
            throw new RegistryException(e.getMessage(), e);
        }
    }

    private String prettyPrintXml(OMElement xml) throws PersistenceException {
        if (xml == null) {
            return null;
        }
        try {
            OutputStream baos = new ByteArrayOutputStream();
            XMLPrettyPrinter.prettify(xml, baos);
            return baos.toString();
        } catch (Exception e) {
            throw new PersistenceException("error while comparing service parameter content.", e);
        }
    }

}
