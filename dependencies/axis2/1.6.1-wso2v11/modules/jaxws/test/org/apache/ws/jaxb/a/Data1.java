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
package org.apache.ws.jaxb.a;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Good JAXB element
 * Data1 contains JAXB annotations.
 * References Data2
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "data1", namespace="urn://jaxb.a", propOrder = {
    "text", "data2"
})

public class Data1 {

    @XmlElement(required = true, nillable = true)
    protected String text;
    
    @XmlElement(required = true, nillable = true)
    protected Data2 data2;

    public Data2 getData2() {
        return data2;
    }

    public void setData2(Data2 data2) {
        this.data2 = data2;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    
}
