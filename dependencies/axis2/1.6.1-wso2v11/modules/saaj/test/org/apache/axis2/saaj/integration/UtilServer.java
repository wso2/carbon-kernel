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

package org.apache.axis2.saaj.integration;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.http.SimpleHTTPServer;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FilenameFilter;
import java.net.ServerSocket;

/**
 * 
 */
public class UtilServer {
    private static int count = 0;

    private static SimpleHTTPServer receiver;

    public static synchronized void deployService(AxisService service) throws AxisFault {
        receiver.getConfigurationContext().getAxisConfiguration().addService(service);
    }

    public static synchronized void unDeployService(QName service) throws AxisFault {
        receiver.getConfigurationContext().getAxisConfiguration().
                removeService(service.getLocalPart());
    }

    public static synchronized void unDeployClientService() throws AxisFault {
        if (receiver.getConfigurationContext().getAxisConfiguration() != null) {
            receiver.getConfigurationContext().getAxisConfiguration()
                    .removeService("AnonymousService");
        }
    }

    public static synchronized void start() throws Exception {
        start(org.apache.axis2.Constants.TESTING_REPOSITORY);
    }

    public static synchronized int start(String repository) throws Exception {
        int testingPort = 0;
        if (count == 0) {
            ConfigurationContext er = getNewConfigurationContext(repository);

            testingPort = findAvailablePort();
            receiver = new SimpleHTTPServer(er, testingPort);

            receiver.start();
            System.out.print("Server started on port " + testingPort + ".....");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                throw new AxisFault("Thread interupted", e1);
            }

        }
        count++;
        return testingPort;
    }

    public static ConfigurationContext getNewConfigurationContext(String repository)
            throws Exception {
        File file = new File(repository);
        if (!file.exists()) {
            throw new Exception("repository directory " + file.getAbsolutePath() +
                    " does not exist");
        }
        return ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(file.getAbsolutePath(), null);
    }

    public static synchronized void stop() throws AxisFault {
        if (count == 1) {
            receiver.stop();
            while (receiver.isRunning()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }
            }
            count = 0;
            System.out.print("Server stopped .....");
        } else {
            count--;
        }
        receiver.getConfigurationContext().terminate();
    }

    public static ConfigurationContext getConfigurationContext() {
        return receiver.getConfigurationContext();
    }

    static class AddressingFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.startsWith("addressing") && name.endsWith(".mar");
        }
    }

    private static File getAddressingMARFile() {
        File dir = new File(org.apache.axis2.Constants.TESTING_REPOSITORY);
        File[] files = dir.listFiles(new AddressingFilter());
        TestCase.assertTrue(files.length == 1);
        File file = files[0];
        TestCase.assertTrue(file.exists());
        return file;
    }

    public static ServiceContext createAdressedEnabledClientSide(
            AxisService service, String clientHome) throws AxisFault {
        File file = getAddressingMARFile();
        TestCase.assertTrue(file.exists());

        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(clientHome, null);
        AxisModule axisModule = DeploymentEngine.buildModule(file,
                                                             configContext.getAxisConfiguration());

        configContext.getAxisConfiguration().addModule(axisModule);
        // sysContext.getAxisConfiguration().engageModule(moduleDesc.getName());

        configContext.getAxisConfiguration().addService(service);

        return new ServiceGroupContext(configContext, service.getAxisServiceGroup())
                .getServiceContext(service);
    }

    protected static int findAvailablePort() {
        try {
            ServerSocket ss = new ServerSocket(0);
            int result = ss.getLocalPort();
            ss.close();
            return result;
        } catch (Exception e) {
            return 5555;
        }
    }
}
