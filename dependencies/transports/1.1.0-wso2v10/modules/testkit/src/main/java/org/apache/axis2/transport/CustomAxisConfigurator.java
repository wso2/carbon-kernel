/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.util.Loader;

public class CustomAxisConfigurator extends DeploymentEngine implements AxisConfigurator {
    public AxisConfiguration getAxisConfiguration() throws AxisFault {
        InputStream configStream = Loader.getResourceAsStream("org/apache/axis2/transport/axis2.xml");
        try {
            axisConfig = populateAxisConfiguration(configStream);
        } finally {
            try {
                configStream.close();
            } catch (IOException ex) {
                throw AxisFault.makeFault(ex);
            }
        }
        try {
            loadRepositoryFromURL(new URL(Loader.getResource("org/apache/axis2/transport/repo/__root__"), "."));
        } catch (MalformedURLException ex) {
            throw AxisFault.makeFault(ex);
        }
        axisConfig.setConfigurator(this);
        return axisConfig;
    }

    public void loadServices() {
        // We don't have any services.
    }

    public void engageGlobalModules() throws AxisFault {
        engageModules();
    }
}
