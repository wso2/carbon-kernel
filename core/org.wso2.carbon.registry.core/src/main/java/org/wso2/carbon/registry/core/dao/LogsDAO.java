/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.core.dao;

import org.wso2.carbon.registry.core.LogEntry;
import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.LogRecord;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for Activity Logs.
 */
public interface LogsDAO {

    /**
     * Save batch of log records
     *
     * @param logRecords an array of log records to save.
     *
     * @throws RegistryException if the operation failed.
     */
    void saveLogBatch(LogRecord[] logRecords) throws RegistryException;

    /**
     * Method to get a list of log entries.
     *
     * @param resourcePath the resource path.
     * @param action       the action in concern.
     * @param userName     the name of the user who we want to get logs for.
     * @param from         the starting date.
     * @param to           the ending date.
     * @param descending   whether descending or ascending.
     * @param dataAccessManager   the data access manager used to connect to the database.
     *
     * @return an array of log entries.
     * @throws RegistryException if an error occurred while obtaining logs.
     */
    List getLogList(String resourcePath, int action,
                        String userName, Date from, Date to, boolean descending,
                        DataAccessManager dataAccessManager)
            throws RegistryException;

    /**
     * Method to get a list of log entries.
     *
     * @param resourcePath        the resource path.
     * @param action              the action in concern.
     * @param userName            the name of the user who we want to get logs for.
     * @param from                the starting date.
     * @param to                  the ending date.
     * @param descending          whether descending or ascending.
     * @param start               the starting index
     * @param pageLen             the length of the array returned.
     * @param dataAccessManager   the data access manager used to connect to the database.
     *
     * @return an array of log entries.
     * @throws RegistryException if an error occurred while obtaining logs.
     */
    LogEntry[] getLogs(String resourcePath,
                              int action,
                              String userName,
                              Date from,
                              Date to,
                              boolean descending,
                              int start,
                              int pageLen,
                              DataAccessManager dataAccessManager)
            throws RegistryException;

    /**
     * Method to get a list of log entries.
     *
     * @param resourcePath        the resource path.
     * @param action              the action in concern.
     * @param userName            the name of the user who we want to get logs for.
     * @param from                the starting date.
     * @param to                  the ending date.
     * @param descending          whether descending or ascending.
     * @param dataAccessManager   the data access manager used to connect to the database.
     *
     * @return an array of log entries.
     * @throws RegistryException if an error occurred while obtaining logs.
     */
    LogEntry[] getLogs(String resourcePath,
                              int action,
                              String userName,
                              Date from,
                              Date to,
                              boolean descending,
                              DataAccessManager dataAccessManager)
            throws RegistryException;

    /**
     * Method to get the number of log entries available
     *
     * @param resourcePath the resource path.
     * @param action       the action in concern.
     * @param userName     the name of the user who we want to get logs for.
     * @param from         the starting date.
     * @param to           the ending date.
     * @param descending   whether descending or ascending.
     *
     * @return the number of logs.
     * @throws RegistryException if an error occurred.
     */
    int getLogsCount(String resourcePath,
                            int action,
                            String userName,
                            Date from,
                            Date to,
                            boolean descending)
            throws RegistryException;
}
