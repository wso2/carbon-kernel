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

package org.apache.axis2.addressing.wsdl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.PortType;

/**
 * Generates a wsa:Action value using the Default Action Pattern defined at
 * http://www.w3.org/TR/2006/WD-ws-addr-wsdl-20060216/#defactionwsdl11
 */
public class WSDL11DefaultActionPatternHelper {

    private static final Log log = LogFactory.getLog(WSDL11DefaultActionPatternHelper.class);

    // String constants used extensively in this class
    private static final String URN = "urn";
    private static final String SLASH = "/";
    private static final String COLON = ":";
    private static final String REQUEST = "Request";
    private static final String RESPONSE = "Response";
    private static final String SOLICIT = "Solicit";
    private static final String FAULT = "Fault";

    /**
     * Generate the Action for an Input using the Default Action Pattern
     * <p/>
     * Pattern is defined as [target namespace][delimiter][port type name][delimiter][input name]
     *
     * @param def            is required to obtain the targetNamespace
     * @param wsdl4jPortType is required to obtain the portType name
     * @param op             is required to generate the input name if not explicitly specified
     * @param input          is required for its name if specified
     * @return a wsa:Action value based on the Default Action Pattern and the provided objects
     */
    public static String generateActionFromInputElement(Definition def, PortType wsdl4jPortType,
                                                        Operation op, Input input) {
        // Get the targetNamespace of the wsdl:definitions
        String targetNamespace = def.getTargetNamespace();

        // Determine the delimiter. Per the spec: 'is ":" when the [target namespace] is a URN, otherwise "/". 
        // Note that for IRI schemes other than URNs which aren't path-based (i.e. those that outlaw the "/" 
        // character), the default action value may not conform to the rules of the IRI scheme. Authors
        // are advised to specify explicit values in the WSDL in this case.'
        String delimiter = SLASH;
        if (targetNamespace.toLowerCase().startsWith(URN)) {
            delimiter = COLON;
        }

        // Get the portType name (as a string to be included in the action)
        String portTypeName = wsdl4jPortType.getQName().getLocalPart();
        // Get the name of the input element (and generate one if none explicitly specified)
        String inputName = getNameFromInputElement(op, input);

        // Append the bits together
        StringBuffer sb = new StringBuffer();
        sb.append(targetNamespace);
        // Deal with the problem that the targetNamespace may or may not have a trailing delimiter
        if (!targetNamespace.endsWith(delimiter)) {
            sb.append(delimiter);
        }
        sb.append(portTypeName);
        sb.append(delimiter);
        sb.append(inputName);

        // Resolve the action from the StringBuffer
        String result = sb.toString();

        if (log.isTraceEnabled()) {
            log.trace("generateActionFromInputElement result: " + result);
        }

        return result;
    }

    /**
     * Get the name of the specified Input element using the rules defined in WSDL 1.1
     * Section 2.4.5 http://www.w3.org/TR/wsdl#_names
     */
    private static String getNameFromInputElement(Operation op, Input input) {
        // Get the name from the input element if specified.
        String result = input.getName();

        // If not we'll have to generate it.
        if (result == null) {
            // If Request-Response or Solicit-Response do something special per
            // WSDL 1.1 Section 2.4.5
            OperationType operationType = op.getStyle();
            if (null != operationType) {
                if (operationType.equals(OperationType.REQUEST_RESPONSE)) {
                    result = op.getName() + REQUEST;
                } else if (operationType.equals(OperationType.SOLICIT_RESPONSE)) {
                    result = op.getName() + RESPONSE;
                }
            }
            // If the OperationType was not available for some reason, assume on-way or notification
            if (result == null) {
                result = op.getName();
            }
        }
        return result;
    }

    protected static String getInputActionFromStringInformation(String messageExchangePattern,
                                                                String targetNamespace,
                                                                String portTypeName,
                                                                String operationName,
                                                                String inputName) {
    	if (messageExchangePattern == null && inputName == null) {
            throw new IllegalArgumentException(
                    "One of messageExchangePattern or inputName must the non-null to generate an action.");
        }

        // Determine the delimiter. Per the spec: 'is ":" when the [target namespace] is a URN, otherwise "/". 
        // Note that for IRI schemes other than URNs which aren't path-based (i.e. those that outlaw the "/" 
        // character), the default action value may not conform to the rules of the IRI scheme. Authors
        // are advised to specify explicit values in the WSDL in this case.'
        String delimiter = SLASH;
        if (targetNamespace.toLowerCase().startsWith(URN)) {
            delimiter = COLON;
        }

        // N.B. Unlike core Axis2 processing WSDL, JAX-WS accotation processing passes in the
        // out-in MEP when constructing the client, hence we need to take account of it here
        // in addition to the expected in-out
        if (inputName == null) {
            inputName = operationName;
            if (messageExchangePattern.indexOf("in-out") >= 0) {
                inputName += REQUEST;
            } else if (messageExchangePattern.indexOf("out-in") >= 0) {
                inputName += RESPONSE;
            }
        }

        // Append the bits together
        StringBuffer sb = new StringBuffer();
        sb.append(targetNamespace);
        // Deal with the problem that the targetNamespace may or may not have a trailing delimiter
        if (!targetNamespace.endsWith(delimiter)) {
            sb.append(delimiter);
        }
        sb.append(portTypeName);
        sb.append(delimiter);
        sb.append(inputName);

        // Resolve the action from the StringBuffer
        String result = sb.toString();

        if (log.isTraceEnabled()) {
            log.trace("getInputActionFromStringInformation result: " + result);
        }

        return result;
    }

    /**
     * Generate the Action for an Output using the Default Action Pattern
     * <p/>
     * Pattern is defined as [target namespace][delimiter][port type name][delimiter][output name]
     *
     * @param def            is required to obtain the targetNamespace
     * @param wsdl4jPortType is required to obtain the portType name
     * @param op             is required to generate the output name if not explicitly specified
     * @param output         is required for its name if specified
     * @return a wsa:Action value based on the Default Action Pattern and the provided objects
     */
    public static String generateActionFromOutputElement(Definition def, PortType wsdl4jPortType,
                                                         Operation op, Output output) {
        // Get the targetNamespace of the wsdl:definition
        String targetNamespace = def.getTargetNamespace();

        // Determine the delimiter. Per the spec: 'is ":" when the [target namespace] is a URN, otherwise "/". 
        // Note that for IRI schemes other than URNs which aren't path-based (i.e. those that outlaw the "/" 
        // character), the default action value may not conform to the rules of the IRI scheme. Authors
        // are advised to specify explicit values in the WSDL in this case.'
        String delimiter = SLASH;
        if (targetNamespace.toLowerCase().startsWith(URN)) {
            delimiter = COLON;
        }

        // Get the portType name (as a string to be included in the action)
        String portTypeName = wsdl4jPortType.getQName().getLocalPart();
        // Get the name of the output element (and generate one if none explicitly specified)
        String outputName = getNameFromOutputElement(op, output);

        // Append the bits together
        StringBuffer sb = new StringBuffer();
        sb.append(targetNamespace);
        // Deal with the problem that the targetNamespace may or may not have a trailing delimiter
        if (!targetNamespace.endsWith(delimiter)) {
            sb.append(delimiter);
        }
        sb.append(portTypeName);
        sb.append(delimiter);
        sb.append(outputName);

        // Resolve the action from the StringBuffer
        String result = sb.toString();

        if (log.isTraceEnabled()) {
            log.trace("generateActionFromOutputElement result: " + result);
        }

        return result;
    }

    /**
     * Get the name of the specified Output element using the rules defined in WSDL 1.1
     * Section 2.4.5 http://www.w3.org/TR/wsdl#_names
     */
    private static String getNameFromOutputElement(Operation op, Output output) {
        // Get the name from the output element if specified.
        String result = output.getName();

        // If not we'll have to generate it.
        if (result == null) {
            // If Request-Response or Solicit-Response do something special per
            // WSDL 1.1 Section 2.4.5
            OperationType operationType = op.getStyle();
            if (null != operationType) {
                if (operationType.equals(OperationType.REQUEST_RESPONSE)) {
                    return op.getName() + RESPONSE;
                } else if (operationType.equals(OperationType.SOLICIT_RESPONSE)) {
                    return op.getName() + SOLICIT;
                }
            }
            // If the OperationType was not available for some reason, assume on-way or notification
            if (result == null) {
                result = op.getName();
            }
        }
        return result;
    }

    protected static String getOutputActionFromStringInformation(String messageExchangePattern,
                                                                 String targetNamespace,
                                                                 String portTypeName,
                                                                 String operationName,
                                                                 String outputName) {
        if (messageExchangePattern == null && outputName == null) {
            throw new IllegalArgumentException(
                    "One of messageExchangePattern or outputName must the non-null to generate an action.");
        }

        // Determine the delimiter. Per the spec: 'is ":" when the [target namespace] is a URN, otherwise "/". 
        // Note that for IRI schemes other than URNs which aren't path-based (i.e. those that outlaw the "/" 
        // character), the default action value may not conform to the rules of the IRI scheme. Authors
        // are advised to specify explicit values in the WSDL in this case.'
        String delimiter = SLASH;
        if (targetNamespace.toLowerCase().startsWith(URN)) {
            delimiter = COLON;
        }

        // N.B. Unlike core Axis2 processing WSDL, JAX-WS annotation processing passes in the
        // out-in MEP when constructing the client, hence we need to take account of it here
        // in addition to the expected in-out
        if (outputName == null) {
            outputName = operationName;
            if (messageExchangePattern.indexOf("in-out") >= 0) {
                outputName += RESPONSE;
            } else if (messageExchangePattern.indexOf("out-in") >= 0) {
                outputName += REQUEST;
            }
        }

        // Append the bits together
        StringBuffer sb = new StringBuffer();
        sb.append(targetNamespace);
        // Deal with the problem that the targetNamespace may or may not have a trailing delimiter
        if (!targetNamespace.endsWith(delimiter)) {
            sb.append(delimiter);
        }
        sb.append(portTypeName);
        sb.append(delimiter);
        sb.append(outputName);

        // Resolve the action from the StringBuffer
        String result = sb.toString();

        if (log.isTraceEnabled()) {
            log.trace("getOutputActionFromStringInformation result: " + result);
        }

        return result;
    }

    /**
     * Generate the Action for a Fault using the Default Action Pattern
     * <p/>
     * Pattern is defined as [target namespace][delimiter][port type name][delimiter][operation name][delimiter]Fault[delimiter][fault name]
     *
     * @param def            is required to obtain the targetNamespace
     * @param wsdl4jPortType is required to obtain the portType name
     * @param op             is required to obtain the operation name
     * @param fault          is required to obtain the fault name
     * @return a wsa:Action value based on the Default Action Pattern and the provided objects
     */
    public static String generateActionFromFaultElement(Definition def, PortType wsdl4jPortType,
                                                        Operation op, Fault fault) {
        // Get the targetNamespace of the wsdl:definition
        String targetNamespace = def.getTargetNamespace();

        // Determine the delimiter. Per the spec: 'is ":" when the [target namespace] is a URN, otherwise "/". 
        // Note that for IRI schemes other than URNs which aren't path-based (i.e. those that outlaw the "/" 
        // character), the default action value may not conform to the rules of the IRI scheme. Authors
        // are advised to specify explicit values in the WSDL in this case.'
        String delimiter = SLASH;
        if (targetNamespace.toLowerCase().startsWith(URN)) {
            delimiter = COLON;
        }

        // Get the portType name (as a string to be included in the action)
        String portTypeName = wsdl4jPortType.getQName().getLocalPart();
        // Get the operation name (as a string to be included in the action)
        String operationName = op.getName();

        // Get the name of the fault element (name is mandatory on fault elements)
        String faultName = fault.getName();

        // Append the bits together
        StringBuffer sb = new StringBuffer();
        sb.append(targetNamespace);
        // Deal with the problem that the targetNamespace may or may not have a trailing delimiter
        if (!targetNamespace.endsWith(delimiter)) {
            sb.append(delimiter);
        }
        sb.append(portTypeName);
        sb.append(delimiter);
        sb.append(operationName);
        sb.append(delimiter);
        sb.append(FAULT);
        sb.append(delimiter);
        sb.append(faultName);

        // Resolve the action from the StringBuffer
        String result = sb.toString();

        if (log.isTraceEnabled()) {
            log.trace("generateActionFromFaultElement result: " + result);
        }

        return result;
    }

    protected static String getFaultActionFromStringInformation(String targetNamespace,
                                                                String portTypeName,
                                                                String operationName,
                                                                String faultName) {
        // Determine the delimiter. Per the spec: 'is ":" when the [target namespace] is a URN, otherwise "/". 
        // Note that for IRI schemes other than URNs which aren't path-based (i.e. those that outlaw the "/" 
        // character), the default action value may not conform to the rules of the IRI scheme. Authors
        // are advised to specify explicit values in the WSDL in this case.'
        String delimiter = SLASH;
        if (targetNamespace.toLowerCase().startsWith(URN)) {
            delimiter = COLON;
        }

//      Append the bits together
        StringBuffer sb = new StringBuffer();
        sb.append(targetNamespace);
        // Deal with the problem that the targetNamespace may or may not have a trailing delimiter
        if (!targetNamespace.endsWith(delimiter)) {
            sb.append(delimiter);
        }
        sb.append(portTypeName);
        sb.append(delimiter);
        sb.append(operationName);
        sb.append(delimiter);
        sb.append(FAULT);
        sb.append(delimiter);
        sb.append(faultName);

        // Resolve the action from the StringBuffer
        String result = sb.toString();

        if (log.isTraceEnabled()) {
            log.trace("getFaultActionFromStringInformation result: " + result);
        }

        return result;
    }
}
