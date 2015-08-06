package org.wso2.carbon.user.core.claim;

/**
 * Created by Chanuka on 7/20/15 AD.
 */
public interface ClaimManagerFactory {

    /**
     * Initialize the ClaimManager.
     *
     * @param tenantId
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    DefaultClaimManager getClaimManager(int tenantId);

}
