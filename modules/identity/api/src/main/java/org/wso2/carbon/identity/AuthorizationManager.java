package org.wso2.carbon.user;

public interface AuthorizationManager extends VirtualAuthorizationStore {

	/**
	 * 
	 * @param authzStoreMaanger
	 */
	public void init(ReadOnlyAuthorizationStoreManager authzStoreMaanger);
}
