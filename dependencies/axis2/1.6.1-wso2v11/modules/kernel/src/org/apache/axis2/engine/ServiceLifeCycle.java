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

package org.apache.axis2.engine;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;

/**
 * When you want to initialize database connections , starting threads and etc..
 * at the time you deploy  service (similar to loadonstartup).
 * You need to implement this interface and add additional (optional) attribute
 * into services.xml <service name="Foo"  class="Service life cycle impl class">
 */

public interface ServiceLifeCycle {

    /**
     * this will be called during the deployement time of the service. irrespective
     * of the service scope this method will be called
     */
    public void startUp(ConfigurationContext configctx, AxisService service);

    /**
     * this will be called during the system shut down time. irrespective
     * of the service scope this method will be called
     */
    public void shutDown(ConfigurationContext configctx, AxisService service);
}