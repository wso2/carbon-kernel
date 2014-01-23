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

package org.apache.axis2.schema.populate.simple;

import org.apache.axis2.databinding.utils.ConverterUtil;

import java.util.Date;

public class SimpleTypeDatePopulateTest extends AbstractSimplePopulater{
    private String values[]={
                "2002-10-10Z",
                "2000-12-31+05:30",
                "2002-02-28Z"
    } ;
    private String xmlString[] = {
            "<dateParam xmlns=\"http://soapinterop.org/xsd\">"+
                    ConverterUtil.convertToString(ConverterUtil.convertToDate(values[0])) +"</dateParam>",
            "<dateParam xmlns=\"http://soapinterop.org/xsd\">"+
                    ConverterUtil.convertToString(ConverterUtil.convertToDate(values[1]))+"</dateParam>",
            "<dateParam xmlns=\"http://soapinterop.org/xsd\">"+
                    ConverterUtil.convertToString(ConverterUtil.convertToDate(values[2]))+"</dateParam>"
    };

    protected void setUp() throws Exception {
        className = "org.soapinterop.xsd.DateParam";
        propertyClass = java.util.Date.class;
    }

    // force others to implement this method
    public void testPopulate() throws Exception {

        Date date = null;

        for (int i = 0; i < values.length; i++) {
            date = ConverterUtil.convertToDate(values[i]);
            checkValue(xmlString[i],ConverterUtil.convertToString(date));
        }
    }

    protected String convertToString(Object o) {
        String s = ConverterUtil.convertToString((Date) o);
        return s;
    }
}
