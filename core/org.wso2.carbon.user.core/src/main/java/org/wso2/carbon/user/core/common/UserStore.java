package org.wso2.carbon.user.core.common;

import org.wso2.carbon.user.core.UserStoreManager;

public class UserStore {

    private UserStoreManager userStoreManager;

    private String domainAwareName;

    private String domainFreeName;

    private String domainAwareUserId;

    private String domainFreeUserId;

    private String domainName;

    private boolean recurssive;

    private boolean hybridRole;

    private boolean systemStore;

    private String domainAwareGroupId;
    private String domainFreeGroupId;
    private String domainAwareGroupName;

    public String getDomainAwareGroupName() {

        return domainAwareGroupName;
    }

    public void setDomainAwareGroupName(String domainAwareGroupName) {

        this.domainAwareGroupName = domainAwareGroupName;
    }

    public String getDomainFreeGroupName() {

        return domainFreeGroupName;
    }

    public void setDomainFreeGroupName(String domainFreeGroupName) {

        this.domainFreeGroupName = domainFreeGroupName;
    }

    private String domainFreeGroupName;

    public String getDomainAwareGroupId() {

        return domainAwareGroupId;
    }

    public void setDomainAwareGroupId(String domainAwareGroupId) {

        this.domainAwareGroupId = domainAwareGroupId;
    }

    public String getDomainFreeGroupId() {

        return domainFreeGroupId;
    }

    public void setDomainFreeGroupId(String domainFreeGroupId) {

        this.domainFreeGroupId = domainFreeGroupId;
    }

    public boolean isHybridRole() {
        return hybridRole;
    }

    public void setHybridRole(boolean hybridRole) {
        this.hybridRole = hybridRole;
    }

    public boolean isRecurssive() {
        return recurssive;
    }

    public void setRecurssive(boolean recurssive) {
        this.recurssive = recurssive;
    }

    public UserStoreManager getUserStoreManager() {
        return userStoreManager;
    }

    public void setUserStoreManager(UserStoreManager userStoreManager) {
        this.userStoreManager = userStoreManager;
    }

    public String getDomainAwareName() {
        return domainAwareName;
    }

    public void setDomainAwareName(String domainAwareName) {
        this.domainAwareName = domainAwareName;
    }

    public String getDomainFreeName() {
        return domainFreeName;
    }

    public void setDomainFreeName(String domainFreeName) {
        this.domainFreeName = domainFreeName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public boolean isSystemStore() {
        return systemStore;
    }

    public void setSystemStore(boolean systemStore) {
        this.systemStore = systemStore;
    }

    public String getDomainAwareUserId() {

        return domainAwareUserId;
    }

    public void setDomainAwareUserId(String domainAwareUserId) {

        this.domainAwareUserId = domainAwareUserId;
    }

    public String getDomainFreeUserId() {

        return domainFreeUserId;
    }

    public void setDomainFreeUserId(String domainFreeUserId) {

        this.domainFreeUserId = domainFreeUserId;
    }
}
