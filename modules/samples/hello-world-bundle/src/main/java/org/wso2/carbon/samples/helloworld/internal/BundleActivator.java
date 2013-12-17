/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.samples.helloworld.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

public class BundleActivator implements org.osgi.framework.BundleActivator {

    private static Log log = LogFactory.getLog(BundleActivator.class);

    public void start(BundleContext bundleContext) throws Exception {
        System.out.println("%%%%%%%%%%%% BundleActivator.start()");
        log.info("%%%%%%%%%%%% BundleActivator.start()");

//        Thread t = new Thread(new Runnable() {
//            public void run() {
//                System.out.println("$$$$$$$$$$ Thread.sleep");
//                try {
//                    Thread.sleep(1000 * 5);
//                    System.setProperty("carbon.server.restart", "falseeee");
//                    System.out.println("$$$$$$$$$$ Thread.sleep -- Done");
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        t.start();
    }

    public void stop(BundleContext bundleContext) throws Exception {
        System.out.println("%%%%%%%%%%%% BundleActivator.stop()");
        log.info("%%%%%%%%%%%% BundleActivator.stop()");
    }
}
