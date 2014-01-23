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
import org.wso2.carbon.registry.core.config.DataBaseConfiguration;

public class DBLevelWorker1 extends Worker {

    public DBLevelWorker1(String threadName, int iterations, Registry registry) {
        super(threadName, iterations, registry);
    }

    public void run() {

        DataBaseConfiguration dbConfiguration = new DataBaseConfiguration();
        dbConfiguration.setDbUrl("jdbc:derby:target/registry/WSO2REGISTRY_DB;create=true");
        dbConfiguration.setDriverName("org.apache.derby.jdbc.EmbeddedDriver");
        dbConfiguration.setUserName("wso2registry");
        dbConfiguration.setPassWord("wso2registry");

        long time1 = System.nanoTime();

        try {
            for (int i = 0; i < iterations; i++) {

            }

        } catch (Exception e) {
            //log.error("Error occurred while running the performance test. Thread: " +
            //        threadName + ", Iterations: " + iterations, e);
            e.printStackTrace();
        }
        long time2 = System.nanoTime();
        long elapsedTime = (time2 - time1)/(1000000*iterations);
        System.out.println("AVG-TIME-PER-THREAD,"+threadName+","+elapsedTime);
    }

}
