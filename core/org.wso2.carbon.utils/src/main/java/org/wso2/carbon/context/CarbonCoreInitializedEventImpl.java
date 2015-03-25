package org.wso2.carbon.context;

/**
 * Since both org.wso2.carbon.context.internal.CarbonContextServiceComponent and org.wso2.carbon.core.internal.CarbonCoreServiceComponent
 * get activate when
 * org.wso2.carbon.user.core.service.RealmService
 * org.wso2.carbon.registry.api.RegistryService exist, we can't guarantee the order of activation.
 *
 * To guarantee the activation order this empty service is used to guarantee the order
 */
public class CarbonCoreInitializedEventImpl implements CarbonCoreInitializedEvent {
}
