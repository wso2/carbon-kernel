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

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.server.AxisHttpRequest;
import org.apache.axis2.transport.http.server.AxisHttpResponse;
import org.apache.axis2.transport.http.server.HttpUtils;
import org.apache.axis2.transport.http.server.Worker;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.apache.axis2.util.JavaUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EncodingUtils;
import org.apache.ws.commons.schema.XmlSchema;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HTTPWorker implements Worker {

    public HTTPWorker() {
    }

    public void service(
            final AxisHttpRequest request,
            final AxisHttpResponse response,
            final MessageContext msgContext) throws HttpException, IOException {
        ConfigurationContext configurationContext = msgContext.getConfigurationContext();
        final String servicePath = configurationContext.getServiceContextPath();
        final String contextPath =
                (servicePath.startsWith("/") ? servicePath : "/" + servicePath) + "/";

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String soapAction = HttpUtils.getSoapAction(request);
        InvocationResponse pi;

        if (method.equals(HTTPConstants.HEADER_GET)) {
            if (uri.equals("/favicon.ico")) {
                response.setStatus(HttpStatus.SC_MOVED_PERMANENTLY);
                response.addHeader(new BasicHeader("Location", "http://ws.apache.org/favicon.ico"));
                return;
            }
            if (!uri.startsWith(contextPath)) {
                response.setStatus(HttpStatus.SC_MOVED_PERMANENTLY);
                response.addHeader(new BasicHeader("Location", contextPath));
                return;
            }
            if (uri.endsWith("axis2/services/")) {
                String s = HTTPTransportReceiver.getServicesHTML(configurationContext);
                response.setStatus(HttpStatus.SC_OK);
                response.setContentType("text/html");
                OutputStream out = response.getOutputStream();
                out.write(EncodingUtils.getBytes(s, HTTP.ISO_8859_1));
                return;
            }
            if (uri.indexOf("?") < 0) {
                if (!uri.endsWith(contextPath)) {
                    if (uri.endsWith(".xsd") || uri.endsWith(".wsdl")) {
                        HashMap services = configurationContext.getAxisConfiguration().getServices();
                        String file = uri.substring(uri.lastIndexOf("/") + 1,
                                uri.length());
                        if ((services != null) && !services.isEmpty()) {
                            Iterator i = services.values().iterator();
                            while (i.hasNext()) {
                                AxisService service = (AxisService) i.next();
                                InputStream stream = service.getClassLoader().
                                getResourceAsStream("META-INF/" + file);
                                if (stream != null) {
                                    OutputStream out = response.getOutputStream();
                                    response.setContentType("text/xml");
                                    ListingAgent.copy(stream, out);
                                    out.flush();
                                    out.close();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            if (uri.endsWith("?wsdl2")) {
                String serviceName = uri.substring(uri.lastIndexOf("/") + 1, uri.length() - 6);
                HashMap services = configurationContext.getAxisConfiguration().getServices();
                AxisService service = (AxisService) services.get(serviceName);
                if (service != null) {
                    boolean canExposeServiceMetadata = canExposeServiceMetadata(service);
                    if (canExposeServiceMetadata) {
                        response.setStatus(HttpStatus.SC_OK);
                        response.setContentType("text/xml");
                        service.printWSDL2(response.getOutputStream(), getHost(request));
                    } else {
                        response.setStatus(HttpStatus.SC_FORBIDDEN);
                    }
                    return;
                }
            }
            if (uri.endsWith("?wsdl")) {
                /**
                 * service name can be hierarchical (axis2/services/foo/1.0.0/Version?wsdl) or
                 * normal (axis2/services/Version?wsdl).
                 */
                String[] temp = uri.split(configurationContext.getServiceContextPath() + "/");
                String serviceName = temp[1].substring(0, temp[1].length() - 5);
                
                HashMap services = configurationContext.getAxisConfiguration().getServices();
                AxisService service = (AxisService) services.get(serviceName);
                if (service != null) {
                    boolean canExposeServiceMetadata = canExposeServiceMetadata(service);
                    if (canExposeServiceMetadata) {
                        response.setStatus(HttpStatus.SC_OK);
                        response.setContentType("text/xml");
                        service.printWSDL(response.getOutputStream(), getHost(request));
                    } else {
                        response.setStatus(HttpStatus.SC_FORBIDDEN);
                    }
                    return;
                }
            }
            if (uri.endsWith("?xsd")) {
                String serviceName = uri.substring(uri.lastIndexOf("/") + 1, uri.length() - 4);
                HashMap services = configurationContext.getAxisConfiguration().getServices();
                AxisService service = (AxisService) services.get(serviceName);
                if (service != null) {
                    boolean canExposeServiceMetadata = canExposeServiceMetadata(service);
                    if (canExposeServiceMetadata) {
                        response.setStatus(HttpStatus.SC_OK);
                        response.setContentType("text/xml");
                        service.printSchema(response.getOutputStream());
                    } else {
                        response.setStatus(HttpStatus.SC_FORBIDDEN);
                    }
                    return;
                }
            }
            //cater for named xsds - check for the xsd name
            if (uri.indexOf("?xsd=") > 0) {
            	// fix for imported schemas
            	String[] uriParts = uri.split("[?]xsd=");
                String serviceName =
                        uri.substring(uriParts[0].lastIndexOf("/") + 1, uriParts[0].length());
                String schemaName = uri.substring(uri.lastIndexOf("=") + 1);

                HashMap services = configurationContext.getAxisConfiguration().getServices();
                AxisService service = (AxisService) services.get(serviceName);
                if (service != null) {
                    boolean canExposeServiceMetadata = canExposeServiceMetadata(service);
                    if (!canExposeServiceMetadata) {
                        response.setStatus(HttpStatus.SC_FORBIDDEN);
                        return;
                    }
                    //run the population logic just to be sure
                    service.populateSchemaMappings();
                    //write out the correct schema
                    Map schemaTable = service.getSchemaMappingTable();
                    XmlSchema schema = (XmlSchema) schemaTable.get(schemaName);
                    if (schema == null) {
                        int dotIndex = schemaName.indexOf('.');
                        if (dotIndex > 0) {
                            String schemaKey = schemaName.substring(0,dotIndex);
                            schema = (XmlSchema) schemaTable.get(schemaKey);
                        }
                    }
                    //schema found - write it to the stream
                    if (schema != null) {
                        response.setStatus(HttpStatus.SC_OK);
                        response.setContentType("text/xml");
                        schema.write(response.getOutputStream());
                        return;
                    } else {
                        InputStream instream = service.getClassLoader()
                            .getResourceAsStream(DeploymentConstants.META_INF + "/" + schemaName);
                        
                        if (instream != null) {
                            response.setStatus(HttpStatus.SC_OK);
                            response.setContentType("text/xml");
                            OutputStream outstream = response.getOutputStream();
                            boolean checkLength = true;
                            int length = Integer.MAX_VALUE;
                            int nextValue = instream.read();
                            if (checkLength) length--;
                            while (-1 != nextValue && length >= 0) {
                                outstream.write(nextValue);
                                nextValue = instream.read();
                                if (checkLength) length--;
                            }
                            outstream.flush();
                            return;
                        } else {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            int ret = service.printXSD(baos, schemaName);
                            if(ret>0) {
                                baos.flush();
                                instream = new ByteArrayInputStream(baos.toByteArray());
                                response.setStatus(HttpStatus.SC_OK);
                                response.setContentType("text/xml");
                                OutputStream outstream = response.getOutputStream();
                                boolean checkLength = true;
                                int length = Integer.MAX_VALUE;
                                int nextValue = instream.read();
                                if (checkLength) length--;
                                while (-1 != nextValue && length >= 0) {
                                    outstream.write(nextValue);
                                    nextValue = instream.read();
                                    if (checkLength) length--;
                                }
                                outstream.flush();
                                return;
                            }
                            // no schema available by that name  - send 404
                            response.sendError(HttpStatus.SC_NOT_FOUND, "Schema Not Found!");
                            return;
                        }
                    }
                }
            }
            if (uri.indexOf("?wsdl2=") > 0) {
                String serviceName =
                        uri.substring(uri.lastIndexOf("/") + 1, uri.lastIndexOf("?wsdl2="));
                if (processInternalWSDL(uri, configurationContext, serviceName, response, getHost(request))) return;
            }
            if (uri.indexOf("?wsdl=") > 0) {
                String serviceName =
                        uri.substring(uri.lastIndexOf("/") + 1, uri.lastIndexOf("?wsdl="));
                if (processInternalWSDL(uri, configurationContext, serviceName, response, getHost(request))) return;
            }

            String contentType = null;
            Header[] headers = request.getHeaders(HTTPConstants.HEADER_CONTENT_TYPE);
            if (headers != null && headers.length > 0) {
                contentType = headers[0].getValue();
                int index = contentType.indexOf(';');
                if (index > 0) {
                    contentType = contentType.substring(0, index);
                }
            }
            
            // deal with GET request
            pi = RESTUtil.processURLRequest(
                    msgContext, 
                    response.getOutputStream(), 
                    contentType);

        } else if (method.equals(HTTPConstants.HEADER_POST)) {
            // deal with POST request

            String contentType = request.getContentType();
            
            if (HTTPTransportUtils.isRESTRequest(contentType)) {
                pi = RESTUtil.processXMLRequest(
                        msgContext, 
                        request.getInputStream(),
                        response.getOutputStream(), 
                        contentType);
            } else {
                String ip = (String)msgContext.getProperty(MessageContext.TRANSPORT_ADDR);
                if (ip != null){
                    uri = ip + uri;
                }
                pi = HTTPTransportUtils.processHTTPPostRequest(
                        msgContext, 
                        request.getInputStream(),
                        response.getOutputStream(),
                        contentType, 
                        soapAction, 
                        uri);
            }

        } else if (method.equals(HTTPConstants.HEADER_PUT)) {

            String contentType = request.getContentType();
            msgContext.setProperty(Constants.Configuration.CONTENT_TYPE, contentType);
            
            pi = RESTUtil.processXMLRequest(
                    msgContext, 
                    request.getInputStream(),
                    response.getOutputStream(), 
                    contentType);

        } else if (method.equals(HTTPConstants.HEADER_DELETE)) {

            pi = RESTUtil.processURLRequest(
                    msgContext, 
                    response.getOutputStream(), 
                    null);

        } else {
            throw new MethodNotSupportedException(method + " method not supported");
        }
        
        Boolean holdResponse =
            (Boolean) msgContext.getProperty(RequestResponseTransport.HOLD_RESPONSE);
        if (pi.equals(InvocationResponse.SUSPEND) ||
                (holdResponse != null && Boolean.TRUE.equals(holdResponse))) {
            try {
                ((RequestResponseTransport) msgContext
                        .getProperty(RequestResponseTransport.TRANSPORT_CONTROL)).awaitResponse();
            }
            catch (InterruptedException e) {
                throw new IOException("We were interrupted, so this may not function correctly:" +
                        e.getMessage());
            }
        }
        
        // Finalize response
        RequestResponseTransport requestResponseTransportControl =
            (RequestResponseTransport) msgContext.
            getProperty(RequestResponseTransport.TRANSPORT_CONTROL);

        if (TransportUtils.isResponseWritten(msgContext) ||
            ((requestResponseTransportControl != null) &&
             requestResponseTransportControl.getStatus().equals(
                RequestResponseTransport.RequestResponseTransportStatus.SIGNALLED))) {
            // The response is written or signalled.  The current status is used (probably SC_OK).
        } else {
            // The response may be ack'd, mark the status as accepted.
            response.setStatus(HttpStatus.SC_ACCEPTED);
        }
    }

    /**
     * Checks whether exposing the WSDL & WSDL elements such as schema & policy have been allowed
     *
     * @param service  The AxisService which needs to be verified
     * @throws IOException If exposing WSDL & WSDL elements has been restricted.
     * @return true - if service metadata can be exposed, false - otherwise
     */
    private boolean canExposeServiceMetadata(AxisService service) throws IOException {
        Parameter exposeServiceMetadata = service.getParameter("exposeServiceMetadata");
        if (exposeServiceMetadata != null &&
            JavaUtils.isFalseExplicitly(exposeServiceMetadata.getValue())) {
            return false;
        }
        return true;
    }

    private boolean processInternalWSDL(String uri, ConfigurationContext configurationContext, 
                                        String serviceName, AxisHttpResponse response, String ip) 
    throws IOException {
        String wsdlName = uri.substring(uri.lastIndexOf("=") + 1);

        HashMap services = configurationContext.getAxisConfiguration().getServices();
        AxisService service = (AxisService) services.get(serviceName);

        if (service != null) {
            response.setStatus(HttpStatus.SC_OK);
            response.setContentType("text/xml");
            service.printUserWSDL(response.getOutputStream(), wsdlName, ip);
            response.getOutputStream().flush();
            return true;

        } else {
            // no schema available by that name  - send 404
            response.sendError(HttpStatus.SC_NOT_FOUND, "Schema Not Found!");
            return true;
        }

    }

    public String getHost(AxisHttpRequest request) throws java.net.SocketException {
        String host = null;
        Header hostHeader = request.getFirstHeader("host");
        if (hostHeader != null) {
            String parts[] = hostHeader.getValue().split("[:]");
            if (parts.length > 0) {
                host = parts[0].trim();
            }
        }
        return host;
    }

}
