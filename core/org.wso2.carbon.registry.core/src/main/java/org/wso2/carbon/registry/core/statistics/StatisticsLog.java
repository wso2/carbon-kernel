/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.statistics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is an class containing a logger that can be used to log statistics related to the use of one
 * or more registry instances on a given server. Logged statistics include connection details,
 * transactions, and also registry operations.
 * <p/>
 * In order to see the statistics logged on the command line, add the following entry to the
 * <b>log4j.properties</b> file on the classpath:
 * <code>log4j.logger.org.wso2.carbon.registry.core.statistics=DEBUG</code>.
 */
public final class StatisticsLog {

    private static Log log = LogFactory.getLog(StatisticsLog.class);

    /**
     * Method to obtain an instance of the logger that can be used to log statistics.
     * @return the logger that can be used to log statistics.
     */
    public static Log getLog() {
        return log;
    }

    // This class is not supposed to be instantiated.
    private StatisticsLog() {
    }

}
