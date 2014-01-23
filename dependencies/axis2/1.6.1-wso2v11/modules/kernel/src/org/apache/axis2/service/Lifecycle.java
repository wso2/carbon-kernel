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

package org.apache.axis2.service;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ServiceContext;

/**
 * The Lifecycle interface should be implemented by your back-end service
 * class if you wish to be notified of creation and cleanup by the Axis2
 * framework.
 */
public interface Lifecycle {

    /**
     * init() is called when a new instance of the implementing class has been created.
     * This occurs in sync with session/ServiceContext creation.  This method gives classes
     * a chance to do any setup work (grab resources, establish connections, etc) before
     * they are invoked by a service request.
     *
     * @param context the active ServiceContext
     * @throws AxisFault if something goes wrong.  Throwing a fault here will result in either
     *                   failed deployment (for application-scoped services) or failed requests.
     */
    void init(ServiceContext context) throws AxisFault;

    /**
     * destroy() is called when Axis2 decides that it is finished with a particular instance
     * of the back-end service class.  It allows classes to clean up resources.
     * @param context the active ServiceContext
     */
    void destroy(ServiceContext context);
}
