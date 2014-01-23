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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.transport.http.HTTPWorkerFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpResponseFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Factory used to configure and create the various instances required in http transports.
 * Either configure this class in axis2.xml, or in code via the setters, or subclass it and specialize some factory methods to gain more control.
 */
public class HttpFactory {

    /**
     * Name of axis2.xml port parameter for SimpleHTTPServer configuration
     */
    public static final String PARAMETER_PORT = "port";

    /**
     * Name of axis2.xml hostname parameter for SimpleHTTPServer configuration
     */
    public static final String PARAMETER_HOST_ADDRESS = "hostname";

    /**
     * Name of axis2.xml originServer parameter for SimpleHTTPServer configuration
     */
    public static final String PARAMETER_ORIGIN_SERVER = "originServer";

    /**
     * Name of axis2.xml requestTimeout parameter for SimpleHTTPServer configuration
     */
    public static final String PARAMETER_REQUEST_SOCKET_TIMEOUT = "requestTimeout";

    /**
     * Name of axis2.xml requestTcpNoDelay parameter for SimpleHTTPServer configuration
     */
    public static final String PARAMETER_REQUEST_TCP_NO_DELAY = "requestTcpNoDelay";

    /**
     * Name of axis2.xml requestCoreThreadPoolSize parameter for SimpleHTTPServer configuration
     */
    public static final String PARAMETER_REQUEST_CORE_THREAD_POOL_SIZE =
            "requestCoreThreadPoolSize";

    /**
     * Name of axis2.xml requestMaxThreadPoolSize parameter for SimpleHTTPServer configuration
     */
    public static final String PARAMETER_REQUEST_MAX_THREAD_POOL_SIZE = "requestMaxThreadPoolSize";

    /**
     * Name of axis2.xml threadKeepAliveTime parameter for SimpleHTTPServer configuration
     */
    public static final String PARAMETER_THREAD_KEEP_ALIVE_TIME = "threadKeepAliveTime";

    /**
     * Name of axis2.xml threadKeepAliveTimeUnit parameter for SimpleHTTPServer configuration
     */
    public static final String PARAMETER_THREAD_KEEP_ALIVE_TIME_UNIT = "threadKeepAliveTimeUnit";

    private ConfigurationContext configurationContext;
    private TransportInDescription httpConfiguration;
    private int port;
    private String hostAddress;
    private String originServer;
    private int requestSocketTimeout;
    private boolean requestTcpNoDelay;
    private int requestCoreThreadPoolSize;
    private int requestMaxThreadPoolSize;
    private long threadKeepAliveTime;
    private TimeUnit threadKeepAliveTimeUnit;

    private WorkerFactory requestWorkerFactory = null;

    /**
     * Create and configure a new HttpFactory
     */
    public HttpFactory(ConfigurationContext configurationContext) throws AxisFault {
        this.configurationContext = configurationContext;
        httpConfiguration = configurationContext.getAxisConfiguration().getTransportIn(Constants.TRANSPORT_HTTP);
        port = getIntParam(PARAMETER_PORT, 6060);
        hostAddress = getStringParam(PARAMETER_HOST_ADDRESS, null);
        originServer = getStringParam(PARAMETER_ORIGIN_SERVER, "Simple-Server/1.1");
        requestSocketTimeout = getIntParam(PARAMETER_REQUEST_SOCKET_TIMEOUT, 20000);
        requestTcpNoDelay = getBooleanParam(PARAMETER_REQUEST_TCP_NO_DELAY, true);
        requestCoreThreadPoolSize = getIntParam(PARAMETER_REQUEST_CORE_THREAD_POOL_SIZE, 100);
        requestMaxThreadPoolSize = getIntParam(PARAMETER_REQUEST_MAX_THREAD_POOL_SIZE, 150);
        threadKeepAliveTime = getLongParam(PARAMETER_THREAD_KEEP_ALIVE_TIME, 180L);
        threadKeepAliveTimeUnit =
                getTimeUnitParam(PARAMETER_THREAD_KEEP_ALIVE_TIME_UNIT, TimeUnit.SECONDS);
    }

    /**
     * Create and configure a new HttpFactory
     */
    public HttpFactory(ConfigurationContext configurationContext, int port) throws AxisFault {
        this(configurationContext);
        this.port = port;
    }

    /**
     * Create and configure a new HttpFactory
     */
    public HttpFactory(ConfigurationContext configurationContext, int port,
                       WorkerFactory requestWorkerFactory) throws AxisFault {
        this(configurationContext, port);
        this.requestWorkerFactory = requestWorkerFactory;
    }

    private int getIntParam(String name, int def) {
        String config = getStringParam(name, null);
        if (config != null) {
            return Integer.parseInt(config);
        } else {
            return def;
        }
    }

    private long getLongParam(String name, long def) {
        String config = getStringParam(name, null);
        if (config != null) {
            return Long.parseLong(config);
        } else {
            return def;
        }
    }

    private boolean getBooleanParam(String name, boolean def) throws AxisFault {
        String config = getStringParam(name, null);
        if (config != null) {
            if (config.equals("yes") || config.equals("true")) {
                return true;
            } else if (config.equals("no") || config.equals("false")) {
                return false;
            } else {
                throw new AxisFault("Boolean value must be yes, true, no or false for parameter " +
                        name + ":  " + config);
            }
        }
        return def;
    }

    private TimeUnit getTimeUnitParam(String name, TimeUnit def) throws AxisFault {
        String config = getStringParam(name, null);
        if (config != null) {
            try {
                return (TimeUnit) TimeUnit.class.getField(config).get(null);
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }
        }
        return def;
    }

    private String getStringParam(String name, String def) {
        Parameter param = httpConfiguration.getParameter(name);
        if (param != null) {
//            assert param.getParameterType() == Parameter.TEXT_PARAMETER;
            String config = (String) param.getValue();
            if (config != null) {
                return config;
            }
        }
        return def;
    }

    /**
     * Return the configured listener manager or create and configure one with configurationContext
     */
    public ListenerManager getListenerManager() {
        ListenerManager lm = configurationContext.getListenerManager();
        if (lm == null) {
            lm = new ListenerManager();
            lm.init(configurationContext);
        }
        return lm;
    }

    /**
     * Create the executor used to launch the single requestConnectionListener
     */
    public ExecutorService newListenerExecutor(int port) {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue(),
                                      new DefaultThreadFactory(
                                              new ThreadGroup("Listener thread group"),
                                              "HttpListener-" + this.port));
    }

    /**
     * Create the listener for request connections
     */
    public IOProcessor newRequestConnectionListener(
            int port,
            final HttpConnectionManager manager, 
            final HttpParams params) throws IOException {
        return new DefaultConnectionListener(
                port, 
                manager, 
                new DefaultConnectionListenerFailureHandler(), 
                params);
    }

    /**
     * Create and set the parameters applied to incoming request connections
     */
    public HttpParams newRequestConnectionParams() {
        HttpParams params = new BasicHttpParams();
        params
                .setIntParameter(HttpConnectionParams.SO_TIMEOUT, requestSocketTimeout)
                .setBooleanParameter(HttpConnectionParams.TCP_NODELAY, requestTcpNoDelay)
                .setIntParameter(HttpConnectionParams.MAX_LINE_LENGTH, 4000)
                .setIntParameter(HttpConnectionParams.MAX_HEADER_COUNT, 500)
                .setIntParameter(HttpConnectionParams.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setParameter(HttpProtocolParams.ORIGIN_SERVER, originServer);
        return params;
    }

    /**
     * Create the connection manager used to launch request threads
     */
    public HttpConnectionManager newRequestConnectionManager(ExecutorService requestExecutor,
                                                             WorkerFactory workerFactory,
                                                             HttpParams params) {
        return new DefaultHttpConnectionManager(configurationContext, requestExecutor,
                                                workerFactory, params);
    }

    /**
     * Create the executor use the manage request processing threads
     */
    public ExecutorService newRequestExecutor(int port) {
        return new ThreadPoolExecutor(requestCoreThreadPoolSize, requestMaxThreadPoolSize,
                                      threadKeepAliveTime, threadKeepAliveTimeUnit,
                                      newRequestBlockingQueue(),
                                      new DefaultThreadFactory(
                                              new ThreadGroup("Connection thread group"),
                                              "HttpConnection-" + port));
    }

    /**
     * Create the queue used to hold incoming requests when requestCoreThreadPoolSize threads are busy.
     * Default is an unbounded queue.
     */
    public BlockingQueue newRequestBlockingQueue() {
        return new LinkedBlockingQueue();
    }

    /**
     * Create the factory for request workers
     */
    public WorkerFactory newRequestWorkerFactory() {
        if (requestWorkerFactory != null) {
            return requestWorkerFactory;
        } else {
            return new HTTPWorkerFactory();
        }
    }

    public HttpProcessor newHttpProcessor() {
        BasicHttpProcessor httpProcessor = new BasicHttpProcessor();
        httpProcessor.addInterceptor(new RequestSessionCookie());
        httpProcessor.addInterceptor(new ResponseDate());
        httpProcessor.addInterceptor(new ResponseServer());
        httpProcessor.addInterceptor(new ResponseContent());
        httpProcessor.addInterceptor(new ResponseConnControl());
        httpProcessor.addInterceptor(new ResponseSessionCookie());
        return httpProcessor;
    }

    public ConnectionReuseStrategy newConnStrategy() {
        return new DefaultConnectionReuseStrategy();
    }

    public HttpResponseFactory newResponseFactory() {
        return new DefaultHttpResponseFactory();
    }

    // *****
    // Getters and Setters
    // *****

    /**
     * Getter for configurationContext
     */
    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    /**
     * Getter for httpConfiguration
     */
    public TransportInDescription getHttpConfiguration() {
        return httpConfiguration;
    }

    /**
     * Getter for port
     * return the port on which to listen for http connections (default = 6060)
     */
    public int getPort() {
        return port;
    }

    /**
     * Setter for port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Getter for hostAddress
     *
     * @return the host address (or name) to be use in reply-to endpoint references, or null if not specified (default = null)
     */
    public String getHostAddress() {
        return hostAddress;
    }

    /**
     * Setter for hostAddress
     */
    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    /**
     * Getter for originServer
     *
     * @return the Server header string for outgoing messages (default "Simple-Server/1.1")
     */
    public String getOriginServer() {
        return originServer;
    }

    /**
     * Setter for originServer
     */
    public void setOriginServer(String originServer) {
        this.originServer = originServer;
    }

    /**
     * Getter for requestSocketTimeout
     *
     * @return the maximum time in millis to wait for data on a request socket (default 20000)
     */
    public int getRequestSocketTimeout() {
        return requestSocketTimeout;
    }

    /**
     * Setter for requestSocketTimeout
     */
    public void setRequestSocketTimeout(int requestSocketTimeout) {
        this.requestSocketTimeout = requestSocketTimeout;
    }

    /**
     * Getter for requestTcpNoDelay
     * return false iff Nagle's algorithm should be used to conserve bandwidth by minimizing segments
     * at the cost of latency and performance (default true, i.e. maximize performance at
     * the cost of bandwidth)
     */
    public boolean getRequestTcpNoDelay() {
        return requestTcpNoDelay;
    }

    /**
     * Setter for requestTcpNoDelay
     */
    public void setRequestTcpNoDelay(boolean requestTcpNoDelay) {
        this.requestTcpNoDelay = requestTcpNoDelay;
    }

    /**
     * Getter for RequestCoreThreadPoolSize
     *
     * @return the size of the thread pool use to process requests assuming there is adequate queue space (default 25)
     */
    public int getRequestCoreThreadPoolSize() {
        return requestCoreThreadPoolSize;
    }

    /**
     * Setter for RequestCoreThreadPoolSize
     */
    public void setRequestCoreThreadPoolSize(int requestCoreThreadPoolSize) {
        this.requestCoreThreadPoolSize = requestCoreThreadPoolSize;
    }

    /**
     * Getter for requestMaxThreadPoolSize
     *
     * @return the maximum size of the thread pool used to process requests if the queue fills up (default 150).
     *         Since the default queue is unbounded this parameter is meaningless unless you override newRequestBlockingQueue()
     */
    public int getRequestMaxThreadPoolSize() {
        return requestMaxThreadPoolSize;
    }

    /**
     * Setter for requestMaxThreadPoolSize
     */
    public void setRequestMaxThreadPoolSize(int requestMaxThreadPoolSize) {
        this.requestMaxThreadPoolSize = requestMaxThreadPoolSize;
    }

    /**
     * Getter for threadKeepAliveTime
     *
     * @return how long a request processing thread in excess of the core pool size will be kept alive it if is inactive
     *         (default with threadKeepAliveTimeUnit to 180 seconds)
     */
    public long getThreadKeepAliveTime() {
        return threadKeepAliveTime;
    }

    /**
     * Setter for threadKeepAliveTime
     */
    public void setThreadKeepAliveTime(long threadKeepAliveTime) {
        this.threadKeepAliveTime = threadKeepAliveTime;
    }

    /**
     * Getter for threadKeepAliveTimeUnit
     * return the time unit for threadKeepAliveTime (default SECONDS)
     */
    public TimeUnit getThreadKeepAliveTimeUnit() {
        return threadKeepAliveTimeUnit;
    }

    /**
     * Setter for threadKeepAliveTimeUnit
     */
    public void setThreadKeepAliveTimeUnit(TimeUnit threadKeepAliveTimeUnit) {
        this.threadKeepAliveTimeUnit = threadKeepAliveTimeUnit;
    }

}
