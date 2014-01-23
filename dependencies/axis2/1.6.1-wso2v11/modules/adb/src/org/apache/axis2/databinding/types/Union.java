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

package org.apache.axis2.databinding.types;


import org.apache.axis2.databinding.utils.ConverterUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.Serializable;
import java.math.BigDecimal;


/** this class is the super class of all the union simple types */
public abstract class Union implements Serializable {
    // object to store values
    protected Object localObject;

    public Object getObject() {
        return localObject;
    }

    public abstract void setObject(Object localObject);

    public String toString() {
        return this.localObject.toString();
    }

    /**
     * method to parse xmlschema objects
     *
     * @param xmlStreamReader
     * @param namespaceURI
     * @param type
     * @throws URI.MalformedURIException
     * @throws XMLStreamException
     */

    public void setObject(XMLStreamReader xmlStreamReader,
                          String namespaceURI,
                          String type) throws URI.MalformedURIException, XMLStreamException {
        String value = xmlStreamReader.getElementText();
        if ("string".equals(type)) {
            setObject(value);
        } else if ("int".equals(type) || "integer".equals(type)) {
            setObject(new Integer(value));
        } else if ("boolean".equals(type)) {
            setObject(new Boolean(value));
        } else if ("anyURI".equals(type)) {
            setObject(new URI(value));
        } else if ("date".equals(type)) {
            setObject(ConverterUtil.convertToDate(value));
        } else if ("QName".equals(type)) {
            if (value.indexOf(":") > 0) {
                // i.e it has a name space
                String prefix = value.substring(0, value.indexOf(":"));
                String localPart = value.substring(value.indexOf(":") + 1);
                String namespace = xmlStreamReader.getNamespaceURI(prefix);
                setObject(new QName(namespace, localPart, prefix));
            } else {
                setObject(new QName(value));
            }
        } else if ("dateTime".equals(type)) {
            setObject(ConverterUtil.convertToDateTime(value));
        } else if ("time".equals(type)) {
            setObject(ConverterUtil.convertToTime(value));
        } else if ("float".equals(type)) {
            setObject(new Float(value));
        } else if ("long".equals(type)) {
            setObject(new Long(value));
        } else if ("double".equals(type)) {
            setObject(new Double(value));
        } else if ("decimal".equals(type)) {
            setObject(new BigDecimal(value));
        } else if ("unsignedLong".equals(type)) {
            setObject(new UnsignedLong(value));
        } else if ("unsignedInt".equals(type)) {
            setObject(new UnsignedInt(value));
        } else if ("unsignedShort".equals(type)) {
            setObject(new UnsignedShort(value));
        } else if ("unsignedByte".equals(type)) {
            setObject(new UnsignedByte(value));
        } else if ("positiveInteger".equals(type)) {
            setObject(new PositiveInteger(value));
        } else if ("negativeInteger".equals(type)) {
            setObject(new NegativeInteger(value));
        } else if ("nonNegativeInteger".equals(type)) {
            setObject(new NonNegativeInteger(value));
        } else if ("nonPositiveInteger".equals(type)) {
            setObject(new NonPositiveInteger(value));
        } else {
            throw new RuntimeException("Object not found");
        }
    }

}
