/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.transport.sms.gsm;

import org.smslib.Service;

import java.util.Map;
import java.util.HashMap;

/**
 * The Repository of started GSM Services
 * Allow to share and reuse the GSM services
 */
class GSMServiceRepository {

    private Map<String, Service> serviceRepo = new HashMap<String,Service>();

    private static GSMServiceRepository me;

    private GSMServiceRepository() {

    }

    public static GSMServiceRepository getInstence() {
        if(me == null) {
            me = new GSMServiceRepository();
        }

        return me;
    }

    /**
     * add a service with a given gateway
     * @param gateway
     * @param service
     */
    public void addService(String gateway , Service service) {
        if(!gatewayInUse(gateway)) {
            serviceRepo.put(gateway,service);
        }
    }

    /**
     * remove the service given the gateway id
     * @param gateway
     */
    public void removeService(String gateway) {
        serviceRepo.remove(gateway);
    }
   

    /**
     * get the service given the gateway id
     * @param gateway
     * @return
     */
    public Service getService(String gateway) {
        return serviceRepo.get(gateway);
    }

    /**
     * to know whether the gateway inuse
     * @param gateway
     * @return
     */
    public boolean gatewayInUse(String gateway) {
        return serviceRepo.containsKey(gateway);
    }


}
