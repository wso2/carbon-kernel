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
package org.wso2.carbon.server.admin.common;

/**
 *
 */
public interface IServerAdmin {
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

    ServerData getServerData() throws Exception;
}
