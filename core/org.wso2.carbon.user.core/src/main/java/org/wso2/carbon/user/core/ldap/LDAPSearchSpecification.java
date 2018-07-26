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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.model.ExpressionAttribute;
import org.wso2.carbon.user.core.model.ExpressionCondition;
import org.wso2.carbon.user.core.model.ExpressionOperation;

import java.util.ArrayList;
import java.util.List;
import javax.naming.directory.SearchControls;

public class LDAPSearchSpecification {

    RealmConfiguration realmConfig;
    SearchControls searchControls;
    String searchBases;
    boolean isMemberOfPropertyFound;
    boolean isMemberShipPropertyFound;
    LDAPFilterQueryBuilder ldapFilterQueryBuilder;

    public LDAPSearchSpecification(RealmConfiguration realmConfig) {

        this.realmConfig = realmConfig;
        this.searchControls = new SearchControls();
        this.searchBases = null;
        this.isMemberOfPropertyFound = false;
        this.isMemberShipPropertyFound = false;
        this.ldapFilterQueryBuilder = null;

    }

    public void setLDAPSearchParamters(boolean isGroupFiltering, boolean isMultiGroupFiltering,
                                       List<ExpressionCondition> expressionConditions, int searchScope)
            throws UserStoreException {

        List<String> returnedAttributes = new ArrayList<>();
        this.searchControls.setSearchScope(searchScope);

        if (isGroupFiltering) {
            String memberOfProperty = realmConfig.getUserStoreProperty(LDAPConstants.MEMBEROF_ATTRIBUTE);
            //Give priority to memberOf attribute
            if (StringUtils.isNotEmpty(memberOfProperty)) {
                this.isMemberOfPropertyFound = true;
                this.searchBases = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
                returnedAttributes.add(realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE));
            } else {//check for member attribute
                String membershipProperty = realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
                if (StringUtils.isEmpty(membershipProperty)) {
                    throw new UserStoreException("Please set member of attribute or membership attribute");
                }
                this.isMemberShipPropertyFound = true;
                this.searchBases = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE);
                returnedAttributes.add(membershipProperty);
            }
        } else {
            this.searchBases = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
            returnedAttributes.add(realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE));
            returnedAttributes.add("sn");
        }

        if (returnedAttributes != null && returnedAttributes.size() > 0) {
            this.searchControls.setReturningAttributes(returnedAttributes.toArray(new String[0]));
        }

        searchFilterBuilder(isGroupFiltering, isMultiGroupFiltering, expressionConditions);
    }

    private void searchFilterBuilder(boolean isGroupFiltering, boolean isMultiGroupFiltering,
                                     List<ExpressionCondition> expressionConditions) throws UserStoreException {

        String userPropertyName = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
        String groupPropertyName = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE);
        String memberOfAttributeName = realmConfig.getUserStoreProperty(LDAPConstants.MEMBEROF_ATTRIBUTE);
        String memberAttributeName = realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);

        if (isGroupFiltering && isMemberShipPropertyFound) {
            ldapFilterQueryBuilder = new LDAPFilterQueryBuilder(realmConfig.
                    getUserStoreProperty(LDAPConstants.GROUP_NAME_LIST_FILTER));
        } else {
            ldapFilterQueryBuilder = new LDAPFilterQueryBuilder(realmConfig.
                    getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER));
        }

        for (ExpressionCondition expressionCondition : expressionConditions) {
            StringBuilder property;
            String attributeName = expressionCondition.getAttributeName();
            StringBuilder value = new StringBuilder(expressionCondition.getAttributeValue());
            String operation = expressionCondition.getOperation();
            boolean isMembershipMulitGroupFilters = false;

            if (ExpressionAttribute.ROLE.toString().equals(attributeName)) {
                if (isMemberOfPropertyFound) {
                    if (ExpressionOperation.EQ.toString().equals(operation)) {
                        property = new StringBuilder(memberOfAttributeName).append("=").append(groupPropertyName);
                        value.append(",").append(realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE));
                    } else {
                        throw new UserStoreException("Can't do regex search on 'memberOf' property. ");
                    }
                } else if (isMultiGroupFiltering && !isMemberOfPropertyFound) {
                    property = new StringBuilder(groupPropertyName);
                    isMembershipMulitGroupFilters = true;
                } else {
                    property = new StringBuilder(groupPropertyName);
                }
            } else if (ExpressionAttribute.USERNAME.toString().equals(expressionCondition.getAttributeName())) {
                if (isMemberShipPropertyFound) {
                    property = new StringBuilder(memberAttributeName).append("=").append(userPropertyName);
                    if (ExpressionOperation.CO.toString().equals(operation) ||
                            ExpressionOperation.EW.toString().equals(operation)) {
                        throw new UserStoreException("Can't use 'co', 'ew' filters on 'member' property.");
                    } else if (ExpressionOperation.EQ.toString().equals(operation)) {
                        value.append(",").append(realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE));
                    }
                } else {
                    property = new StringBuilder(userPropertyName);
                }
            } else {
                if (!isMemberShipPropertyFound) {
                    property = new StringBuilder(expressionCondition.getAttributeName());
                } else {
                    throw new UserStoreException("Claims filtering not supported while using membership" +
                            " group filtering.");
                }
            }
            ExpressionCondition condition = new ExpressionCondition(operation, String.valueOf(property),
                    String.valueOf(value));
            ldapFilterQueryBuilder.addFilter(condition, isMembershipMulitGroupFilters);
        }
    }

    public SearchControls getSearchControls() {

        return searchControls;
    }

    public String getSearchBases() {

        return searchBases;
    }

    public boolean isMemberOfPropertyFound() {

        return isMemberOfPropertyFound;
    }

    public boolean isMemberShipPropertyFound() {

        return isMemberShipPropertyFound;
    }

    public String getSearchFilterQuery() {

        return ldapFilterQueryBuilder.getSearchFilterQuery();
    }
}
