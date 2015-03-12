/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.core.multitenancy;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.dispatchers.HTTPLocationBasedDispatcher;
import org.apache.axis2.dispatchers.RequestURIBasedDispatcher;
import org.apache.axis2.dispatchers.RequestURIOperationDispatcher;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.util.Utils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * This MessageReceiver will try to locate the tenant specific AxisConfiguration and dispatch the
 * request to that AxisConfiguration. Dispatching to the actual service & operation will happen
 * within the tenant specific AxisConfiguration.
 */
public class MultitenantMessageReceiver implements MessageReceiver {

    private static final String TENANT_DELIMITER = "/t/";
	private static final Log log = LogFactory.getLog(MultitenantMessageReceiver.class);                      

    public void receive(MessageContext mainInMsgContext) throws AxisFault {

        EndpointReference toEpr = getDestinationEPR(mainInMsgContext);
        if (toEpr != null) {
            // this is a request coming in to the multitenant environment
            processRequest(mainInMsgContext);
        } else {
            // this is a response coming from a dual channel invocation or a
            // non blocking transport like esb
            processResponse(mainInMsgContext);
        }
    }

    /**
     * Process a response message coming in the multitenant environment
     *
     * @param mainInMsgContext supertenant MessageContext
     * @throws AxisFault if an error occurs
     */
    private void processResponse(MessageContext mainInMsgContext) throws AxisFault {
        MessageContext requestMsgCtx = mainInMsgContext.getOperationContext().
                getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
        if (requestMsgCtx != null) {
            MessageContext tenantRequestMsgCtx = (MessageContext)
                    requestMsgCtx.getProperty(MultitenantConstants.TENANT_REQUEST_MSG_CTX);

            if (tenantRequestMsgCtx != null) {
                MessageContext tenantResponseMsgCtx = tenantRequestMsgCtx.getOperationContext().
                        getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);

                if (tenantResponseMsgCtx == null) {
                    tenantResponseMsgCtx = new MessageContext();
                    tenantResponseMsgCtx.setOperationContext(tenantRequestMsgCtx.getOperationContext());
                }

                tenantResponseMsgCtx.setServerSide(true);
                tenantResponseMsgCtx.setDoingREST(tenantRequestMsgCtx.isDoingREST());

                Iterator itr = mainInMsgContext.getPropertyNames();
                while (itr.hasNext()) {
                     String key = (String) itr.next();
                          if (key != null) {
                           tenantResponseMsgCtx.setProperty(key, mainInMsgContext.getProperty(key));
                          }
                }
                if (tenantRequestMsgCtx.getProperty(MultitenantConstants.TENANT_DOMAIN) != null) {
                    String tenant = (String)tenantRequestMsgCtx.getProperty(MultitenantConstants.TENANT_DOMAIN);
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenant, true);
                    tenantResponseMsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN, tenant);
                }else{
                    log.warn("Tenant domain is not available in tenant request message context, hence it might not be " +
                            "set in the thread local carbon context");
                }
                tenantResponseMsgCtx.setProperty(MessageContext.TRANSPORT_IN, tenantRequestMsgCtx
                        .getProperty(MessageContext.TRANSPORT_IN));
                tenantResponseMsgCtx.setTransportIn(tenantRequestMsgCtx.getTransportIn());
                tenantResponseMsgCtx.setTransportOut(tenantRequestMsgCtx.getTransportOut());

                tenantResponseMsgCtx.setProperty(MessageContext.TRANSPORT_HEADERS,
                        mainInMsgContext.getProperty(MessageContext.TRANSPORT_HEADERS));

                tenantResponseMsgCtx.setAxisMessage(
                        tenantRequestMsgCtx.getOperationContext().getAxisOperation().
                        getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE));

                tenantResponseMsgCtx.setOperationContext(tenantRequestMsgCtx.getOperationContext());
                tenantResponseMsgCtx.setConfigurationContext(
                        tenantRequestMsgCtx.getConfigurationContext());

                tenantResponseMsgCtx.setTo(null);
                tenantResponseMsgCtx.setSoapAction(mainInMsgContext.getSoapAction());

                tenantResponseMsgCtx.setEnvelope(mainInMsgContext.getEnvelope());
                
                if(mainInMsgContext.getProperty(MultitenantConstants.PASS_THROUGH_PIPE) != null){
                	tenantResponseMsgCtx.setProperty(MultitenantConstants.PASS_THROUGH_PIPE, mainInMsgContext.getProperty(MultitenantConstants.PASS_THROUGH_PIPE));
                	tenantResponseMsgCtx.setProperty(MultitenantConstants.PASS_THROUGH_SOURCE_CONFIGURATION, mainInMsgContext.getProperty(MultitenantConstants.PASS_THROUGH_SOURCE_CONFIGURATION));
                	tenantResponseMsgCtx.setProperty("READY2ROCK", mainInMsgContext.getProperty("READY2ROCK"));
                	tenantResponseMsgCtx.setProperty(MultitenantConstants.PASS_THROUGH_SOURCE_CONNECTION, mainInMsgContext.getProperty(MultitenantConstants.PASS_THROUGH_SOURCE_CONNECTION));
                }

                tenantResponseMsgCtx.setProperty(MultitenantConstants.MESSAGE_BUILDER_INVOKED,Boolean.FALSE);
                tenantResponseMsgCtx.setProperty(MultitenantConstants.CONTENT_TYPE, mainInMsgContext.getProperty(MultitenantConstants.CONTENT_TYPE));
                AxisEngine.receive(tenantResponseMsgCtx);
            }
        }
    }

    /**
     * Process a request message coming in to the multitenant environment
     *
     * @param mainInMsgContext super tenant's MessageContext
     * @throws AxisFault if an error occurs
     */
    private void processRequest(MessageContext mainInMsgContext) throws AxisFault {
        ConfigurationContext mainConfigCtx = mainInMsgContext.getConfigurationContext();
        String to = mainInMsgContext.getTo().getAddress();
        int tenantDelimiterIndex = to.indexOf(TENANT_DELIMITER);

        String tenant;
        String serviceAndOperation;

	//for synapse nhttp transport we need to destroy the existing thread contexts and initialise the new value holders
        if (mainInMsgContext.getTransportIn() != null){
            String transportInClassName = mainInMsgContext.getTransportIn().getReceiver().getClass().getName();
            if ("org.apache.synapse.transport.nhttp.HttpCoreNIOListener".equals(transportInClassName) ||
                    "org.apache.synapse.transport.nhttp.HttpCoreNIOSSLListener".equals(transportInClassName) ||
                    "org.apache.synapse.transport.passthru.PassThroughHttpListener".equals(transportInClassName) ||
                    "org.apache.synapse.transport.passthru.PassThroughHttpSSLListener".equals(transportInClassName)
                    ){
                PrivilegedCarbonContext.destroyCurrentContext();
            }
        }


        if (tenantDelimiterIndex != -1) {
            tenant = MultitenantUtils.getTenantDomainFromUrl(to);
            serviceAndOperation = to.substring(tenantDelimiterIndex + tenant.length() + 4);
        } else {
            // in this case tenant detail is not with the url but user may have send it
            // with a soap header or an http header.
            // in that case TenantDomainHandler may have set it.
            tenant = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            serviceAndOperation = Utils.getServiceAndOperationPart(to,
                    mainInMsgContext.getConfigurationContext().getServiceContextPath());
        }

        if (tenant == null) {
            // Throw an AxisFault: Tenant not specified
            handleException(mainInMsgContext, new AxisFault("Tenant not specified"));
            return;
        }

        // this is to prevent non-blocking transports from sending 202
        mainInMsgContext.getOperationContext().setProperty(
                    Constants.RESPONSE_WRITTEN, "SKIP");

        ConfigurationContext tenantConfigCtx =
                TenantAxisUtils.getTenantConfigurationContext(tenant, mainConfigCtx);
        if (tenantConfigCtx == null) {
            // Throw AxisFault: Tenant does not exist
            handleException(mainInMsgContext, new AxisFault("Tenant " + tenant + "  not found"));
            return;
        }

        if (mainInMsgContext.isDoingREST()) { // Handle REST requests
            doREST(mainInMsgContext, to, tenant, tenantConfigCtx, serviceAndOperation);
        } else {
            doSOAP(mainInMsgContext, tenant, tenantConfigCtx, serviceAndOperation);
        }
    }

    /**
     * Process a SOAP request coming in to the multitenant environment
     * @param mainInMsgContext super tenant's message context
     * @param tenant nameof the tenant
     * @param tenantConfigCtx tenant's ConfigurationContext
     * @param serviceName name of the service
     * @throws AxisFault if an error occurs
     */
    private void doSOAP(MessageContext mainInMsgContext,
                        String tenant,
                        ConfigurationContext tenantConfigCtx,
                        String serviceName) throws AxisFault {

        // Call the correct tenant's configuration
        MessageContext tenantInMsgCtx = tenantConfigCtx.createMessageContext();
        tenantInMsgCtx.setMessageID(UUIDGenerator.getUUID());
        Options options = tenantInMsgCtx.getOptions();

        options.setTo(new EndpointReference("local://" + tenantConfigCtx.getServicePath() +
                "/" + serviceName));
        options.setAction(mainInMsgContext.getSoapAction());

        tenantInMsgCtx.setEnvelope(mainInMsgContext.getEnvelope());

        tenantInMsgCtx.setServerSide(true);
        copyProperties(mainInMsgContext, tenantInMsgCtx);

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenant, true);
        tenantInMsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN, tenant);
        try {
            // set a dummy transport out description
            String transportOutName = mainInMsgContext.getTransportOut().getName();
            TransportOutDescription transportOutDescription =
                    tenantConfigCtx.getAxisConfiguration().getTransportOut(transportOutName);
            tenantInMsgCtx.setTransportOut(transportOutDescription);
            TransportInDescription incomingTransport =
                    tenantConfigCtx.getAxisConfiguration().
                            getTransportIn(mainInMsgContext.getIncomingTransportName());
            tenantInMsgCtx.setTransportIn(incomingTransport);

            tenantInMsgCtx.setProperty(MessageContext.TRANSPORT_OUT,
                    mainInMsgContext.getProperty(MessageContext.TRANSPORT_OUT));
            tenantInMsgCtx.setProperty(Constants.OUT_TRANSPORT_INFO,
                    mainInMsgContext.getProperty(Constants.OUT_TRANSPORT_INFO));
            tenantInMsgCtx.setIncomingTransportName(mainInMsgContext.getIncomingTransportName());
            tenantInMsgCtx.setProperty(RequestResponseTransport.TRANSPORT_CONTROL,
                    mainInMsgContext.getProperty(RequestResponseTransport.TRANSPORT_CONTROL));

            // inject the message to the tenant inflow handlers
            AxisEngine.receive(tenantInMsgCtx);

            boolean nioAck = tenantInMsgCtx.isPropertyTrue("NIO-ACK-Requested", false);

            String respWritten = "";
            if (tenantInMsgCtx.getOperationContext() != null) {
                respWritten = (String) tenantInMsgCtx.getOperationContext().getProperty(Constants.RESPONSE_WRITTEN);
            }

            boolean respWillFollow = !Constants.VALUE_TRUE.equals(respWritten) && !"SKIP".equals(respWritten);

            boolean forced = tenantInMsgCtx.isPropertyTrue("FORCE_SC_ACCEPTED");

            if (forced) {
                mainInMsgContext.setProperty("FORCE_SC_ACCEPTED", true);
            }

            if (nioAck || respWillFollow || forced) {
                mainInMsgContext.setProperty("HTTP_SC",
                        tenantInMsgCtx.getProperty("HTTP_SC"));
                mainInMsgContext.setProperty("NIO-ACK-Requested", nioAck);
                mainInMsgContext
                        .removeProperty(MessageContext.TRANSPORT_HEADERS);
                Map<String, String> responseHeaders = (Map<String, String>) tenantInMsgCtx
                        .getProperty(MessageContext.TRANSPORT_HEADERS);
                mainInMsgContext.setProperty(MessageContext.TRANSPORT_HEADERS,
                        responseHeaders);
            }

            if (mainInMsgContext.getOperationContext() != null && tenantInMsgCtx.getOperationContext() != null) {
                mainInMsgContext.getOperationContext().setProperty(Constants.RESPONSE_WRITTEN,
                        tenantInMsgCtx.getOperationContext().getProperty(Constants.RESPONSE_WRITTEN));
            }
        } catch (AxisFault axisFault) {
            // at a fault flow message receiver throws a fault.
            // we need to first catch this fault and invoke the fault flow
            // then thorw the AxisFault for main flow which is catch by the carbon servlet
            // and invoke the fault handlers.

            MessageContext faultContext =
                    MessageContextBuilder.createFaultMessageContext(tenantInMsgCtx, axisFault);
            faultContext.setTransportOut(tenantInMsgCtx.getTransportOut());
            faultContext.setProperty(MultitenantConstants.TENANT_MR_STARTED_FAULT,
                    Constants.VALUE_TRUE);
            AxisEngine.sendFault(faultContext);        

            // we need to set the detial to null. otherwise the details element is copied to
            // new message context and removed from the original one
            axisFault.setDetail(null);
            MessageContext mainFaultContext = MessageContextBuilder.createFaultMessageContext(
                    mainInMsgContext, axisFault);
            mainFaultContext.setTo(faultContext.getTo());
            mainFaultContext.setSoapAction(faultContext.getSoapAction());

            mainFaultContext.setEnvelope(faultContext.getEnvelope());
            throw new AxisFault(axisFault.getMessage(), mainFaultContext);

        }
    }

    /**
     * Process a REST message coming in to the multitenant environment
     * @param mainInMsgContext SuperTenant's Message Context
     * @param to the to address
     * @param tenant tenant name
     * @param tenantConfigCtx Tentnat's configuration context
     * @param serviceName service name
     * @throws AxisFault if an error occurs
     */
    private void doREST(MessageContext mainInMsgContext,
                        String to,
                        String tenant,
                        ConfigurationContext tenantConfigCtx,
                        String serviceName) throws AxisFault {
        HttpServletRequest request =
                (HttpServletRequest) mainInMsgContext.getProperty(
                        HTTPConstants.MC_HTTP_SERVLETREQUEST);

        if (request != null) {
            HttpServletResponse response =
                (HttpServletResponse) mainInMsgContext.getProperty(
                        HTTPConstants.MC_HTTP_SERVLETRESPONSE);
            doServletRest(mainInMsgContext, to, tenant, tenantConfigCtx,
                    serviceName, request, response);
        } else {
            doNhttpREST(mainInMsgContext, to, tenant,
                    tenantConfigCtx, serviceName);
        }
    }

    /**
     * Process a REST message coming in from the Servlet transport.
     * @param mainInMsgContext supertenant's MessageContext
     * @param to the full transport url
     * @param tenant name of the tenant
     * @param tenantConfigCtx tenant ConfigurationContext
     * @param serviceName the part of the to url after the service
     * @param request servlet request
     * @param response servlet response
     * @throws AxisFault if an error occcus
     */
    private void doServletRest(MessageContext mainInMsgContext, String to,
                               String tenant, ConfigurationContext tenantConfigCtx,
                               String serviceName, HttpServletRequest request,
                               HttpServletResponse response) throws AxisFault {
        String serviceWithSlashT = TENANT_DELIMITER + tenant + "/" + serviceName;
        String requestUri = "local://" + tenantConfigCtx.getServicePath() + "/" + serviceName +
                (to.endsWith(serviceWithSlashT) ?
                        "" :
                        "/" + to.substring(to.indexOf(serviceWithSlashT) +
                                serviceWithSlashT.length() + 1));

        MultitenantRESTServlet restServlet = new MultitenantRESTServlet(
                tenantConfigCtx, requestUri, tenant);

        String httpMethod = (String) mainInMsgContext.getProperty(HTTPConstants.HTTP_METHOD);
        try {
            if (httpMethod.equals(Constants.Configuration.HTTP_METHOD_GET)) {
                restServlet.doGet(request, response);
            } else if (httpMethod.equals(Constants.Configuration.HTTP_METHOD_POST)) {
                restServlet.doPost(request, response);
            } else if (httpMethod.equals(Constants.Configuration.HTTP_METHOD_PUT)) {
                restServlet.doPut(request, response);
            } else if (httpMethod.equals(Constants.Configuration.HTTP_METHOD_DELETE)) {
                restServlet.doDelete(request, response);
            } else {
                // TODO: throw exception: Invalid verb
            }
        } catch (ServletException e) {
            throw new AxisFault(e.getMessage(), e);
        } catch (IOException e) {
            throw new AxisFault(e.getMessage(), e);
        }

        // Send the response
        MessageContext tenantOutMsgContext = restServlet.getOutMessageContext();
        MessageContext tenantOutFaultMsgContext = restServlet.getOutFaultMessageContext();

        // for a fault case both out and fault contexts are not null. so first we need to
        // check the fault context
        if (tenantOutFaultMsgContext != null) {
            // TODO: set the fault exception
            MessageContext mainOutFaultMsgContext =
                    MessageContextBuilder.createFaultMessageContext(mainInMsgContext, null);
            mainOutFaultMsgContext.setEnvelope(tenantOutFaultMsgContext.getEnvelope());
            throw new AxisFault("Problem with executing the message", mainOutFaultMsgContext);
        } else if (tenantOutMsgContext != null) {
            MessageContext mainOutMsgContext =
                    MessageContextBuilder.createOutMessageContext(mainInMsgContext);
            mainOutMsgContext.getOperationContext().addMessageContext(mainOutMsgContext);
            mainOutMsgContext.setEnvelope(tenantOutMsgContext.getEnvelope());
	    mainOutMsgContext.setProperty(Constants.Configuration.MESSAGE_TYPE,
                  tenantOutMsgContext.getProperty(Constants.Configuration.MESSAGE_TYPE));
            AxisEngine.send(mainOutMsgContext);
        }
    }

    /**
     * Process a REST message coming in from the NHTTP transport.
     * @param mainInMsgContext supertenant's MessageContext
     * @param to the full transport url
     * @param tenant name of the tenant
     * @param tenantConfigCtx tenant ConfigurationContext
     * @param servicePart the part of the to url after the service
     * @throws AxisFault if an error occcus
     */
    public void doNhttpREST(MessageContext mainInMsgContext,
                        String to,
                        String tenant,
                        ConfigurationContext tenantConfigCtx,
                        String servicePart) throws AxisFault {
        String serviceWithSlashT = TENANT_DELIMITER + tenant + "/" + servicePart;
        String requestUri = "local://" + tenantConfigCtx.getServicePath() + "/" + servicePart +
                (to.endsWith(serviceWithSlashT) ?
                        "" :
                        "/" + to.substring(to.indexOf(serviceWithSlashT) +
                                serviceWithSlashT.length() + 1));
        // Now create the message context to invoke
        MessageContext tenantInMsgCtx = tenantConfigCtx.createMessageContext();

        String trsPrefix = (String) mainInMsgContext.getProperty(
                Constants.Configuration.TRANSPORT_IN_URL);

        int sepindex = trsPrefix.indexOf(':');
        if (sepindex > -1) {
            trsPrefix = trsPrefix.substring(0, sepindex);
            tenantInMsgCtx.setIncomingTransportName(trsPrefix);
        } else {
            tenantInMsgCtx.setIncomingTransportName(Constants.TRANSPORT_HTTP);
        }

        tenantInMsgCtx.setMessageID(UIDGenerator.generateURNString());
        tenantInMsgCtx.setServerSide(true);
        tenantInMsgCtx.setDoingREST(true);
        copyProperties(mainInMsgContext, tenantInMsgCtx);
        tenantInMsgCtx.setTo(new EndpointReference(requestUri));

        // set a dummy transport out description
        String transportOutName = mainInMsgContext.getTransportOut().getName();
        TransportOutDescription transportOutDescription =
                tenantConfigCtx.getAxisConfiguration().getTransportOut(transportOutName);
        tenantInMsgCtx.setTransportOut(transportOutDescription);
        TransportInDescription incomingTransport =
                tenantConfigCtx.getAxisConfiguration().
                        getTransportIn(mainInMsgContext.getIncomingTransportName());
        tenantInMsgCtx.setTransportIn(incomingTransport);

        tenantInMsgCtx.setProperty(MessageContext.TRANSPORT_OUT,
                mainInMsgContext.getProperty(MessageContext.TRANSPORT_OUT));
        tenantInMsgCtx.setProperty(Constants.OUT_TRANSPORT_INFO,
                mainInMsgContext.getProperty(Constants.OUT_TRANSPORT_INFO));
        tenantInMsgCtx.setIncomingTransportName(mainInMsgContext.getIncomingTransportName());

        // When initializing caching, cache manager fetches the tenant domain from threadLocalCarbonContext
        // Without setting this, caching cannot be initialised on the API Gateway.
        String transportInClassName = mainInMsgContext.getTransportIn().getReceiver().getClass().getName();

        if ("org.apache.synapse.transport.nhttp.HttpCoreNIOListener".equals(transportInClassName) ||
                "org.apache.synapse.transport.nhttp.HttpCoreNIOSSLListener".equals(transportInClassName)||
                "org.apache.synapse.transport.passthru.PassThroughHttpListener".equals(transportInClassName) ||
                "org.apache.synapse.transport.passthru.PassThroughHttpSSLListener".equals(transportInClassName)){
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenant, true);
        }

       /* // extract the part of the user after the actual service and set it as
        int index = servicePart.indexOf('/');
        String service = (index > 0 ?
                servicePart.substring(servicePart.indexOf('/') + 1) : servicePart);        
        //String servicePath = TENANT_DELIMITER + tenant + "/" + service;
        //String restSuffic = (to.endsWith(servicePath) ? "" :
        // to.substring(to.indexOf(servicePath) + servicePath.length() + 1));
        tenantInMsgCtx.setProperty("REST_URL_POSTFIX", service);*/

        String service = "";
        String postFix = "";
        int index = servicePart.indexOf("/");
        if (index > 0) {
            service = servicePart.substring(0, index);
            postFix = servicePart.substring(index + 1);

        }

 	if (service.equals("")) {
            service = servicePart;
        }
        tenantInMsgCtx.setProperty("REST_URL_POSTFIX", postFix);

        // handling requests with invalid service portion
        if (tenantConfigCtx.getAxisConfiguration().getService(service) == null) {
            // we assume that the request should go to the default service
            tenantInMsgCtx.setAxisService(tenantConfigCtx.getAxisConfiguration()
                                                         .getService("__SynapseService"));
        } 
        
        tenantInMsgCtx.setEnvelope(mainInMsgContext.getEnvelope());;

        InputStream in = (InputStream) mainInMsgContext.getProperty("nhttp.input.stream");
        OutputStream os = (OutputStream) mainInMsgContext.getProperty("nhttp.output.stream");
        String contentType = (String) mainInMsgContext.getProperty("ContentType");
        try {
            //  String httpMethod = (String) mainInMsgContext.getProperty(HTTPConstants.HTTP_METHOD);
            String httpMethod = (String) mainInMsgContext.getProperty(Constants.Configuration.HTTP_METHOD);
            if (httpMethod.equals(Constants.Configuration.HTTP_METHOD_GET) ||
                    httpMethod.equals(Constants.Configuration.HTTP_METHOD_DELETE) ||
                    "OPTIONS".equals(httpMethod) || Constants.Configuration.HTTP_METHOD_HEAD.equals(httpMethod)) {
                //RESTUtil.processURLRequest(tenantInMsgCtx, os, contentType);
            	this.processRESTRequest(tenantInMsgCtx,os,contentType);
            } else if (httpMethod.equals(Constants.Configuration.HTTP_METHOD_POST) ||
                    httpMethod.equals(Constants.Configuration.HTTP_METHOD_PUT)) {
                //RESTUtil.processXMLRequest(tenantInMsgCtx, in, os, contentType);
            	this.processRESTRequest(tenantInMsgCtx,os,contentType);
            } else {
                // TODO: throw exception: Invalid verb
            }
        } catch (AxisFault axisFault) {
            // at a fault flow message receiver throws a fault.
            // we need to first catch this fault and invoke the fault flow
            // then thorw the AxisFault for main flow which is catch by the carbon servlet
            // and invoke the fault handlers.

            MessageContext faultContext =
                    MessageContextBuilder.createFaultMessageContext(tenantInMsgCtx, axisFault);
            faultContext.setTransportOut(tenantInMsgCtx.getTransportOut());
            faultContext.setProperty(MultitenantConstants.TENANT_MR_STARTED_FAULT,
                    Constants.VALUE_TRUE);
            AxisEngine.sendFault(faultContext);

            // we need to set the detial to null. otherwise the details element is copied to
            // new message context and removed from the original one
            axisFault.setDetail(null);
            MessageContext mainFaultContext = MessageContextBuilder.
                    createFaultMessageContext(mainInMsgContext, axisFault);
            mainFaultContext.setTo(faultContext.getTo());
            mainFaultContext.setSoapAction(faultContext.getSoapAction());

            mainFaultContext.setEnvelope(faultContext.getEnvelope());
            throw new AxisFault(axisFault.getMessage(), mainFaultContext);
        }        
    }

	/**
	 * This is temporary fix to handle REST API invocations requests comes via
	 * multitenancy dispatch service,
	 * 
	 * @param msgContext
	 * @param os
	 * @param contentType
	 * @return
	 * @throws AxisFault
	 */
	private Handler.InvocationResponse processRESTRequest(MessageContext msgContext, OutputStream os, String contentType) throws AxisFault{
		try {
			msgContext.setDoingREST(true);
			String charSetEncoding = BuilderUtil.getCharSetEncoding(contentType);
			msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEncoding);
			dispatchAndVerify(msgContext);
		} catch (AxisFault axisFault) {
            throw axisFault;
        } finally {
			String messageType = (String) msgContext.getProperty(Constants.Configuration.MESSAGE_TYPE);
			if (HTTPConstants.MEDIA_TYPE_X_WWW_FORM.equals(messageType) 
					|| HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA.equals(messageType)) {
				msgContext.setProperty(Constants.Configuration.MESSAGE_TYPE, HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
			}
		}
        
        return AxisEngine.receive(msgContext);

	}
	
	
	 private static void dispatchAndVerify(MessageContext msgContext) throws AxisFault {
	        RequestURIBasedDispatcher requestDispatcher = new RequestURIBasedDispatcher();
	        requestDispatcher.invoke(msgContext);
	        AxisService axisService = msgContext.getAxisService();
	        if (axisService != null) {
	            HTTPLocationBasedDispatcher httpLocationBasedDispatcher =
	                    new HTTPLocationBasedDispatcher();
	            httpLocationBasedDispatcher.invoke(msgContext);
	            if (msgContext.getAxisOperation() == null) {
	                RequestURIOperationDispatcher requestURIOperationDispatcher =
	                        new RequestURIOperationDispatcher();
	                requestURIOperationDispatcher.invoke(msgContext);
	            }

	            AxisOperation axisOperation;
	            if ((axisOperation = msgContext.getAxisOperation()) != null) {
	                AxisEndpoint axisEndpoint =
	                        (AxisEndpoint) msgContext.getProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME);
	                if (axisEndpoint != null) {
	                    AxisBindingOperation axisBindingOperation = (AxisBindingOperation) axisEndpoint
	                            .getBinding().getChild(axisOperation.getName());
	                    msgContext.setProperty(Constants.AXIS_BINDING_OPERATION, axisBindingOperation);
	                }
	                msgContext.setAxisOperation(axisOperation);
	            }
	        }
	    }

	/**
     * Copy the properties from main message context to the tenant message context.
     *
     * @param mainMsgCtx super tenant message context
     * @param tenantMsgCtx tenant message context
     */
    private void copyProperties(MessageContext mainMsgCtx, MessageContext tenantMsgCtx) {
        // do not copy options from the original        
        tenantMsgCtx.setSoapAction(mainMsgCtx.getSoapAction());

        tenantMsgCtx.setDoingREST(mainMsgCtx.isDoingREST());
        tenantMsgCtx.setDoingMTOM(mainMsgCtx.isDoingMTOM());
        tenantMsgCtx.setDoingSwA(mainMsgCtx.isDoingSwA());

        // if the original request carries any attachments, copy them to the clone
        // as well, except for the soap part if any
        Attachments attachments = mainMsgCtx.getAttachmentMap();
        if (attachments != null && attachments.getAllContentIDs().length > 0) {
            String[] cIDs = attachments.getAllContentIDs();
            String soapPart = attachments.getSOAPPartContentID();
            for (String cID : cIDs) {
                if (!cID.equals(soapPart)) {
                    tenantMsgCtx.addAttachment(cID, attachments.getDataHandler(cID));
                }
            }
        }

        Iterator itr = mainMsgCtx.getPropertyNames();
        while (itr.hasNext()) {
            String key = (String) itr.next();
            if (key != null) {                
                tenantMsgCtx.setProperty(key, mainMsgCtx.getProperty(key));
            }
        }
    }

    private void handleException(MessageContext mainInMsgContext, AxisFault fault)
            throws AxisFault {
        MessageContext mainOutMsgContext =
                MessageContextBuilder.createFaultMessageContext(mainInMsgContext, fault);
        OperationContext mainOpContext = mainInMsgContext.getOperationContext();
        mainOpContext.addMessageContext(mainOutMsgContext);
        mainOutMsgContext.setOperationContext(mainOpContext);
        AxisEngine.sendFault(mainOutMsgContext);
    }

    /**
     * Get the EPR for the message passed in
     * @param msgContext the message context
     * @return the destination EPR
     */
    public static EndpointReference getDestinationEPR(MessageContext msgContext) {

        // Trasnport URL can be different from the WSA-To
        String transportURL = (String) msgContext.getProperty(
            Constants.Configuration.TRANSPORT_URL);

        if (transportURL != null) {
            return new EndpointReference(transportURL);
        } else if (
            (msgContext.getTo() != null) && !msgContext.getTo().hasAnonymousAddress()) {
            return msgContext.getTo();
        }
        return null;
    }
}
