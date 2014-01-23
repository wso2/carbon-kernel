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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.deployment.WarBasedAxisConfigurator;
import org.apache.axis2.description.AxisBindingMessage;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.util.QueryStringParser;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.util.OnDemandLogger;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Servlet implementing the HTTP and HTTPS transport. Note that this class doesn't implement
 * {@link TransportListener}. There are two reasons for this:
 * <ul>
 * <li>There must be one instance of {@link TransportListener} for each protocol, but this servlet
 * may implement both HTTP and HTTPS.
 * <li>There is a collision between {@link TransportListener#destroy()} and
 * {@link javax.servlet.Servlet#destroy()}.
 * </ul>
 * The {@link TransportListener} implementation is provided by {@link AxisServletListener}. An
 * instance of that class must be declared in <tt>axis2.xml</tt> for each protocol (HTTP/HTTPS) that
 * the servlet should accept.
 */
public class AxisServlet extends HttpServlet {
    private static final long serialVersionUID = 3105135058353738906L;
    
    static final OnDemandLogger log = new OnDemandLogger(AxisServlet.class);
    public static final String CONFIGURATION_CONTEXT = "CONFIGURATION_CONTEXT";
    public static final String SESSION_ID = "SessionId";
    
    private static final Set<String> metadataQueryParamNames = new HashSet<String>(
            Arrays.asList("wsdl2", "wsdl", "xsd", "policy"));
    
    protected transient ConfigurationContext configContext;
    protected transient AxisConfiguration axisConfiguration;

    protected transient ServletConfig servletConfig;

    protected transient ListingAgent agent;
    protected transient String contextRoot = null;

    protected boolean disableREST = false;
    private static final String LIST_SERVICES_SUFFIX = "/services/listServices";
    private static final String LIST_FAULTY_SERVICES_SUFFIX = "/services/ListFaultyServices";
    private boolean closeReader = true;

    private static final int BUFFER_SIZE = 1024 * 8;
    
    private boolean initCalled = false;
    
    private transient AxisServletListener httpListener;
    private transient AxisServletListener httpsListener;

    /**
     * Implementaion of POST interface
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //set the initial buffer for a larger value
        response.setBufferSize(BUFFER_SIZE);

        
        preprocessRequest(request);

        MessageContext msgContext;
        OutputStream out = response.getOutputStream();
        String contentType = request.getContentType();
        if (!HTTPTransportUtils.isRESTRequest(contentType)) {
            msgContext = createMessageContext(request, response);
            msgContext.setProperty(Constants.Configuration.CONTENT_TYPE, contentType);
            try {
                // adding ServletContext into msgContext;
                String url = request.getRequestURL().toString();
                
                OutputStream bufferedOut = new BufferedOutputStream(out);
                
                InvocationResponse pi = HTTPTransportUtils.
                        processHTTPPostRequest(msgContext,
                                new BufferedInputStream(request.getInputStream()),
                                bufferedOut,
                                contentType,
                                request.getHeader(HTTPConstants.HEADER_SOAP_ACTION),
                                url);

                Boolean holdResponse =
                        (Boolean) msgContext.getProperty(RequestResponseTransport.HOLD_RESPONSE);

                if (pi.equals(InvocationResponse.SUSPEND) ||
                        (holdResponse != null && Boolean.TRUE.equals(holdResponse))) {
                    ((RequestResponseTransport) msgContext
                            .getProperty(RequestResponseTransport.TRANSPORT_CONTROL))
                            .awaitResponse();
                }

                // if data has not been sent back and this is not a signal response
                if (!TransportUtils.isResponseWritten(msgContext)  
                		&& (((RequestResponseTransport) 
                				msgContext.getProperty(
                						RequestResponseTransport.TRANSPORT_CONTROL)).
                						getStatus() != RequestResponseTransport.
                						RequestResponseTransportStatus.SIGNALLED)) {
                    response.setStatus(HttpServletResponse.SC_ACCEPTED);
                    // only set contentType in this scenario, not if response already set
                    log.debug("Response not written. Setting response contentType to text/xml; " +
                            "charset=" +msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING));
                    response.setContentType("text/xml; charset="
                            + msgContext
                            .getProperty(Constants.Configuration.CHARACTER_SET_ENCODING));
                }
                
                // Make sure that no data remains in the BufferedOutputStream even if the message
                // formatter doesn't call flush
                bufferedOut.flush();

            } catch (AxisFault e) {
                setResponseState(msgContext, response);
                log.debug(e);
                if (msgContext != null) {
                    processAxisFault(msgContext, response, out, e);
                } else {
                    throw new ServletException(e);
                }
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
                try {
                    // If the fault is not going along the back channel we should be 202ing
                    if (AddressingHelper.isFaultRedirected(msgContext)) {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                        AxisBindingOperation axisBindingOperation =
                                (AxisBindingOperation) msgContext
                                        .getProperty(Constants.AXIS_BINDING_OPERATION);
                        if (axisBindingOperation != null) {
                            AxisBindingMessage axisBindingMessage = axisBindingOperation.getFault(
                                    (String) msgContext.getProperty(Constants.FAULT_NAME));
                            if(axisBindingMessage != null){
                                Integer code = (Integer) axisBindingMessage
                                        .getProperty(WSDL2Constants.ATTR_WHTTP_CODE);
                                if (code != null) {
                                    response.setStatus(code.intValue());
                                }
                            }
                        }
                    }
                    handleFault(msgContext, out, new AxisFault(t.toString(), t));
                } catch (AxisFault e2) {
                    log.info(e2);
                    throw new ServletException(e2);
                }
            } finally {
                closeStaxBuilder(msgContext);
                TransportUtils.deleteAttachments(msgContext);
            }
        } else {
            if (!disableREST) {
                new RestRequestProcessor(Constants.Configuration.HTTP_METHOD_POST, request, response)
                        .processXMLRequest();
            } else {
                showRestDisabledErrorMessage(response);
            }
        }
    }

    /**
     * Implementation for GET interface
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        preprocessRequest(request);

        // this method is also used to serve for the listServices request.

        String requestURI = request.getRequestURI();
        String query = request.getQueryString();

        // There can be three different request coming to this.
        // 1. wsdl, wsdl2 and xsd requests
        // 2. list services requests
        // 3. REST requests.
        if ((query != null) && new QueryStringParser(query).search(metadataQueryParamNames)) {
            // handling meta data exchange stuff
            agent.processListService(request, response);
        } else if (requestURI.endsWith(".xsd") ||
                requestURI.endsWith(".wsdl")) {
            agent.processExplicitSchemaAndWSDL(request, response);
        } else if (requestURI.endsWith(LIST_SERVICES_SUFFIX) ||
                requestURI.endsWith(LIST_FAULTY_SERVICES_SUFFIX)) {
            // handling list services request
            try {
                agent.handle(request, response);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        } else if (!disableREST) {
            new RestRequestProcessor(Constants.Configuration.HTTP_METHOD_GET, request, response)
                    .processURLRequest();
        } else {
            showRestDisabledErrorMessage(response);
        }
    }

    /**
     * Implementation of DELETE interface
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest request,
                            HttpServletResponse response) throws ServletException, IOException {

        preprocessRequest(request);
        // this method is also used to serve for the listServices request.
        if (!disableREST) {
            new RestRequestProcessor(Constants.Configuration.HTTP_METHOD_DELETE, request, response)
                    .processURLRequest();
        } else {
            showRestDisabledErrorMessage(response);
        }
    }

    /**
     * Implementation of PUT interface
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPut(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        preprocessRequest(request);
        // this method is also used to serve for the listServices request.
        if (!disableREST) {
            new RestRequestProcessor(Constants.Configuration.HTTP_METHOD_PUT, request, response)
                    .processXMLRequest();
        } else {
            showRestDisabledErrorMessage(response);
        }
    }

    /**
     * Private method that deals with disabling of REST support.
     *
     * @param response
     * @throws IOException
     */
    protected void showRestDisabledErrorMessage(HttpServletResponse response) throws IOException {
        PrintWriter writer = new PrintWriter(response.getOutputStream());
        writer.println("<html><body><h2>Please enable REST support in WEB-INF/conf/axis2.xml " +
                "and WEB-INF/web.xml</h2></body></html>");
        writer.flush();
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
    }

    /**
     * Close the builders.
     *
     * @param messageContext
     * @throws ServletException
     */
    void closeStaxBuilder(MessageContext messageContext) throws ServletException {
        if (closeReader && messageContext != null) {
            try {
                SOAPEnvelope envelope = messageContext.getEnvelope();
                if(envelope != null) {
                    StAXBuilder builder = (StAXBuilder) envelope.getBuilder();
                    if (builder != null) {
                        builder.close();
                    }
                }
            } catch (Exception e) {
                log.debug(e.toString(), e);
            }
        }
    }

    /**
     * Processing for faults
     *
     * @param msgContext
     * @param res
     * @param out
     * @param e
     */
    void processAxisFault(MessageContext msgContext, HttpServletResponse res,
                          OutputStream out, AxisFault e) {
        try {
            // If the fault is not going along the back channel we should be 202ing
            if (AddressingHelper.isFaultRedirected(msgContext)) {
                res.setStatus(HttpServletResponse.SC_ACCEPTED);
            } else {

                String status =
                        (String) msgContext.getProperty(Constants.HTTP_RESPONSE_STATE);
                if (status == null) {
                    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } else {
                    res.setStatus(Integer.parseInt(status));
                }

                AxisBindingOperation axisBindingOperation =
                        (AxisBindingOperation) msgContext
                                .getProperty(Constants.AXIS_BINDING_OPERATION);
                if (axisBindingOperation != null) {
                    AxisBindingMessage fault = axisBindingOperation
                            .getFault((String) msgContext.getProperty(Constants.FAULT_NAME));
                    if (fault != null) {
                        Integer code = (Integer) fault.getProperty(WSDL2Constants.ATTR_WHTTP_CODE);
                        if (code != null) {
                            res.setStatus(code.intValue());
                        }
                    }
                }
            }
            handleFault(msgContext, out, e);
        } catch (AxisFault e2) {
            log.info(e2);
        }
    }

    protected void handleFault(MessageContext msgContext, OutputStream out, AxisFault e)
            throws AxisFault {
        msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);

        MessageContext faultContext =
                MessageContextBuilder.createFaultMessageContext(msgContext, e);
        // SOAP 1.2 specification mentions that we should send HTTP code 400 in a fault if the
        // fault code Sender
        HttpServletResponse response =
                (HttpServletResponse) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE);
        if (response != null) {

            //TODO : Check for SOAP 1.2!
            SOAPFaultCode code = faultContext.getEnvelope().getBody().getFault().getCode();

            OMElement valueElement = null;
            if (code != null) {
                valueElement = code.getFirstChildWithName(new QName(
                        SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                        SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME));
            }

            if (valueElement != null) {
                if (SOAP12Constants.FAULT_CODE_SENDER.equals(valueElement.getTextAsQName().getLocalPart())
                        && !msgContext.isDoingREST()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }
        AxisEngine.sendFault(faultContext);
    }

    /**
     * Main init method
     *
     * @param config The ServletConfig
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        
        // prevent this method from being called more than once per instance
        initCalled = true;
        super.init(config);
        try {
            this.servletConfig = config;
            ServletContext servletContext = servletConfig.getServletContext();
            this.configContext =
                    (ConfigurationContext) servletContext.getAttribute(CONFIGURATION_CONTEXT);
            if(configContext == null){
                configContext = initConfigContext(config);
                config.getServletContext().setAttribute(CONFIGURATION_CONTEXT, configContext);
            }
            axisConfiguration = configContext.getAxisConfiguration();
            initTransports();
            initGetRequestProcessors(config);
            initParams();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Initialize HTTP GET request processors
     *
     * @param config The ServletConfig of this Servlet
     */
    protected void initGetRequestProcessors(ServletConfig config) {
        // The ListingAgent is an HTTP GET request processor
        agent = new ListingAgent(configContext);
    }

    /**
     * Initialize HTTP transports
     *
     * @throws AxisFault If an error occurs while initializing transports
     */
    protected void initTransports() throws AxisFault {
        httpListener = getAxisServletListener(Constants.TRANSPORT_HTTP);
        httpsListener = getAxisServletListener(Constants.TRANSPORT_HTTPS);

        if (httpListener == null && httpsListener == null) {
            log.warn("No transportReceiver for " + AxisServletListener.class.getName() +
                     " found. An instance for HTTP will be configured automatically. " +
                     "Please update your axis2.xml file!");
            httpListener = new AxisServletListener();
            TransportInDescription transportInDescription = new TransportInDescription(
                    Constants.TRANSPORT_HTTP);
            transportInDescription.setReceiver(httpListener);
            axisConfiguration.addTransportIn(transportInDescription);
        } else if (httpListener != null && httpsListener != null
                   && httpListener.getPort() == -1 && httpsListener.getPort() == -1) {
            log.warn("If more than one transportReceiver for " +
                     AxisServletListener.class.getName() + " exists, then all instances " +
                     "must be configured with a port number. WSDL generation will be " +
                     "unreliable.");
        }

        ListenerManager listenerManager = new ListenerManager();
        listenerManager.init(configContext);
        listenerManager.start();
    }

    private AxisServletListener getAxisServletListener(String name) {
        TransportInDescription desc = axisConfiguration.getTransportIn(name);
        if (desc == null) {
            return null;
        }
        TransportListener receiver = desc.getReceiver();
        if (receiver instanceof AxisServletListener) {
            return (AxisServletListener)receiver;
        } else {
            return null;
        }
    }

    /**
     * distroy the ConfigurationContext
     */
    @Override
    public void destroy() {
        //stoping listner manager
        try {
            if (configContext != null) {
                configContext.terminate();
            }
        } catch (AxisFault axisFault) {
            log.info(axisFault.getMessage());
        }
        try {
            super.destroy();
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        // AXIS2-4898: MultiThreadedHttpConnectionManager starts a thread that is not stopped by the
        // shutdown of the connection manager. If we want to avoid a resource leak, we need to call
        // shutdownAll here.
        MultiThreadedHttpConnectionManager.shutdownAll();
    }

    /**
     * Initializes the Axis2 parameters.
     */
    protected void initParams() {
        Parameter parameter;
        // do we need to completely disable REST support
        parameter = axisConfiguration.getParameter(Constants.Configuration.DISABLE_REST);
        if (parameter != null) {
            disableREST = !JavaUtils.isFalseExplicitly(parameter.getValue());
        }

        // Should we close the reader(s)
        parameter = axisConfiguration.getParameter("axis2.close.reader");
        if (parameter != null) {
            closeReader = JavaUtils.isTrueExplicitly(parameter.getValue());
        }

    }

    /**
     * Convenient method to re-initialize the ConfigurationContext
     *
     * @throws ServletException
     */
    @Override
    public void init() throws ServletException {
        if (this.servletConfig != null && !initCalled) {
            init(this.servletConfig);
        }
    }

    /**
     * Initialize the Axis configuration context
     *
     * @param config Servlet configuration
     * @return ConfigurationContext
     * @throws ServletException
     */
    protected ConfigurationContext initConfigContext(ServletConfig config) throws ServletException {
        try {
            ConfigurationContext configContext =
                    ConfigurationContextFactory
                            .createConfigurationContext(new WarBasedAxisConfigurator(config));
            configContext.setProperty(Constants.CONTAINER_MANAGED, Constants.VALUE_TRUE);
            return configContext;
        } catch (Exception e) {
            log.info(e);
            throw new ServletException(e);
        }
    }

    /**
     * Set the context root if it is not set already.
     *
     * @param req
     */
    public void initContextRoot(HttpServletRequest req) {
        if (contextRoot != null && contextRoot.trim().length() != 0) {
            return;
        }
        String contextPath = req.getContextPath();
        //handling ROOT scenario, for servlets in the default (root) context, this method returns ""
        if (contextPath != null && contextPath.length() == 0) {
            contextPath = "/";
        }
        this.contextRoot = contextPath;

        configContext.setContextRoot(contextRoot);
    }
    
    /**
     * Preprocess the request. This will:
     * <ul>
     * <li>Set the context root if it is not set already (by calling
     * {@link #initContextRoot(HttpServletRequest)}).
     * <li>Remember the port number if port autodetection is enabled.
     * <li>Reject the request if no {@link AxisServletListener} has been registered for the
     * protocol.
     * </ul>
     * 
     * @param req the request to preprocess
     */
    // This method should not be part of the public API. In particular we must not allow subclasses
    // to override this method because we don't make any guarantees as to when exactly this method
    // is called.
    private void preprocessRequest(HttpServletRequest req) throws ServletException {
        initContextRoot(req);

        TransportInDescription transportInDescription =
                req.isSecure()? this.axisConfiguration.getTransportIn(Constants.TRANSPORT_HTTPS) :
                        this.axisConfiguration.getTransportIn(Constants.TRANSPORT_HTTP);

        if (transportInDescription == null){
            throw new ServletException(req.getScheme() + " is forbidden");
        } else {
            if (transportInDescription.getReceiver() instanceof AxisServletListener){
                AxisServletListener listner = (AxisServletListener) transportInDescription.getReceiver();
                // Autodetect the port number if necessary
                if (listner.getPort() == -1){
                    listner.setPort(req.getServerPort());
                }
            }
        }
        
    }

    /**
     * Get all transport headers.
     *
     * @param req
     * @return Map
     */
    protected Map<String,String> getTransportHeaders(HttpServletRequest req) {
        return new TransportHeaders(req);
    }

    /**
     * @param request
     * @param response
     * @param invocationType : If invocationType=true; then this will be used in SOAP message
     *                       invocation. If invocationType=false; then this will be used in REST message invocation.
     * @return MessageContext
     * @throws IOException
     */
    protected MessageContext createMessageContext(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  boolean invocationType) throws IOException {
        MessageContext msgContext = configContext.createMessageContext();
        String requestURI = request.getRequestURI();

        String trsPrefix = request.getRequestURL().toString();
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

        if (!invocationType) {
            String query = request.getQueryString();
            if (query != null) {
                requestURI = requestURI + "?" + query;
            }
        }

        msgContext.setTo(new EndpointReference(requestURI));
        msgContext.setFrom(new EndpointReference(request.getRemoteAddr()));
        msgContext.setProperty(MessageContext.REMOTE_ADDR, request.getRemoteAddr());
        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                new ServletBasedOutTransportInfo(response));
        // set the transport Headers
        msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, getTransportHeaders(request));
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, request);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE, response);
        try {
            ServletContext context = getServletContext();
            if(context != null) {
                msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT, context);
            }
        } catch (Exception e){
            log.debug(e.getMessage(), e);
        }

        //setting the RequestResponseTransport object
        msgContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL,
                new ServletRequestResponseTransport());

        return msgContext;
    }

    /**
     * This method assumes, that the created MessageContext will be used in only SOAP invocation.
     *
     * @param req
     * @param resp
     * @return MessageContext
     * @throws IOException
     */

    protected MessageContext createMessageContext(HttpServletRequest req,
                                                  HttpServletResponse resp) throws IOException {
        return createMessageContext(req, resp, true);
    }

    protected class ServletRequestResponseTransport implements RequestResponseTransport {
        private boolean responseWritten = false;
        private CountDownLatch responseReadySignal = new CountDownLatch(1);
		// The initial status must be WAITING, as the main servlet will do some other
		// work after setting this RequestResponseTransport up, and we don't want to miss
		// signals that come in before this thread gets to the awaitResponse call.
        private RequestResponseTransportStatus status = RequestResponseTransportStatus.WAITING;
        AxisFault faultToBeThrownOut = null;

        public void acknowledgeMessage(MessageContext msgContext) throws AxisFault {
            status = RequestResponseTransportStatus.ACKED;
            responseReadySignal.countDown();
        }

        public void awaitResponse()
                throws InterruptedException, AxisFault {
            log.debug("Blocking servlet thread -- awaiting response");
            responseReadySignal.await();

            if (faultToBeThrownOut != null) {
                throw faultToBeThrownOut;
            }
        }

        public void signalResponseReady() {
            log.debug("Signalling response available");
            status = RequestResponseTransportStatus.SIGNALLED;
            responseReadySignal.countDown();
        }

        public RequestResponseTransportStatus getStatus() {
            return status;
        }

        public void signalFaultReady(AxisFault fault) {
            faultToBeThrownOut = fault;
            signalResponseReady();
        }
        
        public boolean isResponseWritten() {
        	return responseWritten;
        }
        
        public void setResponseWritten(boolean responseWritten) {
        	this.responseWritten = responseWritten;
        }
        
    }

    void setResponseState(MessageContext messageContext, HttpServletResponse response) {
        String state = (String) messageContext.getProperty(Constants.HTTP_RESPONSE_STATE);
        if (state != null) {
            int stateInt = Integer.parseInt(state);
            if (stateInt == HttpServletResponse.SC_UNAUTHORIZED) { // Unauthorized
                String realm = (String) messageContext.getProperty(Constants.HTTP_BASIC_AUTH_REALM);
                response.addHeader("WWW-Authenticate",
                        "basic realm=\"" + realm + "\"");
            }
        }
    }

    /**
     * Ues in processing REST related Requests.
     * This is the helper Class use in processing of doGet, doPut , doDelete and doPost.
     */
    protected class RestRequestProcessor {
        protected MessageContext messageContext;
        private HttpServletRequest request;
        private HttpServletResponse response;

        public RestRequestProcessor(String httpMethodString,
                                    HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {
            this.request = request;
            this.response = response;
            messageContext = createMessageContext(this.request, this.response, false);
            messageContext.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_METHOD,
                    httpMethodString);
        }

        public void processXMLRequest() throws IOException, ServletException {
            try {
                RESTUtil.processXMLRequest(messageContext, request.getInputStream(),
                        response.getOutputStream(), request.getContentType());
                this.checkResponseWritten();
            } catch (AxisFault axisFault) {
                processFault(axisFault);
            }
            closeStaxBuilder(messageContext);
        }

        public void processURLRequest() throws IOException, ServletException {
            try {
                RESTUtil.processURLRequest(messageContext, response.getOutputStream(),
                        request.getContentType());
                this.checkResponseWritten();
            } catch (AxisFault e) {
                setResponseState(messageContext, response);
                processFault(e);
            }
            closeStaxBuilder(messageContext);

        }

        private void checkResponseWritten() {
        	if (!TransportUtils.isResponseWritten(messageContext)) {
        		response.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
        }

        private void processFault(AxisFault e) throws ServletException, IOException {
            log.debug(e);
            if (messageContext != null) {
                processAxisFault(messageContext, response, response.getOutputStream(), e);
            } else {
                throw new ServletException(e);
            }

        }

    }
}
