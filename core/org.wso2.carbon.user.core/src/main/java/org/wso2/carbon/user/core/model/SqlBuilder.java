/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.user.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlBuilder {

    private List<String> wheres = new ArrayList<>();
    private StringBuilder sql;
    private StringBuilder tail;
    private int count = 1;
    private Map<Integer, Integer> integerParameters = new HashMap<>();
    private Map<Integer, String> stringParameters = new HashMap<>();
    private Map<Integer, Long> longParameters = new HashMap<>();

    public SqlBuilder(StringBuilder sql) {

        this.sql = sql;
    }

    private void appendList(StringBuilder sql, List<String> list) {

        boolean first = true;
        for (String s : list) {
            if (first) {
                sql.append(" WHERE ");
            } else {
                sql.append(" AND ");
            }
            sql.append(s);
            first = false;
        }
    }

    public String getQuery() {

        appendList(sql, wheres);
        wheres = new ArrayList<>();
        return sql.toString()+ tail;
    }

    public SqlBuilder where(String expr, String value) {

        wheres.add(expr);
        stringParameters.put(count, value);
        count++;
        return this;
    }

    public SqlBuilder where(String expr, int value) {

        wheres.add(expr);
        integerParameters.put(count, value);
        count++;
        return this;
    }

    public SqlBuilder where(String expr, long value) {

        wheres.add(expr);
        longParameters.put(count, value);
        count++;
        return this;
    }

    public Map<Integer, Integer> getIntegerParameters() {

        return integerParameters;
    }

    public Map<Integer, String> getStringParameters() {

        return stringParameters;
    }

    public Map<Integer, Long> getLongParameters() {

        return longParameters;
    }

    public void setTail(String tail, Integer... placeHolders) {

        if (this.tail == null) {
            this.tail = new StringBuilder(tail);
        } else {
            this.tail.append(tail);
        }

        for (int value : placeHolders) {
            integerParameters.put(count, value);
            count++;
        }
    }

    public void setTail(String tail, String... placeHolders) {

        if (this.tail == null) {
            this.tail = new StringBuilder(tail);
        } else {
            this.tail.append(tail);
        }

        for (String value : placeHolders) {
            stringParameters.put(count, value);
            count++;
        }
    }

    public void setTail(String tail, Long... placeHolders) {

        if (this.tail == null) {
            this.tail = new StringBuilder(tail);
        } else {
            this.tail.append(tail);
        }

        for (long value : placeHolders) {
            longParameters.put(count, value);
            count++;
        }
    }
}
