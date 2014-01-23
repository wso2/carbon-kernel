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

package org.apache.axis2.clustering;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.description.AxisServiceGroup;

import javax.activation.DataHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

/**
 *  A Utility for handling some of the functions needed by the clustering implementation
 */
public class ClusteringUtils {

    private static final Random RANDOM = new Random();

    /**
     * Load a ServiceGroup having name <code>serviceGroupName</code>
     *
     * @param serviceGroupName
     * @param configCtx
     * @param tempDirectory
     * @throws Exception
     */
    public static void loadServiceGroup(String serviceGroupName,
                                        ConfigurationContext configCtx,
                                        String tempDirectory) throws Exception {
        if (!serviceGroupName.endsWith(".aar")) {
            serviceGroupName += ".aar";
        }
        File serviceArchive;
        String axis2Repo = System.getProperty(Constants.AXIS2_REPO);
        if (isURL(axis2Repo)) {
            DataHandler dh = new DataHandler(new URL(axis2Repo + "services/" + serviceGroupName));
            String tempDirName =
                    tempDirectory + File.separator +
                    (System.currentTimeMillis() + RANDOM.nextDouble());
            if(!new File(tempDirName).mkdirs()) {
                 throw new Exception("Could not create temp dir " + tempDirName);
            }
            serviceArchive = new File(tempDirName + File.separator + serviceGroupName);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(serviceArchive);
                dh.writeTo(out);
                out.close();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        } else {
            serviceArchive = new File(axis2Repo + File.separator + "services" +
                                      File.separator + serviceGroupName);
        }
        if(!serviceArchive.exists()){
            throw new FileNotFoundException("File " + serviceArchive + " not found");
        }
        AxisServiceGroup asGroup =
                DeploymentEngine.loadServiceGroup(serviceArchive, configCtx);
        configCtx.getAxisConfiguration().addServiceGroup(asGroup);
    }

    private static boolean isURL(String location) {
        try {
            new URL(location);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
