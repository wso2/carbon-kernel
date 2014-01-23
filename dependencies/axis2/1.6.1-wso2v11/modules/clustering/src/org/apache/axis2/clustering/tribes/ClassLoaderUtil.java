/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.apache.axis2.clustering.tribes;

import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A util for manipulating classloaders to be used while serializing & deserializing Tribes messages
 */
public class ClassLoaderUtil {

    private static Map<String, ClassLoader> classLoaders =
            new ConcurrentHashMap<String, ClassLoader>();

    public static void init(AxisConfiguration configuration) {
        classLoaders.put("system", configuration.getSystemClassLoader());
        classLoaders.put("axis2", ClassLoaderUtil.class.getClassLoader());
        for (Iterator iter = configuration.getServiceGroups(); iter.hasNext(); ) {
            AxisServiceGroup group = (AxisServiceGroup) iter.next();
            ClassLoader serviceGroupClassLoader = group.getServiceGroupClassLoader();
            if (serviceGroupClassLoader != null) {
                classLoaders.put(getServiceGroupMapKey(group), serviceGroupClassLoader);
            }
        }
        for (Object obj : configuration.getModules().values()) {
            AxisModule module = (AxisModule) obj;
            ClassLoader moduleClassLoader = module.getModuleClassLoader();
            if (moduleClassLoader != null) {
                classLoaders.put(getModuleMapKey(module), moduleClassLoader);
            }
        }
    }

    public static void addServiceGroupClassLoader(AxisServiceGroup serviceGroup) {
        ClassLoader serviceGroupClassLoader = serviceGroup.getServiceGroupClassLoader();
        if (serviceGroupClassLoader != null) {
            classLoaders.put(getServiceGroupMapKey(serviceGroup), serviceGroupClassLoader);
        }
    }

    public static void removeServiceGroupClassLoader(AxisServiceGroup serviceGroup) {
        classLoaders.remove(getServiceGroupMapKey(serviceGroup));
    }

    private static String getServiceGroupMapKey(AxisServiceGroup serviceGroup) {
        return serviceGroup.getServiceGroupName() + "$#sg";
    }

    public static void addModuleClassLoader(AxisModule module) {
        ClassLoader moduleClassLoader = module.getModuleClassLoader();
        if (moduleClassLoader != null) {
            classLoaders.put(getModuleMapKey(module), moduleClassLoader);
        }
    }

    public static void removeModuleClassLoader(AxisModule axisModule) {
        classLoaders.remove(getModuleMapKey(axisModule));
    }

    private static String getModuleMapKey(AxisModule module) {
        return module.getName() + "-" + module.getVersion() + "$#mod";
    }

    public static ClassLoader[] getClassLoaders() {
        return classLoaders.values().toArray(new ClassLoader[classLoaders.size()]);
    }
}