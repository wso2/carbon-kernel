/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.utils;

/**
 * Any Server implementaion which should support restarting or shutting down
 * should implement this interface
 */
public interface Controllable {

    /**
     * Forcefully shutdown the server
     *
     * @throws ServerException If an error occurs during shutting down
     */
    void shutdown() throws ServerException;

    /**
     * Gracefully shutdown the server after serving all requests currently being processed
     *
     * @throws ServerException If an error occurs during shutting down
     */
    void shutdownGracefully() throws ServerException;

    /**
     * Forcefully restart the server
     *
     * @throws ServerException If an error occurs during restarting
     */
    void restart() throws ServerException;

    /**
     * Gracefully restart the server after serving all requests currently being processed
     *
     * @throws ServerException If an error occurs during restarting
     */
    void restartGracefully() throws ServerException;
}
