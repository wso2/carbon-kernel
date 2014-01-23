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

package org.apache.axis2.mex;

/**
 * Contains all the MetadataExchange constants for WS-Mex.
 * 
 */

public interface MexConstants {
	public interface SPEC_VERSIONS {
		public static final String v1_0 = "Spec_2004_09";
	}
    public interface Spec_2004_09 {
		
    	public static final String NS_URI = "http://schemas.xmlsoap.org/ws/2004/09/mex";

	    public interface Actions {
	    	public static final String GET_METADATA_REQUEST = "http://schemas.xmlsoap.org/ws/2004/09/mex/GetMetadata/Request";
	    	public static final String GET_METADATA_RESPONSE = "http://schemas.xmlsoap.org/ws/2004/09/mex/GetMetadata/Response";
			
	    }
    }
    
  
    public interface SOAPVersion {
    	public static final int v1_1 = 1;

    	public static final int v1_2 = 2;
    
	}
    
    
    public interface SPEC {
    	public static final String NS_PREFIX = "mex";
    	public static final String GET_METADATA = "GetMetadata";
    	public static final String DIALECT = "Dialect";
    	public static final String IDENTIFIER = "Identifier";
    	public static final String METADATA = "Metadata";
    	public static final String METADATA_SECTION = "MetadataSection";
    	public static final String METADATA_REFERENCE = "MetadataReference";
    	public static final String LOCATION = "Location";
    	public static final String TYPE = "type";
		
    	public static final String DIALECT_TYPE_WSDL = "http://schemas.xmlsoap.org/wsdl/";
    	public static final String DIALECT_TYPE_POLICY = "http://schemas.xmlsoap.org/ws/2004/09/policy";
    	public static final String DIALECT_TYPE_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    	public static final String DIALECT_TYPE_MEX = "http://schemas.xmlsoap.org/ws/2004/09/mex";
		
	}
    

    /**
     * MEX Configuration 
     * 
     * Sample mex configuration:
     * <parameter name="metadataexchange">
     *     <outputform dialect="http://schemas.xmlsoap.org/wsdl/" forms="location" />
     *     <outputform forms="inline,reference,location" />
     * </parameter> 
     */
    public interface MEX_CONFIG{
    	public static final String MEX_PARM = "metadataExchange";
    	
    	// Allow to disable MEX for a Service if MEX is engaged globally. 
    	public static final String ENABLE_MEX = "enable"; 
        
    	// One or more outputform element can be specified in the mex configuration
    	// outputform element contains an optional "dialect" attribute and an
    	// required forms attribute. 
    	public static final String OUTPUT_FORM_PARM = "outputform";
    	public static final String DIALECT_PARM = "dialect";
    	public static final String FORMS_PARM = "forms";
    	
    	// configurable parameter values
    	public static final String DELIMITER = ",";
    	// possible "forms" attribute values
    	public static final String LOCATION = "location";
    	public static final String INLINE = "inline";
    	public static final String REFERENCE = "reference";
    	
    }
  
}
