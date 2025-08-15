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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlBuilder {

    private static final String START_PARENTHESES = "(";
    private static final String CLOSE_PARENTHESES = ")";

    private List<String> wheres = new ArrayList<>();
    private StringBuilder sql;
    private StringBuilder tail;
    private int count = 1;
    private Map<Integer, Integer> integerParameters = new HashMap<>();
    private Map<Integer, String> stringParameters = new HashMap<>();
    private Map<Integer, Long> longParameters = new HashMap<>();
    private Map<Integer, Timestamp> timestampParameters = new HashMap<>();
    private final List<Integer> attrValueIndexes = new ArrayList<>();
    private boolean addedWhereStatement = false;

    public SqlBuilder(StringBuilder sql) {

        this.sql = sql;
    }

    private void appendList(StringBuilder sql, List<String> list) {

        for (String s : list) {
            if (addedWhereStatement) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
                this.addedWhereStatement = true;
            }
            sql.append(s);
        }
    }

    public String getQuery() {

        appendList(sql, wheres);
        wheres = new ArrayList<>();
        this.addedWhereStatement = false;
        if (tail != null) {
            return buildExecutableSQLStatement(new StringBuilder(sql.toString() + tail));
        } else {
            return buildExecutableSQLStatement(new StringBuilder(sql.toString()));
        }
    }

    public SqlBuilder where(String expr, String value) {

        wheres.add(expr);
        if (expr.contains(UserCoreConstants.UM_ATTRIBUTE_COLUMN)) {
            // If the expression contains UM_ATTRIBUTE_COLUMN, we need to track the index of this value
            // for later use in the SQL statement.
            attrValueIndexes.add(count);
        }
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

    public SqlBuilder where(String expr, Timestamp value) {

        wheres.add(expr);
        timestampParameters.put(count, value);
        count++;
        return this;
    }

    public List<Integer> getAttributeValueIndexes() {

        return attrValueIndexes;
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

    public Map<Integer, Timestamp> getTimestampParameters() {

        return timestampParameters;
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

    public void setTail(String tail, Timestamp... placeHolders) {

        if (this.tail == null) {
            this.tail = new StringBuilder(tail);
        } else {
            this.tail.append(tail);
        }

        for (Timestamp value : placeHolders) {
            timestampParameters.put(count, value);
            count++;
        }
    }

    /**
     * Get sql statement part only.
     *
     * @return SQL string.
     */
    public String getSql() {

        return sql.toString();
    }

    public List<String> getWheres() {

        return wheres;
    }

    public void updateSql(String append) {

        appendList(sql, wheres);
        wheres = new ArrayList<>();
        this.addedWhereStatement = false;
        this.sql.append(append);
    }

    public void updateSqlWithOROperation(String expr, Object value) {

        appendList(sql, wheres);
        wheres = new ArrayList<>();
        this.sql.append(" OR ").append(expr);
        if (value instanceof String) {
            stringParameters.put(count, String.valueOf(value));
        } else if (value instanceof Integer) {
            integerParameters.put(count, (Integer) value);
        } else if (value instanceof Long) {
            longParameters.put(count, (Long) value);
        } else if (value instanceof Timestamp) {
            timestampParameters.put(count, (Timestamp) value);
        }
        count++;
    }

    /**
     * This method to build an executable SQL statement.
     *
     * @param sqlQueryStringBuilder Final sql query string.
     */
    private String buildExecutableSQLStatement(StringBuilder sqlQueryStringBuilder) {

        // Check whether any parentheses which are not closed in the SQL statement if so close it.
        int startParenthesesCounts = StringUtils.countMatches(sqlQueryStringBuilder.toString(), START_PARENTHESES);
        int endParenthesesCounts = StringUtils.countMatches(sqlQueryStringBuilder.toString(), CLOSE_PARENTHESES);
        int needToBeCloseParenthesesCount = startParenthesesCounts - endParenthesesCounts;

        if (needToBeCloseParenthesesCount > 0) {
            for (int i = 0; i < needToBeCloseParenthesesCount; i++) {
                sqlQueryStringBuilder.append(CLOSE_PARENTHESES);
            }
        }
        return sqlQueryStringBuilder.toString();
    }

    /**
     * Retrieves all parameters currently stored in this SqlBuilder instance, ordered by their original 1-based index.
     *
     * @return A List of Objects representing the parameters in order.
     */
    public List<Object> getOrderedParameters() {

        int totalParametersCount = this.count - 1;
        if (totalParametersCount <= 0) {
            return new ArrayList<>();
        }

        Object[] parametersArray = new Object[totalParametersCount];

        // Populate the array using the 1-based indices from the maps.
        integerParameters.forEach((idx, val) -> parametersArray[idx - 1] = val);
        stringParameters.forEach((idx, val) -> parametersArray[idx - 1] = val);
        longParameters.forEach((idx, val) -> parametersArray[idx - 1] = val);
        timestampParameters.forEach((idx, val) -> parametersArray[idx - 1] = val);

        return new ArrayList<>(Arrays.asList(parametersArray));
    }

    /**
     * Appends a raw SQL string fragment (which may contain '?' placeholders) directly to this SqlBuilder instance's
     * current SQL string. It then registers the provided parameters in sequence, incrementing the main parameter count.
     *
     * @param fragment          The SQL fragment string to append.
     * @param paramsForFragment A List of Objects(String, Integer, Long) representing the parameters for the fragment,
     *                          in order.
     */
    public void appendParameterizedSqlFragment(String fragment, List<Object> paramsForFragment) {

        // Append pending WHERE clauses into the SQL string before appending the fragment.
        appendList(this.sql, this.wheres);
        this.wheres = new ArrayList<>();

        // Append the raw SQL fragment (should include leading space or keyword as needed).
        this.sql.append(fragment);

        // Register the parameters.
        if (paramsForFragment != null) {
            for (Object param : paramsForFragment) {
                if (param instanceof String) {
                    stringParameters.put(count, (String) param);
                } else if (param instanceof Integer) {
                    integerParameters.put(count, (Integer) param);
                } else if (param instanceof Long) {
                    longParameters.put(count, (Long) param);
                } else if (param instanceof Timestamp) {
                    timestampParameters.put(count, (Timestamp) param);
                }
                count++;
            }
        }
    }
}
