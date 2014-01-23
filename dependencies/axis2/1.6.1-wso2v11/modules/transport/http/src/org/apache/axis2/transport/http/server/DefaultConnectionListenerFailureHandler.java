/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.transport.http.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default hander for failures in connection listener IOProcessors.
 * Supports configuration of number retries, delay per retry, and uptime interval considered a success (resets number retries to zero).
 */
public class DefaultConnectionListenerFailureHandler implements ConnectionListenerFailureHandler {

    private static final Log LOG = LogFactory.getLog(DefaultConnectionListenerFailureHandler.class);

    protected int retryDelay;
    protected int successInterval;
    protected int maxRetries;

    private long lastFailure;
    private long lastFirstFailure;
    private int numRetries;

    /**
     * Create a new DefaultConnectionListenerFailureHandler with default settings.
     * retryDelay is 1 second, successInterval is 60 seconds, maxRetries is 10
     */
    public DefaultConnectionListenerFailureHandler() {
        this(1000, 60000, 10);
    }

    /**
     * Create a new DefaultConnectionListenerFailureHandler
     *
     * @param retryDelay      millis to wait before retrying
     * @param successInterval millis after which an initial or retry attempt will be deemed a success, resetting retry count to 0
     * @param maxRetries      maximum number of retries allowed without a success, after which the listener will terminate
     */
    public DefaultConnectionListenerFailureHandler(int retryDelay, int successInterval,
                                                   int maxRetries) {
        this.retryDelay = retryDelay;
        this.successInterval = successInterval;
        this.maxRetries = maxRetries;
        this.lastFailure = this.lastFirstFailure = Long.MIN_VALUE;
        this.numRetries = 0;
    }

    /**
     * Default behavior is to log a warning and attempt retry per constructor config, eventually failing with a logged error and notification.
     * May subclass and override this method to change the behavior.
     */
    public boolean failed(IOProcessor connectionListener, Throwable cause) {
        long now = System.currentTimeMillis();
        if (now > lastFailure + successInterval) {
            numRetries = 0;
            lastFirstFailure = now;
        }
        lastFailure = now;
        if (numRetries >= maxRetries) {
            notifyAbnormalTermination(
                    connectionListener,
                    "Terminating connection listener " + connectionListener + " after " +
                            numRetries + "retries in " + (now - lastFirstFailure) / 1000 +
                            " seconds.",
                    cause);
            return false;
        } else {
            numRetries++;
            if (LOG.isWarnEnabled()) {
                LOG.warn("Attempt number " + numRetries + " of " + maxRetries +
                        " to reestalish connection listener " + connectionListener +
                        " due to failure ",
                         cause);
            }
            return true;
        }
    }

    /**
     * Default bevarior is to log the error.
     * May subclass and override this method to change behavior.
     */
    public void notifyAbnormalTermination(IOProcessor connectionListener, String message,
                                          Throwable cause) {
        LOG.error(message, cause);
    }

}
