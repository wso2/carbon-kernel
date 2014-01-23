/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.core.deployment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This task will periodically run & update the artifacts in the local file system from the Registry
 */
public class RegistryBasedRepositoryUpdater {
    private static final Log log = LogFactory.getLog(RegistryBasedRepositoryUpdater.class);

    private static final ScheduledThreadPoolExecutor exec =
            (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(20);
    private static final Map<String, ScheduledFuture> futures =
            new ConcurrentHashMap<String, ScheduledFuture>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                exec.shutdownNow();
            }
        });
    }

    /**
     * Schedule registry based repo update task
     *
     * @param userRegistry   The relevant user registry to fetch the repo from
     * @param registryPath   The path in the <code>userRegistry</code> to fetch the repo from
     * @param fileSystemRepo The path in the file system in which the fetched data will be placed
     * @param startSecs      Starting time of the task in seconds
     * @param periodSecs     Period of the task in seconds
     */
    public static void scheduleAtFixedRate(UserRegistry userRegistry,
                                           String registryPath,
                                           String fileSystemRepo,
                                           long startSecs,
                                           long periodSecs) {
        ScheduledFuture<?> scheduledFuture =
                exec.scheduleAtFixedRate(new UpdaterTask(userRegistry, registryPath, fileSystemRepo),
                                         startSecs, periodSecs, TimeUnit.SECONDS);
        futures.put(fileSystemRepo, scheduledFuture);
    }

    private RegistryBasedRepositoryUpdater() {

    }

    /**
     * Cleanup
     */
    public static void cleanup() {
        exec.shutdownNow();
    }

    /**
     * Cancel a particular task which updates a repo
     *
     * @param fileSystemRepo The location in the file system
     */
    public static void cancelTask(String fileSystemRepo) {
        ScheduledFuture scheduledFuture = futures.get(fileSystemRepo);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            exec.purge();
            futures.remove(fileSystemRepo);
        }
    }

    private static class UpdaterTask implements Runnable {
        private RegistryBasedRepository registryBasedRepository;

        public UpdaterTask(UserRegistry userRegistry,
                           String registryPath,
                           String fileSystemRepo) {
            registryBasedRepository = new RegistryBasedRepository(userRegistry,
                                                                  registryPath,
                                                                  fileSystemRepo);
        }

        public void run() {
            try {
                registryBasedRepository.updateFileSystemFromRegistry();
            } catch (Throwable e) {
                log.error("Cannot load repository from registry", e);
            }
        }
    }
}
