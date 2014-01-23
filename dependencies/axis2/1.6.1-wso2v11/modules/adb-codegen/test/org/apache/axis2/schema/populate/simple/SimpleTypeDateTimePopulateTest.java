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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class SimpleTypeDateTimePopulateTest extends AbstractSimplePopulater{
    private String values[] ={"2002-10-10T12:00:00+05:00",
            "2000-12-31T11:59:59-05:00",
            "2002-10-10T07:00:00Z"
    };
    private String xmlString[] = {
            "<dateTimeParam xmlns=\"http://soapinterop.org/xsd\">"+
                    ConverterUtil.convertToString(ConverterUtil.convertToDateTime(values[0])) +"</dateTimeParam>",
            "<dateTimeParam xmlns=\"http://soapinterop.org/xsd\">"+
                    ConverterUtil.convertToString(ConverterUtil.convertToDateTime(values[1]))+"</dateTimeParam>",
            "<dateTimeParam xmlns=\"http://soapinterop.org/xsd\">"+
                    ConverterUtil.convertToString(ConverterUtil.convertToDateTime(values[2]))+"</dateTimeParam>"
    };
    // force others to implement this method
    public void testPopulate() throws Exception {
        // Skip for JDK1.4
        if (System.getProperty("java.version").startsWith("1.4.")) {
            return;
        }
        Calendar calendar;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        for (int i = 0; i < values.length; i++) {
            calendar = ConverterUtil.convertToDateTime(values[i]);
            checkValue(xmlString[i],ConverterUtil.convertToString(calendar));
       }
    }

    protected void setUp() throws Exception {
        className = "org.soapinterop.xsd.DateTimeParam";
        propertyClass = Calendar.class;
    }

    protected String convertToString(Object o) {
        return ConverterUtil.convertToString((Calendar) o);
    }


}
