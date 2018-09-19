/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ndatasource.rdbms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.interceptor.AbstractQueryReport;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Time-Logging interceptor for JDBC pool.
 * Logs the time taken to execute the query in each pool-ed connection.
 */

public class CorrelationLogInterceptor extends AbstractQueryReport {

    private static final Log correlationLog = LogFactory.getLog("CORRELATION_LOG");

    private static final String CORRELATION_LOG_TIME_TAKEN_KEY = "delta";
    private static final String CORRELATION_LOG_TIME_TAKEN_UNIT = "ms";
    private static final String CORRELATION_LOG_CALL_TYPE_KEY = "callType";
    private static final String CORRELATION_LOG_CALL_TYPE_VALUE = "jdbc";
    private static final String CORRELATION_LOG_START_TIME_KEY = "startTime";
    private static final String CORRELATION_LOG_METHOD_NAME_KEY = "methodName";
    private static final String CORRELATION_LOG_QUERY_KEY = "query";
    private static final String CORRELATION_LOG_CONNECTION_URL_KEY = "connectionUrl";
    private static final String CORRELATION_LOG_SEPARATOR = " | ";
    private static final String CORRELATION_LOG_SYSTEM_PROPERTY = "enableCorrelationLogs";

    private static Log log = LogFactory.getLog(CorrelationLogInterceptor.class);

    @Override
    public void closeInvoked() {

    }

    @Override
    protected void prepareStatement(String s, long l) {

    }

    @Override
    protected void prepareCall(String s, long l) {

    }

    @Override
    public Object createStatement(Object proxy, Method method, Object[] args, Object statement, long time) {

        try {
            if (Boolean.parseBoolean(System.getProperty(CORRELATION_LOG_SYSTEM_PROPERTY))) {
                return invokeProxy(method, args, statement, time);
            } else {
                return statement;
            }

        } catch (Exception e) {
            log.warn("Unable to create statement proxy for slow query report.", e);
            return statement;
        }
    }

    private Object invokeProxy(Method method, Object[] args, Object statement, long time) throws Exception {

        String name = method.getName();
        String sql = null;
        Constructor<?> constructor = null;

        if (this.compare("prepareStatement", name)) {
            sql = (String) args[0];
            constructor = this.getConstructor(1, PreparedStatement.class);
            if (sql != null) {
                this.prepareStatement(sql, time);
            }
        } else if (!this.compare("prepareCall", name)) {
            return statement;
        }

        if (constructor != null) {
            return constructor.newInstance(new StatementProxy(statement, sql));
        } else {
            return null;
        }
    }

    /**
     * Proxy Class that is used to calculate and log the time taken for queries
     */
    protected class StatementProxy implements InvocationHandler {

        protected boolean closed = false;
        protected Object delegate;
        protected final String query;

        public StatementProxy(Object parent, String query) {

            this.delegate = parent;
            this.query = query;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            String name = method.getName();
            boolean close = CorrelationLogInterceptor.this.compare("close", name);
            try {
                if (close && this.closed) {
                    return null;
                } else if (CorrelationLogInterceptor.this.compare("isClosed", name)) {
                    return this.closed;
                } else if (this.closed) {
                    throw new SQLException("Statement closed.");
                } else {
                    boolean process = false;
                    process = CorrelationLogInterceptor.this.isExecute(method, process);

                    long start = System.currentTimeMillis();
                    Object result = null;

                    if (this.delegate != null) {
                        result = method.invoke(this.delegate, args);
                    }

                    //If the query is an execute type of query the time taken is calculated and logged
                    if (process) {
                        long delta = System.currentTimeMillis() - start;
                        CorrelationLogInterceptor.this.reportQuery(this.query, args, name, start, delta);
                        logQueryDetails(start, delta, name);
                    }

                    if (close) {
                        this.closed = true;
                        this.delegate = null;
                    }

                    return result;
                }
            } catch (Exception e) {
                log.error("Unable get query run-time", e);
                return null;
            }
        }

        /**
         * Logs the details from the query
         *
         * @param start      Query start time
         * @param delta      Time taken for query
         * @param methodName Name of the method executing
         */
        private void logQueryDetails(long start, long delta, String methodName) throws SQLException {

            if (this.delegate instanceof PreparedStatement) {
                PreparedStatement preparedStatement = (PreparedStatement) this.delegate;
                if (preparedStatement.getConnection() != null) {
                    DatabaseMetaData metaData = preparedStatement.getConnection().getMetaData();
                    if (correlationLog.isDebugEnabled()) {
                        Map<String, String> logPropertiesMap = new LinkedHashMap<>();
                        logPropertiesMap.put(CORRELATION_LOG_TIME_TAKEN_KEY, Long.toString(delta) +
                                " " + CORRELATION_LOG_TIME_TAKEN_UNIT);
                        logPropertiesMap.put(CORRELATION_LOG_CALL_TYPE_KEY, CORRELATION_LOG_CALL_TYPE_VALUE);
                        logPropertiesMap.put(CORRELATION_LOG_START_TIME_KEY, Long.toString(start));
                        logPropertiesMap.put(CORRELATION_LOG_METHOD_NAME_KEY, methodName);
                        logPropertiesMap.put(CORRELATION_LOG_QUERY_KEY, this.query);
                        logPropertiesMap.put(CORRELATION_LOG_CONNECTION_URL_KEY, metaData.getURL());
                        correlationLog.debug(createLogFormat(logPropertiesMap));
                    }
                }
            }
        }

        /**
         * Creates the log line that should be printed
         *
         * @param logPropertiesMap Contains the type and value that should be printed in the log
         * @return The log line
         */
        private String createLogFormat(Map<String, String> logPropertiesMap) {

            StringBuilder sb = new StringBuilder();
            Object[] keys = logPropertiesMap.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                sb.append(logPropertiesMap.get(keys[i]));
                if (i < keys.length - 1) {
                    sb.append(CORRELATION_LOG_SEPARATOR);
                }
            }
            return sb.toString();
        }
    }
}
