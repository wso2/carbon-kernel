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
package org.wso2.carbon.registry.core.statistics.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is an class containing a logger that can be used to log database query statistics related to
 * the use of one or more registry instances on a given server. Logged statistics registry
 * operations, accessed tables on the database, and type of query (SELECT, INSERT, DELETE, ...).
 * <p/>
 * In order to see the database query statistics logged on the command line, add the following entry
 * to the <b>log4j.properties</b> file on the classpath:
 * <code>log4j.logger.org.wso2.carbon.registry.core.statistics.query=DEBUG</code>.
 * <p/>
 * This implementation, by default does not store information on duplicate accesses on a particular
 * table of a database, to optimize performance. If you wish to see the complete list of accesses,
 * set the <code>carbon.registry.statistics.preserve.duplicate.table.accesses</code> system property
 * to <code>true<code>.
 * <p/>
 * This implementation, by default does not log the list of queries executed on the database, to
 * optimize performance. If you wish to see the complete list of queries executed in chronological
 * order, set the <code>carbon.registry.statistics.output.queries.executed</code> system property
 * to <code>true<code>.
 * <p/>
 * We log statistics for all registry operations by default. If you want to log statistics for a
 * limited set of operations, you need to specify the names of the operations as a list of comma
 * separated values using the <code>carbon.registry.statistics.operations</code> system property.
 */
public final class DBQueryStatisticsLog {

    private static Log log = LogFactory.getLog(DBQueryStatisticsLog.class);

    private static ThreadLocal<StatisticsRecord> tStatisticsRecord =
            new ThreadLocal<StatisticsRecord>() {
                protected StatisticsRecord initialValue() {
                    return new StatisticsRecord();
                }
            };

    /**
     * Method to obtain an instance of the logger that can be used to log database query statistics.
     * @return the logger that can be used to log database query statistics.
     */
    public static Log getLog() {
        return log;
    }

    // This class is not supposed to be instantiated.
    private DBQueryStatisticsLog() {

    }

    /**
     * Method to retrieve a Statistics Record.
     *
     * @return the current statistics record.
     */
    public static StatisticsRecord getStatisticsRecord() {
        return tStatisticsRecord.get();
    }

    /**
     * Method to clear the current statistics record.
     */
    public static void clearStatisticsRecord() {
        tStatisticsRecord.remove();
    }

}
