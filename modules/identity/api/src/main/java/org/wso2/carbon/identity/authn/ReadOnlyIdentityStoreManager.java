package org.wso2.carbon.identity.authn;

import java.util.List;

import org.wso2.carbon.identity.authn.spi.IdentityStore;

public interface ReadOnlyIdentityStoreManager {

	/**
	 * 
	 * @param storeIdentifier
	 * @return
	 */
	public IdentityStore getIdentityStore(StoreIdentifier storeIdentifier);

	/**
	 * 
	 * @return
	 */
	public IdentityStore getPrimaryIdentityStore();

	/**
	 * 
	 * @return
	 */
	public List<StoreIdentifier> getAllIdentityStoreIdentifiers();

}
