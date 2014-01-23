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
package org.apache.axis2.osgi.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.AxisConfigBuilder;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.osgi.framework.BundleContext;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * This is the heart of the OSGi deployment.
 * 
 */
public class OSGiServerConfigurator extends DeploymentEngine implements AxisConfigurator {

    private BundleContext context;

    private URL axis2XmlUrl;

    public OSGiServerConfigurator(BundleContext context) {
        this.context = context;
        Enumeration entries = this.context.getBundle()
                .findEntries("org/apache/axis2/osgi/deployment", "axis2.xml", false);
        if (entries != null && entries.hasMoreElements()) {
            axis2XmlUrl = (URL)entries.nextElement();
        }
    }

    /**
     * OSGiServerConfigurator will create an specifict AxisConfiguration based on axis2.xml which
     * is available in org/apache/axis2.osgi/deployment directory. This axis2.xml doesn't contain
     * any listeners. Listeners should be added as services to AxisConfiguration service.
     *
     * @return AxisConfiguration; an instance of AxisConfiguration is return
     * @throws AxisFault; AxisFault will be thrown wrapping any of IOException
     */
    public AxisConfiguration getAxisConfiguration() throws AxisFault {
        try {
            InputStream inputStream = axis2XmlUrl.openStream();
            populateAxisConfiguration(inputStream);
            axisConfig.validateSystemPredefinedPhases();
            return axisConfig;
        } catch (IOException e) {
            String msg = "Error occured while creating axisConfiguration";
            throw new AxisFault(msg, e);
        }
    }


    public void engageGlobalModules() throws AxisFault {
        //TODO; TBD
    }

    public AxisConfiguration populateAxisConfiguration(InputStream in) throws DeploymentException {
        axisConfig = new AxisConfiguration();
        AxisConfigBuilder builder =
                new AxisConfigBuilder(in, axisConfig, this);
        builder.populateConfig();
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            String msg = "Error in closing input stream";
            throw new DeploymentException(msg, e);
        }
        //TODO: if module deployer neede to set it should be set here.
        return axisConfig;
    }

    public void loadServices() {
        //TODO; TBD
    }
}
