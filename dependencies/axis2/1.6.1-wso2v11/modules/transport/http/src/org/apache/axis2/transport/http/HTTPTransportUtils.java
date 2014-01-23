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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class HTTPTransportUtils {
    private static final Log log = LogFactory.getLog(HTTPTransportUtils.class);

    /**
     * @deprecated This was used only by the now deprecated processHTTPGetRequest() method.
     */
    public static SOAPEnvelope createEnvelopeFromGetRequest(String requestUrl,
                                                            Map map, ConfigurationContext configCtx)
            throws AxisFault {
        String[] values =
                Utils.parseRequestURLForServiceAndOperation(requestUrl,
                                                            configCtx.getServiceContextPath());
        if (values == null) {
            return OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        }

        if ((values[1] != null) && (values[0] != null)) {
            String srvice = values[0];
            AxisService service = configCtx.getAxisConfiguration().getService(srvice);
            if (service == null) {
                throw new AxisFault("service not found: " + srvice);
            }
            String operation = values[1];
            SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
            OMNamespace omNs = soapFactory.createOMNamespace(service.getSchemaTargetNamespace(),
                                                             service.getSchemaTargetNamespacePrefix());
            soapFactory.createOMNamespace(service.getSchemaTargetNamespace(),
                                          service.getSchemaTargetNamespacePrefix());
            OMElement opElement = soapFactory.createOMElement(operation, omNs);
            Iterator it = map.keySet().iterator();

            while (it.hasNext()) {
                String name = (String) it.next();
                String value = (String) map.get(name);
                OMElement omEle = soapFactory.createOMElement(name, omNs);

                omEle.setText(value);
                opElement.addChild(omEle);
            }

            envelope.getBody().addChild(opElement);

            return envelope;
        } else {
            return null;
        }
    }

    /**
     * @param msgContext           - The MessageContext of the Request Message
     * @param out                  - The output stream of the response
     * @param soapAction           - SoapAction of the request
     * @param requestURI           - The URL that the request came to
     * @param configurationContext - The Axis Configuration Context
     * @param requestParameters    - The parameters of the request message
     * @return - boolean indication whether the operation was succesfull
     * @throws AxisFault - Thrown in case a fault occurs
     * @deprecated use RESTUtil.processURLRequest(MessageContext msgContext, OutputStream out, String contentType) instead
     */

    public static boolean processHTTPGetRequest(MessageContext msgContext,
                                                OutputStream out, String soapAction,
                                                String requestURI,
                                                ConfigurationContext configurationContext,
                                                Map requestParameters)
            throws AxisFault {
        if ((soapAction != null) && soapAction.startsWith("\"") && soapAction.endsWith("\"")) {
            soapAction = soapAction.substring(1, soapAction.length() - 1);
        }

        msgContext.setSoapAction(soapAction);
        msgContext.setTo(new EndpointReference(requestURI));
        msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);
        msgContext.setServerSide(true);
        SOAPEnvelope envelope = HTTPTransportUtils.createEnvelopeFromGetRequest(requestURI,
                                                                                requestParameters,
                                                                                configurationContext);

        if (envelope == null) {
            return false;
        } else {
            msgContext.setDoingREST(true);
            msgContext.setEnvelope(envelope);
            AxisEngine.receive(msgContext);
            return true;
        }
    }

    private static final int VERSION_UNKNOWN = 0;
    private static final int VERSION_SOAP11 = 1;
    private static final int VERSION_SOAP12 = 2;

    public static InvocationResponse processHTTPPostRequest(MessageContext msgContext,
                                                            InputStream in,
                                                            OutputStream out,
                                                            String contentType,
                                                            String soapActionHeader,
                                                            String requestURI)
            throws AxisFault {
        int soapVersion = VERSION_UNKNOWN;
        try {
            soapVersion = initializeMessageContext(msgContext, soapActionHeader, requestURI, contentType);
            msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);

            msgContext.setEnvelope(
                    TransportUtils.createSOAPMessage(
                            msgContext,
                            handleGZip(msgContext, in), 
                            contentType));
            return AxisEngine.receive(msgContext);
        } catch (SOAPProcessingException e) {
            throw AxisFault.makeFault(e);
        } catch (AxisFault e) {
            throw e;
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        } catch (OMException e) {
            throw AxisFault.makeFault(e);
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        } catch (FactoryConfigurationError e) {
            throw AxisFault.makeFault(e);
        } finally {
            if ((msgContext.getEnvelope() == null) && soapVersion != VERSION_SOAP11) {
                msgContext.setEnvelope(OMAbstractFactory.getSOAP12Factory().getDefaultEnvelope());
            }
        }
    }

    public static int initializeMessageContext(MessageContext msgContext,
                                                String soapActionHeader,
                                                String requestURI,
                                                String contentType) {
        int soapVersion = VERSION_UNKNOWN;
        // remove the starting and trailing " from the SOAP Action
        if ((soapActionHeader != null) 
                && soapActionHeader.length() > 0 
                && soapActionHeader.charAt(0) == '\"'
                && soapActionHeader.endsWith("\"")) {
            soapActionHeader = soapActionHeader.substring(1, soapActionHeader.length() - 1);
        }

        // fill up the Message Contexts
        msgContext.setSoapAction(soapActionHeader);
        msgContext.setTo(new EndpointReference(requestURI));
        msgContext.setServerSide(true);

        // get the type of char encoding
        String charSetEnc = BuilderUtil.getCharSetEncoding(contentType);
        if (charSetEnc == null) {
            charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }
        msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);

        if (contentType != null) {
            if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
                soapVersion = VERSION_SOAP12;
                TransportUtils.processContentTypeForAction(contentType, msgContext);
            } else if (contentType
                    .indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
                soapVersion = VERSION_SOAP11;
            } else if (isRESTRequest(contentType)) {
                // If REST, construct a SOAP11 envelope to hold the rest message and
                // indicate that this is a REST message.
                soapVersion = VERSION_SOAP11;
                msgContext.setDoingREST(true);
            }
            if (soapVersion == VERSION_SOAP11) {
                // TODO Keith : Do we need this anymore
                // Deployment configuration parameter
            	Parameter disableREST = msgContext
                        .getParameter(Constants.Configuration.DISABLE_REST);
            	if (soapActionHeader == null && disableREST != null) {
            		if (Constants.VALUE_FALSE.equals(disableREST.getValue())) {
                        // If the content Type is text/xml (BTW which is the
                        // SOAP 1.1 Content type ) and the SOAP Action is
                        // absent it is rest !!
                        msgContext.setDoingREST(true);
                    }
                }
            }
        }
        return soapVersion;
    }

    public static InputStream handleGZip(MessageContext msgContext, InputStream in)
            throws IOException {
        Map headers = (Map) msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);

        if (headers != null) {
            if (HTTPConstants.COMPRESSION_GZIP
                    .equals(headers.get(HTTPConstants.HEADER_CONTENT_ENCODING)) ||
                    HTTPConstants.COMPRESSION_GZIP.equals(headers.get(
                            HTTPConstants.HEADER_CONTENT_ENCODING_LOWERCASE))) {
                    in = new GZIPInputStream(in);

            }
        }
        return in;
    }

    /**
     * This will match for content types that will be regarded as REST in WSDL2.0.
     * This contains,
     * 1. application/xml
     * 2. application/x-www-form-urlencoded
     * 3. multipart/form-data
     * <p/>
     * If the request doesnot contain a content type; this will return true.
     *
     * @param contentType content type to check
     * @return Boolean
     */
    public static boolean isRESTRequest(String contentType) {
        return contentType != null &&
               (contentType.indexOf(HTTPConstants.MEDIA_TYPE_APPLICATION_XML) > -1 ||
                contentType.indexOf(HTTPConstants.MEDIA_TYPE_X_WWW_FORM) > -1 ||
                contentType.indexOf(HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA) > -1 ||
                contentType.indexOf(HTTPConstants.MEDIA_TYPE_APPLICATION_JSON) > -1);
    }
    
    public static EndpointReference[] getEPRsForService(ConfigurationContext configurationContext,
            TransportInDescription trpInDesc, String serviceName, String ip, int port) throws AxisFault {
        
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        Parameter param = axisConfiguration.getParameter(Constants.HTTP_FRONTEND_HOST_URL);
        StringBuilder epr = new StringBuilder();
        if (param != null) {
            epr.append(param.getValue());
            String servicePath = configurationContext.getServicePath();
            if (epr.charAt(epr.length()-1) != '/' && !servicePath.startsWith("/")) {
                epr.append('/');
            }
            epr.append(servicePath);
        } else {
            param = trpInDesc.getParameter(TransportListener.HOST_ADDRESS);
            if (param != null) {
                // TODO: Need to decide if we really want to deprecate this parameter.
                //       Reason to deprecate it is that it has a misleading name ("hostname"
                //       while it is actually a URL), that its role overlaps with that
                //       of the "httpFrontendHostUrl" parameter in the Axis configuration and
                //       that there might be a confusion with the "hostname" parameter in the
                //       Axis configuration (which has a different meaning).
                //       If we deprecate it, we need to remove it from all the axis2.xml sample
                //       files. Note that the same parameter seems to be used by the TCP transport,
                //       but it's role is not very clear (since TCP has no concept of request URI).
                log.warn("Transport '" + trpInDesc.getName()
                        + "' is configured with deprecated parameter '"
                        + TransportListener.HOST_ADDRESS + "'. Please set '"
                        + Constants.HTTP_FRONTEND_HOST_URL
                        + "' in the Axis configuration instead.");
                epr.append(param.getValue());
            } else {
                if (ip == null){
                    try {
                        ip = Utils.getIpAddress(configurationContext.getAxisConfiguration());
                    } catch (SocketException ex) {
                        AxisFault.makeFault(ex);
                    }
                }
                String scheme = trpInDesc.getName();
                epr.append(scheme);
                epr.append("://");
                epr.append(ip);
                if (!(scheme.equals("http") && port == 80
                        || scheme.equals("https") && port == 443)) {
                    epr.append(':');
                    epr.append(port);
                }
            }
            String serviceContextPath = configurationContext.getServiceContextPath();
            if (epr.charAt(epr.length()-1) != '/' && !serviceContextPath.startsWith("/")) {
                epr.append('/');
            }
            epr.append(serviceContextPath);
        }
        epr.append('/');
        epr.append(serviceName);
        epr.append('/');
        return new EndpointReference[]{new EndpointReference(epr.toString())};
    }
}
