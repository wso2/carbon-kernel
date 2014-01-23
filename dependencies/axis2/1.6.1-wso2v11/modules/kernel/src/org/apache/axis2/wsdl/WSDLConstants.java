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

package org.apache.axis2.wsdl;

import org.apache.axis2.namespace.Constants;

import javax.xml.namespace.QName;

public interface WSDLConstants {

    String WSDL_1_1_STYLE = "style";

    String STYLE_RPC = "rpc";
    String STYLE_DOC = "document";
    String STYLE_MSG = "msg";

    String WSDL_4_J_DEFINITION = "wsdl4jDefinition";
    String WSDL_20_DESCRIPTION = "WSDL20Description";
    /**
     * Field WSDL2_0_NAMESPACE
     */
    public static final String WSDL2_0_NAMESPACE = Constants.NS_URI_WSDL20;

    /**
     * Field WSDL1_1_NAMESPACE
     */
    public static final String WSDL1_1_NAMESPACE = Constants.NS_URI_WSDL11;


    /**
     * Field WSDL_MESSAGE_DIRECTION_IN
     */
    public static final String WSDL_MESSAGE_DIRECTION_IN = "in";

    public static final String WSDL_MESSAGE_IN_MESSAGE = "inMessage";

    public static final String WSDL_MESSAGE_OUT_MESSAGE = "outMessage";

    public static final String SOAP_HEADER = "soap_header";

    public static final String HTTP_HEADER = "http_header";


    /**
     * Field WSDL_MESSAGE_DIRECTION_OUT
     */
    public static final String WSDL_MESSAGE_DIRECTION_OUT = "out";

    //////////////////////////////////////////////////
    //////////////// Message Labels///////////////////
    //////////////////////////////////////////////////

    /**
     * Constant to represent the message label "In" which is used by the
     * following WSDL 2.0 defined MEPs: In-Only, Robust In-Only, In-Out,
     * In-Optional-Out, Out-In, Out-Optional-In.
     */
    public static final byte MESSAGE_LABEL_IN = 0;

    public static final String MESSAGE_LABEL_IN_VALUE = "In";
    public static final String MESSAGE_LABEL_FAULT_VALUE = "Fault";

    /**
     * Constant to represent the message label "Out" which is used by the
     * following WSDL 2.0 defined MEPs: In-Out, In-Optional-Out, Out-Only,
     * Robust Out-Only, Out-In, Out-Optional-In.
     */
    public static final int MESSAGE_LABEL_OUT = 1;

    public static final String MESSAGE_LABEL_OUT_VALUE = "Out";

    /**
     *
     */
    public static final String WSDL_USE_LITERAL = "literal";
    public static final String WSDL_USE_ENCODED = "encoded";


    int WSDL_1_1 = 1;
    int WSDL_2_0 = 2;
    String INPUT_PART_QNAME_SUFFIX = "_input";
    String OUTPUT_PART_QNAME_SUFFIX = "_output";


    public static interface WSDL11Constants {

        /**
         * The Type name for the SOAP Address defined in the Port/Endpoint
         */
        QName SOAP_11_ADDRESS = new QName(
                Constants.URI_WSDL11_SOAP, "address");
        QName SOAP_12_ADDRESS = new QName(
                Constants.URI_WSDL12_SOAP, "address");
        QName SOAP_11_OPERATION = new QName(
                Constants.URI_WSDL11_SOAP, "operation");
        QName SOAP_12_OPERATION = new QName(
                Constants.URI_WSDL12_SOAP, "operation");
        QName SCHEMA = new QName(
                Constants.URI_2001_SCHEMA_XSD, "schema");
        QName SOAP_11_BODY = new QName(
                Constants.URI_WSDL11_SOAP, "body");
        QName SOAP_12_BODY = new QName(
                Constants.URI_WSDL12_SOAP, "body");
        QName SOAP_11_HEADER = new QName(
                Constants.URI_WSDL11_SOAP, "header");
        QName SOAP_12_HEADER = new QName(
                Constants.URI_WSDL12_SOAP, "header");
        QName SOAP_11_BINDING = new QName(
                Constants.URI_WSDL11_SOAP, "binding");
        QName SOAP_12_BINDING = new QName(
                Constants.URI_WSDL12_SOAP, "binding");
        QName POLICY = new QName(
                Constants.URI_POLICY, "Policy");
        QName POLICY_REFERENCE = new QName(
                Constants.URI_POLICY, "PolicyReference");
    }

    public static final int MEP_CONSTANT_OUT_IN = 16;
    public static final int MEP_CONSTANT_OUT_ONLY = 14;
    public static final int MEP_CONSTANT_IN_ONLY = 10;
    public static final int MEP_CONSTANT_ROBUST_IN_ONLY = 11;
    public static final int MEP_CONSTANT_IN_OUT = 12;
    public static final int MEP_CONSTANT_IN_OPTIONAL_OUT = 13;
    int MEP_CONSTANT_OUT_OPTIONAL_IN = 17;
    int MEP_CONSTANT_INVALID = -1;
    int MEP_CONSTANT_ROBUST_OUT_ONLY = 15;


    public static interface WSDL20_2006Constants {

        /**
         * http://www.w3.org/TR/2006/CR-wsdl20-adjuncts-20060327/#in-only
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_IN_ONLY
         */
        public String MEP_URI_IN_ONLY = "http://www.w3.org/2006/01/wsdl/in-only";
        /**
         * http://www.w3.org/TR/2006/CR-wsdl20-adjuncts-20060327/#robust-in-only
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_ROBUST_IN_ONLY
         */
        public String MEP_URI_ROBUST_IN_ONLY = "http://www.w3.org/2006/01/wsdl/robust-in-only";
        /**
         * http://www.w3.org/TR/2006/CR-wsdl20-adjuncts-20060327/#in-out
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_IN_OUT
         */
        public String MEP_URI_IN_OUT = "http://www.w3.org/2006/01/wsdl/in-out";
        /**
         * http://www.w3.org/TR/2006/CR-wsdl20-adjuncts-20060327/#in-opt-out
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT
         */
        public String MEP_URI_IN_OPTIONAL_OUT = "http://www.w3.org/2006/01/wsdl/in-opt-out";
        /**
         * http://www.w3.org/TR/2006/CR-wsdl20-adjuncts-20060327/#out-only
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_OUT_ONLY
         */
        public String MEP_URI_OUT_ONLY = "http://www.w3.org/2006/01/wsdl/out-only";
        /**
         * http://www.w3.org/TR/2006/CR-wsdl20-adjuncts-20060327/#robust-out-only
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY
         */
        public String MEP_URI_ROBUST_OUT_ONLY = "http://www.w3.org/2006/01/wsdl/robust-out-only";
        /**
         * http://www.w3.org/TR/2006/CR-wsdl20-adjuncts-20060327/#out-in
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_OUT_IN
         */
        public String MEP_URI_OUT_IN = "http://www.w3.org/2006/01/wsdl/out-in";
        /**
         * http://www.w3.org/TR/2006/CR-wsdl20-adjuncts-20060327/#out-opt-in
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN
         */
        public String MEP_URI_OUT_OPTIONAL_IN = "http://www.w3.org/2006/01/wsdl/out-opt-in";

    }

    public static interface WSDL20_2004_Constants {

        /**
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_IN_ONLY
         */
        public String MEP_URI_IN_ONLY = "http://www.w3.org/2004/08/wsdl/in-only";

        /**
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_ROBUST_IN_ONLY
         */
        public String MEP_URI_ROBUST_IN_ONLY = "http://www.w3.org/2004/08/wsdl/robust-in-only";

        /**
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_IN_OUT
         */

        public String MEP_URI_IN_OUT = "http://www.w3.org/2004/08/wsdl/in-out";

        /**
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT
         */
        public String MEP_URI_IN_OPTIONAL_OUT = "http://www.w3.org/2004/08/wsdl/in-opt-out";

        /**
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_OUT_ONLY
         */
        public String MEP_URI_OUT_ONLY = "http://www.w3.org/2004/08/wsdl/out-only";

        /**
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY
         */
        public String MEP_URI_ROBUST_OUT_ONLY = "http://www.w3.org/2004/08/wsdl/robust-out-only";

        /**
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_OUT_IN
         */
        public String MEP_URI_OUT_IN = "http://www.w3.org/2004/08/wsdl/out-in";

        /**
         * @deprecated Please use org.apache.axis2.description.WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN
         */
        public String MEP_URI_OUT_OPTIONAL_IN = "http://www.w3.org/2004/08/wsdl/out-opt-in";
    }
}
