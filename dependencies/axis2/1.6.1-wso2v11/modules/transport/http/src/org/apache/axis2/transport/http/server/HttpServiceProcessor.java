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
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * I/O processor intended to process requests and fill in responses.
 */
public class HttpServiceProcessor implements IOProcessor {

    private static final Log LOG = LogFactory.getLog(HttpServiceProcessor.class);

    /** Counter used to create unique IDs. */
    private static AtomicLong counter = new AtomicLong(0L);

    private AtomicBoolean terminated;

    private final AxisHttpService httpservice;

    private final AxisHttpConnection conn;

    private final IOProcessorCallback callback;

    /**
     * Unique identifier used by {@linkplain #equals(Object)} and
     * {@linkplain #hashCode()}.
     * <p>
     * This field is needed to allow the equals method to work properly when this
     * HttpServiceProcessor has to be removed from the list of processors.
     * 
     * @see DefaultHttpConnectionManager
     */
    private final long id;


    public HttpServiceProcessor(final AxisHttpService httpservice,
            final AxisHttpConnection conn, final IOProcessorCallback callback) {
        super();
        this.httpservice = httpservice;
        this.conn = conn;
        this.callback = callback;
        this.terminated = new AtomicBoolean(false);

        id = counter.incrementAndGet();
    }


    public void run() {
        LOG.debug("New connection thread");
        HttpContext context = new BasicHttpContext(null);
        try {
            while (! Thread.interrupted() && ! isDestroyed() && this.conn.isOpen()) {
                this.httpservice.handleRequest(this.conn, context);
            }
        } catch (ConnectionClosedException ex) {
            LOG.debug("Client closed connection");
        } catch (IOException ex) {
            if (ex instanceof SocketTimeoutException) {
                LOG.debug(ex.getMessage());
            } else if (ex instanceof SocketException) {
                LOG.debug(ex.getMessage());
            } else {
                LOG.warn(ex.getMessage(), ex);
            }
        } catch (HttpException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("HTTP protocol error: " + ex.getMessage());
            }
        } finally {
            destroy();
            if (this.callback == null) {
                throw new NullPointerException("The callback object can't be null");
            }
            this.callback.completed(this);
        }
    }


    public void close() throws IOException {
        this.conn.close();
    }


    public void destroy() {
        if (this.terminated.compareAndSet(false, true)) {
            try {
//                this.conn.shutdown();
                close();
            } catch (IOException ex) {
                LOG.debug("I/O error shutting down connection");
            }
        }
    }


    public boolean isDestroyed() {
        return this.terminated.get();
    }


    // -------------------------------------------------- Methods from Object

    /**
     * Returns the unique ID of this HttpServiceProcessor.
     * 
     * @return The unique ID of this HttpServiceProcessor.
     */
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (int) (id ^ (id >>> 32));
        return result;
    }


   /**
    * Indicates whether some other object is "equal to" this one.
    * 
    * @return <code>true</code> if this HttpServiceProcessor refere to the same 
    * object as obj or they have the same {@linkplain #id}, <code>false</code> otherwise.
    */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final HttpServiceProcessor other = (HttpServiceProcessor) obj;
        if (id != other.id)
            return false;
        return true;
    }

}
