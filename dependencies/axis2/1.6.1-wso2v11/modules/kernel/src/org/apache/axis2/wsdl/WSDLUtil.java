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

import org.apache.axis2.description.WSDL2Constants;

import javax.xml.namespace.QName;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some utility methods for the WSDL users
 */
public class WSDLUtil {

    /**
     * returns whether the given mep uri is one of the
     * input meps
     *
     * @param mep
     */
    public static boolean isInputPresentForMEP(String mep) {
        return WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OPTIONAL_OUT.equals(mep) ||
                WSDL2Constants.MEP_URI_IN_ONLY.equals(mep) ||
                WSDL2Constants.MEP_URI_IN_OUT.equals(mep) ||
                WSDL2Constants.MEP_URI_OUT_IN.equals(mep) ||
                WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(mep) ||
                WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(mep);
    }

    /**
     * returns whether the given mep URI is one of the output meps
     *
     * @param MEP
     */
    public static boolean isOutputPresentForMEP(String MEP) {
        return WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                WSDL2Constants.MEP_URI_IN_OUT.equals(MEP) ||
                WSDL2Constants.MEP_URI_OUT_IN.equals(MEP) ||
                WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP) ||
                WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP);
    }

    /**
     * part names are not unique across messages. Hence
     * we need some way of making the part name a unique
     * one (due to the fact that the type mapper
     * is a global list of types).
     * The seemingly best way to do that is to
     * specify a namespace for the part QName reference which
     * is stored in the  list. This part qname is
     * temporary and should not be used with it's
     * namespace URI (which happened to be the operation name)
     * with _input (or a similar suffix) attached to it
     *
     * @param opName
     * @param suffix
     * @param partName
     */
    public static QName getPartQName(String opName,
                                     String suffix,
                                     String partName) {
        return new QName(opName + suffix, partName);
    }

    public static String getConstantFromHTTPLocation(String httpLocation, String httpMethod) {
        if (httpLocation.charAt(0) != '?') {
            httpLocation = "/" + httpLocation;
        }
        int index = httpLocation.indexOf("{");
        if (index > -1) {
            httpLocation = httpLocation.substring(0, index);
        }
        return httpMethod + httpLocation;
    }

    public static Pattern getConstantFromHTTPLocationForResource(String httpLocation, String httpMethod) {
        httpLocation = getRegexForLocation(httpLocation);
        String location = httpMethod + httpLocation;
        return Pattern.compile(location);
    }

    private static String getRegexForLocation(String httpLocation) {
        StringTokenizer tokenizer = new StringTokenizer(httpLocation, "/");
        StringBuilder regex = new StringBuilder("");
        while (tokenizer.hasMoreElements()) {
            String param = tokenizer.nextElement().toString();
            if (param.startsWith("{")) {
                regex = regex.append("/.*");
            } else {
                regex = regex.append("/").append(param);
            }
        }
        return regex.toString();
    }

}
