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
package org.wso2.carbon.logging.appenders.internal;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.bootstrap.logging.DefaultLoggingBridge;
import org.wso2.carbon.bootstrap.logging.LoggingBridge;
import org.wso2.carbon.bootstrap.logging.LoggingBridgeRegister;
import org.wso2.carbon.bootstrap.logging.LoggingUtils;

import java.util.Enumeration;

/**
 * @deprecated Replaced by pax-logging Activator
 */
@Deprecated
public class CarbonLoggingActivator  implements BundleActivator{
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        // set logging bridge to push java logging logs to log4j appender
        Enumeration<Appender> it = Logger.getRootLogger().getAllAppenders();
        while (it.hasMoreElements()) {
            Appender appender = it.nextElement();
            if(appender instanceof LoggingBridge){
                LoggingBridgeRegister.addAppender(appender.getName(), (LoggingBridge)appender);
            } else {
                LoggingBridgeRegister.addAppender(appender.getName(), new DefaultLoggingBridge());
            }
         }
        LoggingUtils.clear();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
         // nothing to do
    }
}
