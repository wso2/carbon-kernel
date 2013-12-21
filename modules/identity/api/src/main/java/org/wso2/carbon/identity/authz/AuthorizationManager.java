package org.wso2.carbon.identity.authz;


public interface AuthorizationManager extends VirtualAuthorizationStore {

	/**
	 * 
	 * @param authzStoreMaanger
	 */
	public void init(ReadOnlyAuthorizationStoreManager authzStoreMaanger);
}
