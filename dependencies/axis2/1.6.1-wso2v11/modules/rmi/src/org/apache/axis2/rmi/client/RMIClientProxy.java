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

package org.apache.axis2.rmi.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.rmi.Configurator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * this class is used to generate the proxy clients.
 */

public class RMIClientProxy {
    public static Object createProxy(Class interfaceClass,
                                     Configurator configurator,
                                     String epr) throws AxisFault {
        InvocationHandler invocationHandler = new ProxyInvocationHandler(interfaceClass,configurator,epr);
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                              new Class[]{interfaceClass},
                                              invocationHandler);
        return proxy;

    }

    public static Object createProxy(Class interfaceClass,
                                     String epr) throws AxisFault {
        return createProxy(interfaceClass,new Configurator(),epr);
    }
}
