package org.wso2.carbon.axis2.runtime.bridge.http;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import javax.xml.parsers.FactoryConfigurationError;

/**
 * This is HttpCarbonAxis2Bridge.
 *
 * @since 1.0.0
 */
public class HttpCarbonAxis2TransportSender implements TransportSender {
    private static final Logger logger = LoggerFactory.getLogger(HttpCarbonAxis2TransportSender.class);

    private TransportOutDescription transportOutDescription;

    @Override
    public void cleanup(MessageContext msgContext) throws AxisFault {

    }

    @Override
    public void init(ConfigurationContext confContext, TransportOutDescription transportOutDescription)
            throws AxisFault {
        this.transportOutDescription = transportOutDescription;
    }

    @Override
    public void stop() {

    }

    @Override
    public void init(HandlerDescription handlerDesc) {

    }

    @Override
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
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

//            // set the property values only if they are not set by the user explicitly
//            if (msgContext.getProperty(HTTPConstants.SO_TIMEOUT) == null) {
//                msgContext.setProperty(HTTPConstants.SO_TIMEOUT, soTimeout);
//            }
//
//            if (msgContext.getProperty(HTTPConstants.CONNECTION_TIMEOUT) == null) {
//                msgContext.setProperty(HTTPConstants.CONNECTION_TIMEOUT, connectionTimeout);
//            }

            //if a parameter has set been set, we will omit the SOAP action for SOAP 1.2
            if (!msgContext.isSOAP11()) {
                Parameter param = transportOutDescription.getParameter(HTTPConstants.OMIT_SOAP_12_ACTION);
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
//            EndpointReference epr = null;
//            String transportURL = (String) msgContext
//                    .getProperty(Constants.Configuration.TRANSPORT_URL);

//            if (transportURL != null) {
//                epr = new EndpointReference(transportURL);
//            } else if (msgContext.getTo() != null
//                    && !msgContext.getTo().hasAnonymousAddress()) {
//                epr = msgContext.getTo();
//            }

            // Check for the REST behavior, if you desire rest behavior
            // put a <parameter name="doREST" value="true"/> at the
            // server.xml/client.xml file
            // ######################################################
            // Change this place to change the wsa:toepr
            // epr = something
            // ######################################################

//            if (epr != null) {
//                if (!epr.hasNoneAddress()) {
//                    writeMessageWithCommons(msgContext, epr, format);
//                }else{
//                    if(msgContext.isFault()){
//                        if(log.isDebugEnabled()){
//                            log.debug("Fault sent to WS-A None URI: "+msgContext.getEnvelope().getBody().getFault());
//                        }
//                    }
//                }
//            } else {
            if (msgContext.getProperty(MessageContext.TRANSPORT_OUT) != null) {
                sendUsingOutputStream(msgContext, format);
                TransportUtils.setResponseWritten(msgContext, true);
            } else {
                throw new AxisFault("Both the TO and MessageContext.TRANSPORT_OUT property " +
                        "are null, so nowhere to send");
            }
//            }
        } catch (FactoryConfigurationError e) {
            logger.debug("Factory config error", e);
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            logger.debug("IO Error", e);
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
//        OutTransportInfo transportInfo = (OutTransportInfo) msgContext
//                .getProperty(Constants.OUT_TRANSPORT_INFO);
//
//        if (transportInfo == null) throw new AxisFault("No transport info in MessageContext");
//
//        ServletBasedOutTransportInfo servletBasedOutTransportInfo = null;
//        if (transportInfo instanceof ServletBasedOutTransportInfo) {
//            servletBasedOutTransportInfo =
//                    (ServletBasedOutTransportInfo) transportInfo;
//
//            // if sending a fault, set HTTP status code to 500
//            if (msgContext.isFault()) {
//                servletBasedOutTransportInfo.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            }
//
//            Object customHeaders = msgContext.getProperty(HTTPConstants.HTTP_HEADERS);
//            if (customHeaders != null) {
//                if (customHeaders instanceof List) {
//                    Iterator iter = ((List) customHeaders).iterator();
//                    while (iter.hasNext()) {
//                        NamedValue nv = (NamedValue) iter.next();
//                        if (nv != null) {
//                            servletBasedOutTransportInfo
//                                    .addHeader(nv.getName(), nv.getValue());
//                        }
//                    }
//                } else if (customHeaders instanceof Map) {
//                    Iterator iter = ((Map) customHeaders).entrySet().iterator();
//                    while (iter.hasNext()) {
//                        Map.Entry header = (Map.Entry) iter.next();
//                        if (header != null) {
//                            servletBasedOutTransportInfo
//                                    .addHeader((String) header.getKey(), (String) header.getValue());
//                        }
//                    }
//                }
//            }
//        } else if (transportInfo instanceof AxisHttpResponse) {
//            Object customHeaders = msgContext.getProperty(HTTPConstants.HTTP_HEADERS);
//            if (customHeaders != null) {
//                if (customHeaders instanceof List) {
//                    Iterator iter = ((List) customHeaders).iterator();
//                    while (iter.hasNext()) {
//                        NamedValue nv = (NamedValue) iter.next();
//                        if (nv != null) {
//                            ((AxisHttpResponse) transportInfo)
//                                    .addHeader(nv.getName(), nv.getValue());
//                        }
//                    }
//                } else if (customHeaders instanceof Map) {
//                    Iterator iter = ((Map) customHeaders).entrySet().iterator();
//                    while (iter.hasNext()) {
//                        Map.Entry header = (Map.Entry) iter.next();
//                        if (header != null) {
//                            ((AxisHttpResponse) transportInfo)
//                                    .addHeader((String) header.getKey(), (String) header.getValue());
//                        }
//                    }
//                }
//            }
//        }

        MessageFormatter messageFormatter = MessageProcessorSelector.getMessageFormatter(msgContext);
        if (messageFormatter == null) {
            throw new AxisFault("No MessageFormatter in MessageContext");
        }

        // Once we get to this point, exceptions should NOT be turned into faults and sent,
        // because we're already sending!  So catch everything and log it, but don't pass
        // upwards.

//        try {
//            transportInfo.setContentType(
//                    messageFormatter.getContentType(msgContext, format, findSOAPAction(msgContext)));
//
//            Object gzip = msgContext.getOptions().getProperty(HTTPConstants.MC_GZIP_RESPONSE);
//            if (gzip != null && JavaUtils.isTrueExplicitly(gzip)) {
//                if (servletBasedOutTransportInfo != null)
//                    servletBasedOutTransportInfo.addHeader(HTTPConstants.HEADER_CONTENT_ENCODING,
//                            HTTPConstants.COMPRESSION_GZIP);
//                try {
//                    out = new GZIPOutputStream(out);
//                    out.write(messageFormatter.getBytes(msgContext, format));
//                    ((GZIPOutputStream) out).finish();
//                    out.flush();
//                } catch (IOException e) {
//                    throw new AxisFault("Could not compress response");
//                }
//            } else {
                messageFormatter.writeTo(msgContext, format, out, false);
//            }
//        } catch (AxisFault axisFault) {
//            log.error(axisFault.getMessage(), axisFault);
//            throw axisFault;
//        }
    }

    @Override
    public void flowComplete(MessageContext msgContext) {

    }

    @Override
    public HandlerDescription getHandlerDesc() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Parameter getParameter(String name) {
        return null;
    }
}
