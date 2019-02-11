/*
 * Copyright 2009-2010 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.bridge;


import java.lang.management.ManagementPermission;

/**
 * This will be used when Carbon is restarted using the management console or the OSGi console
 */
public class FrameworkRestarter implements Runnable {

    public void run() {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
            secMan.checkPermission(new ManagementPermission("control"));
        }

          // Carbon is running within an AppServer
            FrameworkLauncher frameworkLauncher = FrameworkLauncherFactory.getFrameworkLauncher();
            frameworkLauncher.stop();  //TODO FIXME  There is an Equinox memory leak which causes ChildFirstURLClassloader to remain alive
            System.setProperty(FrameworkLauncher.START_TIME, String.valueOf(System.currentTimeMillis()));
            frameworkLauncher.deploy();
            frameworkLauncher.start();

    }
}