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

package org.apache.axis2.description;

import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.wsdl.WSDLConstants;

import java.util.HashMap;
import java.util.Map;

public class WSDL20DefaultValueHolder {

    private static Map defaultValuesMap = new HashMap();

    public static final String WHTTP_METHOD_WSDLX_SAFE = "whttp:methodWSDLsafe";
    public static final String WHTTP_METHOD_WSDLX_NOTSAFE = "whttp:methodWSDLNotsafe";
    public static final String ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR_DEFAULT = "&";


    static {
        defaultValuesMap.put(WSDL2Constants.ATTR_WSOAP_VERSION,
                             SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        defaultValuesMap.put(WSDL2Constants.ATTR_WSOAP_ACTION, "\\\"\\\"");
        defaultValuesMap.put(WHTTP_METHOD_WSDLX_SAFE,
                             org.apache.axis2.Constants.Configuration.HTTP_METHOD_GET);
        defaultValuesMap.put(WHTTP_METHOD_WSDLX_SAFE,
                             org.apache.axis2.Constants.Configuration.HTTP_METHOD_POST);
        defaultValuesMap.put(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
                             ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR_DEFAULT);
        defaultValuesMap.put(WSDLConstants.WSDL_1_1_STYLE, WSDLConstants.STYLE_DOC);
        defaultValuesMap.put(WSDL2Constants.ATTR_WSOAP_MEP,WSDL2Constants.MEP_URI_IN_OUT);
    }

    public static String getDefaultValue(String name) {
        return (String) defaultValuesMap.get(name);
    }
}
