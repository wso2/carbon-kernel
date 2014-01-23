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
package org.apache.axis2.jaxws.message;

import java.lang.reflect.Array;
import java.util.List;

import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used by the marshaling engine to wrap an array or List
 * to indicate that the components should be marshaled
 * as a series of elements (occurrence elements).
 * 
 * The DocLitMinimalMethodMarshaller will create
 * a JAXBElement whose name is the name of the elements
 * and whose value is a OccurrenceArray that 
 * holds a List or array
 * 
 * @See DocLitWrappedMinimalMethodMarshaler
 * @See JAXBDSContext
 */
public class OccurrenceArray {
    
    private static Log log = LogFactory.getLog(OccurrenceArray.class);
    
    // The held value will be a List or Array
    Object value = null;
    public OccurrenceArray(Object value) {
        if (log.isDebugEnabled()) {
            log.debug("Creating OccurrenceArray for " + JavaUtils.getObjectIdentity(value));
        }
        this.value = value;
    }
    
    /**
     * Get the List or array as a Object[]
     * @return Object[] 
     */
    public Object[] getAsArray() {
        Object[] objects = null;
        if (value == null) {
            return new Object[0];
        } else if (value instanceof List) {
            List l = (List) value;
            objects = new Object[l.size()];
            for (int i=0; i<l.size(); i++) {
                objects[i] = l.get(i);
            }
        } else {
            objects = new Object[Array.getLength(value)];
            for (int i=0; i<objects.length; i++) {
                objects[i] = Array.get(value, i);
            }
        }
        return objects;
    }
}
