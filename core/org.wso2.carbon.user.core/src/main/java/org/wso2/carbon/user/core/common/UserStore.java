package org.wso2.carbon.user.core.common;

import org.wso2.carbon.user.core.UserStoreManager;

public class UserStore {

	private UserStoreManager userStoreManager;

	private String domainAwareName;

	private String domainFreeName;
	
	private String domainName;
	
	private boolean recurssive;
	
	private boolean hybridRole;

    private boolean systemStore;
			

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
}
