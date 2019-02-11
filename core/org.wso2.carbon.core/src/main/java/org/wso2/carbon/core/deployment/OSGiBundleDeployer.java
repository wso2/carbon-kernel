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
package org.wso2.carbon.core.deployment;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.wso2.carbon.utils.deployment.Axis2ModuleRegistry;

/*
*
*/
public class OSGiBundleDeployer implements Deployer, BundleListener {
	private static Log log = LogFactory.getLog(OSGiBundleDeployer.class);

    /**
     *
     */
    private BundleContext context;

    /**
     *
     */
    private AxisConfiguration axisConfig;

    /**
     * 
     */
    private Axis2ModuleRegistry registry;


    public OSGiBundleDeployer(AxisConfiguration axisConfig, BundleContext context) {
        this.axisConfig = axisConfig;
        this.context = context;
        this.context.addBundleListener(this);
        this.registry = new Axis2ModuleRegistry(this.axisConfig);
    }

    public void init(ConfigurationContext configCtx) {
        // Ignore
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        for (Bundle bundle : context.getBundles()) {
            if (bundle.getState() == Bundle.ACTIVE) {
                registry.register(bundle);
            }
        }

    }

    public void setDirectory(String directory) {
        // Ignore
    }

    public void setExtension(String extension) {
        // Ignore
    }

    public void undeploy(String fileName) throws DeploymentException {
        // Ignore
    }

    public void cleanup() throws DeploymentException {
        // Ignore
    }

    public void bundleChanged(BundleEvent event) {
        Bundle bundle = event.getBundle();

        switch (event.getType()) {
            case BundleEvent.STARTED:
                if (context.getBundle() != bundle) {
                    registry.register(event.getBundle());
                }
                break;

            case BundleEvent.STOPPED:
                if (context.getBundle() != bundle) {
                    registry.register(event.getBundle());
                }
                break;
        }
    }
}
