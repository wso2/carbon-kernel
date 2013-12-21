package org.wso2.carbon.identity.authn;

import java.util.Properties;

public interface IdentityManager extends VirtualIdentityStore {

	/**
	 * 
	 * @param identityStoreMaanger
	 */
	public void init(ReadOnlyIdentityStoreManager identityStoreMaanger, Properties properties);

}
