/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.tomcat.ext.transport.statistics;


import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Container to keep transport statistics coming from Tomcat layer in a queue.
 * Transport statistics persistence logic can use this global queue to get transport statistics entries.
 */
public final class TransportStatisticsContainer {

    private TransportStatisticsContainer() {
    }

    private static final ConcurrentLinkedQueue<TransportStatisticsEntry> transportStatistics =
            new ConcurrentLinkedQueue<TransportStatisticsEntry>();

    /**
     * Add transport statistics entry to queue.
     * @param entry  transport statistics containing request, response sizes and request url.
     */
    public static void addTransportStatisticsEntry(TransportStatisticsEntry entry){
        transportStatistics.add(entry);
    }

    /**
     * Get the queue which holds transport statistics.
     * @return concurrent linked queue holding transport statistics entries.
     */
    public static Queue<TransportStatisticsEntry> getTransportStatistics(){
        return transportStatistics;
    }


}
