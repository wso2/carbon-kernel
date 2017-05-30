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

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class Worker2 extends Worker {
    
    public Worker2(String threadName, int iterations, Registry registry) {
        super(threadName, iterations, registry);
    }

    public void run() {

        long time1 = System.nanoTime();

        try {
            for (int i = 0; i < iterations; i++) {
                Resource r1 = registry.newResource();
                r1.setContent("test content".getBytes());
                r1.setProperty("property1", "value1");
                r1.setProperty("property2", "value2");
                r1.setProperty("property3", "value3");
                long putStart = System.nanoTime();
                //*****************************//
                registry.put(basePath + i, r1);
                //*****************************//
                long putEnd = System.nanoTime();
                long putTime = putEnd - putStart;
                System.out.println("CSV,"+threadName+","+"put,"+putTime/1000000);
                r1.discard();

                long getStart = System.nanoTime();
                //*****************************//
                Resource r2 = registry.get(basePath + i);
                //*****************************//
                long getEnd = System.nanoTime();
                long getTime = getEnd - getStart;
                System.out.println("CSV,"+threadName+","+"get,"+getTime/1000000);

                r2.discard();

                long deleteStart = System.nanoTime();
                //*****************************//
                registry.delete(basePath + i);
                //*****************************//
                long deleteEnd = System.nanoTime();
                long deleteTime = deleteEnd - deleteStart;
                System.out.println("CSV,"+threadName+","+"delete,"+deleteTime/1000000);

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
