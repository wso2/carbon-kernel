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

package org.wso2.carbon.utils.logging.appenders;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.wso2.carbon.bootstrap.logging.LoggingBridge;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.logging.LoggingUtils;
import org.wso2.carbon.utils.logging.TenantAwareLoggingEvent;

import java.util.logging.LogRecord;


public class CarbonConsoleAppender extends ConsoleAppender implements LoggingBridge {
    @Override
    public void push(LogRecord record) {
        LoggingEvent loggingEvent = LoggingUtils.getLogEvent(record);
        doAppend(loggingEvent);
    }

    /**
     * This overriden method takes a Logging event, get it wrapped with tenant information taken
     * from PrivilegedCarbonContext (by utilizing LoggingUtils.getTenantAwareLogEvent) and passes
     * the TenantAwareLoggingEvent to super class.
     * <p/>
     * The passed TenantAwareLoggingEvent is later used in TenantAwareNamedPatternConverter to
     * extract the required tenant information as well as other logging information.
     *
     * @param loggingEvent
     *         - the logging event without tenant information
     */
    @Override
    protected void subAppend(LoggingEvent loggingEvent) {
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext
                .getThreadLocalCarbonContext();
        int tenantId = privilegedCarbonContext.getTenantId();
        String serviceName = privilegedCarbonContext.getApplicationName();

        // acquire the tenant aware logging event from the logging event
        TenantAwareLoggingEvent tenantAwareLoggingEvent = LoggingUtils
                .getTenantAwareLogEvent(loggingEvent, tenantId, serviceName);
        super.subAppend(tenantAwareLoggingEvent);
    }
}
