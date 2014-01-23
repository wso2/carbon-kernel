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

import org.apache.axis2.databinding.types.Duration;
import org.apache.axis2.databinding.utils.ConverterUtil;

public class SimpleTypeDurationPopulateTest extends AbstractSimplePopulater{
    private String values[]= {
            "P1Y2MT2H",
            "P1347M",
            "P0Y1347M0D"
    };

    private String xmlString[] = {
            "<durationParam xmlns=\"http://soapinterop.org/xsd\">"+values[0]+"</durationParam>",
            "<durationParam xmlns=\"http://soapinterop.org/xsd\">"+values[1]+"</durationParam>",
            "<durationParam xmlns=\"http://soapinterop.org/xsd\">"+values[2]+"</durationParam>"
    };

    protected void setUp() throws Exception {
        className = "org.soapinterop.xsd.DurationParam";
        propertyClass = Duration.class;
    }

    // force others to implement this method
    public void testPopulate() throws Exception {
        for (int i = 0; i < values.length; i++) {
            checkValue(xmlString[i],values[i]);
        }
    }

    protected void compare(String val1, String val2) {
        assertTrue(new  Duration(val1).equals(new Duration(val2)));
    }

    protected String convertToString(Object o) {
        return ConverterUtil.convertToString((Duration)o);
    }
}
