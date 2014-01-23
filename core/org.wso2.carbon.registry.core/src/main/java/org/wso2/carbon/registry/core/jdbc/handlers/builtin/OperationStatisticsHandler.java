/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.jdbc.handlers.builtin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.Filter;
import org.wso2.carbon.registry.core.statistics.StatisticsLog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This handler is used to record operation-level statistics related to a given server instance.
 */
public class OperationStatisticsHandler extends Handler {

    private static Log log = LogFactory.getLog(OperationStatisticsHandler.class);

    // The instance of the logger to be used to log statistics.
    private static Log statsLog = StatisticsLog.getLog();

    // Map of statistic records.
    private static Map<String, Long> records = null;

    // The executor service used to create threads to record operation statistics.
    private static ExecutorService executor = null;

    static {
        if (statsLog.isDebugEnabled()) {
            initializeStatisticsLogging();
        }
    }

    private static synchronized void initializeStatisticsLogging() {
        if (executor != null) {
            return;
        }
        records = new HashMap<String, Long>();
        executor = Executors.newCachedThreadPool();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run() {
                executor.shutdownNow();
            }
        });
        for (String s : new String[]{Filter.GET, Filter.PUT, Filter.IMPORT, Filter.MOVE,
                Filter.COPY, Filter.RENAME, Filter.DELETE, Filter.ADD_ASSOCIATION,
                Filter.REMOVE_ASSOCIATION, Filter.GET_ASSOCIATIONS, Filter.GET_ALL_ASSOCIATIONS,
                Filter.EXECUTE_QUERY, Filter.RESOURCE_EXISTS, Filter.DUMP, Filter.RESTORE}) {
            records.put(s, 0l);
        }
        final ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run() {
                scheduler.shutdownNow();
            }
        });
        Runnable runnable = new Runnable() {
            public void run() {
                if (records == null) {
                    log.error("Unable to store operation statistics.");
                } else {
                    synchronized (this) {
                        statsLog.debug("Total Number of get calls                : " +
                                records.get(Filter.GET));
                        statsLog.debug("Total Number of put calls                : " +
                                records.get(Filter.PUT));
                        statsLog.debug("Total Number of import calls             : " +
                                records.get(Filter.IMPORT));
                        statsLog.debug("Total Number of move calls               : " +
                                records.get(Filter.MOVE));
                        statsLog.debug("Total Number of copy calls               : " +
                                records.get(Filter.COPY));
                        statsLog.debug("Total Number of rename calls             : " +
                                records.get(Filter.RENAME));
                        statsLog.debug("Total Number of delete calls             : " +
                                records.get(Filter.DELETE));
                        statsLog.debug("Total Number of addAssociation calls     : " +
                                records.get(Filter.ADD_ASSOCIATION));
                        statsLog.debug("Total Number of removeAssociation calls  : " +
                                records.get(Filter.REMOVE_ASSOCIATION));
                        statsLog.debug("Total Number of getAssociations calls    : " +
                                records.get(Filter.GET_ASSOCIATIONS));
                        statsLog.debug("Total Number of getAllAssociations calls : " +
                                records.get(Filter.GET_ALL_ASSOCIATIONS));
                        statsLog.debug("Total Number of executeQuery calls       : " +
                                records.get(Filter.EXECUTE_QUERY));
                        statsLog.debug("Total Number of resourceExists calls     : " +
                                records.get(Filter.RESOURCE_EXISTS));
                        statsLog.debug("Total Number of dump calls               : " +
                                records.get(Filter.DUMP));
                        statsLog.debug("Total Number of restore calls            : " +
                                records.get(Filter.RESTORE));
                    }
                }
            }
        };
        scheduler.scheduleAtFixedRate(runnable, 60, 60, TimeUnit.SECONDS);
    }

    private void incrementRecord(final String operation) {
        Runnable runnable = new Runnable() {
            public void run() {
                if (records == null) {
                    log.error("Unable to store operation statistics.");
                } else {
                    synchronized (this) {
                        records.put(operation, records.get(operation) + 1);
                    }
                }
            }
        };
        if (executor != null) {
            executor.execute(runnable);
        } else {
            initializeStatisticsLogging();
            executor.execute(runnable);
        }
    }

    public Resource get(RequestContext requestContext) throws RegistryException {
        if (statsLog.isDebugEnabled()) {
            incrementRecord(Filter.GET);
        }
        return super.get(requestContext);
    }

    public void put(RequestContext requestContext) throws RegistryException {
        if (statsLog.isDebugEnabled()) {
            incrementRecord(Filter.PUT);
        }
        super.put(requestContext);
    }

    public void importResource(RequestContext requestContext) throws RegistryException {
        if (statsLog.isDebugEnabled()) {
            incrementRecord(Filter.IMPORT);
        }
        super.importResource(requestContext);
    }

    public String move(RequestContext requestContext) throws RegistryException {
        if (statsLog.isDebugEnabled()) {
            incrementRecord(Filter.MOVE);
        }
        return super.move(requestContext);
    }

    public String copy(RequestContext requestContext) throws RegistryException {
        if (statsLog.isDebugEnabled()) {
            incrementRecord(Filter.COPY);
        }
        return super.copy(requestContext);
    }

    public String rename(RequestContext requestContext) throws RegistryException {
        if (statsLog.isDebugEnabled()) {
            incrementRecord(Filter.RENAME);
        }
        return super.rename(requestContext);
    }

    public void delete(RequestContext requestContext) throws RegistryException {
        if (statsLog.isDebugEnabled()) {
            incrementRecord(Filter.DELETE);
        }
        super.delete(requestContext);
    }

    public void addAssociation(RequestContext requestContext) throws RegistryException {
        if (statsLog.isDebugEnabled()) {
            incrementRecord(Filter.ADD_ASSOCIATION);
        }
        super.addAssociation(requestContext);
    }

    public void removeAssociation(RequestContext requestContext) throws RegistryException {
        if (statsLog.isDebugEnabled()) {
            incrementRecord(Filter.REMOVE_ASSOCIATION);
        }
        super.removeAssociation(requestContext);
    }

    public Association[] getAllAssociations(RequestContext requestContext)
            throws RegistryException {
        if (statsLog.isDebugEnabled()) {
            incrementRecord(Filter.GET_ALL_ASSOCIATIONS);
        }
        return super.getAllAssociations(requestContext);
    }

    public Association[] getAssociations(RequestContext requestContext) throws RegistryException {
        if (statsLog.isDebugEnabled()) {
            incrementRecord(Filter.GET_ASSOCIATIONS);
        }
        return super.getAssociations(requestContext);
    }

    public Collection executeQuery(RequestContext requestContext) throws RegistryException {
        if (statsLog.isDebugEnabled()) {
            incrementRecord(Filter.EXECUTE_QUERY);
        }
        return super.executeQuery(requestContext);
    }

    public boolean resourceExists(RequestContext requestContext) throws RegistryException {
        if (statsLog.isDebugEnabled()) {
            incrementRecord(Filter.RESOURCE_EXISTS);
        }
        return super.resourceExists(requestContext);
    }

    public void dump(RequestContext requestContext) throws RegistryException {
        if (statsLog.isDebugEnabled()) {
            incrementRecord(Filter.DUMP);
        }
        super.dump(requestContext);
    }

    public void restore(RequestContext requestContext) throws RegistryException {
        if (statsLog.isDebugEnabled()) {
            incrementRecord(Filter.RESTORE);
        }
        super.restore(requestContext);
    }
}
