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
package org.wso2.carbon.bridge;

import java.lang.management.ManagementPermission;

/**
 * Return the relevant OSGi framework. This is needed only if we have support for multiple OSGi
 * frameworks. However, at the moment, we support only Eclipse Equinox
 */
public class FrameworkLauncherFactory {

    private static FrameworkLauncher frameworkLauncher;

    public static FrameworkLauncher getFrameworkLauncher(){
        SecurityManager secMan = System.getSecurityManager();
        if(secMan != null){
           secMan.checkPermission(new ManagementPermission("control")); 
        }
        if(frameworkLauncher == null){
            frameworkLauncher = new EquinoxFrameworkLauncher();
        }
        return frameworkLauncher;
    }
}
