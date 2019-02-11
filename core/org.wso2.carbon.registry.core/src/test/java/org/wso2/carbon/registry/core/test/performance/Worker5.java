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
package org.wso2.carbon.registry.core.test.performance;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;

public class Worker5 extends Worker {

    public Worker5(String threadName, int iterations, Registry registry) {
        super(threadName, iterations, registry);
    }

    public void run() {

        long timePerThread = 0;
        //RemoteRegistry registry = null;
        Resource resource;
        try {
            Resource resource1 = registry.newResource();
            resource1.setContent("ABC");
            registry.put("/ama/test/path",resource1);
            for (int i = 0; i < iterations; i++) {
            	long start = System.nanoTime();
                Resource res = registry.newResource();
                res.setContent("abc");
                registry.put("/test/"+i,res);
                System.out.println("Updated "+i);
                resource = registry.get("/ama/test/path");
                resource.setContent("updated");
                resource.setProperty("abc","abc");
                registry.put("/ama/test/path",resource);
                resource.discard();
                long end = System.nanoTime();
                timePerThread += (end-start);                
                
                Thread.sleep(100);
            }
            long averageTime = timePerThread/(iterations*1000000);
            System.out.println("CSV-avg-time-per-thread,"+threadName+","+averageTime);          

        } catch (Exception e) {
            e.printStackTrace();
        }       
    }
}
