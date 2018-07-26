package org.wso2.carbon.user.core.ldap;

import org.wso2.carbon.user.core.model.ExpressionCondition;
import org.wso2.carbon.user.core.model.ExpressionOperation;

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
