/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.format;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.activation.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class to create {@link ManagedDataSource} objects.
 */
public class ManagedDataSourceFactory {
    private static class ManagedInputStream extends FilterInputStream {
        private DataSourceManager manager;
        
        public ManagedInputStream(DataSourceManager manager, InputStream parent) {
            super(parent);
            this.manager = manager;
        }

        @Override
        public void close() throws IOException {
            if (manager != null) {
                manager.notifyStreamClosed(this);
                manager = null;
            }
            super.close();
        }
    }
    
    private static class DataSourceManager implements InvocationHandler {
        private static final Log log = LogFactory.getLog(DataSourceManager.class);
        
        private static final Method getInputStreamMethod;
        private static final Method destroyMethod;
        
        static {
            try {
                getInputStreamMethod = DataSource.class.getMethod("getInputStream");
                destroyMethod = ManagedDataSource.class.getMethod("destroy");
            } catch (NoSuchMethodException ex) {
                throw new NoSuchMethodError(ex.getMessage());
            }
        }

        private final DataSource dataSource;
        private final List<ManagedInputStream> openStreams = Collections.synchronizedList(
                new LinkedList<ManagedInputStream>());
        
        public DataSourceManager(DataSource dataSource) {
            this.dataSource = dataSource;
        }
        
        public void notifyStreamClosed(ManagedInputStream managedInputStream) {
            if (!openStreams.remove(managedInputStream)) {
                throw new IllegalStateException();
            }
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if (method.equals(getInputStreamMethod)) {
                    InputStream in = (InputStream)method.invoke(dataSource, args);
                    ManagedInputStream in2 = new ManagedInputStream(this, in);
                    openStreams.add(in2);
                    return in2;
                } else if (method.equals(destroyMethod)) {
                    while (!openStreams.isEmpty()) {
                        try {
                            openStreams.get(0).close();
                        } catch (IOException ex) {
                            log.warn("Exception when closing open stream from managed data source", ex);
                        }
                    }
                    return null;
                } else {
                    return method.invoke(dataSource, args);
                }
            } catch (InvocationTargetException ex) {
                throw ex.getCause();
            }
        }
        
    }
    
    /**
     * Create a {@link ManagedDataSource} proxy for an existing data source.
     * This will create a dynamic proxy implementing the same interfaces as
     * the original data source.
     * 
     * @param ds the original data source
     * @return a data source proxy implementing {@link ManagedDataSource}
     */
    public static ManagedDataSource create(DataSource ds) {
        Class<?>[] orgIfaces = ds.getClass().getInterfaces();
        Class<?>[] ifaces = new Class[orgIfaces.length+1];
        ifaces[0] = ManagedDataSource.class;
        System.arraycopy(orgIfaces, 0, ifaces, 1, orgIfaces.length);
        return (ManagedDataSource)Proxy.newProxyInstance(
                ManagedDataSourceFactory.class.getClassLoader(), ifaces,
                new DataSourceManager(ds));
    }
}
