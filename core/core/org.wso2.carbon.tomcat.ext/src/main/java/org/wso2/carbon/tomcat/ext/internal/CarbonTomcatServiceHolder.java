package org.wso2.carbon.tomcat.ext.internal;

import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;

/**
 * Holder class for services, captured using declarative service component
 */
public class CarbonTomcatServiceHolder {
    private static ServerConfigurationService serverConfigurationService;
    private static CarbonTomcatService carbonTomcatService;
    private static ClassLoader tccl;

    public static ServerConfigurationService getServerConfigurationService() {
        return serverConfigurationService;
    }

    public static void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        CarbonTomcatServiceHolder.serverConfigurationService = serverConfigurationService;
    }

    public static CarbonTomcatService getCarbonTomcatService() {
        return carbonTomcatService;
    }

    public static void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        CarbonTomcatServiceHolder.carbonTomcatService = carbonTomcatService;
    }

    public static void setTccl(ClassLoader tccl) {
        CarbonTomcatServiceHolder.tccl = tccl;
    }

    public static ClassLoader getTccl() {
        return tccl;
    }

}
