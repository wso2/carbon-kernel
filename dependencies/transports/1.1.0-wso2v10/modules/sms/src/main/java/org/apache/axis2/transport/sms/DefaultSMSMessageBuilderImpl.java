/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.transport.sms;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.description.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.util.MultipleEntryHashMap;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.om.*;
import org.apache.ws.commons.schema.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.activation.DataHandler;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * Builds the MessageContext for the from the incoming SMS
 * accepts the RPC style message format
 * serviceName : operationName : param_1 = value_1 : param_2 = value_2 : param_3 = value _3 : ... : param_n = value_n
 * eg: Election : vote : party = XXX : candidate = XXX
 */
public class DefaultSMSMessageBuilderImpl implements SMSMessageBuilder {
    public static final String KEY_VALUE_SEPERATOR="=";
     /** the reference to the actual commons logger to be used for log messages */
    protected Log log = LogFactory.getLog(this.getClass());


    public MessageContext buildMessaage(SMSMessage msg,ConfigurationContext configurationContext)
            throws InvalidMessageFormatException {
        String message = msg.getContent();
        String sender =  msg.getSender();
        String receiver = msg.getReceiver();
        String[] parts = message.split(":");


        //may be can add feature to send message format for a request like ????
        if (parts.length < 2) {
            throw new InvalidMessageFormatException("format must be  \"service_name \" : \"opration_name\" : " +
                    "\"parm_1=val_1\" :..:\"param_n = val_n\"");
        } else {
            AxisConfiguration repo = configurationContext.getAxisConfiguration();
            MessageContext messageContext = configurationContext.createMessageContext();

            parts = trimSplited(parts);

            try {
                AxisService axisService = repo.getService(parts[0]);
                if (axisService == null) {

                    throw new InvalidMessageFormatException("Service : " + parts[0] + "does not exsist");

                } else {
                    messageContext.setAxisService(axisService);
                    AxisOperation axisOperation = axisService.getOperation(new QName(parts[1]));

                    if (axisOperation == null) {
                        throw new InvalidMessageFormatException("Operation: " + parts[1] + " does not exsist");
                    }

                    messageContext.setAxisOperation(axisOperation);

                    messageContext.setAxisMessage(axisOperation.getMessage(
                            WSDLConstants.MESSAGE_LABEL_IN_VALUE));

                    Map params = getParams(parts,2);

                    SOAPEnvelope soapEnvelope = createSoapEnvelope(messageContext , params);
                    messageContext.setServerSide(true);
                    messageContext.setEnvelope(soapEnvelope);
                    TransportInDescription in = configurationContext.getAxisConfiguration().getTransportIn("sms");
                    TransportOutDescription out = configurationContext.getAxisConfiguration().getTransportOut("sms");
                    messageContext.setProperty(SMSTransportConstents.SEND_TO , sender);
                    messageContext.setProperty(SMSTransportConstents.DESTINATION , receiver);
                    messageContext.setTransportIn(in);
                    messageContext.setTransportOut(out);
                    handleSMSProperties(msg , messageContext);
                    return messageContext;
                }


            } catch (AxisFault axisFault) {
                log.debug("[DefaultSMSMessageBuilderImpl] Error while extracting the axis2Service \n" +
                        axisFault);
            }

        }


        return null;
    }

    /**
     * this will add the SMSMessage properties to the Axis2MessageContext 
     * @param msg
     * @param messageContext
     */
    protected void handleSMSProperties(SMSMessage msg , MessageContext messageContext) {

        Iterator<String> it = msg.getProperties().keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            messageContext.setProperty(key , msg.getProperties().get(key));
        }

       
    }
    private SOAPEnvelope createSoapEnvelope(MessageContext messageContext , Map params) {
        SOAPFactory soapFactory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope inEnvlope = soapFactory.getDefaultEnvelope();
        SOAPBody inBody = inEnvlope.getBody();
        XmlSchemaElement xmlSchemaElement;
        AxisOperation axisOperation = messageContext.getAxisOperation();
        if (axisOperation != null) {
            AxisMessage axisMessage =
                    axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            xmlSchemaElement = axisMessage.getSchemaElement();

            if (xmlSchemaElement == null) {
                OMElement bodyFirstChild =
                        soapFactory
                                .createOMElement(messageContext.getAxisOperation().getName(), inBody);
                createSOAPMessageWithoutSchema(soapFactory,bodyFirstChild,params);
            }  else {

                // first get the target namespace from the schema and the wrapping element.
                // create an OMElement out of those information. We are going to extract parameters from
                // url, create OMElements and add them as children to this wrapping element.
                String targetNamespace = xmlSchemaElement.getQName().getNamespaceURI();
                QName bodyFirstChildQName;
                if (targetNamespace != null && !"".equals(targetNamespace)) {
                    bodyFirstChildQName = new QName(targetNamespace, xmlSchemaElement.getName());
                } else {
                    bodyFirstChildQName = new QName(xmlSchemaElement.getName());
                }
                OMElement bodyFirstChild = soapFactory.createOMElement(bodyFirstChildQName, inBody);

                // Schema should adhere to the IRI style in this. So assume IRI style and dive in to
                // schema
                XmlSchemaType schemaType = xmlSchemaElement.getSchemaType();
                if (schemaType instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType complexType = ((XmlSchemaComplexType)schemaType);
                    XmlSchemaParticle particle = complexType.getParticle();
                    if (particle instanceof XmlSchemaSequence || particle instanceof XmlSchemaAll) {
                        XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase)particle;
                        Iterator iterator = xmlSchemaGroupBase.getItems().getIterator();

                        // now we need to know some information from the binding operation.

                        while (iterator.hasNext()) {
                            XmlSchemaElement innerElement = (XmlSchemaElement)iterator.next();
                            QName qName = innerElement.getQName();
                            if (qName == null && innerElement.getSchemaTypeName()
                                    .equals(org.apache.ws.commons.schema.constants.Constants.XSD_ANYTYPE)) {
                                createSOAPMessageWithoutSchema(soapFactory, bodyFirstChild,
                                                               params);
                                break;
                            }
                            long minOccurs = innerElement.getMinOccurs();
                            boolean nillable = innerElement.isNillable();
                            String name =
                                    qName != null ? qName.getLocalPart() : innerElement.getName();
                            Object value;
                            OMNamespace ns = (qName == null ||
                                              qName.getNamespaceURI() == null
                                              || qName.getNamespaceURI().length() == 0) ?
                                    null : soapFactory.createOMNamespace(
                                    qName.getNamespaceURI(), null);

                            // FIXME changed
                            if ((value = params.get(name)) != null ) {
                                addRequestParameter(soapFactory,
                                                    bodyFirstChild, ns, name, value);
                                minOccurs--;
                            }
                            if (minOccurs > 0) {
                                if (nillable) {

                                    OMNamespace xsi = soapFactory.createOMNamespace(
                                            Constants.URI_DEFAULT_SCHEMA_XSI,
                                            Constants.NS_PREFIX_SCHEMA_XSI);
                                    OMAttribute omAttribute =
                                            soapFactory.createOMAttribute("nil", xsi, "true");
                                    soapFactory.createOMElement(name, ns,
                                                                bodyFirstChild)
                                            .addAttribute(omAttribute);

                                } else {
//                                    throw new AxisFault("Required element " + qName +
//                                                        " defined in the schema can not be" +
//                                                        " found in the request");
                                }
                            }
                        }
                    }
                }
            }

        }



        return inEnvlope;

    }

    private static void createSOAPMessageWithoutSchema(SOAPFactory soapFactory,
                                                       OMElement bodyFirstChild,
                                                       Map requestParameterMap) {

        // first add the parameters in the URL
        if (requestParameterMap != null) {
            Iterator requestParamMapIter = requestParameterMap.keySet().iterator();
            while (requestParamMapIter.hasNext()) {
                String key = (String) requestParamMapIter.next();
                Object value = requestParameterMap.get(key);
                if (value != null) {
                    addRequestParameter(soapFactory, bodyFirstChild, null, key,
                            value);
                }

            }
        }
    }

    private static void addRequestParameter(SOAPFactory soapFactory,
                                            OMElement bodyFirstChild,
                                            OMNamespace ns,
                                            String key,
                                            Object parameter) {
        if (parameter instanceof DataHandler) {
            DataHandler dataHandler = (DataHandler) parameter;
            OMText dataText = bodyFirstChild.getOMFactory().createOMText(
                    dataHandler, true);
            soapFactory.createOMElement(key, ns, bodyFirstChild).addChild(
                    dataText);
        } else {
            String textValue = parameter.toString();
            soapFactory.createOMElement(key, ns, bodyFirstChild).setText(
                    textValue);
        }
    }

    private Map getParams(String []array , int startIndex) throws InvalidMessageFormatException{
        HashMap params  = new HashMap();
        for(int i=startIndex ;i < array.length ;i++) {
            String [] pramParts = array[i].split(KEY_VALUE_SEPERATOR);

            pramParts = trimSplited(pramParts);

            if(pramParts == null || pramParts.length != 2) {
                throw new InvalidMessageFormatException("format must be  \"service_name \" : \"opration_name\" : " +
                    "\"parm_1=val_1\" :..:\"param_n = val_n\"");
            }

            params.put( pramParts[0] , pramParts[1] );
        }
        return params;
    }

    private String[] trimSplited(String parts[]) {
        if (parts == null) {
            return null;
        }
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        return parts;
    }

}


