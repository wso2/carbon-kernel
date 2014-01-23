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

package org.apache.axis2.dataretrieval;

/**
 * Defines constants referenced in data retrieval classes.
 */
public interface DRConstants {
    public interface SPEC_VERSIONS {
        String v1_0 = "Spec_2004_09";
    }


    public interface SOAPVersion {
        int v1_1 = 1;

        int v1_2 = 2;

    }

    /**
     * Defines contants references in WS-Mex specification
     */

    public interface SPEC {
        String NS_URI = "http://schemas.xmlsoap.org/ws/2004/09/mex";

        public interface Actions {
            String GET_METADATA_REQUEST =
                    "http://schemas.xmlsoap.org/ws/2004/09/mex/GetMetadata/Request";
            String GET_METADATA_RESPONSE =
                    "http://schemas.xmlsoap.org/ws/2004/09/mex/GetMetadata/Response";

        }

        String NS_PREFIX = "mex";
        String GET_METADATA = "GetMetadata";

        /** @deprecated Please use DIALECT instead.  todo: delete me after 1.5 */
        String DIALET = "Dialect";
        
        String DIALECT = "Dialect";
        String IDENTIFIER = "Identifier";
        String METADATA = "Metadata";
        String METADATA_SECTION = "MetadataSection";
        String METADATA_REFERENCE = "MetadataReference";
        String LOCATION = "Location";
        String TYPE = "type";
        String DIALECT_TYPE_WSDL = "http://schemas.xmlsoap.org/wsdl/";
        String DIALECT_TYPE_POLICY = "http://schemas.xmlsoap.org/ws/2004/09/policy";
        String DIALECT_TYPE_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    }


    // Following constants used for configuring Data Locator in axis.xml and services.xml
    String DATA_LOCATOR_ELEMENT = "dataLocator";
    String DIALECT_LOCATOR_ELEMENT = "dialectLocator";
    String DIALECT_ATTRIBUTE = "dialect";
    String CLASS_ATTRIBUTE = "class";

    // Service level and Global level type Data Locator
    String SERVICE_LEVEL = "ServiceLevel";
    String GLOBAL_LEVEL = "GlobalLevel";

    /**
     * Defines contants references in Service Data
     */

    public interface SERVICE_DATA {
        String FILE_NAME = "ServiceData.xml";
        String FILE_TYPE = "svcData";

        String DATA = "Data";
        String ENDPOINT_REFERENCE = "EndpointReference";
        String URL = "URL";
        String FILE = "file";
        String DIALECT = "dialect";
        String IDENTIFIER = "identifier";

    }

}
