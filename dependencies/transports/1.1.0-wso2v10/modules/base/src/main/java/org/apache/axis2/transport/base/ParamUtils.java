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
package org.apache.axis2.transport.base;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.util.JavaUtils;

/**
 * Utility class with methods to manipulate service or transport parameters.
 */
public class ParamUtils {
    private ParamUtils() {}

    public static String getRequiredParam(ParameterInclude paramInclude, String paramName) throws AxisFault {
        Parameter param = paramInclude.getParameter(paramName);
        if (param != null && param.getValue() != null && param.getValue() instanceof String) {
            return (String) param.getValue();
        } else {
            throw new AxisFault("Cannot find parameter '" + paramName + "' for "
                    + getDescriptionFor(paramInclude));
        }
    }

    public static String getOptionalParam(ParameterInclude paramInclude, String paramName) throws AxisFault {
        Parameter param = paramInclude.getParameter(paramName);
        if (param != null && param.getValue() != null && param.getValue() instanceof String) {
            return (String) param.getValue();
        } else {
            return null;
        }
    }

    public static Integer getOptionalParamInt(ParameterInclude paramInclude, String paramName) throws AxisFault {
        Parameter param = paramInclude.getParameter(paramName);
        if (param == null || param.getValue() == null) {
            return null;
        } else {
            Object paramValue = param.getValue();
            if (paramValue instanceof Integer) {
                return (Integer)paramValue;
            } else if (paramValue instanceof String) {
                try {
                    return Integer.valueOf((String)paramValue);
                } catch (NumberFormatException ex) {
                    throw new AxisFault("Invalid value '" + paramValue + "' for parameter '" + paramName +
                            "' for " + getDescriptionFor(paramInclude));
                }
            } else {
                throw new AxisFault("Invalid type for parameter '" + paramName + "' for " +
                        getDescriptionFor(paramInclude));
            }
        }
    }

    public static int getOptionalParamInt(ParameterInclude paramInclude, String paramName, int defaultValue) throws AxisFault {
        Integer value = getOptionalParamInt(paramInclude, paramName);
        return value == null ? defaultValue : value.intValue();
    }

    public static boolean getOptionalParamBoolean(ParameterInclude paramInclude, String paramName, boolean defaultValue) throws AxisFault {
        Parameter param = paramInclude.getParameter(paramName);
        return param == null ? defaultValue : JavaUtils.isTrueExplicitly(param.getValue(), defaultValue);
    }

    public static int getRequiredParamInt(ParameterInclude paramInclude, String paramName) throws AxisFault {
        Integer value = getOptionalParamInt(paramInclude, paramName);
        if (value == null) {
            throw new AxisFault("Cannot find parameter '" + paramName +
                    "' for " + getDescriptionFor(paramInclude));
        } else {
            return value.intValue();
        }
    }
    
    private static String getDescriptionFor(ParameterInclude paramInclude) {
        if (paramInclude instanceof AxisService) {
            return "service '" + ((AxisService)paramInclude).getName() + "'";
        } else if (paramInclude instanceof TransportInDescription) {
            return "transport receiver '" + ((TransportInDescription)paramInclude).getName() + "'";
        } else if (paramInclude instanceof TransportOutDescription) {
            return "transport sender '" + ((TransportOutDescription)paramInclude).getName() + "'";
        } else {
            return paramInclude.getClass().getName();
        }
    }
}
