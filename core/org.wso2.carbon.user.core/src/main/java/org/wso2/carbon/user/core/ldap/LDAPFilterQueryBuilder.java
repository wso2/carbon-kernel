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
package org.wso2.carbon.user.core.ldap;

import org.wso2.carbon.user.core.model.ExpressionCondition;
import org.wso2.carbon.user.core.model.ExpressionOperation;

/**
 * This class is to generate LDAP multi attribute search filter query.
 * Currently only support for 'AND' operation.
 */
public class LDAPFilterQueryBuilder {

    private static final String OPEN_PARENTHESIS = "(";
    private static final String CLOSE_PARENTHESIS = ")";
    private static final String AND_OPERATION = "&";
    private static final String OR_OPERATION = "|";
    private static final String EQUALS_SIGN = "=";
    private static final String ANY_STRING = "*";

    private StringBuilder searchFilter;
    private StringBuilder membershipMultiGroupFilters;

    public LDAPFilterQueryBuilder(String searchFilter) {

        this.searchFilter = new StringBuilder(OPEN_PARENTHESIS).append(AND_OPERATION).append(searchFilter);
        this.membershipMultiGroupFilters = new StringBuilder();
    }

    /**
     * Add a new filter to the query.
     *
     * @param condition
     * @param isMembershipMulitGroupFilters
     */
    public void addFilter(ExpressionCondition condition, boolean isMembershipMulitGroupFilters) {

        String property = condition.getAttributeName();
        String operation = condition.getOperation();
        String value = condition.getAttributeValue();

        if (isMembershipMulitGroupFilters) {
            buildFilter(membershipMultiGroupFilters, property, operation, value);
        } else {
            buildFilter(searchFilter, property, operation, value);
        }
    }

    /**
     * Generate filter depends on given filter operation.
     *
     * @param stringBuilder
     * @param property
     * @param operation
     * @param value
     */
    private void buildFilter(StringBuilder stringBuilder, String property, String operation, String value) {

        if (ExpressionOperation.EQ.toString().equals(operation)) {
            stringBuilder.append(equalFilterBuilder(property, value));
        } else if (ExpressionOperation.CO.toString().equals(operation)) {
            stringBuilder.append(containsFilterBuilder(property, value));
        } else if (ExpressionOperation.EW.toString().equals(operation)) {
            stringBuilder.append(endWithFilterBuilder(property, value));
        } else if (ExpressionOperation.SW.toString().equals(operation)) {
            stringBuilder.append(startWithFilterBuilder(property, value));
        }
    }

    /**
     * Generate "EQ" filter.
     *
     * @param property
     * @param value
     * @return
     */
    private String equalFilterBuilder(String property, String value) {

        StringBuilder filter = new StringBuilder();
        filter.append(OPEN_PARENTHESIS).append(property).append(EQUALS_SIGN).append(value).append(CLOSE_PARENTHESIS);
        return filter.toString();
    }

    /**
     * Generate "CO" filter.
     *
     * @param property
     * @param value
     * @return
     */
    private String containsFilterBuilder(String property, String value) {

        StringBuilder filter = new StringBuilder();
        filter.append(OPEN_PARENTHESIS).append(property).append(EQUALS_SIGN).append(ANY_STRING).append(value).
                append(ANY_STRING).append(CLOSE_PARENTHESIS);
        return filter.toString();
    }

    /**
     * Generate "EW" filter.
     *
     * @param property
     * @param value
     * @return
     */
    private String endWithFilterBuilder(String property, String value) {

        StringBuilder filter = new StringBuilder();
        filter.append(OPEN_PARENTHESIS).append(property).append(EQUALS_SIGN).append(ANY_STRING).append(value).
                append(CLOSE_PARENTHESIS);
        return filter.toString();
    }

    /**
     * Generate "SW" filter.
     *
     * @param property
     * @param value
     * @return
     */
    private String startWithFilterBuilder(String property, String value) {

        StringBuilder filter = new StringBuilder();
        filter.append(OPEN_PARENTHESIS).append(property).append(EQUALS_SIGN).append(value).append(ANY_STRING).
                append(CLOSE_PARENTHESIS);
        return filter.toString();
    }

    /**
     * Get final search filter query.
     *
     * @return
     */
    public String getSearchFilterQuery() {

        if (membershipMultiGroupFilters != null && !membershipMultiGroupFilters.toString().equals("")) {
            searchFilter.append(OPEN_PARENTHESIS).append(OR_OPERATION).append(membershipMultiGroupFilters).
                    append(CLOSE_PARENTHESIS);
        }
        searchFilter.append(CLOSE_PARENTHESIS);
        return String.valueOf(searchFilter);
    }
}
