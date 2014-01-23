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

package org.wso2.carbon.server.admin.service;

/**
 * MBean interface for exposing Server Adminstration functionalities
 */
public interface ServerAdminMBean {

    /**
     * Forcefully restart this WSAS instance
     *
     * @throws Exception If an error occurs while restarting
     * @return true - If successful, false otherwise
     */
    boolean restart() throws Exception;

    /**
     * Forcefully shutdown this WSAS instance
     *
     * @throws Exception If an error occurs while shutting down
     * @return true - If successful, false otherwise
     */
    boolean shutdown() throws Exception;

    /**
     * Gracefully restart this WSAS instance.
     * All client connections will be served before restarting the server
     *
     * @throws Exception If an error occurs while restarting
     * @return true - If successful, false otherwise
     */
    boolean restartGracefully() throws Exception;

    /**
     * Gracefully shutdown this WSAS instance
     * All client connections will be served before shutting down the server
     *
     * @throws Exception If an error occurs while shutting down
     * @return true - If successful, false otherwise
     */
    boolean shutdownGracefully() throws Exception;


    /**
     * Method to switch a node to maintenance mode.
     * <p/>
     * Here is the sequence of events:
     * <p/>
     * <oll>
     * <li>Client calls this method</li>
     * <li>The server stops accepting new requests/connections, but continues to stay alive so
     * that old requests & connections can be served</li>
     * <li>Once all requests have been processed, the method returns</li
     * </ol>
     *
     * @throws Exception If an error occurred while switching to maintenace mode
     */
    void startMaintenance() throws Exception;

    /**
     * Method to change the state of a node from "maintenance" to "normal"
     *
     * @throws Exception If an error occurred while switching to normal mode
     */
    void endMaintenance() throws Exception;

    /**
     * Get information about this WSAS instance
     *
     * @return The server information as a string
     * @throws Exception If an error occurred while retrieving server information
     */
    String getServerDataAsString() throws Exception;

    /**
     * Get the version of this WSAS instance
     *
     * @return The version of this WSAS instance
     */
    String getServerVersion();

    /**
     * Method to check whether this WSAS instance is alive
     *
     * @return True always
     */
    boolean isAlive();

    /**
     * Get the current status of this WSAS instance
     *
     * @return The current server status. <br/>
     *         Possible values are, <br/>
     *         {@link org.wso2.carbon.core.ServerStatus#STATUS_RUNNING}, <br/>
     *         {@link org.wso2.carbon.core.ServerStatus#STATUS_SHUTTING_DOWN},<br/>
     *         {@link org.wso2.carbon.core.ServerStatus#STATUS_RESTARTING},<br/>
     *         {@link org.wso2.carbon.core.ServerStatus#STATUS_IN_MAINTENANCE} <br/>
     * @see org.wso2.carbon.core.ServerStatus
     * @throws Exception If an error occurs while retrieving the status
     */
    String getServerStatus() throws Exception;
}
