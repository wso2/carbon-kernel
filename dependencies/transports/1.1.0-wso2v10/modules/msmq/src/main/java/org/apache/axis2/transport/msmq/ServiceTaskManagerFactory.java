/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.transport.msmq;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.base.threads.WorkerPool;


public class ServiceTaskManagerFactory {
    public static ServiceTaskManager createTaskManagerForService(AxisService service,
                                                                 WorkerPool workerPool){
        String serviceName = service.getName();
        ServiceTaskManager stm = new ServiceTaskManager();
        stm.setServiceName(serviceName);
        stm.setWorkerPool(workerPool);
        // TODO: set other service level parameters
        return stm;
    }

}
