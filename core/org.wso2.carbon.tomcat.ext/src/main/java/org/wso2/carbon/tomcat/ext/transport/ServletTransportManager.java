package org.wso2.carbon.tomcat.ext.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.ServerStatus;
import org.wso2.carbon.tomcat.ext.internal.CarbonTomcatServiceHolder;


/**
 * The class provides transport ports to all the other parties inside carbon. It takes system properties
 * and carbon.xml params in to account while doing so.
 */
public class ServletTransportManager {
    private static Log log = LogFactory.getLog(ServletTransportManager.class);
    private static int httpPort;
    private static int httpsPort;
    private static int httpProxyPort = -1;   // initializing with invalid proxy port params
    private static int httpsProxyPort = -1;
    private static int portOffset;


    public static void init() {
        ServerConfigurationService serverConfigurationService = CarbonTomcatServiceHolder.getServerConfigurationService();
        String portOffset = System.getProperty("portOffset",
                                               serverConfigurationService.getFirstProperty("Ports.Offset"));
        //There is a default value in carbon.xml, therefor not doing a null check here
        ServletTransportManager.portOffset = Integer.parseInt(portOffset);
        ServletTransportManager.httpPort = CarbonTomcatServiceHolder.getCarbonTomcatService().getPort("http") +
                                           ServletTransportManager.portOffset;
        System.setProperty("mgt.transport.http.port", String.valueOf(ServletTransportManager.httpPort));
        ServletTransportManager.httpsPort = CarbonTomcatServiceHolder.getCarbonTomcatService().getPort("https") +
                                            ServletTransportManager.portOffset;
        System.setProperty("mgt.transport.https.port", String.valueOf(ServletTransportManager.httpsPort));

        //Setting proxy ports if specified in http/https connectors
        int tempHttpProxyPort = CarbonTomcatServiceHolder.getCarbonTomcatService().getProxyPort("http");
        // if not specified default proxy value equals to zero. Only non negative values are allowed
        if (tempHttpProxyPort > 0) {
            ServletTransportManager.httpProxyPort = tempHttpProxyPort;
            System.setProperty("mgt.transport.http.proxyPort", String.valueOf(tempHttpProxyPort));
        }
        int tempHttpsProxyPort = CarbonTomcatServiceHolder.getCarbonTomcatService().getProxyPort("https");
        if (tempHttpsProxyPort > 0) {
            ServletTransportManager.httpsProxyPort = tempHttpsProxyPort;
            System.setProperty("mgt.transport.https.proxyPort", String.valueOf(tempHttpsProxyPort));
        }

    }

    @SuppressWarnings("unused")
    public static int getPort(String scheme) {
        if ("http".equals(scheme)) {
            return ServletTransportManager.httpPort;
        } else if ("https".equals(scheme)) {
            return ServletTransportManager.httpsPort;
        }
        if (log.isDebugEnabled()) {
            log.debug("Unrecognized transport scheme" + scheme);
        }
        return -1;
    }

    public static int getProxyPort(String scheme) {
        if ("http".equals(scheme)) {
            return ServletTransportManager.httpProxyPort;
        } else if ("https".equals(scheme)) {
            return ServletTransportManager.httpsProxyPort;
        }
        if (log.isDebugEnabled()) {
            log.debug("Unrecognized transport scheme" + scheme);
        }
        return -1;
    }

    @SuppressWarnings("unused")
    public static void startTransports() {
        CarbonTomcatServiceHolder.getCarbonTomcatService().startConnectors(ServletTransportManager.portOffset);
    }

    @SuppressWarnings("unused")
    public static void stopTransports() {
        if (ServerStatus.getCurrentStatus().equals(ServerStatus.STATUS_RUNNING)) {
            CarbonTomcatServiceHolder.getCarbonTomcatService().stopConnectors();
        }
    }

    @SuppressWarnings("unused")
    public static void startTransport(String scheme) {
        CarbonTomcatServiceHolder.getCarbonTomcatService().startConnector(scheme,
                                                                          ServletTransportManager.portOffset);
    }

    @SuppressWarnings("unused")
    public static void stopTransport(String scheme) {
        if (ServerStatus.getCurrentStatus().equals(ServerStatus.STATUS_RUNNING)) {
            CarbonTomcatServiceHolder.getCarbonTomcatService().stopConnector(scheme);
        }
    }
}
