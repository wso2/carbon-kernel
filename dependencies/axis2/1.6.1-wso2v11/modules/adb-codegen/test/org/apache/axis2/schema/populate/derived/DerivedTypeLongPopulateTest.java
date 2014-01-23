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

package org.apache.axis2.schema.populate.derived;

import org.apache.axis2.databinding.utils.ConverterUtil;

public class DerivedTypeLongPopulateTest extends AbstractDerivedPopulater{

    private String values[]= {
            "17578283282",
            "-1228284443",
            "2",
            Long.MAX_VALUE + "",
            Long.MIN_VALUE + ""
    };

    private String xmlString[] = {
            "<DerivedLong xmlns=\"http://soapinterop.org/xsd\">"+values[0]+"</DerivedLong>",
            "<DerivedLong xmlns=\"http://soapinterop.org/xsd\">"+values[1]+"</DerivedLong>",
            "<DerivedLong xmlns=\"http://soapinterop.org/xsd\">"+values[2]+"</DerivedLong>",
            "<DerivedLong xmlns=\"http://soapinterop.org/xsd\">"+values[3]+"</DerivedLong>",
            "<DerivedLong xmlns=\"http://soapinterop.org/xsd\">"+values[4]+"</DerivedLong>"
    };




    protected void setUp() throws Exception {
        className = "org.soapinterop.xsd.DerivedLong";
        propertyClass = long.class;
    }

    // force others to implement this method
    public void testPopulate() throws Exception {
        for (int i = 0; i < 2; i++) {
            checkValue(xmlString[i],values[i]);
        }
    }

    protected String convertToString(Object o) {
        return ConverterUtil.convertToString((Long)o);
    }
}
