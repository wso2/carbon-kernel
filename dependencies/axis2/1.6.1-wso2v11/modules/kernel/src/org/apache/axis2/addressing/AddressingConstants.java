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

package org.apache.axis2.addressing;

import javax.xml.namespace.QName;

/**
 * Interface AddressingConstants
 */
public interface AddressingConstants {

    // ====================== Common Message Addressing Properties ===================
    static final String WSA_MESSAGE_ID = "MessageID";
    static final String WSA_RELATES_TO = "RelatesTo";
    static final String WSA_RELATES_TO_RELATIONSHIP_TYPE = "RelationshipType";
    static final String WSA_TO = "To";
    static final String WSA_REPLY_TO = "ReplyTo";
    static final String WSA_FROM = "From";
    static final String WSA_FAULT_TO = "FaultTo";
    static final String WSA_ACTION = "Action";
    static final String EPR_SERVICE_NAME = "ServiceName";
    static final String EPR_REFERENCE_PARAMETERS = "ReferenceParameters";

    // ====================== Common EPR Elements ============================
    static final String EPR_ADDRESS = "Address";
    static final String WS_ADDRESSING_VERSION = "WSAddressingVersion";
    static final String WSA_DEFAULT_PREFIX = "wsa";
    static final String PARAM_SERVICE_GROUP_CONTEXT_ID =
            "ServiceGroupContextIdFromAddressing";
    static final String IS_ADDR_INFO_ALREADY_PROCESSED = "IsAddressingProcessed";
    static final String DISABLE_ADDRESSING_FOR_IN_MESSAGES = "disableAddressingForInMessages";
    static final String ADDR_VALIDATE_ACTION = "addressing.validateAction";
    static final String ADDR_VALIDATE_INVOCATION_PATTERN = "addressing.validateInvocationPattern";
    static final String ENDPOINT_REFERENCE = "EndpointReference";

    // ====================== WSDL Binding Constants ========================
    static final String USING_ADDRESSING = "UsingAddressing";
    static final String ANONYMOUS = "Anonymous";

    // ====================== Addressing Requirement Levels ==================
    // These are used to represent the requirement level on WS-Addressing indicated
    // in a services.xml or a WSDL file.
    static final String ADDRESSING_REQUIREMENT_PARAMETER = "addressingRequirementParameter";
    // ADDRESSING_UNSPECIFIED is the equivalent of no UsingAddressing flag in a
    // WSDL file and the default of the WSAddressingRequred attribute in service.xml
    static final String ADDRESSING_UNSPECIFIED = "unspecified";
    // ADDRESSING_OPTIONAL is the equivalent of <wsaw:UsingAddressing required="false" />
    // in a WSDL file
    static final String ADDRESSING_OPTIONAL = "optional";
    // ADDRESSING_REQUIRED is the equivalent of <wsaw:UsingAddressing required="true" />
    // in a WSDL file
    static final String ADDRESSING_REQUIRED = "required";

    // If this property is set, addressing headers will be replaced from the information in the
    // message context.  
    static final String REPLACE_ADDRESSING_HEADERS = "ReplaceAddressingHeaders";

    // this property once set to Boolean.TRUE will make the messages to skip Addressing Handler.
    // So you will not see Addressing Headers in the OUT path.
    static final String DISABLE_ADDRESSING_FOR_OUT_MESSAGES =
            "disableAddressingForOutMessages";

    static final String ADD_MUST_UNDERSTAND_TO_ADDRESSING_HEADERS =
            "addMustUnderstandToAddressingHeaders";

    static final String SOAP_ROLE_FOR_ADDRESSING_HEADERS = "wsaddressingRole";
    
    /**
     * A property pointing to an ArrayList of OMAttribute objects representing any attributes
     * of the wsa:Action header.
     */
    static final String ACTION_ATTRIBUTES = "actionAttributes";
    /**
     * A property pointing to an ArrayList of OMAttribute objects representing any attributes
     * of the wsa:MessageID header.
     */
    static final String MESSAGEID_ATTRIBUTES = "messageidAttributes";

    /**
     * When set to Boolean.TRUE this will cause the addressing out handler to output all
     * populated addressing headers in a message, including any optional ones.
     */
    static final String INCLUDE_OPTIONAL_HEADERS = "includeOptionalHeaders";

    /**
     * This property, if set to Boolean.TRUE, will mean that the addressing handler allows partially
     * ws-addressed messages to be sent even if they are then invalid rather than throwing a fault.
     * <p/>
     * It is not clear how necessary this property is and it may be removed before the next release if
     * it is not seen to be necessary - davidillsley@apache.org
     */
    static final String DISABLE_OUTBOUND_ADDRESSING_VALIDATION =
            "disableAddressingOutboundValidation";

    /**
     * This parameter is used to attach OMElement ReferenceParameters found in an EPR embedded in a
     * WSDL to an AxisEndpoint object.
     */
    static final String REFERENCE_PARAMETER_PARAMETER = "referenceParameters";

    /**
     * This parameter is used to decide whether the reference parameters in an inbound request
     * message are to be processed or not.
     */
    static final String DISABLE_REF_PARAMETER_EXTRACT = "disableRefParamExtract";
    
    static final String WSAM_INVOCATION_PATTERN_PARAMETER_NAME = "wsamInvocationPattern";
    static final String WSAM_INVOCATION_PATTERN_SYNCHRONOUS = "synchronous";
    static final String WSAM_INVOCATION_PATTERN_ASYNCHRONOUS = "asynchronous";
    static final String WSAM_INVOCATION_PATTERN_BOTH = "both";

    // ======================== Common Faults ==============================
    static final String FAULT_ACTION_NOT_SUPPORTED = "ActionNotSupported";
    static final String FAULT_ACTION_NOT_SUPPORTED_REASON =
            "The [action] cannot be processed at the receiver.";
    static final String FAULT_ADDRESSING_DESTINATION_UNREACHABLE =
            "DestinationUnreachable";
    
    // ==================== WS-AddressingAndIdentity ========================
    
    static final String ADDRESSING_IDENTITY_PREFIX = "wsid";
    static final String ADDRESSING_IDENTITY_NS = 
        "http://schemas.xmlsoap.org/ws/2006/02/addressingidentity";
    static final String XML_SIG_PREFIX = "ds";
    static final String XML_SIG_NS = "http://www.w3.org/2000/09/xmldsig#";
    
    static final String IDENTITY = "Identity";
    static final String IDENTITY_KEY_INFO = "KeyInfo";
    static final String IDENTITY_X509_DATA = "X509Data";
    static final String IDENTITY_X509_CERT = "X509Certificate";
    
    static final String IDENTITY_PARAMETER = "WSAddressingAndIdentity";
    
    static final QName QNAME_IDENTITY = new QName(ADDRESSING_IDENTITY_NS,IDENTITY,ADDRESSING_IDENTITY_PREFIX);
    static final QName QNAME_IDENTITY_KEY_INFO = new QName(XML_SIG_NS,IDENTITY_KEY_INFO,XML_SIG_PREFIX);
    static final QName QNAME_IDENTITY_X509_DATA = new QName(XML_SIG_NS,IDENTITY_X509_DATA,XML_SIG_PREFIX);
    static final QName QNAME_IDENTITY_X509_CERT = new QName(XML_SIG_NS,IDENTITY_X509_CERT,XML_SIG_PREFIX);
    
    interface Final {

        // ====================== Addressing 1.0 Final Version Constants ====================
        static final String WSA_NAMESPACE =
                "http://www.w3.org/2005/08/addressing";
        static final String WSAW_NAMESPACE =
            "http://www.w3.org/2006/05/addressing/wsdl";
        static final String WSAM_NAMESPACE = 
        	"http://www.w3.org/2007/05/addressing/metadata";
        /**
         * @deprecated use {@link #WSA_DEFAULT_RELATIONSHIP_TYPE} instead.
         */
        static final String WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE =
                "http://www.w3.org/2005/08/addressing/reply";
        static final String WSA_IS_REFERENCE_PARAMETER_ATTRIBUTE = "IsReferenceParameter";
        static final String WSA_ANONYMOUS_URL =
                "http://www.w3.org/2005/08/addressing/anonymous";
        static final String WSA_NONE_URI =
                "http://www.w3.org/2005/08/addressing/none";
        static final String WSA_FAULT_ACTION =
                "http://www.w3.org/2005/08/addressing/fault";
        static final String WSA_SOAP_FAULT_ACTION =
                "http://www.w3.org/2005/08/addressing/soap/fault";
        static final String WSA_TYPE_ATTRIBUTE_VALUE = "true";
        static final String WSA_SERVICE_NAME_ENDPOINT_NAME = "EndpointName";
        static final String WSA_POLICIES = "Policies";
        static final String WSA_METADATA = "Metadata";
        static final String WSA_DEFAULT_METADATA_PREFIX = "wsam";
        static final String WSA_ORIGINAL_METADATA_PREFIX = "wsaw";

        static final String WSA_INTERFACE_NAME = "InterfaceName";

        static final String WSA_DEFAULT_RELATIONSHIP_TYPE =
                "http://www.w3.org/2005/08/addressing/reply";

        // fault information
        static final String FAULT_HEADER_PROB_HEADER_QNAME = "ProblemHeaderQName";
        static final String FAULT_HEADER_PROB_HEADER = "ProblemHeader";
        static final String FAULT_HEADER_PROB_IRI = "ProblemIRI";
        static final String FAULT_HEADER_DETAIL = "FaultDetail";
        static final String FAULT_INVALID_HEADER = "InvalidAddressingHeader";
        static final String FAULT_INVALID_HEADER_REASON =
                "A header representing a Message Addressing Property is not valid and the message cannot be processed";
        static final String FAULT_ADDRESSING_HEADER_REQUIRED =
                "MessageAddressingHeaderRequired";
        static final String FAULT_ADDRESSING_HEADER_REQUIRED_REASON =
                "A required header representing a Message Addressing Property is not present";
        static final String FAULT_ADDRESSING_DESTINATION_UNREACHABLE_REASON =
                "No route can be determined to reach [destination]";
        static final String FAULT_INVALID_CARDINALITY = "InvalidCardinality";
        static final String FAULT_ONLY_ANONYMOUS_ADDRESS_SUPPORTED =
                "OnlyAnonymousAddressSupported";
        static final String FAULT_ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED =
                "OnlyNonAnonymousAddressSupported";
        static final String FAULT_PROBLEM_ACTION_NAME = "ProblemAction";

        static final QName WSAW_USING_ADDRESSING =
                new QName(WSAW_NAMESPACE, USING_ADDRESSING);
        static final QName WSAW_ANONYMOUS = new QName(WSAW_NAMESPACE, USING_ADDRESSING);
        static final QName WSA_ENDPOINT_REFERENCE = new QName(WSA_NAMESPACE, ENDPOINT_REFERENCE, WSA_DEFAULT_PREFIX);
        static final QName WSA_ADDRESS = new QName(WSA_NAMESPACE, EPR_ADDRESS,WSA_DEFAULT_PREFIX);

        final QName QNAME_WSA_TO = new QName(WSA_NAMESPACE, WSA_TO);
        final QName QNAME_WSA_FROM = new QName(WSA_NAMESPACE, WSA_FROM);
        final QName QNAME_WSA_REPLY_TO = new QName(WSA_NAMESPACE, WSA_REPLY_TO);
        final QName QNAME_WSA_RELATES_TO = new QName(WSA_NAMESPACE, WSA_RELATES_TO);
        final QName QNAME_WSA_MESSAGE_ID = new QName(WSA_NAMESPACE, WSA_MESSAGE_ID);
        final QName QNAME_WSA_HEADER_DETAIL = new QName(WSA_NAMESPACE, FAULT_HEADER_DETAIL);
        final QName QNAME_PROBLEM_HEADER = new QName(WSA_NAMESPACE, FAULT_HEADER_PROB_HEADER_QNAME);
        final QName QNAME_INVALID_HEADER = new QName(WSA_NAMESPACE, FAULT_INVALID_HEADER);
    }


    interface Submission {

        // ====================== Addressing Submission Version Constants ===================
        static final String WSA_NAMESPACE =
                "http://schemas.xmlsoap.org/ws/2004/08/addressing";
        /**
         * @deprecated use {@link #WSA_DEFAULT_RELATIONSHIP_TYPE} instead.
         */
        static final String WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE = "wsa:Reply";
        static final String WSA_DEFAULT_RELATIONSHIP_TYPE = "wsa:Reply";
        static final String WSA_ANONYMOUS_URL =
                "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous";

        static final String EPR_REFERENCE_PROPERTIES = "ReferenceProperties";
        static final String WSA_FAULT_ACTION =
                "http://schemas.xmlsoap.org/ws/2004/08/addressing/fault";
        static final String WSA_SERVICE_NAME_ENDPOINT_NAME = "PortName";
        static final String WSA_INTERFACE_NAME = "PortType";

        // fault information
        static final String FAULT_INVALID_HEADER = "InvalidMessageInformationHeader";
        static final String FAULT_INVALID_HEADER_REASON =
                "A message information header is not valid and the message cannot be processed. The validity failure can be either structural or semantic, e.g. a [destination] that is not a URI or a [relationship] to a [message id] that was never issued.";
        static final String FAULT_ADDRESSING_HEADER_REQUIRED =
                "MessageInformationHeaderRequired";
        static final String FAULT_ADDRESSING_HEADER_REQUIRED_REASON =
                "A required message information header, To, MessageID, or Action, is not present.";
        static final String FAULT_ADDRESSING_DESTINATION_UNREACHABLE_REASON =
                "No route can be determined to reach the destination role defined by the WS-Addressing To.";

        static final QName WSAW_USING_ADDRESSING =
                new QName(WSA_NAMESPACE, USING_ADDRESSING);

        final QName QNAME_WSA_TO = new QName(WSA_NAMESPACE, WSA_TO);
        final QName QNAME_WSA_FROM = new QName(WSA_NAMESPACE, WSA_FROM);
        final QName QNAME_WSA_REPLY_TO = new QName(WSA_NAMESPACE, WSA_REPLY_TO);
        final QName QNAME_WSA_RELATES_TO = new QName(WSA_NAMESPACE, WSA_RELATES_TO);
        final QName QNAME_WSA_MESSAGE_ID = new QName(WSA_NAMESPACE, WSA_MESSAGE_ID);
    }
}
