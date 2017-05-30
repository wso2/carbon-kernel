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

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Deprecated
public class OperationPersistenceManager extends AbstractPersistenceManager {

    private static final Log log = LogFactory.getLog(OperationPersistenceManager.class);

    /**
     * Constructor gets the axis config and calls the super constructor.
     *
     * @param axisConfig - AxisConfiguration
     * @param pf
     * @throws AxisFault - if the config registry is not found
     */
    public OperationPersistenceManager(AxisConfiguration axisConfig, PersistenceFactory pf) throws AxisFault {
        super(axisConfig, pf.getServiceGroupFilePM(), pf);
    }

    /**
     * Constructor gets the axis config and calls the super constructor.
     *
     * @param axisConfig - AxisConfiguration
     * @throws AxisFault - if the config registry is not found
     */
    public OperationPersistenceManager(AxisConfiguration axisConfig) throws AxisFault {
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
     * Handle the engagement of the module to operation at the registry level
     *
     * @param module    - AxisModule instance
     * @param operation - AxisOperation instance
     * @throws Exception - on error
     */
    public void engageModuleForOperation(AxisModule module, AxisOperation operation)
            throws Exception {
        try {
            handleModuleForAxisDescription(operation.getAxisService().getAxisServiceGroup().getServiceGroupName(),
                    module,
                    PersistenceUtils.getResourcePath(operation), true);
            if (log.isDebugEnabled()) {
                log.debug("Successfully engaged " + module.getName() +
                        " module for " + operation.getName() + " operation");
            }
        } catch (Throwable e) {
            handleExceptionWithRollback(module.getName(), "Unable to engage " + module.getName() +
                    " module to " + module.getOperations() + " operation ", e);
        }
    }

    /**
     * Handle the dis-engagement of the module to operation at the registry level
     *
     * @param module    - AxisModule instance
     * @param operation - AxisOperation instance
     * @throws Exception - on error
     */
    public void disengageModuleForOperation(AxisModule module, AxisOperation operation)
            throws Exception {
        try {
            handleModuleForAxisDescription(operation.getAxisService().getAxisServiceGroup().getServiceGroupName(),
                    module, PersistenceUtils.getResourcePath(operation), false);
            if (log.isDebugEnabled()) {
                log.debug("Successfully disengaged " + module.getName() +
                        " module from " + operation.getName() + " operation");
            }
        } catch (Throwable e) {
            handleExceptionWithRollback(module.getName(), "Unable to disengage " + module.getName() +
                    " module from " + module.getOperations() + " operation ", e);
        }
    }

    /**
     * Remove the specified parameter from the given operation
     *
     * @param operation - AxisOperation instance
     * @param parameter - parameter to remove
     * @throws Exception - on error
     */
    public void removeOperationParameter(AxisOperation operation, Parameter parameter)
            throws Exception {
        removeParameter(operation.getAxisService().getAxisServiceGroup().getServiceGroupName(),
                parameter.getName(), PersistenceUtils.getResourcePath(operation));
    }

    /**
     * Persist the given operation parameter. If the parameter already exists in registry, update
     * it. Otherwise, create a new parameter.
     *
     * @param operation - AxisOperation instance
     * @param parameter - parameter to persist
     * @throws Exception - on registry call errors
     */
    public void updateOperationParameter(AxisOperation operation, Parameter parameter)
            throws Exception {
        try {
            updateParameter(operation.getAxisService().getAxisServiceGroup().getServiceGroupName(),
                    parameter, PersistenceUtils.getResourcePath(operation));
        } catch (Throwable e) {
            handleExceptionWithRollback(operation.getAxisService().getAxisServiceGroup().getServiceGroupName(),
                    "Unable to update the operation parameter " +
                            parameter.getName() + " of operation " + operation.getName(), e);
        }
    }
}
