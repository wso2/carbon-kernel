package org.wso2.carbon.utils.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.utils.CarbonUtils;

public class CarbonUtilsDataHolder {
    private static ConfigurationContext configContext;

    public static void setConfigContext(ConfigurationContext configContext) {
        CarbonUtilsDataHolder.configContext = configContext;
    }

    public static ConfigurationContext getConfigContext() {
        CarbonUtils.checkSecurity();
        return configContext;
    }

}
