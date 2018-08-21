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

package org.wso2.carbon.core.test.caching;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.caching.impl.DataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastClusteringAgent;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

/**
 * Simple cluster member.
 * Used only for testing Hazelcast based cache invalidation.
 *
 */
public class SimpleMember {

    private static final Log log = LogFactory.getLog(ClusterLauncher.class);

    private static final String CACHE_NAME = "TestSimpleMemberCache";
    private static final String CACHE_MGR_NAME = CACHE_NAME + "Manager";
    private CacheManager cacheManager;
    private Random random;
    private DataHolder dataHolder;
    private HazelcastClusteringAgent hazelcastClusteringAgent;
    private long addedEntries;
    private long removedEntries;

    public static void main(String[] args) throws URISyntaxException, ClusteringFault {

        log.debug("Starting Simple Memner for test");
        setCarbonHome();

        SimpleMember simpleMember = new SimpleMember();
        simpleMember.init();
        final AtomicBoolean exit = new AtomicBoolean(false);
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                exit.set(true);
            }
        };
        timer.schedule(timerTask, 10000);
        simpleMember.performTest(0, 10000);
        simpleMember.shutDown();
        timer.cancel();
    }

    private static void setCarbonHome() {

        URL u = SimpleMember.class.getResource("/carbon_home/test.txt");
        String path = u.getPath();
        Path p = Paths.get(path);

        System.setProperty(CarbonBaseConstants.CARBON_HOME, p.getParent().toString());
    }

    /**
     * Creates records in the cache.
     * Called reflectively.
     * @param totalRecords
     */
    public void createRecords(int totalRecords) {

        simulateAddition(0, totalRecords);
    }

    /**
     * Deleted records in the cache within the given range.
     * Called reflectively.
     */
    public void deleteRecords(int start, int end) {

        simulateRemoval(start, end);
    }

    public void init() throws ClusteringFault {

        setCarbonHome();
        hazelcastClusteringAgent = new HazelcastClusteringAgent();
        hazelcastClusteringAgent.setConfigurationContext(new ConfigurationContext(new AxisConfiguration()));
        hazelcastClusteringAgent.init();

        dataHolder.getInstance().setDistributedMapProvider(hazelcastClusteringAgent.getDistributedMapProvider());
        dataHolder.getInstance().setClusteringAgent(hazelcastClusteringAgent);
        withTenant(() -> {
            cacheManager = getCacheManager();
        });

        random = new Random();
    }

    private CacheManager getCacheManager() {

        return dataHolder.getInstance().getCachingProvider().getCacheManagerFactory()
                .getCacheManager(CACHE_MGR_NAME);
    }

    public void shutDown() throws ClusteringFault {

        withTenant(() -> {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            cache.removeAll();
            CacheManager cacheManager = getCacheManager();
            cacheManager.removeCache(CACHE_NAME);
            Caching.getCacheManagerFactory().close();
        });
        hazelcastClusteringAgent.shutdown();
    }

    public boolean isCacheEmpty() {

        AtomicBoolean result = new AtomicBoolean(false);
        withTenant(() -> {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            result.set(!cache.keys().hasNext());
        });
        return result.get();
    }

    private void performTest(int start, int end) {

        simulateAddition(start, end);
        simulateRemoval(start, end);

        log.info("Created : " + addedEntries + " , Removed : " + removedEntries);
    }

    private void simulateAddition(int start, int end) {

        withTenant(() -> {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            for (int i = start; i < end; i++) {
                String key = "Key1_" + i;
                byte[] value = new byte[1000];
                cache.put(key, value);
                addedEntries++;
            }
        });
    }

    private void simulateRemoval(int start, int end) {

        withTenant(() -> {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            for (int i = start; i < end; i++) {
                String key = "Key1_" + i;
                cache.remove(key);
                removedEntries++;
            }
        });
    }

    private <T> void withTenant(WrapperFn fn) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_NAME);
        try {
            fn.accept();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private @FunctionalInterface
    interface WrapperFn {

        void accept();
    }
}
