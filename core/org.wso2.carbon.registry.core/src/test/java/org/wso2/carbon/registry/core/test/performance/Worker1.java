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
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class Worker1 extends Worker {

    public Worker1(String threadName, int iterations, Registry registry) {
        super(threadName, iterations, registry);
    }

    public void run() {

        long time1 = System.nanoTime();

        try {
        	long timePerThread = 0;
            for (int i = 0; i < iterations; i++) {
            	long start = System.nanoTime();
            	
                Resource r1 = registry.newResource();
                r1.setContent("test content".getBytes());
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
                
                
                long getAgainStart = System.nanoTime();
                try{
                	registry.get(basePath + i);                	
                }catch(RegistryException e){
                	System.out.println(e.getMessage());
                }
                long getAgainEnd = System.nanoTime();
                long getAgainTime = getAgainEnd - getAgainStart;
                System.out.println("CSV,"+threadName+","+"get-again,"+getAgainTime/1000000);

                
                long end = System.nanoTime();
                timePerThread += (end-start);                
            }
            long averageTime = timePerThread/(iterations*1000000);
            System.out.println("CSV-avg-time-per-thread,"+threadName+","+averageTime);          
            

        } catch (RegistryException e) {
            //log.error("Error occurred while running the performance test. Thread: " +
            //        threadName + ", Iterations: " + iterations, e);
            e.printStackTrace();
        }
        long time2 = System.nanoTime();
        long elapsedTime = (time2 - time1)/(1000000*iterations);
        System.out.println("AVG-TIME-PER-THREAD,"+threadName+","+elapsedTime);
    }
}
