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

package org.apache.axis2.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;

import java.util.HashMap;
import java.util.Iterator;

/**
 * 
 */
public class SchemaUtil {
    private static final Log log = LogFactory.getLog(SchemaUtil.class);


    public static XmlSchema[] getAllSchemas(XmlSchema schema) {
        HashMap map = new HashMap();
        traverseSchemas(schema, map);
        return (XmlSchema[]) map.values().toArray(new XmlSchema[map.values().size()]);
    }

    private static void traverseSchemas(XmlSchema schema, HashMap map) {
        String key = schema.getTargetNamespace() + ":" + schema.getSourceURI();
        if (map.containsKey(key)) {
            return;
        }
        map.put(key, schema);

        XmlSchemaObjectCollection includes = schema.getIncludes();
        if (includes != null) {
            Iterator tempIterator = includes.getIterator();
            while (tempIterator.hasNext()) {
                Object o = tempIterator.next();
                if (o instanceof XmlSchemaImport) {
                    XmlSchema schema1 = ((XmlSchemaImport) o).getSchema();
                    if (schema1 != null) {
                        traverseSchemas(schema1, map);
                    }
                }
                if (o instanceof XmlSchemaInclude) {
                    XmlSchema schema1 = ((XmlSchemaInclude) o).getSchema();
                    if (schema1 != null) {
                        traverseSchemas(schema1, map);
                    }
                }
            }
        }
    }
}
