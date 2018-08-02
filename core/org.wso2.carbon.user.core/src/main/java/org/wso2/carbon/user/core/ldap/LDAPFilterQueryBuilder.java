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
 * This class is to generate LDAP search filter query. Also can add multiple filter with AND operation.
 */
public class LDAPFilterQueryBuilder {

    private StringBuilder searchFilter;
    private StringBuilder membershipMulitGroupFilters;

    public LDAPFilterQueryBuilder(String searchFilter) {

        this.searchFilter = new StringBuilder("(&").append(searchFilter);
        this.membershipMulitGroupFilters = new StringBuilder();
    }

    public void addFilter(ExpressionCondition condition, boolean isMembershipMulitGroupFilters) {

        String property = condition.getAttributeName();
        String operation = condition.getOperation();
        String value = condition.getAttributeValue();

        if (isMembershipMulitGroupFilters) {
            buildFilter(membershipMulitGroupFilters, property, operation, value);
        } else {
            buildFilter(searchFilter, property, operation, value);
        }
    }

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

    private String equalFilterBuilder(String property, String value) {

        return "(" + property + "=" + value + ")";
    }

    private String containsFilterBuilder(String property, String value) {

        return "(" + property + "=*" + value + "*)";
    }

    private String endWithFilterBuilder(String property, String value) {

        return "(" + property + "=*" + value + ")";
    }

    private String startWithFilterBuilder(String property, String value) {

        return "(" + property + "=" + value + "*)";
    }

    public String getSearchFilterQuery() {

        if (membershipMulitGroupFilters != null && !membershipMulitGroupFilters.toString().equals("")) {
            searchFilter.append("(| ").append(membershipMulitGroupFilters).append(")");
        }
        searchFilter.append(")");
        return String.valueOf(searchFilter);
    }
}
