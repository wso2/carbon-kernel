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

package org.apache.axis2.builder;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.WSDL20DefaultValueHolder;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.util.URIEncoderDecoder;
import org.apache.axis2.util.MultipleEntryHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;


public class XFormURLEncodedBuilder implements Builder {

    private static final Log log = LogFactory.getLog(XFormURLEncodedBuilder.class);

    /**
     * @return Returns the document element.
     */
    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext)
            throws AxisFault {

        MultipleEntryHashMap parameterMap = new MultipleEntryHashMap();
        SOAPFactory soapFactory;
        AxisBindingOperation axisBindingOperation =
                (AxisBindingOperation) messageContext.getProperty(
                        Constants.AXIS_BINDING_OPERATION);
        String queryParameterSeparator = null;
        String templatedPath = null;
        if (axisBindingOperation != null) {
            queryParameterSeparator = (String) axisBindingOperation
                    .getProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR);
            templatedPath =
                    (String) axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION);
        }
        if (queryParameterSeparator == null) {
            queryParameterSeparator =
                    WSDL20DefaultValueHolder.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR_DEFAULT;
        }

        AxisEndpoint axisEndpoint =
                (AxisEndpoint) messageContext.getProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME);
        if (axisEndpoint != null) {
            AxisBinding axisBinding = axisEndpoint.getBinding();
            String soapVersion =
                    (String) axisBinding.getProperty(WSDL2Constants.ATTR_WSOAP_VERSION);
            soapFactory = getSOAPFactory(soapVersion);
        } else {
            soapFactory = getSOAPFactory(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        }
        EndpointReference endpointReference = messageContext.getTo();
        if (endpointReference == null) {
            throw new AxisFault("Cannot create DocumentElement without destination EPR");
        }

        String requestURL = endpointReference.getAddress();
        try {
            requestURL = extractParametersUsingHttpLocation(templatedPath, parameterMap,
                                                            requestURL,
                                                            queryParameterSeparator);
        } catch (UnsupportedEncodingException e) {
            throw AxisFault.makeFault(e);
        }

        String query = requestURL;
        int index;
        if ((index = requestURL.indexOf("?")) > -1) {
            query = requestURL.substring(index + 1);
        }

        extractParametersFromRequest(parameterMap, query, queryParameterSeparator,
                                     (String) messageContext.getProperty(
                                             Constants.Configuration.CHARACTER_SET_ENCODING),
                                     inputStream);

        messageContext.setProperty(Constants.REQUEST_PARAMETER_MAP, parameterMap);
        return BuilderUtil.buildsoapMessage(messageContext, parameterMap,
                                            soapFactory);
    }

    protected void extractParametersFromRequest(MultipleEntryHashMap parameterMap,
                                                String query,
                                                String queryParamSeparator,
                                                final String charsetEncoding,
                                                final InputStream inputStream)
            throws AxisFault {

        if (query != null && !"".equals(query)) {

            String parts[] = query.split(queryParamSeparator);
            for (int i = 0; i < parts.length; i++) {
                int separator = parts[i].indexOf("=");
                if (separator > 0) {
                    String value = parts[i].substring(separator + 1);
                    try {
                        value = URIEncoderDecoder.decode(value);
                    } catch (UnsupportedEncodingException e) {
                        throw AxisFault.makeFault(e);
                    }

                    parameterMap
                            .put(parts[i].substring(0, separator),
                                 value);
                }
            }

        }

        if (inputStream != null) {
            try {
                InputStreamReader inputStreamReader =
                        null;
                try {
                    inputStreamReader = (InputStreamReader) AccessController.doPrivileged(
                            new PrivilegedExceptionAction() {
                                public Object run() throws UnsupportedEncodingException {
                                    return new InputStreamReader(inputStream, charsetEncoding);
                                }
                            }
                    );
                } catch (PrivilegedActionException e) {
                    throw (UnsupportedEncodingException) e.getException();
                }
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line != null) {
                        String parts[] = line.split(
                                WSDL20DefaultValueHolder.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR_DEFAULT);
                        for (int i = 0; i < parts.length; i++) {
                            int separator = parts[i].indexOf("=");
                            String value = parts[i].substring(separator + 1);
                            parameterMap.put(parts[i].substring(0, separator),
                                             URIEncoderDecoder.decode(value));
                        }
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                throw AxisFault.makeFault(e);
            }
        }
    }

    /**
     * Here is what I will try to do here. I will first try to identify the location of the first
     * template element in the request URI. I am trying to deduce the location of that location
     * using the httpLocation element of the binding (it is passed in to this
     * method).
     * If there is a contant part in the httpLocation, then I will identify it. For this, I get
     * the index of {, from httpLocation param, and whatever to the left of it is the contant part.
     * Then I search for this constant part inside the url. This will give us the access to the first
     * template parameter.
     * To find the end of this parameter, we need to get the index of the next constant, from
     * httpLocation attribute. Likewise we keep on discovering parameters.
     * <p/>
     * Assumptions :
     * 1. User will always append the value of httpLocation to the address given in the
     * endpoint.
     * 2. I was talking about the constants in the httpLocation. Those constants will not occur,
     * to a reasonable extend, before the constant we are looking for.
     *
     * @param templatedPath
     * @param parameterMap
     */
    protected String extractParametersUsingHttpLocation(String templatedPath,
                                                        MultipleEntryHashMap parameterMap,
                                                        String requestURL,
                                                        String queryParameterSeparator)
            throws AxisFault, UnsupportedEncodingException {


        if (templatedPath != null && !"".equals(templatedPath) && templatedPath.indexOf("{") > -1) {
            StringBuffer pathTemplate = new StringBuffer(templatedPath);

            // this will hold the index, from which we need to process the request URI
            int startIndex = 0;
            int templateStartIndex = 0;
            int templateEndIndex = 0;
            int indexOfNextConstant = 0;

            StringBuffer requestURIBuffer = new StringBuffer(requestURL);

            while (startIndex < requestURIBuffer.length()) {
                // this will always hold the starting index of a template parameter
                templateStartIndex = pathTemplate.indexOf("{", templateStartIndex);

                if (templateStartIndex > 0) {
                    // get the preceding constant part from the template
                    String constantPart =
                            pathTemplate.substring(templateEndIndex + 1, templateStartIndex);
                    constantPart = constantPart.replaceAll("\\{\\{","{");
                    constantPart = constantPart.replaceAll("}}","}");

                    // get the index of the end of this template param
                    templateEndIndex = pathTemplate.indexOf("}", templateStartIndex);
                    if ((pathTemplate.length() -1) > templateEndIndex && pathTemplate.charAt(templateEndIndex +1) == '}') {
                        templateEndIndex = pathTemplate.indexOf("}", templateEndIndex +2);
                    }

                    String parameterName =
                            pathTemplate.substring(templateStartIndex + 1, templateEndIndex);
                    // next try to find the next constant
                    templateStartIndex = pathTemplate.indexOf("{", templateEndIndex);
                    if (pathTemplate.charAt(templateStartIndex +1) == '{') {
                        templateStartIndex = pathTemplate.indexOf("{", templateStartIndex +2);
                    }

                    int endIndexOfConstant = requestURIBuffer
                            .indexOf(constantPart, indexOfNextConstant) + constantPart.length();

                    if (templateStartIndex == -1) {
                        if (templateEndIndex == pathTemplate.length() - 1) {

                            // We may have occations where we have templates of the form foo/{name}.
                            // In this case the next connstant will be ? and not the
                            // queryParameterSeparator
                            indexOfNextConstant =
                                    requestURIBuffer
                                            .indexOf("?", endIndexOfConstant);
                            if (indexOfNextConstant == -1) {
                                indexOfNextConstant =
                                        requestURIBuffer
                                                .indexOf(queryParameterSeparator,
                                                         endIndexOfConstant);
                            }
                            if (indexOfNextConstant > 0) {
                                addParameterToMap(parameterMap, parameterName,
                                                  requestURIBuffer.substring(endIndexOfConstant,
                                                                             indexOfNextConstant));
                                return requestURL.substring(indexOfNextConstant);
                            } else {

                                addParameterToMap(parameterMap, parameterName,
                                                  requestURIBuffer.substring(
                                                          endIndexOfConstant));
                                return "";
                            }

                        } else {

                            constantPart =
                                    pathTemplate.substring(templateEndIndex + 1,
                                                           pathTemplate.length());
                            constantPart = constantPart.replaceAll("\\{\\{","{");
                            constantPart = constantPart.replaceAll("}}","}");
                            indexOfNextConstant =
                                    requestURIBuffer.indexOf(constantPart, endIndexOfConstant);

                            addParameterToMap(parameterMap, parameterName,
                                              requestURIBuffer.substring(
                                                      endIndexOfConstant, indexOfNextConstant));

                            if (requestURIBuffer.length() > indexOfNextConstant + 1) {
                                return requestURIBuffer.substring(indexOfNextConstant + 1);
                            }
                            return "";
                        }
                    } else {

                        // this is the next constant from the template
                        constantPart =
                                pathTemplate
                                        .substring(templateEndIndex + 1, templateStartIndex);
                        constantPart = constantPart.replaceAll("\\{\\{","{");
                        constantPart = constantPart.replaceAll("}}","}");

                        indexOfNextConstant =
                                requestURIBuffer.indexOf(constantPart, endIndexOfConstant);
                        addParameterToMap(parameterMap, parameterName, requestURIBuffer.substring(
                                endIndexOfConstant, indexOfNextConstant));
                        startIndex = indexOfNextConstant;

                    }

                }

            }
        }

        return requestURL;
    }

    private void addParameterToMap(MultipleEntryHashMap parameterMap, String paramName,
                                   String paramValue)
            throws UnsupportedEncodingException, AxisFault {
        try {
            paramValue = URIEncoderDecoder.decode(paramValue);
        } catch (UnsupportedEncodingException e) {
            throw AxisFault.makeFault(e);
        }
        if (paramName.startsWith(WSDL2Constants.TEMPLATE_ENCODE_ESCAPING_CHARACTER)) {
            parameterMap.put(paramName.substring(1), paramValue);
        } else {
            parameterMap.put(paramName, paramValue);
        }

    }

    private SOAPFactory getSOAPFactory(String nsURI) throws AxisFault {
        if (nsURI == null) {
            return OMAbstractFactory.getSOAP12Factory();
        }
        else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP12Factory();
        } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP11Factory();
        } else {
            throw new AxisFault(Messages.getMessage("invalidSOAPversion"));
        }
    }
}