package org.wso2.carbon.kernel.internal.context;

import org.wso2.carbon.kernel.config.CarbonConfigProvider;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;

public class CarbonConfigProviderImpl implements CarbonConfigProvider {
    @Override
    public CarbonConfiguration getCarbonConfiguration() {
        return new CarbonConfiguration();
    }
}
