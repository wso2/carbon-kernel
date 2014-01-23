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

package org.apache.axis2.transport.http;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.server.AxisHttpResponseImpl;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.httpclient.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.FactoryConfigurationError;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class CommonsHTTPTransportSender extends AbstractHandler implements
        TransportSender {
    /**
     * The {@link TransportOutDescription} object received by the call to
     * {@link #init(ConfigurationContext, TransportOutDescription)}.
     */
    private TransportOutDescription transportOut;

    private static final Log log = LogFactory
            .getLog(CommonsHTTPTransportSender.class);

    /**
     * Default HTTP version as configured in <tt>axis2.xml</tt>. This may be overridden on a per
     * message basis using the {@link HTTPConstants#HTTP_PROTOCOL_VERSION} property.
     */
    private String defaultHttpVersion = HTTPConstants.HEADER_PROTOCOL_11;

    /**
     * Specifies whether chunked encoding is enabled by default. This is configured in
     * <tt>axis2.xml</tt> and may be overridden on a per message basis using the
     * {@link HTTPConstants#CHUNKED} property.
     */
    private boolean defaultChunked = false;

    private int soTimeout = HTTPConstants.DEFAULT_SO_TIMEOUT;

    private int connectionTimeout = HTTPConstants.DEFAULT_CONNECTION_TIMEOUT;

    public void cleanup(MessageContext msgContext) throws AxisFault {
        HttpMethod httpMethod = (HttpMethod) msgContext.getProperty(HTTPConstants.HTTP_METHOD);

        if (httpMethod != null) {
            // TODO : Don't do this if we're not on the right thread! Can we confirm?
            log.trace("cleanup() releasing connection for " + httpMethod);

            httpMethod.releaseConnection();
            msgContext.removeProperty(HTTPConstants.HTTP_METHOD); // guard against multiple calls
        }
    }

    public void init(ConfigurationContext confContext,
                     TransportOutDescription transportOut) throws AxisFault {
        this.transportOut = transportOut;
        
        // <parameter name="PROTOCOL">HTTP/1.0</parameter> or
        // <parameter name="PROTOCOL">HTTP/1.1</parameter> is
        // checked
        Parameter version = transportOut
                .getParameter(HTTPConstants.PROTOCOL_VERSION);
        if (version != null) {
            if (HTTPConstants.HEADER_PROTOCOL_11.equals(version.getValue())) {
                defaultHttpVersion = HTTPConstants.HEADER_PROTOCOL_11;

                Parameter transferEncoding = transportOut
                        .getParameter(HTTPConstants.HEADER_TRANSFER_ENCODING);

                if ((transferEncoding != null)
                        && HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED
                        .equals(transferEncoding.getValue())) {
                    defaultChunked = true;
                }
            } else if (HTTPConstants.HEADER_PROTOCOL_10.equals(version
                    .getValue())) {
                defaultHttpVersion = HTTPConstants.HEADER_PROTOCOL_10;
            } else {
                throw new AxisFault("Parameter "
                        + HTTPConstants.PROTOCOL_VERSION
                        + " Can have values only HTTP/1.0 or HTTP/1.1");
            }
        }

        // Get the timeout values from the configuration
        try {
            Parameter tempSoTimeoutParam = transportOut
                    .getParameter(HTTPConstants.SO_TIMEOUT);
            Parameter tempConnTimeoutParam = transportOut
                    .getParameter(HTTPConstants.CONNECTION_TIMEOUT);

            if (tempSoTimeoutParam != null) {
                soTimeout = Integer.parseInt((String) tempSoTimeoutParam
                        .getValue());
            }

            if (tempConnTimeoutParam != null) {
                connectionTimeout = Integer
                        .parseInt((String) tempConnTimeoutParam.getValue());
            }
        } catch (NumberFormatException nfe) {

            // If there's a problem log it and use the default values
            log.error("Invalid timeout value format: not a number", nfe);
        }

        Parameter cacheHttpClientParam = transportOut.getParameter(HTTPConstants.CACHE_HTTP_CLIENT);
        if (cacheHttpClientParam != null && "true".equals(cacheHttpClientParam.getValue())) {

            Parameter defaultMaxConnectionsPerHostParam = transportOut.getParameter(
                    HTTPConstants.DEFAULT_MAX_CONNECTIONS_PER_HOST);

            Parameter totalConnectionsParam = transportOut.getParameter(
                    HTTPConstants.MAX_TOTAL_CONNECTIONS);

            HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

            if (defaultMaxConnectionsPerHostParam != null &&
                    defaultMaxConnectionsPerHostParam.getValue() != null) {

                try {
                    int defaultMaxConnectionsPerHost = Integer.parseInt(
                            (String) defaultMaxConnectionsPerHostParam.getValue());
                    connectionManager.getParams().setDefaultMaxConnectionsPerHost(
                            defaultMaxConnectionsPerHost);
                } catch (NumberFormatException nfe) {
                    // If there's a problem log it and use the default values
                    log.error("Invalid defaultMaxConnectionsPerHost " +
                            "value format: not a number", nfe);
                }
            } else {
                connectionManager.getParams().setDefaultMaxConnectionsPerHost(100);
            }

            if (totalConnectionsParam != null && totalConnectionsParam.getValue() != null) {
                try {
                    int totalConnections = Integer.parseInt(
                            (String) totalConnectionsParam.getValue());
                    connectionManager.getParams().setMaxTotalConnections(totalConnections);
                } catch (NumberFormatException nfe) {
                    // If there's a problem log it and use the default values
                    log.error("Invalid totalConnections value format: not a number", nfe);
                }
            } else {
                connectionManager.getParams().setMaxTotalConnections(1000);
            }

            HttpClient httpClient = new HttpClient(connectionManager);

            confContext.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, "true");
            confContext.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);
        }
    }

    public void stop() {
        // Any code that , need to invoke when sender stop
        //MultiThreadedHttpConnectionManager.shutdownAll();
    }

    public InvocationResponse invoke(MessageContext msgContext)
            throws AxisFault {
        try {
            OMOutputFormat format = new OMOutputFormat();
            // if (!msgContext.isDoingMTOM())
            msgContext.setDoingMTOM(TransportUtils.doWriteMTOM(msgContext));
            msgContext.setDoingSwA(TransportUtils.doWriteSwA(msgContext));
            msgContext.setDoingREST(TransportUtils.isDoingREST(msgContext));
            format.setSOAP11(msgContext.isSOAP11());
            format.setDoOptimize(msgContext.isDoingMTOM());
            format.setDoingSWA(msgContext.isDoingSwA());
            format.setCharSetEncoding(TransportUtils.getCharSetEncoding(msgContext));

            Object mimeBoundaryProperty = msgContext
                    .getProperty(Constants.Configuration.MIME_BOUNDARY);
            if (mimeBoundaryProperty != null) {
                format.setMimeBoundary((String) mimeBoundaryProperty);
            }

             // set the timeout properties
            Parameter soTimeoutParam = transportOut.getParameter(HTTPConstants.SO_TIMEOUT);
            Parameter connTimeoutParam = transportOut.getParameter(HTTPConstants.CONNECTION_TIMEOUT);

            // set the property values only if they are not set by the user explicitly
            if ((soTimeoutParam != null) &&
                    (msgContext.getProperty(HTTPConstants.SO_TIMEOUT) == null)) {
                msgContext.setProperty(HTTPConstants.SO_TIMEOUT,
                        new Integer((String) soTimeoutParam.getValue()));
            }

            if ((connTimeoutParam != null) &&
                    (msgContext.getProperty(HTTPConstants.CONNECTION_TIMEOUT) == null)) {
                msgContext.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 
                        new Integer((String) connTimeoutParam.getValue()));
            }

            //if a parameter has set been set, we will omit the SOAP action for SOAP 1.2
            if (!msgContext.isSOAP11()) {
                Parameter param = transportOut.getParameter(HTTPConstants.OMIT_SOAP_12_ACTION);
                Object parameterValue = null;
                if (param != null) {
                    parameterValue = param.getValue();
                }

                if (parameterValue != null && JavaUtils.isTrueExplicitly(parameterValue)) {
                    //Check whether user has already overridden this.
                    Object propertyValue = msgContext.getProperty(
                            Constants.Configuration.DISABLE_SOAP_ACTION);

                    if (propertyValue == null || !JavaUtils.isFalseExplicitly(propertyValue)) {
                        msgContext.setProperty(Constants.Configuration.DISABLE_SOAP_ACTION,
                                Boolean.TRUE);
                    }
                }
            }

            // Transport URL can be different from the WSA-To. So processing
            // that now.
            EndpointReference epr = null;
            String transportURL = (String) msgContext
                    .getProperty(Constants.Configuration.TRANSPORT_URL);

            if (transportURL != null) {
                epr = new EndpointReference(transportURL);
            } else if (msgContext.getTo() != null
                    && !msgContext.getTo().hasAnonymousAddress()) {
                epr = msgContext.getTo();
            }

            // Check for the REST behavior, if you desire rest behavior
            // put a <parameter name="doREST" value="true"/> at the
            // server.xml/client.xml file
            // ######################################################
            // Change this place to change the wsa:toepr
            // epr = something
            // ######################################################

            if (epr != null) {
                if (!epr.hasNoneAddress()) {
                    writeMessageWithCommons(msgContext, epr, format);
                }else{
                	if(msgContext.isFault()){
                		if(log.isDebugEnabled()){
                			log.debug("Fault sent to WS-A None URI: "+msgContext.getEnvelope().getBody().getFault());
                		}
                	}
                }
            } else {
                if (msgContext.getProperty(MessageContext.TRANSPORT_OUT) != null) {
                    sendUsingOutputStream(msgContext, format);
                    TransportUtils.setResponseWritten(msgContext, true);
                } else {
                    throw new AxisFault("Both the TO and MessageContext.TRANSPORT_OUT property " +
                            "are null, so nowhere to send");
                }
            }
        } catch (FactoryConfigurationError e) {
            log.debug(e);
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            log.debug(e);
            throw AxisFault.makeFault(e);
        }
        return InvocationResponse.CONTINUE;
    }

    /**
     * Send a message (which must be a response) via the OutputStream sitting in the
     * MessageContext TRANSPORT_OUT property.  Since this class is used for both requests and
     * responses, we split the logic - this method always gets called when we're
     * writing to the HTTP response stream, and sendUsingCommons() is used for requests.
     *
     * @param msgContext the active MessageContext
     * @param format output formatter for our message
     * @throws AxisFault if a general problem arises
     */
    private void sendUsingOutputStream(MessageContext msgContext,
                                       OMOutputFormat format) throws AxisFault {
        OutputStream out = (OutputStream) msgContext.getProperty(MessageContext.TRANSPORT_OUT);

        // I Don't think we need this check.. Content type needs to be set in
        // any case. (thilina)
        // if (msgContext.isServerSide()) {
        OutTransportInfo transportInfo = (OutTransportInfo) msgContext
                .getProperty(Constants.OUT_TRANSPORT_INFO);

        if (transportInfo == null) throw new AxisFault("No transport info in MessageContext");

        ServletBasedOutTransportInfo servletBasedOutTransportInfo = null;
        if (transportInfo instanceof ServletBasedOutTransportInfo) {
            servletBasedOutTransportInfo =
                    (ServletBasedOutTransportInfo) transportInfo;

            // if sending a fault, set HTTP status code to 500
            if (msgContext.isFault()) {
                servletBasedOutTransportInfo.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

            Object customHeaders = msgContext.getProperty(HTTPConstants.HTTP_HEADERS);
            if (customHeaders != null) {
                if (customHeaders instanceof List) {
                    Iterator iter = ((List) customHeaders).iterator();
                    while (iter.hasNext()) {
                        Header header = (Header) iter.next();
                        if (header != null) {
                            servletBasedOutTransportInfo
                                    .addHeader(header.getName(), header.getValue());
                        }
                    }
                } else if (customHeaders instanceof Map) {
                    Iterator iter = ((Map) customHeaders).entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry header = (Map.Entry) iter.next();
                        if (header != null) {
                            servletBasedOutTransportInfo
                                    .addHeader((String) header.getKey(), (String) header.getValue());
                        }
                    }
                }
            }
        } else if (transportInfo instanceof AxisHttpResponseImpl) {
            Object customHeaders = msgContext.getProperty(HTTPConstants.HTTP_HEADERS);
            if (customHeaders != null) {
                if (customHeaders instanceof List) {
                    Iterator iter = ((List) customHeaders).iterator();
                    while (iter.hasNext()) {
                        Header header = (Header) iter.next();
                        if (header != null) {
                            ((AxisHttpResponseImpl) transportInfo)
                                    .addHeader(header.getName(), header.getValue());
                        }
                    }
                } else if (customHeaders instanceof Map) {
                    Iterator iter = ((Map) customHeaders).entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry header = (Map.Entry) iter.next();
                        if (header != null) {
                            ((AxisHttpResponseImpl) transportInfo)
                                    .addHeader((String) header.getKey(), (String) header.getValue());
                        }
                    }
                }
            }
        }

        format.setAutoCloseWriter(true);

        MessageFormatter messageFormatter = MessageProcessorSelector.getMessageFormatter(msgContext);
        if (messageFormatter == null) throw new AxisFault("No MessageFormatter in MessageContext");

        // Once we get to this point, exceptions should NOT be turned into faults and sent,
        // because we're already sending!  So catch everything and log it, but don't pass
        // upwards.

        try {
            transportInfo.setContentType(
                messageFormatter.getContentType(msgContext, format, findSOAPAction(msgContext)));

            Object gzip = msgContext.getOptions().getProperty(HTTPConstants.MC_GZIP_RESPONSE);
            if (gzip != null && JavaUtils.isTrueExplicitly(gzip)) {
                if (servletBasedOutTransportInfo != null)
                    servletBasedOutTransportInfo.addHeader(HTTPConstants.HEADER_CONTENT_ENCODING,
                                                           HTTPConstants.COMPRESSION_GZIP);
                try {
                    out = new GZIPOutputStream(out);
                    out.write(messageFormatter.getBytes(msgContext, format));
                    ((GZIPOutputStream) out).finish();
                    out.flush();
                } catch (IOException e) {
                    throw new AxisFault("Could not compress response");
                }
            } else {
                messageFormatter.writeTo(msgContext, format, out, false);
            }
        } catch (AxisFault axisFault) {
            log.error(axisFault.getMessage(), axisFault);
            throw axisFault;
        }
    }

    private void writeMessageWithCommons(MessageContext messageContext,
                                         EndpointReference toEPR, OMOutputFormat format)
            throws AxisFault {
        try {
            URL url = new URL(toEPR.getAddress());

            // select the Message Sender depending on the REST status
            AbstractHTTPSender sender;

            sender = new HTTPSender();

            boolean chunked;
            if (messageContext.getProperty(HTTPConstants.CHUNKED) != null) {
                chunked = JavaUtils.isTrueExplicitly(messageContext
                        .getProperty(HTTPConstants.CHUNKED));
            } else {
                chunked = defaultChunked;
            }

            String httpVersion;
            if (messageContext.getProperty(HTTPConstants.HTTP_PROTOCOL_VERSION) != null) {
                httpVersion = (String) messageContext
                        .getProperty(HTTPConstants.HTTP_PROTOCOL_VERSION);
            } else {
                httpVersion = defaultHttpVersion;
            }
            // Following order needed to be preserved because,
            // HTTP/1.0 does not support chunk encoding
            sender.setChunked(chunked);
            sender.setHttpVersion(httpVersion);
            sender.setFormat(format);

            sender.send(messageContext, url, findSOAPAction(messageContext));
        } catch (MalformedURLException e) {
            log.debug(e);
            throw AxisFault.makeFault(e);
        } catch (HttpException e) {
            log.debug(e);
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            log.debug(e);
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * @param actionString
     * @return true if the specified String represents a generated (anonymous name)
     */
    public static boolean isGeneratedName(String actionString) {
        if (actionString == null) {
            return false;
        }
        
        // Different releases may have different constructed names or 
        // namespaces or quames.  However all equal or end with the following
        // sequences
        if (actionString.indexOf("anon") >= 0) {
            if (actionString.equals("anonOutInOp") ||
                actionString.endsWith(":anonOutInOp") ||
                actionString.endsWith("/anonOutInOp") ||
                actionString.endsWith("}anonOutInOp") ||

                actionString.equals("anonOutonlyOp") ||
                actionString.endsWith(":anonOutonlyOp") ||
                actionString.endsWith("/anonOutonlyOp") ||
                actionString.endsWith("}anonOutonlyOp") ||

                actionString.equals("anonRobustOp") ||
                actionString.endsWith(":anonRobustOp") ||
                actionString.endsWith("/anonRobustOp") ||
                actionString.endsWith("}anonRobustOp") ) {
                return true;
            }
        }
        return false;
    }

    
    private static String findSOAPAction(MessageContext messageContext) {
        String soapActionString = null;

        Parameter parameter =
                messageContext.getTransportOut().getParameter(HTTPConstants.OMIT_SOAP_12_ACTION);
        if (parameter != null && JavaUtils.isTrueExplicitly(parameter.getValue()) &&
            !messageContext.isSOAP11()) {
            return "\"\"";
        }

        Object disableSoapAction = messageContext.getOptions().getProperty(
                Constants.Configuration.DISABLE_SOAP_ACTION);

        if (!JavaUtils.isTrueExplicitly(disableSoapAction)) {
            // first try to get the SOAP action from message context
            soapActionString = messageContext.getSoapAction();
            if (log.isDebugEnabled()) {
                log.debug("SOAP Action from messageContext : (" + soapActionString + ")");
                
            }
            if (isGeneratedName(soapActionString)) {
                if (log.isDebugEnabled()) {
                    log.debug("Will not use SOAP Action because (" + soapActionString + ") was auto-generated");
                }
                soapActionString = null;
            }
            if ((soapActionString == null) || (soapActionString.length() == 0)) {
                // now let's try to get WSA action
                soapActionString = messageContext.getWSAAction();
                if (log.isDebugEnabled()) {
                    log.debug("SOAP Action from getWSAAction was : (" + soapActionString + ")");
                }
                if (messageContext.getAxisOperation() != null
                        && ((soapActionString == null) || (soapActionString
                        .length() == 0))) {
                    // last option is to get it from the axis operation
                    String axisOpSOAPAction = messageContext.getAxisOperation().
                        getSoapAction();
                    if (log.isDebugEnabled()) {
                        log.debug("SOAP Action from AxisOperation was : (" + axisOpSOAPAction + ")");
                    }
                    if (isGeneratedName(axisOpSOAPAction)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Will not override SOAP Action because (" + axisOpSOAPAction + ") in AxisOperation was auto-generated");
                        }   
                    } else {
                        soapActionString = axisOpSOAPAction;
                    }   
                }
            }
        }

        //Since action is optional for SOAP 1.2 we can return null here.
        if (soapActionString == null && messageContext.isSOAP11()) {
            soapActionString = "\"\"";
        }

        return soapActionString;
    }
}
