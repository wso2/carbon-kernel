/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.core.test.performance;

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.HashMap;
import java.util.Map;

public class Worker3 extends Worker {

    public static final String QUERY_PATH = "/qs/q";

    public Worker3(String threadName, int iterations, Registry registry) {
        super(threadName, iterations, registry);

        try {
            String sql1 = "SELECT RT.REG_RATING_ID FROM REG_RESOURCE_RATING RT, REG_RESOURCE R " +
                    "WHERE (R.REG_VERSION=RT.REG_VERSION OR " +
                    "(R.REG_PATH_ID=RT.REG_PATH_ID AND R.REG_NAME=RT.REG_RESOURCE_NAME)) " +
                    "AND R.REG_DESCRIPTION LIKE ?";
            Resource q1 = registry.newResource();
            q1.setContent(sql1);
            q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
            q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                    RegistryConstants.RATINGS_RESULT_TYPE);
            registry.put(QUERY_PATH, q1);
        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        long time1 = System.nanoTime();

        try {
            for (int i = 0; i < iterations; i++) {

                System.out.println("Iteration number: " + i);

                Resource r1 = registry.newResource();
                r1.setContent("test content for custom queries".getBytes());
                r1.setDescription("production ready.");

                long putStart = System.nanoTime();
                //*****************************//
                registry.put(basePath + i + "/s1/a1", r1);
                //*****************************//
                long putEnd = System.nanoTime();
                long putTime = putEnd - putStart;
                System.out.println("CSV," + threadName + "," + "put," + putTime / 1000000);
                r1.discard();

                long queryStart = System.nanoTime();
                //*****************************//
                Map<String, String> params = new HashMap<String, String>();
                params.put("1", "%production%");
                Collection r2 = registry.executeQuery(QUERY_PATH, params);

                //*****************************//
                long queryEnd = System.nanoTime();
                long queryTime = queryEnd - queryStart;
                System.out.println("CSV," + threadName + "," + "query," + queryTime / 1000000);

                r2.discard();

                long deleteStart = System.nanoTime();
                //*****************************//
                registry.delete(basePath + i + "/s1/a1");
                //*****************************//
                long deleteEnd = System.nanoTime();
                long deleteTime = deleteEnd - deleteStart;
                System.out.println("CSV," + threadName + "," + "delete," + deleteTime / 1000000);

            }

        } catch (RegistryException e) {
            //log.error("Error occurred while running the performance test. Thread: " +
            //        threadName + ", Iterations: " + iterations, e);
            e.printStackTrace();
        }

        long time2 = System.nanoTime();

        long elapsedTime = time2 - time1;
        System.out.println("============= Thread: " + threadName + ". Time taken for test: " +
                elapsedTime + "  =============");
    }
}
