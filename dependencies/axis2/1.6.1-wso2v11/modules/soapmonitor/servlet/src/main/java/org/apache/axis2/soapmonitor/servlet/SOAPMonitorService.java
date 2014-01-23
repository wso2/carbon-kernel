/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.soapmonitor.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This is a SOAP Monitor Service class.
 * <p/>
 * During the HTTP server startup, the servlet init method
 * is invoked.  This allows the code to open a server
 * socket that will be used to communicate with running
 * applets.
 * <p/>
 * When an HTTP GET request is received, the servlet
 * dynamically produces an HTML document to load the SOAP
 * monitor applet and supply the port number being used by
 * the server socket (so the applet will know how to
 * connect back to the server).
 * <p/>
 * Each time a socket connection is established, a new
 * thread is created to handle communications from the
 * applet.
 * <p/>
 * The publishMethod routine is invoked by the SOAP monitor
 * handler when a SOAP message request or response is
 * detected.  The information about the SOAP message is
 * then forwared to all current socket connections for
 * display by the applet.
 */

public class SOAPMonitorService extends HttpServlet {

    /**
     * Private data
     */
    private static ServerSocket serverSocket = null;
    private static Vector connections = null;

    private static final Log log = LogFactory.getLog(SOAPMonitorService.class);

    /**
     * Constructor
     */
    public SOAPMonitorService() {
    }


    /**
     * Publish a SOAP message to listeners
     */
    public static void publishMessage(Long id,
                                      Integer type,
                                      String target,
                                      String soap) {
        if (connections != null) {
            Enumeration e = connections.elements();
            while (e.hasMoreElements()) {
                ConnectionThread ct = (ConnectionThread) e.nextElement();
                ct.publishMessage(id, type, target, soap);
            }
        }
    }

    /**
     * Servlet initialiation
     */
    public void init() throws ServletException {
        if (connections == null) {
            // Create vector to hold connection information
            connections = new Vector();
        }
        if (serverSocket == null) {
            // Get the server socket port from the init params
            ServletConfig config = super.getServletConfig();
            String hostName = config.getInitParameter(SOAPMonitorConstants.SOAP_MONITOR_HOST_NAME);
            String port = config.getInitParameter(SOAPMonitorConstants.SOAP_MONITOR_PORT);
            if (port == null) {
                log.error("SOAPMonitorService can't find ServletConfig init parameter 'port'");
                port = "0";
            }
            try {
                if (hostName != null) {
                    serverSocket = new ServerSocket(Integer.parseInt(port),
                                                    50,
                                                    InetAddress.getByName(hostName));
                } else {
                    serverSocket = new ServerSocket(Integer.parseInt(port));
                }
            } catch (Exception ex) {
                // Let someone know we could not open the socket
                log.error("Unable to open server socket using port: " + port);
                log.error(ex.getMessage(), ex);
                // Fail as loudly as possible for those without logging configured
                config.getServletContext().
                        log("Unable to open server socket using port " + port + ":", ex);
                serverSocket = null;
            }
            if (serverSocket != null) {
                // Start the server socket thread
                new Thread(new ServerSocketThread()).start();
            }
        }
    }

    /**
     * Servlet termination
     */
    public void destroy() {
        // End all connection threads
        Enumeration e = connections.elements();
        while (e.hasMoreElements()) {
            ConnectionThread ct = (ConnectionThread) e.nextElement();
            ct.close();
        }
        // End main server socket thread
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception x) {
            }
            serverSocket = null;
        }
    }

    /**
     * HTTP GET request
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // Create HTML to load the SOAP monitor applet
        int port = 0;
        if (serverSocket != null) {
            port = serverSocket.getLocalPort();
            log.debug("Sending param to SOAP monitor applet as port: " + port);
        }
        response.setContentType("text/html");
        response.getWriter().println("<html>");
        response.getWriter().println("<head>");
        response.getWriter().println("<title>SOAP Monitor</title>");
        response.getWriter().println("</head>");
        response.getWriter().println("<body>");
        response.getWriter().println("<object classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\" width=100% height=100% codebase=\"http://java.sun.com/products/plugin/1.3/jinstall-13-win32.cab#Version=1,3,0,0\">");
        response.getWriter().println("<param name=code value=org.apache.axis2.soapmonitor.applet.SOAPMonitorApplet.class>");
        response.getWriter().println("<param name=\"type\" value=\"application/x-java-applet;version=1.3\">");
        response.getWriter().println("<param name=\"scriptable\" value=\"false\">");
        response.getWriter().println("<param name=\"port\" value=\"" + port + "\">");
        response.getWriter().println("<comment>");
        response.getWriter().println("<embed type=\"application/x-java-applet;version=1.3\" code=org.apache.axis2.soapmonitor.applet.SOAPMonitorApplet.class width=100% height=100% port=\"" + port + "\" scriptable=false pluginspage=\"http://java.sun.com/products/plugin/1.3/plugin-install.html\">");
        response.getWriter().println("<noembed>");
        response.getWriter().println("</comment>");
        response.getWriter().println("</noembed>");
        response.getWriter().println("</embed>");
        response.getWriter().println("</object>");
        response.getWriter().println("</body>");
        response.getWriter().println("</html>");
    }

    /**
     * Thread class for handling the server socket
     */
    class ServerSocketThread implements Runnable {

        /**
         * Thread for handling the server socket
         */
        public void run() {
            // Wait for socket connections
            while (serverSocket != null) {
                try {
                    Socket socket = serverSocket.accept();
                    new Thread(new ConnectionThread(socket)).start();
                } catch (IOException ioe) {
                }
            }
        }
    }

    /**
     * Thread class for handling socket connections
     */
    class ConnectionThread implements Runnable {

        private Socket socket = null;
        private ObjectInputStream in = null;
        private ObjectOutputStream out = null;
        private boolean closed = false;

        /**
         * Constructor
         */
        public ConnectionThread(Socket s) {
            socket = s;
            try {
                // Use object streams for input and output
                //
                // NOTE: We need to be sure to create and flush the
                // output stream first because the ObjectOutputStream
                // constructor writes a header to the stream that is
                // needed by the ObjectInputStream on the other end
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());
            } catch (Exception e) {
            }
            // Add the connection to our list
            synchronized (connections) {
                connections.addElement(this);
            }
        }

        /**
         * Close the socket connection
         */
        public void close() {
            closed = true;
            try {
                socket.close();
            } catch (IOException ioe) {
            }
        }

        /**
         * Thread to handle the socket connection
         */
        public void run() {
            try {
                while (!closed) {
                    Object o = in.readObject();
                }
            } catch (Exception e) {
            }
            // Cleanup connection list
            synchronized (connections) {
                connections.removeElement(this);
            }
            // Cleanup I/O streams
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                }
                out = null;
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
                in = null;
            }
            // Be sure the socket is closed
            close();
        }

        /**
         * Publish SOAP message information
         */
        public synchronized void publishMessage(Long id,
                                                Integer message_type,
                                                String target,
                                                String soap) {
            // If we have a valid output stream, then
            // send the data to the applet
            if (out != null) {
                try {
                    switch (message_type.intValue()) {
                        case SOAPMonitorConstants.SOAP_MONITOR_REQUEST:
                            out.writeObject(message_type);
                            out.writeObject(id);
                            out.writeObject(target);
                            out.writeObject(soap);
                            out.flush();
                            break;
                        case SOAPMonitorConstants.SOAP_MONITOR_RESPONSE:
                            out.writeObject(message_type);
                            out.writeObject(id);
                            out.writeObject(soap);
                            out.flush();
                            break;
                    }
                } catch (Exception e) {
                }
            }
        }
    }
}

