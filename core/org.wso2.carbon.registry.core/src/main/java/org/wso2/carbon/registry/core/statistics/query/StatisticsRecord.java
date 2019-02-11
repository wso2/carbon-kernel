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
package org.wso2.carbon.registry.core.statistics.query;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Defines a statistics record for a set of database queries for a given operation.
 */
public class StatisticsRecord {
    private AtomicInteger nestedDepth = new AtomicInteger();
    private Collection<String> records = null;
    private List<String> queries = new LinkedList<String>();
    private String operation = null;

    /**
     * Creates a statistics record.
     */
    public StatisticsRecord() {
        init();
    }

    private void init() {
        if (Boolean.toString(true).equals(
                System.getProperty("carbon.registry.statistics.preserve.duplicate.table." +
                        "accesses"))) {
            records = new LinkedList<String>();
        } else {
            records = new LinkedHashSet<String>();
        }
    }

    /**
     * Creates a clone of an existing statistics record.
     *
     * @param record the existing record.
     */
    public StatisticsRecord(StatisticsRecord record) {
        init();
        this.nestedDepth.set(record.nestedDepth.get());
        this.records.addAll(record.records);
        this.queries.addAll(record.queries);
        this.operation = record.operation;
    }

    /**
     * Increments the nested depth of the transaction.
     *
     * @return current depth.
     */
    public int increment() {
        return nestedDepth.getAndIncrement();
    }

    /**
     * Decrements the nested depth of the transaction.
     *
     * @return current depth.
     */
    public int decrement() {
        return nestedDepth.decrementAndGet();
    }

    /**
     * Returns the list of records on database tables accessed.
     *
     * @return list of records on database tables accessed.
     */
    public String[] getTableRecords() {
        return records.toArray(new String[records.size()]);
    }

    /**
     * Adds a record on database tables accessed.
     *
     * @param record a record on database tables accessed.
     */
    public void addRecord(String record) {
        records.add(record);
    }

    /**
     * Method to obtain the current list of queries.
     *
     * @return the current list of queries.
     */
    public String[] getQueries() {
        return queries.toArray(new String[queries.size()]);
    }

    /**
     * Method to add a query to the current list of queries.
     *
     * @param query the query.
     */
    public void addQuery(String query) {
        queries.add(query);
    }

    /**
     * Method to get the operation.
     *
     * @return the registry operation's name.
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Method to set the operation.
     *
     * @param operation the registry operation's name.
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }
}
