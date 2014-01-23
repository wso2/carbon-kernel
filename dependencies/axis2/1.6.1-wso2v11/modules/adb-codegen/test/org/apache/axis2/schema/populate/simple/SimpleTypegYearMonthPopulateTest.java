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

import org.apache.axis2.databinding.types.YearMonth;
import org.apache.axis2.databinding.utils.ConverterUtil;

public class SimpleTypegYearMonthPopulateTest extends AbstractSimplePopulater{
    private String values[]= {
            "0001-05",
            "-0234-09",
            "1978-01",
            "1978-01-GMT"
    };
    private String xmlString[] = {
            "<yearMonthParam xmlns=\"http://soapinterop.org/xsd\">"+values[0]+"</yearMonthParam>",
            "<yearMonthParam xmlns=\"http://soapinterop.org/xsd\">"+values[1]+"</yearMonthParam>",
            "<yearMonthParam xmlns=\"http://soapinterop.org/xsd\">"+values[2]+"</yearMonthParam>"
    };

    protected void setUp() throws Exception {
        className = "org.soapinterop.xsd.YearMonthParam";
        propertyClass = YearMonth.class;
    }

    // force others to implement this method
    public void testPopulate() throws Exception {
        for (int i = 0; i < 2; i++) {
            checkValue(xmlString[i],values[i]);
        }

        try {
            process(xmlString[3],"org.soapinterop.yearMonthParam");
            fail();
        } catch (Exception e) {

        }
    }

    protected String convertToString(Object o) {
        return ConverterUtil.convertToString((YearMonth)o);
    }

}
