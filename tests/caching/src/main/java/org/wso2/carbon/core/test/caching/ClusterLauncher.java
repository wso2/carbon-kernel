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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastClusteringAgent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.cache.CacheException;

/**
 * Create sample cluster and start the test.
 * Uses isolated classloader so that the Hazelcast cluster can be simulated in single JVM.
 * Uses reflection to call the real Simple Cluster Member class, as the cluster runs on its own isolated class-loader.
 */
public class ClusterLauncher {

    private static final Log log = LogFactory.getLog(ClusterLauncher.class);
    private static final int CLUSTER_NODE_COUNT = 3;
    private static final int UNIQUE_ENTRIES_PER_NODE = 4000;
    private static final int TOTAL_ENTRIES_ON_CLUSTER = CLUSTER_NODE_COUNT * UNIQUE_ENTRIES_PER_NODE;
    private static final int WAIT_FOR_CLEANUP_RETRIES = 50;
    private static final int THREAD_YIELD_SLEEP_TIME_MILLIS = 200;

    public static void main(String[] args) throws IOException, InterruptedException {

        ClusterLauncher clusterLauncher = new ClusterLauncher();
        clusterLauncher.runAndTest();
    }

    public boolean runAndTest() throws InterruptedException {

        Executor executor = Executors.newFixedThreadPool(CLUSTER_NODE_COUNT);
        List<Future> futureList = new ArrayList<>();

        List<MemberRunner> memberRunners = new ArrayList<>();
        for (int i = 0; i < CLUSTER_NODE_COUNT; i++) {
            MemberRunner memberRunner = new MemberRunner(i);
            memberRunner.init();
            memberRunners.add(memberRunner);
        }

        for (MemberRunner memberRunner : memberRunners) {
            memberRunner.createRecords();
        }

        long startTime = System.currentTimeMillis();
        for (MemberRunner memberRunner : memberRunners) {
            Future future = ((ExecutorService) executor).submit(() -> memberRunner.deleteRecords());
            futureList.add(future);
        }

        for (Future future : futureList) {
            if (!future.isDone()) {
                Thread.sleep(THREAD_YIELD_SLEEP_TIME_MILLIS);
            }
        }

        for (MemberRunner memberRunner : memberRunners) {
            for (int i = 0; i < WAIT_FOR_CLEANUP_RETRIES; i++) {
                boolean isEmpty = memberRunner.isCacheEmpty();

                if (!isEmpty) {
                    log.debug("Member " + memberRunner.noderIndex + ", Check Attempt: " + i + " cache empty? : " + isEmpty);
                    Thread.sleep(THREAD_YIELD_SLEEP_TIME_MILLIS);
                }
            }
        }

        long finishedTime = System.currentTimeMillis();
        long timeTaken = finishedTime - startTime;

        ((ExecutorService) executor).shutdown();
        ((ExecutorService) executor).awaitTermination(10, TimeUnit.SECONDS);

        boolean hasAllEntriesClear = true;
        for (MemberRunner memberRunner : memberRunners) {
            boolean isEmpty = memberRunner.isCacheEmpty();
            log.debug("Member " + memberRunner.noderIndex + " Cache empty? " + isEmpty);
            hasAllEntriesClear &= isEmpty;
        }

        for (MemberRunner memberRunner : memberRunners) {
            memberRunner.shutdown();
        }

        log.debug("Total time taken : " + timeTaken + " ms to process " + TOTAL_ENTRIES_ON_CLUSTER + " records.");
        log.info("Cache Invalidation throughput " + (TOTAL_ENTRIES_ON_CLUSTER / timeTaken) * 1000 + " requests/s");
        return hasAllEntriesClear;
    }

    /**
     * Runner, Simulates a seperate JVM, by isolating the classloader.
     */
    private static class MemberRunner {

        private int noderIndex = 0;

        private Class refletedClass;
        private Object member;
        private ClassLoader classLoader;
        private static final String CLASS_NAME = "org.wso2.carbon.core.test.caching.SimpleMember";

        public MemberRunner(int noderIndex) {

            this.noderIndex = noderIndex;
        }

        public void init() throws CacheException {

            URLClassLoader currentClassloader = (URLClassLoader) ClusterLauncher.class.getClassLoader();
            URLClassLoader isolatedClassLoader = new URLClassLoader(currentClassloader.getURLs(),
                    ClassLoader.getSystemClassLoader().getParent());
            classLoader = isolatedClassLoader;
            try {
                refletedClass = classLoader.loadClass(CLASS_NAME);
                member = refletedClass.newInstance();
                invokeIsolated("init", new Class[0], null);
            } catch (ClassNotFoundException e) {
                throw new CacheException("Could not load class : " + CLASS_NAME, e);
            } catch (IllegalAccessException e) {
                throw new CacheException("Could note access member of the class: " + CLASS_NAME, e);
            } catch (InstantiationException e) {
                throw new CacheException("Could note instantiate class " + CLASS_NAME, e);
            }
        }

        public void createRecords() {

            Class[] types = {Integer.TYPE};
            Object[] params = new Object[1];
            params[0] = TOTAL_ENTRIES_ON_CLUSTER;
            invokeIsolated("createRecords", types, params);
        }

        public void deleteRecords() {

            Class[] types = {Integer.TYPE, Integer.TYPE};
            Object[] params = new Object[2];
            params[0] = noderIndex * UNIQUE_ENTRIES_PER_NODE;
            params[1] = ((noderIndex + 1) * UNIQUE_ENTRIES_PER_NODE) - 1;
            invokeIsolated("deleteRecords", types, params);
        }

        public void shutdown() {

            invokeIsolated("shutDown", null, null);
        }

        public boolean isCacheEmpty() {

            return invokeIsolated("isCacheEmpty", null, null);
        }

        /**
         * Invokes a method reflectively on isolated classloader created for cluster node.
         *
         * @param methodName
         * @param types
         * @param params
         */
        private <R> R invokeIsolated(String methodName, Class[] types, Object[] params) {

            ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(classLoader);
                Method m;
                if (types == null || types.length == 0) {
                    m = refletedClass.getMethod(methodName);
                } else {
                    m = refletedClass.getMethod(methodName, types);
                }
                if (params == null || params.length == 0) {
                    return (R) m.invoke(member);
                } else {
                    return (R) m.invoke(member, params);
                }
            } catch (NoSuchMethodException e) {
                throw new CacheException("Could not find method : " + methodName + " on class : " + CLASS_NAME, e);
            } catch (IllegalAccessException e) {
                throw new CacheException("Could not access method : " + methodName + " on class : " + CLASS_NAME, e);
            } catch (InvocationTargetException e) {
                throw new CacheException("Could not invoke method : " + methodName + " on class : " + CLASS_NAME, e);
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassloader);
            }
        }
    }
}
