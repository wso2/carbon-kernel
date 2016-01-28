package org.wso2.carbon.axis2.runtime.bridge.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.axis2.runtime.bridge.CarbonAxis2Bridge;
import org.wso2.carbon.axis2.runtime.bridge.CarbonAxis2BridgeException;
import org.wso2.carbon.axis2.runtime.bridge.ResponseStatus;
import org.wso2.carbon.axis2.runtime.internal.DataHolder;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is HttpCarbonAxis2Bridge.
 *
 * @since 1.0.0
 */
public class HttpInCarbonAxis2Bridge implements TransportListener, CarbonAxis2Bridge {
    private static final Logger logger = LoggerFactory.getLogger(HttpInCarbonAxis2Bridge.class);

    @Override
    public void init(ConfigurationContext configurationContext, TransportInDescription transportInDescription)
            throws AxisFault {

    }

    @Override
    public void start() throws AxisFault {
        DataHolder.getInstance().getBundleContext().registerService(CarbonAxis2Bridge.class, this, null);
        logger.info("HttpInCarbonAxis2Bridge is started");
    }

    @Override
    public void stop() throws AxisFault {
        logger.info("HttpInCarbonAxis2Bridge is stopped");
    }

    @Override
    public EndpointReference[] getEPRsForService(String s, String s1) throws AxisFault {
        return new EndpointReference[0];
    }

    @Override
    public SessionContext getSessionContext(MessageContext messageContext) {
        return null;
    }

    @Override
    public void destroy() {

    }

    @Override
    public ResponseStatus processMessage(InputStream inputStream, OutputStream outputStream, String contentType,
                               String soapActionHeader, String requestUri) throws CarbonAxis2BridgeException {

        MessageContext messageContext = createMessageContext(requestUri);
        messageContext.setProperty(Constants.Configuration.CONTENT_TYPE, contentType);

        Handler.InvocationResponse pi;
        try {
            pi = HTTPTransportUtils.
                    processHTTPPostRequest(messageContext, inputStream, outputStream, contentType, soapActionHeader,
                            requestUri);

            if (pi.equals(Handler.InvocationResponse.SUSPEND)) {
                // TODO : Need to await for the response
                if (logger.isDebugEnabled()) {
                    logger.debug("InvocationResponse.SUSPEND");
                }
            }

            return ResponseStatus.READY;
        } catch (AxisFault axisFault) {
            throw new CarbonAxis2BridgeException("Failed to process message", axisFault);
        }
    }

    protected MessageContext createMessageContext(String requestUri) {
        ConfigurationContext configurationContext = DataHolder.getInstance().getConfigurationContext();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();

        MessageContext msgContext = configurationContext.createMessageContext();
        //String requestURI = request.getRequestURI();

//        String trsPrefix = request.getRequestURL().toString();
        String trsPrefix = requestUri;
        int sepindex = trsPrefix.indexOf(':');
        if (sepindex > -1) {
            trsPrefix = trsPrefix.substring(0, sepindex);
            msgContext.setIncomingTransportName(trsPrefix);
        } else {
            msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);
            trsPrefix = Constants.TRANSPORT_HTTP;
        }
        TransportInDescription transportIn =
                axisConfiguration.getTransportIn(msgContext.getIncomingTransportName());
        //set the default output description. This will be http

        TransportOutDescription transportOut = axisConfiguration.getTransportOut(trsPrefix);
        if (transportOut == null) {
            // if the req coming via https but we do not have a https sender
            transportOut = axisConfiguration.getTransportOut(Constants.TRANSPORT_HTTP);
        }


        msgContext.setTransportIn(transportIn);
        msgContext.setTransportOut(transportOut);
        msgContext.setServerSide(true);

//        if (!invocationType) {
//            String query = request.getQueryString();
//            if (query != null) {
//                requestURI = requestURI + "?" + query;
//            }
//        }

        msgContext.setTo(new EndpointReference(requestUri));
//        msgContext.setFrom(new EndpointReference(request.getRemoteAddr()));
//        msgContext.setProperty(MessageContext.REMOTE_ADDR, request.getRemoteAddr());
//        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
//                new ServletBasedOutTransportInfo(response));
        // set the transport Headers
//        msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, getTransportHeaders(request));
//        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, request);
//        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE, response);
//        try {
//            ServletContext context = getServletContext();
//            if(context != null) {
//                msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT, context);
//            }
//        } catch (Exception e){
//            log.debug(e.getMessage(), e);
//        }

        //setting the RequestResponseTransport object
//        msgContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL,
//                new ServletRequestResponseTransport());

        return msgContext;
    }
}
