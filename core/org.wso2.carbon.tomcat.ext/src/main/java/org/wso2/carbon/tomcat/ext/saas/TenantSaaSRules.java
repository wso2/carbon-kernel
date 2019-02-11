package org.wso2.carbon.tomcat.ext.saas;

import java.util.ArrayList;

public class TenantSaaSRules {

    private String tenant;
    private ArrayList<String> users = null;
    private ArrayList<String> roles = null;

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public ArrayList<String> getRoles() {
        return roles;
    }

    public void setRoles(ArrayList<String> roles) {
        this.roles = roles;
    }

    public boolean isTenantRulesDefined() {
        return users != null || roles != null;
    }
}