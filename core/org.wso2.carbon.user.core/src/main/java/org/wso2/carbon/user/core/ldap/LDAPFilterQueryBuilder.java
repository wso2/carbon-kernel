package org.wso2.carbon.user.core.ldap;

import org.wso2.carbon.user.core.model.ExpressionOperation;

public class LDAPFilterQueryBuilder {

    StringBuilder searchFilter= new StringBuilder();
    String filterQuery ;
    StringBuilder multiGroupFilterWithMembership = new StringBuilder();
    String userPropertyName;
    String groupPropertyName ;

    public LDAPFilterQueryBuilder(String searchFilter) {
        this.searchFilter.append("(&");
        this.searchFilter.append(searchFilter);
    }

    public void appendGroupFilters(String attributeName, String operation, String attributeValue) {
        if (ExpressionOperation.EQ.toString().equals(operation)){
            searchFilter.append(equalFilterBuilder(attributeName, attributeValue));
        }
    }


    private String equalFilterBuilder(String attributeName, String attributeValue){
        return "(".concat(attributeName).concat("=*").concat(attributeValue).concat("*)");
    }

    private String containsFilterBuilder(String attributeName, String attributeValue){
        return "(".concat(attributeName).concat("=*").concat(attributeValue).concat("*)");
    }

    private String endWithFilterBuilder(String attributeName, String attributeValue){
        return "(".concat(attributeName).concat("=*").concat(attributeValue).concat(")");
    }

    private String startWithFilterBuilder(String attributeName, String attributeValue){
        return "(".concat(attributeName).concat("=").concat(attributeValue).concat("*)");
    }
}
