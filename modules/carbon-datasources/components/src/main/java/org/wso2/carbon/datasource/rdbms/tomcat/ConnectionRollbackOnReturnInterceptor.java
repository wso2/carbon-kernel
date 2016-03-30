/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.datasource.rdbms.tomcat;

import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.apache.tomcat.jdbc.pool.PooledConnection;

import java.lang.reflect.Method;

/**
 * This class represents a JDBC-Pool JDBC interceptor class which rollbacks the connections when
 * they are returned to the pool, if the mode is autoCommit=true.
 */
public class ConnectionRollbackOnReturnInterceptor extends JdbcInterceptor {

    private volatile PooledConnection connection = null;

    @Override
    public void reset(ConnectionPool parent, PooledConnection connection) {
        this.connection = connection;
    }

    @SuppressWarnings("finally")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        PooledConnection pc = this.connection;
        try {
            if (compare(CLOSE_VAL, method)) {
                this.connection = null;
                if (pc != null && pc.getXAConnection() == null && !pc.getConnection().getAutoCommit()) {
                    pc.getConnection().rollback();
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            return super.invoke(proxy, method, args);
        }

    }
}