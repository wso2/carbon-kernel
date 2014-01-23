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

package org.apache.axis2.transport.http.server;

import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.ProtocolException;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.UnsupportedHttpVersionException;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

/**
 * This class is an extension of the default HTTP service responsible for
 * maintaining and populating the {@link MessageContext} for incoming Axis
 * requests.
 */
public class AxisHttpService {

    private static final Log LOG = LogFactory.getLog(AxisHttpService.class);

    private final HttpProcessor httpProcessor;
    private final ConnectionReuseStrategy connStrategy;
    private final HttpResponseFactory responseFactory;
    private final ConfigurationContext configurationContext;
    private final Worker worker;

    private HttpParams params;

    public AxisHttpService(
            final HttpProcessor httpProcessor,
            final ConnectionReuseStrategy connStrategy,
            final HttpResponseFactory responseFactory,
            final ConfigurationContext configurationContext,
            final Worker worker) {
        super();
        if (httpProcessor == null) {
            throw new IllegalArgumentException("HTTP processor may not be null");
        }
        if (connStrategy == null) {
            throw new IllegalArgumentException("Connection strategy may not be null");
        }
        if (responseFactory == null) {
            throw new IllegalArgumentException("Response factory may not be null");
        }
        if (worker == null) {
            throw new IllegalArgumentException("Worker may not be null");
        }
        if (configurationContext == null) {
            throw new IllegalArgumentException("Configuration context may not be null");
        }
        this.httpProcessor = httpProcessor;
        this.connStrategy = connStrategy;
        this.responseFactory = responseFactory;
        this.configurationContext = configurationContext;
        this.worker = worker;

    }

    public HttpParams getParams() {
        return this.params;
    }

    public void setParams(final HttpParams params) {
        this.params = params;
    }

    public void handleRequest(final AxisHttpConnection conn, final HttpContext context)
            throws IOException, HttpException {

        MessageContext msgContext = configurationContext.createMessageContext();
        msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);

        if (conn != null) {
            msgContext.setProperty(MessageContext.REMOTE_ADDR,
                                   conn.getRemoteAddress().getHostAddress());
            msgContext.setProperty(MessageContext.TRANSPORT_ADDR,
                                   conn.getLocalAddress().getHostAddress());

            if (LOG.isDebugEnabled()) {
                LOG.debug("Remote address of the connection : " +
                          conn.getRemoteAddress().getHostAddress());
            }
        }

        HttpResponse response;
        try {
            HttpRequest request = conn.receiveRequest();
            RequestLine requestLine = request.getRequestLine();
            if (requestLine != null) {
                msgContext.setProperty(HTTPConstants.HTTP_METHOD, requestLine.getMethod());
            }
            request.setParams(
                new DefaultedHttpParams(request.getParams(), this.params));
            ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
            if (!ver.lessEquals(HttpVersion.HTTP_1_1)) {
                // Downgrade protocol version if greater than HTTP/1.1 
                ver = HttpVersion.HTTP_1_1;
            }

            response = this.responseFactory.newHttpResponse
                    (ver, HttpStatus.SC_OK, context);
            response.setParams(
                new DefaultedHttpParams(response.getParams(), this.params));

            if (request instanceof HttpEntityEnclosingRequest) {
                if (((HttpEntityEnclosingRequest) request).expectContinue()) {
                    HttpResponse ack = this.responseFactory.newHttpResponse
                            (ver, HttpStatus.SC_CONTINUE, context);
                    ack.setParams(
                        new DefaultedHttpParams(ack.getParams(), this.params));
                    conn.sendResponse(ack);
                    conn.flush();
                }
            }

            // Create Axis request and response objects
            AxisHttpRequestImpl axisreq = new AxisHttpRequestImpl(
                    conn,
                    request,
                    this.httpProcessor,
                    context);
            AxisHttpResponseImpl axisres = new AxisHttpResponseImpl(
                    conn,
                    response,
                    this.httpProcessor,
                    context);

            // Prepare HTTP request
            axisreq.prepare();

            // Run the service
            doService(axisreq, axisres, context, msgContext);

            // Make sure the request content is fully consumed
            InputStream instream = conn.getInputStream();
            if (instream != null) {
                instream.close();
            }

            // Commit response if not committed
            if (!axisres.isCommitted()) {
                axisres.commit();
            }

            // Make sure the response content is properly terminated
            OutputStream outstream = conn.getOutputStream();
            if (outstream != null) {
                outstream.close();
            }

        } catch (HttpException ex) {
            response = this.responseFactory.newHttpResponse
                    (HttpVersion.HTTP_1_0, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                     context);
            response.setParams(
                new DefaultedHttpParams(response.getParams(), this.params));
            handleException(ex, response);
            this.httpProcessor.process(response, context);
            conn.sendResponse(response);
        }

        conn.flush();
        if (!this.connStrategy.keepAlive(response, context)) {
            conn.close();
        } else {
            conn.reset();
        }
    }

    protected void handleException(final HttpException ex, final HttpResponse response) {
        if (ex instanceof MethodNotSupportedException) {
            response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
        } else if (ex instanceof UnsupportedHttpVersionException) {
            response.setStatusCode(HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED);
        } else if (ex instanceof ProtocolException) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        } else {
            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected void doService(
            final AxisHttpRequest request,
            final AxisHttpResponse response,
            final HttpContext context,
            final MessageContext msgContext) throws HttpException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Request method: " + request.getMethod());
            LOG.debug("Target URI: " + request.getRequestURI());
        }

        try {
            TransportOutDescription transportOut = this.configurationContext.getAxisConfiguration()
                    .getTransportOut(Constants.TRANSPORT_HTTP);
            TransportInDescription transportIn = this.configurationContext.getAxisConfiguration()
                    .getTransportIn(Constants.TRANSPORT_HTTP);

            String sessionKey = (String) context.getAttribute(HTTPConstants.COOKIE_STRING);
            msgContext.setTransportIn(transportIn);
            msgContext.setTransportOut(transportOut);
            msgContext.setServerSide(true);
            msgContext.setProperty(HTTPConstants.COOKIE_STRING, sessionKey);
            msgContext.setProperty(Constants.Configuration.TRANSPORT_IN_URL,
                                   request.getRequestURI());

            // set the transport Headers
            HashMap headerMap = new HashMap();
            for (Iterator it = request.headerIterator(); it.hasNext();) {
                Header header = (Header) it.next();
                headerMap.put(header.getName(), header.getValue());
            }
            msgContext.setProperty(MessageContext.TRANSPORT_HEADERS,
                                   headerMap);
            msgContext.setProperty(Constants.Configuration.CONTENT_TYPE,
                                   request.getContentType());

            msgContext.setProperty(MessageContext.TRANSPORT_OUT,
                                   response.getOutputStream());
            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                                   response);
            msgContext.setTo(new EndpointReference(request.getRequestURI()));
            msgContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL,
                                   new SimpleHTTPRequestResponseTransport());


            this.worker.service(request, response, msgContext);
        } catch (SocketException ex) {
            // Socket is unreliable. 
            throw ex;
        } catch (HttpException ex) {
            // HTTP protocol violation. Transport is unreliable
            throw ex;
        } catch (Throwable e) {

            msgContext.setProperty(MessageContext.TRANSPORT_OUT,
                                   response.getOutputStream());
            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                                   response);

            MessageContext faultContext =
                    MessageContextBuilder.createFaultMessageContext(msgContext, e);
            // If the fault is not going along the back channel we should be 202ing
            if (AddressingHelper.isFaultRedirected(msgContext)) {
                response.setStatus(HttpStatus.SC_ACCEPTED);
            } else {
                String state = (String) msgContext.getProperty(Constants.HTTP_RESPONSE_STATE);
                if (state != null) {
                    int stateInt = Integer.parseInt(state);
                    response.setStatus(stateInt);
                    if (stateInt == HttpServletResponse.SC_UNAUTHORIZED) { // Unauthorized
                        String realm =
                                (String) msgContext.getProperty(Constants.HTTP_BASIC_AUTH_REALM);
                        response.addHeader("WWW-Authenticate",
                                           "basic realm=\"" + realm + "\"");
                    }
                } else {
                    if (e instanceof AxisFault) {
                        response.sendError(getStatusFromAxisFault((AxisFault)e), e.getMessage());
                    } else {
                        response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                           "Internal server error");
                    }
                }
            }
            AxisEngine.sendFault(faultContext);
        }
    }

    public int getStatusFromAxisFault(AxisFault fault) {
        QName faultCode = fault.getFaultCode();
        if (SOAP12Constants.QNAME_SENDER_FAULTCODE.equals(faultCode) ||
                SOAP11Constants.QNAME_SENDER_FAULTCODE.equals(faultCode)) {
            return HttpServletResponse.SC_BAD_REQUEST;
        }

        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    class SimpleHTTPRequestResponseTransport implements RequestResponseTransport {

        private CountDownLatch responseReadySignal = new CountDownLatch(1);
        RequestResponseTransportStatus status = RequestResponseTransportStatus.WAITING;
        AxisFault faultToBeThrownOut = null;
        private boolean responseWritten = false;

        public void acknowledgeMessage(MessageContext msgContext) throws AxisFault {
            //TODO: Once the core HTTP API allows us to return an ack before unwinding, then the should be fixed
            status = RequestResponseTransportStatus.ACKED;
            responseReadySignal.countDown();
        }

        public void awaitResponse() throws InterruptedException, AxisFault {
            responseReadySignal.await();

            if (faultToBeThrownOut != null) {
                throw faultToBeThrownOut;
            }
        }

        public void signalResponseReady() {
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

}
