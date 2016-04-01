/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.kernel.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * The class which is responsible for registering MBeans.
 *
 * @since 5.1.0
 */
public class MBeanRegistrator {
    private static final Logger logger = LoggerFactory.getLogger(MBeanRegistrator.class);
    private static List<ObjectName> mBeans = new ArrayList<>();

    private MBeanRegistrator() {
    }

    /**
     * Registers an object as an MBean with the MBean server.
     *
     * @param mBeanInstance - The MBean to be registered as an MBean.
     */
    public static void registerMBean(Object mBeanInstance) throws RuntimeException {

        String className = mBeanInstance.getClass().getName();
        if (className.indexOf('.') != -1) {
            className = className.substring(className.lastIndexOf('.') + 1);
        }

        String objectName = Constants.SERVER_PACKAGE + ":type=" + className;
        try {
            MBeanServer mBeanServer = MBeanManagementFactory.getMBeanServer();
            Set set = mBeanServer.queryNames(new ObjectName(objectName), null);
            if (set.isEmpty()) {
                try {
                    ObjectName name = new ObjectName(objectName);
                    mBeanServer.registerMBean(mBeanInstance, name);
                    mBeans.add(name);
                } catch (InstanceAlreadyExistsException e) {
                    throw new RuntimeException("MBean " + objectName + " already exists", e);
                } catch (MBeanRegistrationException | NotCompliantMBeanException e) {
                    throw new RuntimeException("Execption when registering MBean" , e);
                }
            } else {
                throw new RuntimeException("MBean " + objectName + " already exists");
            }
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException("Could not register " + mBeanInstance.getClass() + " MBean", e);
        }
    }

    /**
     * Unregisters all MBeans from the MBean server.
     *
     */
    public static void unregisterAllMBeans() {
        MBeanServer mBeanServer = MBeanManagementFactory.getMBeanServer();
        for (ObjectName name : mBeans) {
            try {
                mBeanServer.unregisterMBean(name);
            } catch (InstanceNotFoundException | MBeanRegistrationException e) {
                logger.error("Cannot unregister MBean " + name.getCanonicalName(), e);
            }
        }
    }
}
