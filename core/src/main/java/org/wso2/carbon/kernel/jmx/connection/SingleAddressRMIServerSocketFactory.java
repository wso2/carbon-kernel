package org.wso2.carbon.kernel.jmx.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

/**
 * SingleAddressRMIServerSocketFactory
 */
public class SingleAddressRMIServerSocketFactory implements RMIServerSocketFactory {
    private final InetAddress inetAddress;

    public SingleAddressRMIServerSocketFactory(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port, 0, inetAddress);
    }
}
