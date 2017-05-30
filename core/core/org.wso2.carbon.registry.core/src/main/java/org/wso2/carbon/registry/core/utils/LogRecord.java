/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.core.utils;

import java.util.Date;

/**
 * Represents a log record
 */
public class LogRecord {

    private String resourcePath;
    private String userName;
    private long timestamp;
    private int action;
    private String actionData;
    private int tenantId;

    /**
     * Method to obtain the resource path.
     *
     * @return the resource path.
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Method to obtain the resource path.
     *
     * @param resourcePath the resource path.
     */
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * Method to obtain the username.
     *
     * @return the username.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Method to obtain the username.
     *
     * @param userName the username.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Method to obtain the timestamp.
     *
     * @return the timestamp.
     */
    public Date getTimestamp() {
        return new Date(timestamp);
    }

    /**
     * Method to obtain the timestamp.
     *
     * @param timestamp the timestamp.
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp.getTime();
    }

    /**
     * Method to obtain the action.
     *
     * @return the action.
     */
    public int getAction() {
        return action;
    }

    /**
     * Method to obtain the action.
     *
     * @param action the action.
     */
    public void setAction(int action) {
        this.action = action;
    }

    /**
     * Method to obtain the action data.
     *
     * @return the action data.
     */
    public String getActionData() {
        return actionData;
    }

    /**
     * Method to obtain the action data.
     *
     * @param actionData the action data.
     */
    public void setActionData(String actionData) {
        this.actionData = actionData;
    }

    /**
     * Method to obtain the tenant identifier.
     *
     * @return the tenant identifier.
     */
    public int getTenantId() {
        return tenantId;
    }

    /**
     * Method to set the tenant identifier.
     *
     * @param tenantId the tenant identifier.
     */
    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * returns a string representation of this record.
     *
     * @return string representation of this record
     */
	public String toString() {
        return resourcePath + ":" + userName + ":" + action + ":" + actionData + ":" + tenantId;
	}	
	
}
