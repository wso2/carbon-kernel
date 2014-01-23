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

package org.apache.axis2.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.WSDL20DefaultValueHolder;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.transport.http.util.URIEncoderDecoder;
import org.apache.woden.wsdl20.extensions.http.HTTPLocation;
import org.apache.woden.wsdl20.extensions.http.HTTPLocationTemplate;
import org.apache.woden.wsdl20.extensions.soap.SOAPFaultCode;
import org.apache.woden.wsdl20.extensions.soap.SOAPFaultSubcodes;

import javax.xml.namespace.QName;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

public class WSDL20Util {
    public static void extractWSDL20SoapFaultInfo(Map options, OMElement bindingMessageElement, OMFactory omFactory, OMNamespace wsoap) {
        // Fault specific properties
        SOAPFaultCode faultCode = (SOAPFaultCode) options
                .get(WSDL2Constants.ATTR_WSOAP_CODE);
        if (faultCode != null && faultCode.getQName() != null) {
            bindingMessageElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_CODE, wsoap, faultCode.getQName().getLocalPart()));
        }
        SOAPFaultSubcodes soapFaultSubcodes = (SOAPFaultSubcodes) options
                .get(WSDL2Constants.ATTR_WSOAP_SUBCODES);
        QName faultCodes[];
        if (soapFaultSubcodes != null && (faultCodes = soapFaultSubcodes.getQNames()) != null) {
            for (int i = 0; i < faultCodes.length; i++) {
                bindingMessageElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.ATTRIBUTE_SUBCODES, wsoap, faultCodes[0].getLocalPart()));
            }
        }
    }

    /**
     * This method is used to resolve httplocation property. It changes the URL as stipulated by
     * the httplocation property.
     *
     * @param messageContext - The MessageContext of the request
     * @param rawURLString   - The raw URL containing httplocation templates
     * @param detach         - Boolean value specifying whether the element should be detached from the
     *                       envelop. When serializing data as application/x-form-urlencoded what goes in the body is the
     *                       remainder and therefore we should detach the element from the envelop.
     * @return - String with templated values replaced
     * @throws org.apache.axis2.AxisFault - Thrown in case an exception occurs
     */
    public static String applyURITemplating(MessageContext messageContext, String rawURLString,
                                             boolean detach) throws AxisFault {

        OMElement firstElement;
        if (detach) {
            firstElement = messageContext.getEnvelope().getBody().getFirstElement();
        } else {
            firstElement =
                    messageContext.getEnvelope().getBody().getFirstElement().cloneOMElement();
        }
        String queryParameterSeparator = (String) messageContext.getProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR);
        if (queryParameterSeparator == null) {
            queryParameterSeparator = WSDL20DefaultValueHolder.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR_DEFAULT;
        }
        HTTPLocation httpLocation = new HTTPLocation(rawURLString);
        HTTPLocationTemplate[] templates = httpLocation.getTemplates();

        for (int i = 0; i < templates.length; i++) {
            HTTPLocationTemplate template = templates[i];
            String localName = template.getName();
            String elementValue = getOMElementValue(localName, firstElement);
            if (template.isEncoded()) {
                try {

                    if (template.isQuery()) {
                        template.setValue(URIEncoderDecoder.quoteIllegal(
                                elementValue,
                                WSDL2Constants.LEGAL_CHARACTERS_IN_QUERY.replaceAll(queryParameterSeparator, "")));
                    } else {
                        template.setValue(URIEncoderDecoder.quoteIllegal(
                                elementValue,
                                WSDL2Constants.LEGAL_CHARACTERS_IN_PATH));
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new AxisFault("Unable to encode Query String");
                }

            } else {
                template.setValue(elementValue);
            }
        }

        return httpLocation.getFormattedLocation();
    }

    /**
     * This method is used to retrive elements from the soap envelop
     *
     * @param elementName   - The name of the required element
     * @param parentElement - The parent element that the required element should be retrived from
     * @return - The value of the element as a string
     */
    private static String getOMElementValue(String elementName, OMElement parentElement) {

        OMElement httpURLParam = null;
        Iterator children = parentElement.getChildElements();

        while (children.hasNext()) {
            OMElement child = (OMElement) children.next();
            QName qName = child.getQName();
            if (elementName.equals(qName.getLocalPart())) {
                httpURLParam = child;
                break;
            }
        }

        if (httpURLParam != null) {
            httpURLParam.detach();

            if (parentElement.getFirstOMChild() == null) {
                parentElement.detach();
            }
            return httpURLParam.getText();
        }
        return "";

    }

}
