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

package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.util.Loader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class URLBasedAxisConfigurator extends DeploymentEngine implements AxisConfigurator {

    private static final Log log = LogFactory.getLog(URLBasedAxisConfigurator.class);
    private URL axis2xml;
    private URL repository;

    public URLBasedAxisConfigurator(URL axis2xml, URL repository) throws AxisFault {
        this.axis2xml = axis2xml;
        this.repository = repository;
    }

    public AxisConfiguration getAxisConfiguration() throws AxisFault {
        InputStream axis2xmlStream;
        try {
            if (axis2xml == null) {
                axis2xmlStream =
                        Loader.getResourceAsStream(DeploymentConstants.AXIS2_CONFIGURATION_RESOURCE);
            } else {
                axis2xmlStream = axis2xml.openStream();
            }
            axisConfig = populateAxisConfiguration(axis2xmlStream);
            if (repository == null) {
                Parameter axis2repoPara = axisConfig.getParameter(DeploymentConstants.AXIS2_REPO);
                if (axis2repoPara != null) {
                    String repoValue = (String) axis2repoPara.getValue();
                    if (repoValue != null && !"".equals(repoValue.trim())) {
                        if (repoValue.startsWith("file:/")) {
                            // we treat this case specially , by assuming file is
                            // located in the local machine
                            loadRepository(repoValue);
                        } else {
                            loadRepositoryFromURL(new URL(repoValue));
                        }
                    }
                } else {
                    log.info("No repository found , module will be loaded from classpath");
                    loadFromClassPath();
                }
            } else {
                loadRepositoryFromURL(repository);
            }

        } catch (IOException e) {
            throw new AxisFault(e.getMessage());
        }
        axisConfig.setConfigurator(this);
        return axisConfig;
    }

    //to load services
    public void loadServices() {
        try {
            if (repository == null) {
                Parameter axis2repoPara = axisConfig.getParameter(DeploymentConstants.AXIS2_REPO);
                if (axis2repoPara != null) {
                    String repoValue = (String) axis2repoPara.getValue();
                    if (repoValue != null && !"".equals(repoValue.trim())) {
                        if (repoValue.startsWith("file://")) {
                            // we treat this case specially , by assuming file is
                            // locate in the local machine
                            super.loadServices();
                        } else {
                            loadServicesFromUrl(new URL(repoValue));
                        }
                    }
                }
            } else {
                loadServicesFromUrl(repository);
            }
        } catch (MalformedURLException e) {
            log.info(e);
        }
    }

    //To engage globally listed modules
    public void engageGlobalModules() throws AxisFault {
        engageModules();
    }
}
