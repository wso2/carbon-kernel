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
package org.apache.axis2.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.description.AxisService;

public final class JSONUtil {
    private JSONUtil() {}
    
    public static Map<String,String> getNS2JNSMap(AxisService service) {
        Map<String,String> ns2jnsMap = new HashMap<String,String>();
        Object value = service.getParameterValue("xmlToJsonNamespaceMap");
        if (value != null) {
            if (value instanceof OMElement && ((OMElement)value).getLocalName().equals("mappings")) {
                for (Iterator it = ((OMElement)value).getChildrenWithName(new QName("mapping")); it.hasNext(); ) {
                    OMElement mapping = (OMElement)it.next();
                    ns2jnsMap.put(mapping.getAttributeValue(new QName("xmlNamespace")),
                                  mapping.getAttributeValue(new QName("jsonNamespace")));
                }
            } else {
                throw new IllegalArgumentException("Expected <mappings> element");
            }
        } else {
            // If no namespace map is defined, use a default map compatible with earlier Axis2 versions
            ns2jnsMap.put("", "");
        }
        return ns2jnsMap;
    }
}
