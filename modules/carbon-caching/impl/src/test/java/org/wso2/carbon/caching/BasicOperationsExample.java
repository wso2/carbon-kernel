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
package org.wso2.carbon.caching;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * Basic example
 * Configures a cache with access expiry of 10 secs.
 *
 */
public class BasicOperationsExample  extends AbstractApp {


    public static void main(String[] args) throws InterruptedException {
        new BasicOperationsExample().runApp();
    }

    public void runApp()
            throws InterruptedException {

        //first thin is we need to initialize the cache Manager
        final CacheManager cacheManager = initCacheManager();

        //create a cache with the provided name
        final Cache<String, Integer> cache = initCache("theCache", cacheManager);

        //lets populate the content
        populateCache(cache);

        //so we print the content whatever we have
        printContent(cache);

        //lets wait for 10 sec to expire the content
        sleepFor(10 * 1000);

        //and print the content again, and see everything has expired and values are null
        printContent(cache);

        //lastly shutdown the cache manager
        shutdown();
    }
}
