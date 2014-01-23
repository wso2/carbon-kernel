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


import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URL;

public class HTTPSender extends AbstractHTTPSender {

    private static final Log log = LogFactory.getLog(HTTPSender.class);

    public void send(MessageContext msgContext, URL url, String soapActionString)
            throws IOException {

        // execute the HtttpMethodBase - a connection manager can be given for
        // handle multiple

        String httpMethod =
                (String) msgContext.getProperty(Constants.Configuration.HTTP_METHOD);

        if ((httpMethod != null)) {

            if (Constants.Configuration.HTTP_METHOD_GET.equalsIgnoreCase(httpMethod)) {
                this.sendViaGet(msgContext, url, soapActionString);

                return;
            } else if (Constants.Configuration.HTTP_METHOD_DELETE.equalsIgnoreCase(httpMethod)) {
                this.sendViaDelete(msgContext, url, soapActionString);

                return;
            } else if (Constants.Configuration.HTTP_METHOD_PUT.equalsIgnoreCase(httpMethod)) {
                this.sendViaPut(msgContext, url, soapActionString);

                return;
            }
        }

        this.sendViaPost(msgContext, url, soapActionString);
    }

    /**
     * Used to send a request via HTTP Get method
     *
     * @param msgContext        - The MessageContext of the message
     * @param url               - The target URL
     * @param soapActiionString - The soapAction string of the request
     * @throws AxisFault - Thrown in case an exception occurs
     */
    private void sendViaGet(MessageContext msgContext, URL url, String soapActiionString)
            throws AxisFault {

        GetMethod getMethod = new GetMethod();
        HttpClient httpClient = getHttpClient(msgContext);
        MessageFormatter messageFormatter =
                populateCommonProperties(msgContext, url, getMethod, httpClient, soapActiionString);

        // Need to have this here because we can have soap action when using the soap response MEP
        String soapAction =
                messageFormatter.formatSOAPAction(msgContext, format, soapActiionString);

        if (soapAction != null && !msgContext.isDoingREST()) {
            getMethod.setRequestHeader(HTTPConstants.HEADER_SOAP_ACTION, soapAction);
        }
        try {
            executeMethod(httpClient, msgContext, url, getMethod);
            handleResponse(msgContext, getMethod);
        } catch (IOException e) {
            log.info("Unable to sendViaGet to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            cleanup(msgContext, getMethod);
        }
    }

    private void cleanup(MessageContext msgContext, HttpMethod method) {
        if (msgContext.isPropertyTrue(HTTPConstants.AUTO_RELEASE_CONNECTION)) {
            log.trace("AutoReleasing " + method);
            method.releaseConnection();
        }
    }

    /**
     * Used to send a request via HTTP Delete Method
     *
     * @param msgContext        - The MessageContext of the message
     * @param url               - The target URL
     * @param soapActiionString - The soapAction string of the request
     * @throws AxisFault - Thrown in case an exception occurs
     */
    private void sendViaDelete(MessageContext msgContext, URL url, String soapActiionString)
            throws AxisFault {

        DeleteMethod deleteMethod = new DeleteMethod();
        HttpClient httpClient = getHttpClient(msgContext);
        populateCommonProperties(msgContext, url, deleteMethod, httpClient, soapActiionString);

        try {
            executeMethod(httpClient, msgContext, url, deleteMethod);
            handleResponse(msgContext, deleteMethod);
        } catch (IOException e) {
            log.info("Unable to sendViaDelete to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            cleanup(msgContext, deleteMethod);
        }
    }

    /**
     * Used to send a request via HTTP Post Method
     *
     * @param msgContext       - The MessageContext of the message
     * @param url              - The target URL
     * @param soapActionString - The soapAction string of the request
     * @throws AxisFault - Thrown in case an exception occurs
     */
    private void sendViaPost(MessageContext msgContext, URL url,
                             String soapActionString) throws AxisFault {


        HttpClient httpClient = getHttpClient(msgContext);

/*  What's up with this, it never gets used anywhere?? --Glen
        String charEncoding =
                (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

        if (charEncoding == null) {
            charEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }
*/

        PostMethod postMethod = new PostMethod();
        if (log.isTraceEnabled()) {
            log.trace(Thread.currentThread() + " PostMethod " + postMethod + " / " + httpClient);
        }
        MessageFormatter messageFormatter =
                populateCommonProperties(msgContext, url, postMethod, httpClient, soapActionString);

        postMethod.setRequestEntity(new AxisRequestEntity(messageFormatter,
                                                          msgContext, format, soapActionString,
                                                          chunked, isAllowedRetry));

        if (!httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10) && chunked) {
            postMethod.setContentChunked(true);
        }

        String soapAction = messageFormatter.formatSOAPAction(msgContext, format, soapActionString);

        if (soapAction != null && !msgContext.isDoingREST()) {
            postMethod.setRequestHeader(HTTPConstants.HEADER_SOAP_ACTION, soapAction);
        }

        /*
         *   main excecution takes place..
         */
        try {
            executeMethod(httpClient, msgContext, url, postMethod);
            handleResponse(msgContext, postMethod);
        } catch (IOException e) {
            log.info("Unable to sendViaPost to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            cleanup(msgContext, postMethod);
        }
    }

    /**
     * Used to send a request via HTTP Put Method
     *
     * @param msgContext       - The MessageContext of the message
     * @param url              - The target URL
     * @param soapActionString - The soapAction string of the request
     * @throws AxisFault - Thrown in case an exception occurs
     */
    private void sendViaPut(MessageContext msgContext, URL url,
                            String soapActionString) throws AxisFault {


        HttpClient httpClient = getHttpClient(msgContext);

/*  Same deal - this value never gets used, why is it here? --Glen
        String charEncoding =
                (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

        if (charEncoding == null) {
            charEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }
*/

        PutMethod putMethod = new PutMethod();
        MessageFormatter messageFormatter =
                populateCommonProperties(msgContext, url, putMethod, httpClient, soapActionString);

        putMethod.setRequestEntity(new AxisRequestEntity(messageFormatter,
                                                         msgContext, format, soapActionString,
                                                         chunked, isAllowedRetry));

        if (!httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10) && chunked) {
            putMethod.setContentChunked(true);
        }

        String soapAction = messageFormatter.formatSOAPAction(msgContext, format, soapActionString);
        if (soapAction != null && !msgContext.isDoingREST()) {
            putMethod.setRequestHeader(HTTPConstants.HEADER_SOAP_ACTION, soapAction);
        }

        /*
         *   main excecution takes place..
         */
        try {
            executeMethod(httpClient, msgContext, url, putMethod);
            handleResponse(msgContext, putMethod);
        } catch (IOException e) {
            log.info("Unable to sendViaPut to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            cleanup(msgContext, putMethod);
        }
    }

    /**
     * Used to handle the HTTP Response
     *
     * @param msgContext - The MessageContext of the message
     * @param method     - The HTTP method used
     * @throws IOException - Thrown in case an exception occurs
     */
    private void handleResponse(MessageContext msgContext,
                                HttpMethodBase method) throws IOException {
        int statusCode = method.getStatusCode();
        HTTPStatusCodeFamily family = getHTTPStatusCodeFamily(statusCode);
        log.trace("Handling response - " + statusCode);
        if (statusCode == HttpStatus.SC_ACCEPTED) {
        	/* When an HTTP 202 Accepted code has been received, this will be the case of an execution 
        	 * of an in-only operation. In such a scenario, the HTTP response headers should be returned,
        	 * i.e. session cookies. */
        	obtainHTTPHeaderInformation(method, msgContext);
        	// Since we don't expect any content with a 202 response, we must release the connection
        	method.releaseConnection();
        } else if (HTTPStatusCodeFamily.SUCCESSFUL.equals(family)) {
            // Save the HttpMethod so that we can release the connection when cleaning up
            msgContext.setProperty(HTTPConstants.HTTP_METHOD, method);
            processResponse(method, msgContext);
        } else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR ||
                statusCode == HttpStatus.SC_BAD_REQUEST) {
            // Save the HttpMethod so that we can release the connection when cleaning up
            msgContext.setProperty(HTTPConstants.HTTP_METHOD, method);
            Header contenttypeHeader =
                    method.getResponseHeader(HTTPConstants.HEADER_CONTENT_TYPE);
            String value = null;
            if (contenttypeHeader != null) {
                value = contenttypeHeader.getValue();
            }
             OperationContext opContext = msgContext.getOperationContext();
            if(opContext!=null){
                MessageContext inMessageContext =
                        opContext.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if(inMessageContext!=null){
                    inMessageContext.setProcessingFault(true);
                }
            }
            if (value != null) {

                processResponse(method, msgContext);
            }
            
            if (org.apache.axis2.util.Utils.isClientThreadNonBlockingPropertySet(msgContext)) {
            	 throw new AxisFault(Messages.getMessage("transportError",
                         String.valueOf(statusCode),
                         method.getStatusText()));
            }
        } else {
            // Since we don't process the response, we must release the connection immediately
            method.releaseConnection();
            throw new AxisFault(Messages.getMessage("transportError",
                                                    String.valueOf(statusCode),
                                                    method.getStatusText()));
        }
    }

    /**
     * +     * Used to determine the family of HTTP status codes to which the given code
     * +     * belongs.
     * +     *
     * +     * @param statusCode - The HTTP status code
     * +
     */
    protected HTTPStatusCodeFamily getHTTPStatusCodeFamily(int statusCode) {
        switch (statusCode / 100) {
            case 1:
                return HTTPStatusCodeFamily.INFORMATIONAL;
            case 2:
                return HTTPStatusCodeFamily.SUCCESSFUL;
            case 3:
                return HTTPStatusCodeFamily.REDIRECTION;
            case 4:
                return HTTPStatusCodeFamily.CLIENT_ERROR;
            case 5:
                return HTTPStatusCodeFamily.SERVER_ERROR;
            default:
                return HTTPStatusCodeFamily.OTHER;
        }
    }

    /**
     * The set of HTTP status code families.
     */
    protected enum HTTPStatusCodeFamily {
        INFORMATIONAL, SUCCESSFUL, REDIRECTION, CLIENT_ERROR, SERVER_ERROR, OTHER
    }
}
