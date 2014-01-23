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

package org.apache.axis2.engine;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004_Constants;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2006Constants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DispatchPhase extends Phase {

    public DispatchPhase() {
    }

    public DispatchPhase(String phaseName) {
        super(phaseName);
    }
    
    
    private Boolean getDisableAck(MessageContext msgContext) throws AxisFault {
    
       // We should send an early ack to the transport whever possible, but some modules need
       // to use the backchannel, so we need to check if they have disabled this code.
       Boolean disableAck = (Boolean) msgContext.getProperty(Constants.Configuration.DISABLE_RESPONSE_ACK);
       if(disableAck == null) {
          disableAck = (Boolean) (msgContext.getAxisService() != null ? msgContext.getAxisService().getParameterValue(Constants.Configuration.DISABLE_RESPONSE_ACK) : null);
       }    
       
       return disableAck;
    }

    public void checkPostConditions(MessageContext msgContext) throws AxisFault {
        EndpointReference toEPR = msgContext.getTo();

        if (msgContext.getAxisService() == null) {
            AxisFault fault = new AxisFault(Messages.getMessage("servicenotfoundforepr",
                                                    ((toEPR != null) ? toEPR.getAddress() : "")));
            fault.setFaultCode(org.apache.axis2.namespace.Constants.FAULT_CLIENT);
            throw fault;
        }

        AxisService service = msgContext.getAxisService();
        AxisOperation operation = msgContext.getAxisOperation();
        // If we're configured to do so, check the service for a single op...
        if (operation == null &&
                JavaUtils.isTrue(service.getParameterValue(AxisService.SUPPORT_SINGLE_OP))) {
            Iterator<AxisOperation> ops = service.getOperations();
            // If there's exactly one, that's the one we want.  If there's more, forget it.
            if (ops.hasNext()) {
                operation = (AxisOperation)ops.next();
                if (ops.hasNext()) {
                    operation = null;
                }
            }
            msgContext.setAxisOperation(operation);
        }

        // If we still don't have an operation, fault.
        if (operation == null) {
            AxisFault fault = new AxisFault(Messages.getMessage("operationnotfoundforepr2",
                                                    ((toEPR != null) ? toEPR.getAddress()
                                                            : ""), msgContext.getWSAAction()));
            fault.setFaultCode(org.apache.axis2.namespace.Constants.FAULT_CLIENT);
            throw fault;
        }

        validateTransport(msgContext);
        
        validateBindings(msgContext);

        loadContexts(service, msgContext);

        if (msgContext.getOperationContext() == null) {
            throw new AxisFault(Messages.getMessage("cannotBeNullOperationContext"));
        }

        if (msgContext.getServiceContext() == null) {
            throw new AxisFault(Messages.getMessage("cannotBeNullServiceContext"));
        }

        // TODO - review this
        if ((msgContext.getAxisOperation() == null) && (msgContext.getOperationContext() != null)) {
            msgContext.setAxisOperation(msgContext.getOperationContext().getAxisOperation());
        }

        if ((msgContext.getAxisService() == null) && (msgContext.getServiceContext() != null)) {
            msgContext.setAxisService(msgContext.getServiceContext().getAxisService());
        }

        // We should send an early ack to the transport whever possible, but some modules need
        // to use the backchannel, so we need to check if they have disabled this code.
        String mepString = msgContext.getAxisOperation().getMessageExchangePattern();

        if (isOneway(mepString)) {
            Object requestResponseTransport = msgContext.getProperty(RequestResponseTransport.TRANSPORT_CONTROL);
            if (requestResponseTransport != null) {

                Boolean disableAck = getDisableAck(msgContext);                    
                if (disableAck == null || disableAck.booleanValue() == false) {
                    ((RequestResponseTransport) requestResponseTransport).acknowledgeMessage(msgContext);
                }
            }
        } else if (AddressingHelper.isReplyRedirected(msgContext) && AddressingHelper.isFaultRedirected(msgContext)) {


            if (mepString.equals(WSDL20_2006Constants.MEP_URI_IN_OUT)
                    || mepString.equals(WSDL20_2004_Constants.MEP_URI_IN_OUT)
                    || mepString.equals(WSDL2Constants.MEP_URI_IN_OUT)) { 
                // OR, if 2 way operation but the response is intended to not use the response channel of a 2-way transport
                // then we don't need to keep the transport waiting.

                Object requestResponseTransport = msgContext.getProperty(RequestResponseTransport.TRANSPORT_CONTROL);
                if (requestResponseTransport != null) {

                    // We should send an early ack to the transport whever possible, but some modules need
                    // to use the backchannel, so we need to check if they have disabled this code.
                    Boolean disableAck = getDisableAck(msgContext);

                    if (disableAck == null || disableAck.booleanValue() == false) {
                        ((RequestResponseTransport) requestResponseTransport).acknowledgeMessage(msgContext);
                    }
                    
                }
            }        
        }
        

        ArrayList operationChain = msgContext.getAxisOperation().getRemainingPhasesInFlow();
        msgContext.setExecutionChain((ArrayList) operationChain.clone());
    }

    private void loadContexts(AxisService service, MessageContext msgContext) throws AxisFault {
        String scope = service == null ? null : service.getScope();
        ServiceContext serviceContext = msgContext.getServiceContext();

        if ((msgContext.getOperationContext() != null)
                && (serviceContext != null)) {
            msgContext.setServiceGroupContextId(
                    ((ServiceGroupContext)serviceContext.getParent()).getId());
            return;
        }
        if (Constants.SCOPE_TRANSPORT_SESSION.equals(scope)) {
            fillContextsFromSessionContext(msgContext);
        } else if (Constants.SCOPE_SOAP_SESSION.equals(scope)) {
            extractServiceGroupContextId(msgContext);
        }

        AxisOperation axisOperation = msgContext.getAxisOperation();
//        if (axisOperation == null) {
//            return;
//        }
        OperationContext operationContext =
                axisOperation.findForExistingOperationContext(msgContext);

        if (operationContext != null) {
            // register operation context and message context
//            axisOperation.registerOperationContext(msgContext, operationContext);
            axisOperation.registerMessageContext(msgContext, operationContext);

            serviceContext = (ServiceContext) operationContext.getParent();
            ServiceGroupContext serviceGroupContext =
                    (ServiceGroupContext) serviceContext.getParent();

            msgContext.setServiceContext(serviceContext);
            msgContext.setServiceGroupContext(serviceGroupContext);
            msgContext.setServiceGroupContextId(serviceGroupContext.getId());
        } else {    // 2. if null, create new opCtxt
            if (serviceContext == null) {
                // fill the service group context and service context info
                msgContext.getConfigurationContext().
                        fillServiceContextAndServiceGroupContext(msgContext);
                serviceContext = msgContext.getServiceContext();
            }
            operationContext = serviceContext.createOperationContext(axisOperation);
            axisOperation.registerMessageContext(msgContext, operationContext);
        }

        serviceContext.setMyEPR(msgContext.getTo());
    }

    /**
     * To check whether the incoming request has come in valid transport , simply the transports
     * that service author wants to expose
     *
     * @param msgctx the current MessageContext
     * @throws AxisFault in case of error
     */
    private void validateTransport(MessageContext msgctx) throws AxisFault {
        AxisService service = msgctx.getAxisService();
        if (service.isEnableAllTransports()) {
            return;
        } else {
            List trs = service.getExposedTransports();
            String incomingTrs = msgctx.getIncomingTransportName();

            //local transport is a special case, it need not be exposed.
            if (Constants.TRANSPORT_LOCAL.equals(incomingTrs)) {
                return;
            }

            for (int i = 0; i < trs.size(); i++) {
                String tr = (String) trs.get(i);
                if (incomingTrs != null && incomingTrs.startsWith(tr)) {
                    return;
                }
            }
        }
        EndpointReference toEPR = msgctx.getTo();
        throw new AxisFault(Messages.getMessage("servicenotfoundforepr",
                                                ((toEPR != null) ? toEPR.getAddress() : "")));
    }
    
    /**
     * To check whether the incoming request has come in valid binding , we check whether service
     * author has disabled any binding using parameters
     * <code>org.apache.axis2.Constants.Configuration.DISABLE_SOAP12</code>
     * <code>org.apache.axis2.Constants.Configuration.DISABLE_SOAP11</code>
     * <code>org.apache.axis2.Constants.Configuration.DISABLE_REST</code>
     * @param service msgctx the current MessageContext
     * @throws AxisFault in case of error
     */
    private void validateBindings(MessageContext msgctx) throws AxisFault {
        
        AxisService service = msgctx.getAxisService();        
   
        if (msgctx.isDoingREST()) {
            
            boolean disableREST = false;
            Parameter disableRESTParameter = service
                            .getParameter(org.apache.axis2.Constants.Configuration.DISABLE_REST);
            if (disableRESTParameter != null
                            && JavaUtils.isTrueExplicitly(disableRESTParameter.getValue())) {
                    disableREST = true;
            }         
            
            if (disableREST) {
                throw new AxisFault(Messages.getMessage("bindingDisabled","Http"));
            }
        } else if (msgctx.isSOAP11()) {

            boolean disableSOAP11 = false;
            Parameter disableSOAP11Parameter = service
                            .getParameter(org.apache.axis2.Constants.Configuration.DISABLE_SOAP11);
            if (disableSOAP11Parameter != null
                            && JavaUtils.isTrueExplicitly(disableSOAP11Parameter.getValue())) {
                    disableSOAP11 = true;
            }         
         
            if (disableSOAP11) {
                throw new AxisFault(Messages.getMessage("bindingDisabled","SOAP11"));
            }
        } else {
         
            boolean disableSOAP12 = false;
            Parameter disableSOAP12Parameter = service
                            .getParameter(org.apache.axis2.Constants.Configuration.DISABLE_SOAP12);
            if (disableSOAP12Parameter != null
                            && JavaUtils
                                            .isTrueExplicitly(disableSOAP12Parameter.getValue())) {
                    disableSOAP12 = true;
            }         
         
            if(disableSOAP12) {
                throw new AxisFault(Messages.getMessage("bindingDisabled","SOAP12"));  
            }
        }
    }

    private void fillContextsFromSessionContext(MessageContext msgContext) throws AxisFault {
        AxisService service = msgContext.getAxisService();
        if (service == null) {
            throw new AxisFault(Messages.getMessage("unabletofindservice"));
        }
        SessionContext sessionContext = msgContext.getSessionContext();
        if (sessionContext == null) {
            TransportListener listener = msgContext.getTransportIn().getReceiver();
            sessionContext = listener.getSessionContext(msgContext);
            if (sessionContext == null) {
                createAndFillContexts(service, msgContext, sessionContext);
                return;
            }
        }
        String serviceGroupName = msgContext.getAxisServiceGroup().getServiceGroupName();
        ServiceGroupContext serviceGroupContext =
                sessionContext.getServiceGroupContext(serviceGroupName);
        if (serviceGroupContext != null) {
            //setting service group context
            msgContext.setServiceGroupContext(serviceGroupContext);
            // setting Service context
            msgContext.setServiceContext(serviceGroupContext.getServiceContext(service));
        } else {
            createAndFillContexts(service, msgContext, sessionContext);
        }
        ServiceContext serviceContext = sessionContext.getServiceContext(service);
        //found the serviceContext from session context , so adding that into msgContext
        if (serviceContext != null) {
            msgContext.setServiceContext(serviceContext);
            serviceContext.setProperty(HTTPConstants.COOKIE_STRING, sessionContext.getCookieID());
        }
    }

    private void createAndFillContexts(AxisService service,
                                       MessageContext msgContext,
                                       SessionContext sessionContext) throws AxisFault {
        ServiceGroupContext serviceGroupContext;
        AxisServiceGroup axisServiceGroup = service.getAxisServiceGroup();
        ConfigurationContext configCtx = msgContext.getConfigurationContext();
        serviceGroupContext = configCtx.createServiceGroupContext(axisServiceGroup);

        msgContext.setServiceGroupContext(serviceGroupContext);
        ServiceContext serviceContext = serviceGroupContext.getServiceContext(service);
        msgContext.setServiceContext(serviceContext);
        if (sessionContext != null) {
            sessionContext.addServiceContext(serviceContext);
            sessionContext.addServiceGroupContext(serviceGroupContext);
        }
    }

    private static final QName SERVICE_GROUP_QNAME = new QName(Constants.AXIS2_NAMESPACE_URI,
                                                               Constants.SERVICE_GROUP_ID,
                                                               Constants.AXIS2_NAMESPACE_PREFIX);

    private void extractServiceGroupContextId(MessageContext msgContext) throws AxisFault {
        SOAPHeader soapHeader = msgContext.getEnvelope().getHeader();
        if (soapHeader != null) {
            OMElement serviceGroupId = soapHeader.getFirstChildWithName(SERVICE_GROUP_QNAME);
            if (serviceGroupId != null) {
                msgContext.setServiceGroupContextId(serviceGroupId.getText());
            }
        }
    }
    
    /**
     * This method will determine if the MEP indicated by 'mepString' specifies
     * a oneway MEP.
     */
    @SuppressWarnings("deprecation")
    boolean isOneway(String mepString) {
        return (mepString.equals(WSDL20_2006Constants.MEP_URI_IN_ONLY)
                || mepString.equals(WSDL20_2004_Constants.MEP_URI_IN_ONLY)
                || mepString.equals(WSDL2Constants.MEP_URI_IN_ONLY));
    }

}