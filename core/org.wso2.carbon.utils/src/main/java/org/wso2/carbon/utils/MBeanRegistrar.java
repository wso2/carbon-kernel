/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The class which is responsible for registering MBeans
 */
public class MBeanRegistrar {
    private static Log log = LogFactory.getLog(MBeanRegistrar.class);
    private static List<ObjectName> mbeans = new ArrayList<ObjectName>();
    
    private MBeanRegistrar() {
        //disable external instantiation
    }

    public static void registerMBean(Object mbeanInstance,
                                     String objectName) throws Exception {

        MBeanServer mbs = ManagementFactory.getMBeanServer();
        Set set = mbs.queryNames(new ObjectName(objectName), null);
        if (set.isEmpty()) {
            ObjectName name = new ObjectName(objectName);
            mbs.registerMBean(mbeanInstance, name);
            mbeans.add(name);
        } else {
            log.debug("MBean " + objectName + " already exists");
            throw new Exception("MBean " + objectName + " already exists");
        }
    }

    public static void registerMBean(Object mbeanInstance) {
        String serverPackage = ServerConfiguration.getInstance().getFirstProperty("Package");
        if (serverPackage == null) {
            serverPackage = "wso2";
        }
        try {
            String className = mbeanInstance.getClass().getName();
            if (className.indexOf('.') != -1) {
                className = className.substring(className.lastIndexOf('.') + 1);
            }
            registerMBean(mbeanInstance, serverPackage + ":type=" + className);
        } catch (Exception e) {
            String msg = "Could not register " + mbeanInstance.getClass() + " MBean";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public static void unregisterAllMBeans() {
        MBeanServer mbs = ManagementFactory.getMBeanServer();
        for (ObjectName name : mbeans) {
            try {
                mbs.unregisterMBean(name);
            } catch (Exception e) {
                log.error("Cannot unregister MBean " + name.getCanonicalName());
            }
        }
    }
}
