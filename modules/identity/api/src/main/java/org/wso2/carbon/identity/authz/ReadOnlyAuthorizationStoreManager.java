package org.wso2.carbon.identity.authz;

import java.util.List;

import org.wso2.carbon.identity.authn.StoreIdentifier;
import org.wso2.carbon.identity.authz.spi.AuthorizationStore;

public interface ReadOnlyAuthorizationStoreManager {

	/**
	 * 
	 * @param storeIdentifier
	 * @return
	 */
	public AuthorizationStore getAuthorizationStore(StoreIdentifier storeIdentifier);

	/**
	 * 
	 * @return
	 */
	public AuthorizationStore getPrimaryAuthorizationStore();

	/**
	 * 
	 * @return
	 */
	public List<StoreIdentifier> getAllAuthorizationStoreIdentifiers();
}
