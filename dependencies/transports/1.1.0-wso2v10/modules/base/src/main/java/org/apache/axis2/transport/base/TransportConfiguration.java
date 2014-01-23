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

package org.apache.axis2.transport.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

/**
 * 
 */
public class TransportConfiguration {

    // defaults
    private static final int WORKERS_CORE_THREADS  = 20;
    private static final int WORKERS_MAX_THREADS   = 100;
    private static final int WORKER_KEEP_ALIVE     = 5;
    private static final int BLOCKING_QUEUE_LENGTH = -1;

    // server listener
    private static final String S_T_CORE     = "snd_t_core";
    private static final String S_T_MAX      = "snd_t_max";
    private static final String S_T_ALIVE    = "snd_alive_sec";
    private static final String S_T_QLEN     = "snd_qlen";

    // client sender
    private static final String C_T_CORE     = "lst_t_core";
    private static final String C_T_MAX      = "lst_t_max";
    private static final String C_T_ALIVE    = "lst_alive_sec";
    private static final String C_T_QLEN     = "lst_qlen";

    private static final Log log = LogFactory.getLog(TransportConfiguration.class);
    private static Map<String, TransportConfiguration> _configurations
            = new HashMap<String, TransportConfiguration>();
    private Properties props;

    private TransportConfiguration(String transportName) {
        try {
            props = BaseUtils.loadProperties(transportName + ".properties");
        } catch (Exception ignore) {}
    }

    public static TransportConfiguration getConfiguration(String transportName) {
        if (_configurations.containsKey(transportName)) {
            return _configurations.get(transportName);
        } else {
            TransportConfiguration config = new TransportConfiguration(transportName);
            _configurations.put(transportName, config);
            return config;
        }
    }

    public int getServerCoreThreads() {
        return getProperty(S_T_CORE, WORKERS_CORE_THREADS);
    }

    public int getServerMaxThreads() {
        return getProperty(S_T_MAX, WORKERS_MAX_THREADS);
    }

    public int getServerKeepalive() {
        return getProperty(S_T_ALIVE, WORKER_KEEP_ALIVE);
    }

    public int getServerQueueLen() {
        return getProperty(S_T_QLEN, BLOCKING_QUEUE_LENGTH);
    }

    public int getClientCoreThreads() {
        return getProperty(C_T_CORE, WORKERS_CORE_THREADS);
    }

    public int getClientMaxThreads() {
        return getProperty(C_T_MAX, WORKERS_MAX_THREADS);
    }

    public int getClientKeepalive() {
        return getProperty(C_T_ALIVE, WORKER_KEEP_ALIVE);
    }

    public int getClientQueueLen() {
        return getProperty(C_T_QLEN, BLOCKING_QUEUE_LENGTH);
    }

    /**
     * Get properties that tune nhttp transport. Preference to system properties
     * @param name name of the system/config property
     * @param def default value to return if the property is not set
     * @return the value of the property to be used
     */
    public int getProperty(String name, int def) {
        String val = System.getProperty(name);
        if (val == null) {
            val = props.getProperty(name);
        }

        if (val != null && Integer.valueOf(val) > 0) {
            if (log.isDebugEnabled()) {
                log.debug("Using transport tuning parameter : " + name + " = " + val);
            }
            return Integer.valueOf(val);
        }
        return def;
    }

    /**
     * Get properties that tune nhttp transport. Preference to system properties
     * @param name name of the system/config property
     * @param def default value to return if the property is not set
     * @return the value of the property to be used
     */
    public boolean getBooleanValue(String name, boolean def) {
        String val = System.getProperty(name);
        if (val == null) {
            val = props.getProperty(name);
        }

        if (val != null && Boolean.parseBoolean(val)) {
            if (log.isDebugEnabled()) {
                log.debug("Using transport tuning parameter : " + name);
            }
            return true;
        }
        return def;
    }
}
