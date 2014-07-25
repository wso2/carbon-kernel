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
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.persistence.file.ModuleFilePersistenceManager;
import org.wso2.carbon.core.persistence.file.ServiceGroupFilePersistenceManager;

/**
 * Factory to create different PersistenceManager instances
 */
@Deprecated
public class PersistenceFactory {

    private static Log log = LogFactory.getLog(PersistenceFactory.class);

    private ServicePersistenceManager spm;
    private ServiceGroupPersistenceManager sgpm;
    private ModulePersistenceManager mpm;
    private OperationPersistenceManager opm;

    private ServiceGroupFilePersistenceManager sfpm;
    private ModuleFilePersistenceManager mfpm;

    private AxisConfiguration axisConfig;

    /**
     * For carbon components use this#getInstance method
     *
     * @param axisConfig the axis configuration
     */
    private PersistenceFactory(AxisConfiguration axisConfig) {
        this.axisConfig = axisConfig;
    }

    /**
     * Only one PersistenceFactory instance should be there per server instance
     *
     * @param axisConfig the axis configuration
     * @return the PersistenceFactory instance
     * @throws AxisFault if error in adding PersistenceFactory instance to axisconfig as a parameter
     */
    public static PersistenceFactory getInstance(AxisConfiguration axisConfig) throws AxisFault {

        Object obj = axisConfig.getParameterValue(Resources.PERSISTENCE_FACTORY_PARAM_NAME);
        PersistenceFactory pf = null;
        if (obj instanceof PersistenceFactory) {
            pf = (PersistenceFactory) obj;
        } else {
            pf = new PersistenceFactory(axisConfig);
            axisConfig.addParameter(Resources.PERSISTENCE_FACTORY_PARAM_NAME, pf);
        }
        return pf;
    }

    /**
     * @return The ServicePersistenceManager instance
     */
    public ServicePersistenceManager getServicePM() {
        if (spm == null) {
            try {
                spm = new ServicePersistenceManager(axisConfig, this);
            } catch (AxisFault axisFault) {
                log.error("Error while initializing " +
                        "the ServicePersistenceManager instance", axisFault);
            }
        }
        return spm;
    }

    /**
     * @return The ServiceGroupManager instance for the current tenant/supertenant
     */
    public ServiceGroupPersistenceManager getServiceGroupPM() {
        if (sgpm == null) {
            try {
                sgpm = new ServiceGroupPersistenceManager(axisConfig, this);
            } catch (AxisFault axisFault) {
                log.error("Error while initializing the " +
                        "ServiceGroupPersistenceManager instance", axisFault);
            }
        }
        return sgpm;
    }

    /**
     * @return The OperationPersistenceManager instance
     */
    public OperationPersistenceManager getOperationPM() {
        if (opm == null) {
            try {
                opm = new OperationPersistenceManager(axisConfig, this);
            } catch (AxisFault axisFault) {
                log.error("Error while initializing the " +
                        "OperationServicePersistenceManager instance", axisFault);
            }
        }
        return opm;
    }

    /**
     * @return The ModulePersistenceManager instance
     */
    public ModulePersistenceManager getModulePM() {
        if (mpm == null) {
            try {
                mpm = new ModulePersistenceManager(axisConfig, this);
            } catch (AxisFault axisFault) {
                log.error("Error while initializing the " +
                        "ModulePersistenceManager instance", axisFault);
            }
        }
        return mpm;
    }

    /**
     * Though this looks like same as other persistencemanagers, it's not.
     * ServiceGroupFilePersistenceManager runs just underneath other *PersistenceManagers
     * <p/>
     * <p/>
     * NOTE: Make sure you set the metafile directory location by using this#setMetafileDir
     * <br/>
     * You may ignore doing this if you created at least one PersistenceFactory instance
     *
     * @return
     */
    public ServiceGroupFilePersistenceManager getServiceGroupFilePM() {
        if (sfpm == null) {
            try {
                sfpm = new ServiceGroupFilePersistenceManager(axisConfig);
            } catch (AxisFault axisFault) {
                log.error("Error while initializing the " +
                        "ServiceGroupFilePersistenceManager instance", axisFault);
                return null;
            }
        }

        sfpm.init();
        return sfpm;
    }

    /**
     * Though this looks like same as other persistencemanagers, it's not.
     * ServiceGroupFilePersistenceManager runs just underneath other *PersistenceManagers
     * <p/>
     * <p/>
     * Make sure you set the metafile directory location by using this#setMetafileDir
     * <br/>
     * You may ignore doing this if you created at least one PersistenceFactory instance
     *
     * @return
     */
    public ModuleFilePersistenceManager getModuleFilePM() {
        if (mfpm == null) {
            try {
                mfpm = new ModuleFilePersistenceManager(axisConfig);
            } catch (AxisFault axisFault) {
                log.error("Error while initializing the " +
                        "ModuleFilePersistenceManager instance", axisFault);
                return null;
            }
        }

        mfpm.init();
        return mfpm;
    }
}
