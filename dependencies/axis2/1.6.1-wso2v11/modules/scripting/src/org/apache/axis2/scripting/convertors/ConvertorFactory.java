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

package org.apache.axis2.scripting.convertors;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.scripting.ScriptReceiver;

public class ConvertorFactory {

    /**
     * Creates an OMElementConvertor for the script.
     * 
     * The OMElementConvertor impl class name is either:
     * - the convertor class name attribute from the script element in the sevices.xml
     * - the value of the parameter name XXOMElementConvertor in the axis2.xml
     *   where XX is the script language suffix upper cased, eg. JS or RB
     * - org.apache.axis2.receivers.scripting.convertors.XXOMElementConvertor where
     *   where XX is the script language suffix upper cased, eg. JS or RB
     * - org.apache.axis2.receivers.scripting.convertors.DefaultOMElementConvertor
     */
    public static OMElementConvertor createOMElementConvertor(AxisService axisService, String scriptName) throws AxisFault {
        OMElementConvertor oc = getScriptConvertor(axisService);
        if (oc == null) {
            oc = getAxis2Convertor(axisService, scriptName);
        } 
        if (oc == null) {
            try {
                oc = getDefaultScriptConvertor(axisService, scriptName);
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }
        } 
        if (oc == null) {
            oc = new DefaultOMElementConvertor();
        } 
        return oc;
    }

    /**
     * 
     */
    protected static OMElementConvertor getScriptConvertor(AxisService axisService) {
        OMElementConvertor oc = null;
        Parameter convertorParam = axisService.getParameter(ScriptReceiver.CONVERTOR_ATTR);
        if (convertorParam != null) {
            String convertorClassName = (String) convertorParam.getValue(); 
            try {
                oc = (OMElementConvertor) Class.forName(convertorClassName, true, axisService.getClassLoader()).newInstance();
            } catch (Exception e) {
                // ignore
            }
        }
        return oc;
    }

    protected static OMElementConvertor getAxis2Convertor(AxisService axisService, String scriptName) {
        OMElementConvertor oc = null;
        int lastDot = scriptName.lastIndexOf('.');
        String suffix = scriptName.substring(lastDot + 1).toUpperCase();
        String className = OMElementConvertor.class.getName();
        int i = className.lastIndexOf('.');
        String convertorClassNameProp = suffix.toUpperCase() + className.substring(i + 1);
        AxisConfiguration axisConfig = axisService.getAxisConfiguration();
        Parameter convertorParam = axisConfig.getParameter(convertorClassNameProp);
        if (convertorParam != null) {
            String convertorClassName = (String) convertorParam.getValue();
            try {
                oc = (OMElementConvertor) Class.forName(convertorClassName, true, axisService.getClassLoader()).newInstance();
            } catch (Exception e) {
                // ignore
            }
        }
        return oc;
    }

    protected static OMElementConvertor getDefaultScriptConvertor(AxisService axisService, String scriptName) throws InstantiationException, IllegalAccessException {
        OMElementConvertor oc = null;
        int lastDot = scriptName.lastIndexOf('.');
        String suffix = scriptName.substring(lastDot + 1).toUpperCase();
        String className = OMElementConvertor.class.getName();
        int i = className.lastIndexOf('.');
        String convertorClassName = suffix.toUpperCase() + className.substring(i + 1);
        AxisConfiguration axisConfig = axisService.getAxisConfiguration();
        Parameter convertorParam = axisConfig.getParameter(convertorClassName);
        if (convertorParam != null) {
            try {
                oc = (OMElementConvertor) Class.forName((String) convertorParam.getValue(), true, axisService.getClassLoader()).newInstance();
            } catch (ClassNotFoundException e) {
                // ignore
            }
        } else {
            String fqConvertorClassName = className.substring(0, i + 1) + convertorClassName;
            try {
                oc = (OMElementConvertor) Class.forName(fqConvertorClassName, true, axisService.getClassLoader()).newInstance();
            } catch (ClassNotFoundException e) {
                // ignore
            }
            if (oc == null) {
                try {
                    oc = (OMElementConvertor) Class.forName(fqConvertorClassName, true, ConvertorFactory.class.getClassLoader()).newInstance();
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
        return oc;
    }

}
