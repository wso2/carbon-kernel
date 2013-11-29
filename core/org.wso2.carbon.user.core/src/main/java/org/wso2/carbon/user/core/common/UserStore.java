package org.wso2.carbon.user.core.common;

public class UserStore {

	private AbstractUserStoreManager userStoreManager;

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

	public AbstractUserStoreManager getUserStoreManager() {
		return userStoreManager;
	}

	public void setUserStoreManager(AbstractUserStoreManager userStoreManager) {
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
