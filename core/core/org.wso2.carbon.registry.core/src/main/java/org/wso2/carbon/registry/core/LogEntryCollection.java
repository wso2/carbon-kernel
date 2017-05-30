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
package org.wso2.carbon.registry.core;

import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.dao.LogsDAO;

import java.util.Date;

/**
 * The main purpose of this class is to handle pagination for log entries. From the registry API it
 * will returns LogEntryCollection , and from the LogEntryCollection user can get all the logs or he
 * can ask for log for a given range
 */
public class LogEntryCollection {

    private int logCount;
    private DataAccessManager dataAccessManager;

    private String resourcePath;
    private int action;
    private String userName;
    private Date from;
    private Date to;
    private boolean recentFirst;

    /**
     * Get the count of the log entries.
     *
     * @return the count of the log entries
     */
    @SuppressWarnings("unused")
    public int getLogCount() {
        return logCount;
    }

    /**
     * Method to set the count of the log entries.
     *
     * @param logCount the count of the log entries
     */
    public void setLogCount(int logCount) {
        this.logCount = logCount;
    }

    /**
     * Returns an array of log entries filtered by the provided information.
     *
     * @return an array of log entries.
     * @throws RegistryException throws if the operation fail.
     */
    @SuppressWarnings("unused")
    public LogEntry[] getLogEntries() throws RegistryException {
        LogsDAO logsDAO = dataAccessManager.getDAOManager().getLogsDAO();
        return logsDAO.getLogs(resourcePath,
                action,
                userName,
                from,
                to,
                recentFirst,
                dataAccessManager);
    }

    /**
     * Returns an array of log entries filtered by the provided information and in the provided
     * range.
     *
     * @param start   the start of the range.
     * @param pageLen number of items to return.
     *
     * @return an array of log entries.
     * @throws RegistryException throws if the operation fail.
     */
    @SuppressWarnings("unused")
    public LogEntry[] getLogEntries(int start, int pageLen) throws RegistryException {
        LogsDAO logsDAO = dataAccessManager.getDAOManager().getLogsDAO();
        return logsDAO.getLogs(resourcePath,
                action,
                userName,
                from,
                to,
                recentFirst,
                start,
                pageLen,
                dataAccessManager);
    }

    /**
     * Get the data access manager associated with the log entry.
     *
     * @return the data access manager.
     */
    @SuppressWarnings("unused")
    public DataAccessManager getDataAccessManager() {
        return dataAccessManager;
    }

    /**
     * Set the data access manager associated with the log entry.
     *
     * @param dataAccessManager the data access manager.
     */
    public void setDataAccessManager(DataAccessManager dataAccessManager) {
        this.dataAccessManager = dataAccessManager;
    }

    /**
     * Method to set the resource path to filter with.
     *
     * @param resourcePath the resource path.
     */
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * Method to set the action to filter with.
     *
     * @param action the action.
     */
    public void setAction(int action) {
        this.action = action;
    }

    /**
     * Method to set the user name to filter with.
     *
     * @param userName the user name.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Method to set the from date to filter with.
     *
     * @param from the from date.                                   
     */
    public void setFrom(Date from) {
        // We are creating a new instance of the date object to hide the internal representation.
        if (from != null) {
            this.from = new Date(from.getTime());
        }
    }

    /**
     * Method to set the to date to filter with.
     *
     * @param to the 'to' date.
     */
    public void setTo(Date to) {
        // We are creating a new instance of the date object to hide the internal representation.
        if (to != null) {
            this.to = new Date(to.getTime());
        }
    }

    /**
     * Set whether the returned entries should be ordered so the recent one is appeared first.
     *
     * @param recentFirst whether the returned entries should be ordered or not.
     */
    public void setRecentFirst(boolean recentFirst) {
        this.recentFirst = recentFirst;
    }
}
