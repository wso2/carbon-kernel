package org.wso2.carbon.user.core.ldap;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.ArrayList;
import java.util.List;
import javax.naming.directory.SearchControls;

public class LDAPSearchSpecification {

    SearchControls searchControls;
    String searchBases;
    boolean isMemberOfPropertyFound;
    boolean isMemberShipPropertyFound;
    String searchFilter;

    public LDAPSearchSpecification() {

        this.searchControls = new SearchControls();
        this.searchBases = "";
        this.isMemberOfPropertyFound = false;
        this.isMemberShipPropertyFound = false;
        this.searchFilter = "";
    }

    public void setLDAPSearchParamters(RealmConfiguration realmConfig, boolean isGroupFiltering, int searchScope) throws UserStoreException {

        List<String> returnedAttributes = new ArrayList<>();
        this.searchControls.setSearchScope(searchScope);
        if (isGroupFiltering) {
            String memberOfProperty = realmConfig.getUserStoreProperty(LDAPConstants.MEMBEROF_ATTRIBUTE);
            if (StringUtils.isNotEmpty(memberOfProperty)) {
                this.isMemberOfPropertyFound = true;
                this.searchBases = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
                returnedAttributes.add(realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE));
            } else {
                String membershipProperty = realmConfig.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
                if (!StringUtils.isNotEmpty(membershipProperty)) {
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

    public String getSearchFilter() {

        return searchFilter;
    }
}
