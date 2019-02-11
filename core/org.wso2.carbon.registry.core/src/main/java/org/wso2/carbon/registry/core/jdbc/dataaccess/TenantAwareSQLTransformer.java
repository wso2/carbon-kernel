/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.core.jdbc.dataaccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.StringTokenizer;

/**
 * class to transform the sql to be tenant-aware. Once the query is passed to the constructor this
 * will be keeping the transformed query.
 */
public class TenantAwareSQLTransformer {

    private static final Log log = LogFactory.getLog(TenantAwareSQLTransformer.class);
    private String transformedQuery = null;
    private int parameterCount = 0;
    private int trailingParameterCount = 0;

    /**
     * Construct the TenantAwareSQLTransformer for a given query
     *
     * @param sqlQuery the query to transform to tenant aware sql
     *
     * @throws RegistryException throws if the transformation failed.
     */
    public TenantAwareSQLTransformer(String sqlQuery) throws RegistryException {
        //parse SQL for possible injected SQLs
        try{
            sanityCheckSQL(sqlQuery);
        }catch (RegistryException e){
            throw new RegistryException("SQL query provided failed validity check. Reason for failure is : "+
                    e.getMessage() + ".SQL Query received is : "+sqlQuery );
        }

        String sqlQueryUCase = sqlQuery.toUpperCase();
        parameterCount = 0;
        int endOfFromIndex = sqlQueryUCase.indexOf("FROM");
        if (endOfFromIndex == -1) {
            String msg = "Error in parsing the query. You should have a 'FROM' token in your " +
                    "custom query";
            log.error(msg);
            throw new RegistryException(msg);
        }
        endOfFromIndex += 4;
        int startOfWhereIndex = sqlQueryUCase.indexOf("WHERE");
        int startOfThirdClauseIndex = -1;
        for (String s : new String[] {"GROUP BY", "HAVING", "ORDER BY", "LIMIT"}) {
            int index = sqlQueryUCase.indexOf(s);
            if (index > 0 && (startOfThirdClauseIndex == -1 || index < startOfThirdClauseIndex)) {
                startOfThirdClauseIndex = index;
            }
        }
        boolean whereNotFound = false;
        if (startOfWhereIndex == -1) {
            // no 'WHERE'
            whereNotFound = true;
            startOfWhereIndex = sqlQueryUCase.length();
            if (startOfThirdClauseIndex != -1) {
                startOfWhereIndex = startOfThirdClauseIndex;
            }
        }
        String fromPart = sqlQuery.substring(endOfFromIndex + 1, startOfWhereIndex);
        StringTokenizer tokenizer = new StringTokenizer(fromPart, ",");

        String additionalWherePart;
        StringBuilder sb = new StringBuilder();
        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            token = token.trim();
            token = token.replaceAll("[\t\r\n]+", " ");
            int separator = token.indexOf(' ');
            String firstPart;
            String secondPart;
            if (separator == -1) {
                firstPart = token;
                secondPart = null;
            } else {
                firstPart = token.substring(0, separator);
                secondPart = token.substring(separator + 1);
                firstPart = firstPart.trim();
                secondPart = secondPart.trim();
            }
            // now the first part contains the table name
            if (secondPart == null) {
                if (sb.length() < 1) {
                    sb.append(firstPart).append(".REG_TENANT_ID=?");
                } else {
                    sb.append(" AND ").append(firstPart).append(".REG_TENANT_ID=?");
                }
            } else {
                if (sb.length() < 1) {
                    sb.append(secondPart).append(".REG_TENANT_ID=?");
                } else {
                    sb.append(" AND ").append(secondPart).append(".REG_TENANT_ID=?");
                }
            }
            parameterCount++;
        }
        additionalWherePart = sb.toString();
//        if (whereNotFound) {
//            if (startOfThirdClauseIndex == -1) {
//                transformedQuery = sqlQuery + " WHERE " + additionalWherePart;
//            } else {
//                String[] parts = sqlQuery.substring(startOfThirdClauseIndex).split("[?]");
//                if (parts != null && parts.length > 1) {
//                    trailingParameterCount += parts.length - 1;
//                    if (sqlQuery.substring(startOfThirdClauseIndex).endsWith("?")) {
//                        trailingParameterCount += 1;
//                    }
//                }
//                transformedQuery = sqlQuery.substring(0, startOfThirdClauseIndex) + " WHERE "
//                        + additionalWherePart + " " + sqlQuery.substring(startOfThirdClauseIndex);
//            }
//        } else {
//            int endOfWhereIndex = startOfWhereIndex + 5;
//            if (startOfThirdClauseIndex == -1) {
//                transformedQuery = sqlQuery.substring(0, endOfWhereIndex) + " (" +
//                        sqlQuery.substring(endOfWhereIndex) + ") " + " AND "
//                        + additionalWherePart;
//            } else {
//                String[] parts = sqlQuery.substring(startOfThirdClauseIndex).split("[?]");
//                if (parts != null && parts.length > 1) {
//                    trailingParameterCount += parts.length - 1;
//                    if (sqlQuery.substring(startOfThirdClauseIndex).endsWith("?")) {
//                        trailingParameterCount += 1;
//                    }
//                }
//                transformedQuery = sqlQuery.substring(0, endOfWhereIndex) + " (" +
//                        sqlQuery.substring(endOfWhereIndex, startOfThirdClauseIndex) + ") " + " AND "
//                        + additionalWherePart + " " + sqlQuery.substring(startOfThirdClauseIndex);
//            }
//        }
        if (whereNotFound) {
            if (startOfThirdClauseIndex == -1) {
                transformedQuery = sqlQuery + " WHERE " + additionalWherePart;
            } else {
                String[] parts = sqlQuery.substring(startOfThirdClauseIndex).split("[?]");
                if (parts != null && parts.length > 1) {
                    trailingParameterCount += parts.length - 1;
                    if (sqlQuery.substring(startOfThirdClauseIndex).endsWith("?")) {
                        trailingParameterCount += 1;
                    }
                }
                transformedQuery = sqlQuery.substring(0, startOfThirdClauseIndex) + " WHERE "
                        + additionalWherePart + " " + sqlQuery.substring(startOfThirdClauseIndex);
            }
        } else {
            int endOfWhereIndex = startOfWhereIndex + 5;
            transformedQuery = sqlQuery.substring(0, endOfWhereIndex) + " (" + additionalWherePart +
                    ") AND " + sqlQuery.substring(endOfWhereIndex);
        }
    }

    /**
     * Parse sqlQuery for possible malicious injections
     * @param sqlQuery
     * @return
     */
    private void sanityCheckSQL(String sqlQuery) throws RegistryException{
        //size check
        if(sqlQuery.trim().length() == 0){
            throw new RegistryException("SQL String is empty");
        }

        String sqlQueryUCase = sqlQuery.toUpperCase();
        //constants for search criteria
        //M : MySQL
        //S : SQL Server
        //O : Oracle
        //P : PostgreSQL
        //G : Global (applies to app databases)

        //comments
        final String MS_COMMENT_START = "/*";
        final String MS_COMMENT_END = "*/";
        final String MS_COMMENT1_START = "--";

        //riskier functions
        final String MO_FUNC_LOAD_FILE = "LOAD_FILE";

        //keywords and symbols
        final String G_SQL_START = "SELECT";
        final String G_SEMICOLON = ";";
        final String G_DOUBLE_SLASH = "//";
        final String G_HASH = "#";

        //All queries should start with SELECT. We do not allow EXEC,CALL
        if (! sqlQueryUCase.startsWith(G_SQL_START)){
            throw new RegistryException("SQL query does not start with "+G_SQL_START);
        }

        for (String s : new String[] {MS_COMMENT_START, MS_COMMENT_END, MS_COMMENT1_START,
                MO_FUNC_LOAD_FILE,G_SEMICOLON, G_DOUBLE_SLASH,G_HASH}) {
            int index = sqlQueryUCase.indexOf(s);
            if (index > 0 ){
                throw new RegistryException("SQL query contains "+s);
            }
        }
    }

    /**
     * Get the transformed query.
     *
     * @return the transformed query.
     */
    String getTransformedQuery() {
        return transformedQuery;
    }

    /**
     * Get additional parameter count. The additional number of tenant id to be set.
     *
     * @return the additional parameter count.
     */
    int getAdditionalParameterCount() {
        return parameterCount;
    }

    /**
     * Get trailing parameter count.
     *
     * @return the trailing parameter count
     */
    @SuppressWarnings("unused")
    int getTrailingParameterCount() {
        return trailingParameterCount;
    }
}