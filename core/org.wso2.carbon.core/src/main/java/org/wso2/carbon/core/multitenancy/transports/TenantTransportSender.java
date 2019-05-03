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

package org.wso2.carbon.core.multitenancy.transports;

import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.OutOnlyAxisOperation;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.httpclient.HttpMethod;
import org.wso2.carbon.core.internal.MultitenantMsgContextDataHolder;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.multitenancy.MultitenantMessageReceiver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.Map;

public class TenantTransportSender extends AbstractHandler implements TransportSender {

    private ConfigurationContext superTenantConfigurationContext;
    private AxisService superTenantSenderClientService;
    private AxisServiceGroup superTenantSenderServiceGroup;
    private static final Log log = LogFactory.getLog(TenantTransportSender.class);

    private static final String SERVICE_PREFIX = "SERVICE_PREFIX";
    private static final String REQUEST_HOST_HEADER = "REQUEST_HOST_HEADER";
    private static final String HTTP_ETAG = "HTTP_ETAG";
    private static final String NO_ENTITY_BODY = "NO_ENTITY_BODY";
    private static final String HTTP_SC_DESC = "HTTP_SC_DESC";
    private static final String EXCESS_TRANSPORT_HEADERS = "EXCESS_TRANSPORT_HEADERS";
    private static final String FORCE_SC_ACCEPTED = "FORCE_SC_ACCEPTED";
    private static final String FORCE_POST_PUT_NOBODY = "FORCE_POST_PUT_NOBODY";
    private static final String DELETE_REQUEST_WITH_PAYLOAD = "DELETE_REQUEST_WITH_PAYLOAD";
    private static final QName IN_OUT_OPERATION = new QName("anonInOutOp");
    private static final QName OUT_ONLY_OPERATION = new QName("anonOutonlyOp");
    private static final String SERVICE_KEY = "tenantClientService";

    private MultitenantMsgContextDataHolder dataHolder = MultitenantMsgContextDataHolder.getInstance();

    public TenantTransportSender(ConfigurationContext superTenantConfigurationContext) {

        try {
            setupTenantClientOutService(superTenantConfigurationContext);
        } catch (AxisFault axisFault) {
            log.error("Error while setting up tenant client out service in TenantTransportSender", axisFault);
        }
    }

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        MessageContext superTenantOutMessageContext = superTenantConfigurationContext.createMessageContext();
        superTenantOutMessageContext
                .setProperty(MessageContext.TRANSPORT_OUT, msgContext.getProperty(MessageContext.TRANSPORT_OUT));
        superTenantOutMessageContext
                .setProperty(Constants.OUT_TRANSPORT_INFO, msgContext.getProperty(Constants.OUT_TRANSPORT_INFO));
        String incomingMEP = msgContext.getAxisOperation().getMessageExchangePattern();
        AxisOperation axisOperation = incomingMEP.equals(WSDL2Constants.MEP_URI_OUT_ONLY) ?
                superTenantSenderClientService.getOperation(OUT_ONLY_OPERATION) :
                superTenantSenderClientService.getOperation(IN_OUT_OPERATION);
        if (log.isDebugEnabled()) {
            log.debug("Incoming message MEP " + incomingMEP + "Selected axisOperation MEP " + axisOperation
                    .getMessageExchangePattern());
        }
        superTenantOutMessageContext.setAxisService(superTenantSenderClientService);

        ServiceGroupContext serviceGroupContext = superTenantConfigurationContext
                .createServiceGroupContext(superTenantSenderServiceGroup);
        ServiceContext serviceContext = serviceGroupContext.getServiceContext(superTenantSenderClientService);

        superTenantOutMessageContext.setServiceContext(serviceContext);

        OperationContext operationContext = serviceContext.createOperationContext(axisOperation);
        operationContext.addMessageContext(superTenantOutMessageContext);

        superTenantOutMessageContext.setOperationContext(operationContext);

        String transportOutName = msgContext.getTransportOut().getName();
        superTenantOutMessageContext.setTransportOut(
                superTenantConfigurationContext.getAxisConfiguration().getTransportOut(transportOutName));

        superTenantOutMessageContext.setEnvelope(msgContext.getEnvelope());
        superTenantOutMessageContext.setProperty(MultitenantConstants.SYNAPSE_JSON_INPUT_STREAM,
                msgContext.getProperty(MultitenantConstants.SYNAPSE_JSON_INPUT_STREAM));

        superTenantOutMessageContext.setTo(msgContext.getTo());
        superTenantOutMessageContext.setSoapAction(msgContext.getSoapAction());
        superTenantOutMessageContext.setDoingREST(msgContext.isDoingREST());
        superTenantOutMessageContext.setDoingMTOM(msgContext.isDoingMTOM());
        superTenantOutMessageContext.setProperty(MessageContext.TRANSPORT_HEADERS,
                msgContext.getProperty(MessageContext.TRANSPORT_HEADERS));
        superTenantOutMessageContext.setProperty(EXCESS_TRANSPORT_HEADERS,
                msgContext.getProperty(EXCESS_TRANSPORT_HEADERS));

        superTenantOutMessageContext.setProperty(MultitenantConstants.HTTP_SC,msgContext.getProperty(MultitenantConstants.HTTP_SC));
        superTenantOutMessageContext.setProperty(HTTP_SC_DESC,
                msgContext.getProperty(HTTP_SC_DESC));
        superTenantOutMessageContext.setProperty(HTTPConstants.HTTP_HEADERS,msgContext.getProperty(HTTPConstants.HTTP_HEADERS));


        // Copy Message type and Content type from the original message ctx
        // so that the content type will be set properly
        String msgTypeProperty = (String)
                msgContext.getProperty(Constants.Configuration.MESSAGE_TYPE);
        superTenantOutMessageContext.setProperty(
                Constants.Configuration.MESSAGE_TYPE, msgTypeProperty);

        String contentTypeProperty = (String)
                msgContext.getProperty(Constants.Configuration.CONTENT_TYPE);
        superTenantOutMessageContext.setProperty(
                Constants.Configuration.CONTENT_TYPE, contentTypeProperty);

        superTenantOutMessageContext.setDoingMTOM(msgContext.isDoingMTOM());

        superTenantOutMessageContext.setProperty(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING,
                msgContext.getProperty(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING));
        superTenantOutMessageContext.setProperty(org.apache.axis2.Constants.Configuration.ENABLE_MTOM,
                msgContext.getProperty(org.apache.axis2.Constants.Configuration.ENABLE_MTOM));
        superTenantOutMessageContext.setProperty(org.apache.axis2.Constants.Configuration.ENABLE_SWA,
                msgContext.getProperty(org.apache.axis2.Constants.Configuration.ENABLE_SWA));
        superTenantOutMessageContext.setProperty(Constants.Configuration.HTTP_METHOD,
                msgContext.getProperty(Constants.Configuration.HTTP_METHOD));
        superTenantOutMessageContext.setProperty(MultitenantConstants.DISABLE_CHUNKING,
                msgContext.getProperty(MultitenantConstants.DISABLE_CHUNKING));
        superTenantOutMessageContext.setProperty(MultitenantConstants.NO_KEEPALIVE,
                msgContext.getProperty(MultitenantConstants.NO_KEEPALIVE));
        boolean forced = msgContext.isPropertyTrue(FORCE_SC_ACCEPTED);
        if (forced) {
            superTenantOutMessageContext.setProperty(FORCE_SC_ACCEPTED, true);
        }
        boolean forcedNoBody = msgContext.isPropertyTrue(FORCE_POST_PUT_NOBODY);
        if (forcedNoBody) {
            superTenantOutMessageContext.setProperty(FORCE_POST_PUT_NOBODY, true);
        }

        if (msgContext.getReplyTo() == null) {
            superTenantOutMessageContext.setReplyTo(new EndpointReference("http://www.w3.org/2005/08/addressing/none"));
        } else {
            superTenantOutMessageContext.setReplyTo(msgContext.getReplyTo());
        }

        Map headers = (Map) msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);

        if (headers != null && headers.get(MultitenantConstants.CONTENT_LENGTH) != null) {
            superTenantOutMessageContext.setProperty(MultitenantConstants.ORGINAL_CONTENT_LENGTH,
                    headers.get(MultitenantConstants.CONTENT_LENGTH));
        }

        boolean forceContentLength = msgContext.isPropertyTrue(MultitenantConstants.FORCE_HTTP_CONTENT_LENGTH);
        boolean contentLengthCopy = msgContext.isPropertyTrue(MultitenantConstants.
                COPY_CONTENT_LENGTH_FROM_INCOMING);

        superTenantOutMessageContext.setProperty(MultitenantConstants.POST_TO_URI, 
        		 msgContext.getProperty(MultitenantConstants.POST_TO_URI));        
        
        superTenantOutMessageContext.setProperty(MultitenantConstants.FORCE_HTTP_CONTENT_LENGTH,
                forceContentLength);
        superTenantOutMessageContext.setProperty(MultitenantConstants.COPY_CONTENT_LENGTH_FROM_INCOMING,
                contentLengthCopy);



        superTenantOutMessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE,
                msgContext.getProperty(Constants.Configuration.MESSAGE_TYPE));


        if (msgContext.getOperationContext() != null){
            MessageContext inMessageContext =
                    msgContext.getOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inMessageContext != null){
                superTenantOutMessageContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL, 
                        inMessageContext.getProperty(RequestResponseTransport.TRANSPORT_CONTROL));
            }
        }

        if ((msgContext.getProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES) != null)
                && (Boolean) msgContext.getProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES)) {
            superTenantOutMessageContext.setProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES,
                    msgContext.getProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES));
        }
        
        if(msgContext.getProperty(MultitenantConstants.PASS_THROUGH_PIPE) != null){
        	superTenantOutMessageContext.setProperty(MultitenantConstants.PASS_THROUGH_PIPE, msgContext.getProperty(MultitenantConstants.PASS_THROUGH_PIPE));
        	superTenantOutMessageContext.setProperty(MultitenantConstants.MESSAGE_BUILDER_INVOKED,msgContext.getProperty(MultitenantConstants.MESSAGE_BUILDER_INVOKED) != null?msgContext.getProperty(MultitenantConstants.MESSAGE_BUILDER_INVOKED):Boolean.FALSE);
        }

        if (msgContext.getProperty(MultitenantConstants.PASS_THROUGH_SOURCE_CONNECTION) != null) {
            superTenantOutMessageContext.
                    setProperty(MultitenantConstants.PASS_THROUGH_SOURCE_CONNECTION,
                                msgContext.getProperty(MultitenantConstants.PASS_THROUGH_SOURCE_CONNECTION));
        }

        if (msgContext.getProperty(MultitenantConstants.PASS_THROUGH_SOURCE_CONFIGURATION) != null) {
            superTenantOutMessageContext.
                    setProperty(MultitenantConstants.PASS_THROUGH_SOURCE_CONFIGURATION,
                                msgContext.getProperty(MultitenantConstants.PASS_THROUGH_SOURCE_CONFIGURATION));
        }

         /*Handling HTTP DELETE*/
        if (msgContext.getProperty(MultitenantConstants.REST_GET_DELETE_INVOKE) != null) {
            superTenantOutMessageContext.setProperty(MultitenantConstants.REST_GET_DELETE_INVOKE,
                    msgContext.getProperty(MultitenantConstants.REST_GET_DELETE_INVOKE));
        }

        if (msgContext.getProperty(SERVICE_PREFIX) != null) {
            superTenantOutMessageContext.setProperty(SERVICE_PREFIX, msgContext.getProperty(SERVICE_PREFIX));
        }

        if (msgContext.getProperty(HTTP_ETAG) != null) {
            superTenantOutMessageContext.setProperty(HTTP_ETAG, msgContext.getProperty(HTTP_ETAG));
        }

        if (msgContext.getProperty(REQUEST_HOST_HEADER) != null) {
            superTenantOutMessageContext.setProperty(REQUEST_HOST_HEADER,
                                                     msgContext.getProperty(REQUEST_HOST_HEADER));
        }

        if (msgContext.getProperty(NO_ENTITY_BODY) != null) {
            superTenantOutMessageContext.setProperty(NO_ENTITY_BODY, msgContext.getProperty(NO_ENTITY_BODY));
        }

        // set additional multitenant message context properties read from multitenant-msg-context.properties file
        for (String property : dataHolder.getTenantMsgContextProperties()) {
            if (msgContext.getProperty(property) != null) {
                superTenantOutMessageContext.setProperty(property, msgContext.getProperty(property));
            }
        }

        setDeleteRequestWithPayloadProperty(superTenantOutMessageContext, msgContext);

        EndpointReference epr = getDestinationEPR(msgContext);
        // this is a request message so we need to set the response message context
        if (epr != null && !incomingMEP.equals(WSDL2Constants.MEP_URI_OUT_ONLY)) {
            String messageId = UIDGenerator.generateURNString();
            superTenantOutMessageContext.setMessageID(messageId);
            superTenantOutMessageContext.setProperty(MultitenantConstants.TENANT_REQUEST_MSG_CTX, 
                    msgContext);

            superTenantOutMessageContext.setServerSide(true);

            MessageContext superTenantInMessageContext = new MessageContext();
            superTenantInMessageContext.setMessageID(superTenantOutMessageContext.getMessageID());
            superTenantInMessageContext.setProperty(
                    "synapse.RelatesToForPox", messageId);
            superTenantInMessageContext.setServerSide(true);
            
            superTenantInMessageContext.setMessageID(messageId);
            superTenantInMessageContext.setServiceContext(serviceContext);
            axisOperation.registerOperationContext(superTenantInMessageContext, operationContext);
        }

        if (!JavaUtils.isTrueExplicitly(msgContext.getProperty(
                MultitenantConstants.TENANT_MR_STARTED_FAULT))){
            // if the
            AxisEngine.send(superTenantOutMessageContext);

            // Keep TRANSPORT_IN alive 
            if (superTenantOutMessageContext.getProperty(MessageContext.TRANSPORT_IN) != null) {
                msgContext.getOperationContext().
                 setProperty(MessageContext.TRANSPORT_IN,
                             superTenantOutMessageContext.getProperty(MessageContext.TRANSPORT_IN));
            }
            msgContext.setProperty(HTTPConstants.HTTP_METHOD, superTenantOutMessageContext.
                    getProperty(HTTPConstants.HTTP_METHOD));
        }
        return InvocationResponse.CONTINUE;
    }

    /**
     * This method will setup the Axis2 service and two operations for that service which is used in sending the message
     * out in tenant mode. Two operations are , In-Out axis operation and Out-Only operation. The operation context for
     * the individual requests will be set dynamically in the TenantTransportSender.invoke() method depending on the
     * incoming msgContext message exchange pattern. Same operation will be share only with the similar MEPs.
     *
     * @param superTenantConfigurationContext Super tenant's configuration context for creating the tenant out service,
     *                                        One of the reasons to require super tenant's axis configuration is,
     *                                        If not, Axis2 transport definitions will be not available
     *                                        to outgoing message
     * @throws AxisFault
     */
    private void setupTenantClientOutService(ConfigurationContext superTenantConfigurationContext) throws AxisFault {
        // preserve the super tenant's configuration context to be later used in creating message context per each
        // Outgoing requests
        this.superTenantConfigurationContext = superTenantConfigurationContext;

        // Get AxisConfiguration from the super tenant's configuration context and
        // Create AxisServiceGroup -> AxisService -> AxisOperation consequently
        AxisConfiguration superTenantCachedAxisConfiguration = this.superTenantConfigurationContext
                .getAxisConfiguration();

        this.superTenantSenderServiceGroup = new AxisServiceGroup(superTenantCachedAxisConfiguration);
        superTenantCachedAxisConfiguration.addServiceGroup(superTenantSenderServiceGroup);
        superTenantSenderServiceGroup.setServiceGroupName(SERVICE_KEY);
        // This axis serive instance is binding to each out message contexts in invoke() method
        this.superTenantSenderClientService = new AxisService(SERVICE_KEY);
        this.superTenantSenderClientService.addParameter(CarbonConstants.HIDDEN_SERVICE_PARAM_NAME, "true");
        superTenantSenderServiceGroup.addService(superTenantSenderClientService);

        // Create two axis operations and bind them to above created service.The relevant operation context will be
        // dynamically created (inside invoke() method) and attach to outgoing message context (to service context)
        // in runtime
        this.superTenantSenderClientService.addOperation(new InOutAxisOperation(IN_OUT_OPERATION));
        this.superTenantSenderClientService.getOperation(IN_OUT_OPERATION)
                .setMessageReceiver(new MultitenantMessageReceiver());

        this.superTenantSenderClientService.addOperation(new OutOnlyAxisOperation(OUT_ONLY_OPERATION));
        this.superTenantSenderClientService.getOperation(OUT_ONLY_OPERATION)
                .setMessageReceiver(new MultitenantMessageReceiver());

        superTenantSenderClientService.setClientSide(true);
        if (log.isDebugEnabled()) {
            log.debug("Deployed " + SERVICE_KEY);
        }
    }

    public void cleanup(MessageContext msgContext) throws AxisFault {
        HttpMethod httpMethod = (HttpMethod) msgContext.getProperty(HTTPConstants.HTTP_METHOD);
        if (httpMethod != null) {
            httpMethod.releaseConnection();
            msgContext.removeProperty(HTTPConstants.HTTP_METHOD); // guard against multiple calls
        }
    }

    public void init(ConfigurationContext confContext, TransportOutDescription transportOut)
            throws AxisFault {

    }

    public void stop() {

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

    /**
     * Set property DELETE_REQUEST_WITH_PAYLOAD to superTenantOutMessageContext.
     *
     * @param superTenantOutMessageContext message context to be send out
     * @param msgContext                   message context coming in
     */
    protected void setDeleteRequestWithPayloadProperty(MessageContext superTenantOutMessageContext,
            MessageContext msgContext) {
        if (msgContext.getProperty(DELETE_REQUEST_WITH_PAYLOAD) != null) {
            superTenantOutMessageContext
                    .setProperty(DELETE_REQUEST_WITH_PAYLOAD, msgContext.getProperty(DELETE_REQUEST_WITH_PAYLOAD));
        }
    }
}
