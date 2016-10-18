package org.wso2.carbon.http.socket.appender;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.net.AbstractSocketManager;
import org.apache.logging.log4j.util.Strings;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Manager of HTTP Socket connections.
 */
public class HTTPSocketManager extends AbstractSocketManager {

    /**
     * The default reconnection delay (30000 milliseconds or 30 seconds).
     */
    public static final int DEFAULT_RECONNECTION_DELAY_MILLIS = 30000;
    /**
     * The default port number of remote logging server (4560).
     */
    private static final int DEFAULT_PORT = 4560;
    private static HttpURLConnection connection = null;
    private Socket socket;


    public HTTPSocketManager(String name, OutputStream os, InetAddress inetAddress, String host, int port, Layout<? extends Serializable> layout) {
        super(name, os, inetAddress, host, port, layout);
        this.socket = socket;
    }

    /**
     * Obtain a TcpSocketManager.
     *
     * @param host                 The host to connect to.
     * @param port                 The port on the host.
     * @param connectTimeoutMillis the connect timeout in milliseconds
     * @return A TcpSocketManager.
     */
    public static HTTPSocketManager getSocketManager(final String host, int port, final int connectTimeoutMillis,
                                                     final Layout<? extends Serializable> layout) {
        if (Strings.isEmpty(host)) {
            throw new IllegalArgumentException("A host name is required");
        }
        if (port <= 0) {
            port = DEFAULT_PORT;
        }

        InetAddress inetAddress = null;
        OutputStream os = null;
        Socket socket = new Socket();
        try {
            inetAddress = InetAddress.getByName(host);
        } catch (final UnknownHostException ex) {
            LOGGER.error("Could not find address of " + host, ex);
            return null;
        }
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), connectTimeoutMillis);
            os = socket.getOutputStream();


        } catch (IOException e) {

        }
        return new HTTPSocketManager("HTTP:" + host + ":" + port, os,inetAddress, host, port, layout);
    }

    @Override
    protected void write(final byte[] bytes, final int offset, final int length) {
        if (socket == null) {
            if (socket == null) {
                final String msg = "Error writing to " + getName() + " socket not available";
                throw new AppenderLoggingException(msg);
            }
        }
        synchronized (this) {
            try {
                connection.setRequestProperty( "Content-Length", Integer.toString( bytes.length ));
                DataOutputStream wr = new DataOutputStream( connection.getOutputStream());
                wr.write(bytes);
            } catch (final IOException ex) {
                final String msg = "Error writing to " + getName();
                throw new AppenderLoggingException(msg, ex);
            }
        }
    }

}
