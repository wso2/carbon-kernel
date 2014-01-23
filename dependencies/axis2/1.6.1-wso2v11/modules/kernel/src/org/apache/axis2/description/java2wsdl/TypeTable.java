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

package org.apache.axis2.description.java2wsdl;

import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.net.URI;

public class TypeTable {
    
    private static HashMap  simpleTypetoxsd;
    public static final QName ANY_TYPE = new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "anyType", "xs");

    private HashMap complexTypeMap;

    /**
     * this map is used to keep the class names with the Qnames.
     */
    private Map<QName, String> qNameToClassMap;

    public TypeTable() {
        //complex type table is resetted every time this is
        //instantiated
        complexTypeMap = new HashMap();
        this.qNameToClassMap = new HashMap();
    }

    /* statically populate the simple type map  - this is not likely to
    * change and we need not populate it over and over */
    static{
          populateSimpleTypes();
    }

    /* populate the simpletype hashmap */
    private static void populateSimpleTypes() {
        simpleTypetoxsd = new HashMap();
        //todo pls use the types from org.apache.ws.commons.schema.constants.Constants
        simpleTypetoxsd.put("int",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "int", "xs"));
        simpleTypetoxsd.put("java.lang.String",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "string", "xs"));
        simpleTypetoxsd.put("boolean",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "boolean", "xs"));
        simpleTypetoxsd.put("float",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "float", "xs"));
        simpleTypetoxsd.put("double",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "double", "xs"));
        simpleTypetoxsd.put("short",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "short", "xs"));
        simpleTypetoxsd.put("long",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "long", "xs"));
        simpleTypetoxsd.put("byte",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "byte", "xs"));
        simpleTypetoxsd.put("char",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "string", "xs"));
        simpleTypetoxsd.put("java.lang.Integer",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "int", "xs"));
        simpleTypetoxsd.put("java.lang.Double",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "double", "xs"));
        simpleTypetoxsd.put("java.lang.Float",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "float", "xs"));
        simpleTypetoxsd.put("java.lang.Long",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "long", "xs"));
        simpleTypetoxsd.put("java.lang.Character",
                ANY_TYPE);
        simpleTypetoxsd.put("java.lang.Boolean",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "boolean", "xs"));
        simpleTypetoxsd.put("java.lang.Byte",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "byte", "xs"));
        simpleTypetoxsd.put("java.lang.Short",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "short", "xs"));
        simpleTypetoxsd.put("java.util.Date",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "date", "xs"));
        simpleTypetoxsd.put("java.util.Calendar",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "dateTime", "xs"));

        // SQL date time
         simpleTypetoxsd.put("java.sql.Date",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "date", "xs"));
         simpleTypetoxsd.put("java.sql.Time",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "time", "xs"));
        simpleTypetoxsd.put("java.sql.Timestamp",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "dateTime", "xs"));

         //consider BigDecimal, BigInteger, Day, Duration, Month, MonthDay,
        //Time, Year, YearMonth as SimpleType as well
        simpleTypetoxsd.put("java.math.BigDecimal",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "decimal", "xs"));
        simpleTypetoxsd.put("java.math.BigInteger",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "integer", "xs"));
        simpleTypetoxsd.put("org.apache.axis2.databinding.types.Day",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "gDay", "xs"));
        simpleTypetoxsd.put("org.apache.axis2.databinding.types.Duration",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "duration", "xs"));
        simpleTypetoxsd.put("org.apache.axis2.databinding.types.Month",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "gMonth", "xs"));
        simpleTypetoxsd.put("org.apache.axis2.databinding.types.MonthDay",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "gMonthDay", "xs"));
        simpleTypetoxsd.put("org.apache.axis2.databinding.types.Time",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "time", "xs"));
        simpleTypetoxsd.put("org.apache.axis2.databinding.types.Year",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "gYear", "xs"));
        simpleTypetoxsd.put("org.apache.axis2.databinding.types.YearMonth",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "gYearMonth", "xs"));       
        simpleTypetoxsd.put("java.lang.Object",ANY_TYPE);

        simpleTypetoxsd.put(URI.class.getName(), new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "anyURI", "xs"));

        simpleTypetoxsd.put(OMElement.class.getName(),
                ANY_TYPE);
        simpleTypetoxsd.put(ArrayList.class.getName(),
                ANY_TYPE);
        simpleTypetoxsd.put(Vector.class.getName(),
                ANY_TYPE);
        simpleTypetoxsd.put(List.class.getName(),
                ANY_TYPE);
         simpleTypetoxsd.put(HashMap.class.getName(),
                 ANY_TYPE);
         simpleTypetoxsd.put(Hashtable.class.getName(),
                 ANY_TYPE);
        //byteArrat
        simpleTypetoxsd.put("base64Binary",
                new QName(Java2WSDLConstants.URI_2001_SCHEMA_XSD, "base64Binary", "xs"));
    }

    /**
     * Return the schema type QName given the type class name
     * @param typeName  the name of the type
     * @return   the name of the simple type or null if it is not a simple type
     */
    public QName getSimpleSchemaTypeName(String typeName) {
        QName qName = (QName) simpleTypetoxsd.get(typeName);
        if(qName == null){
            if((typeName.startsWith("java.lang")||typeName.startsWith("javax.")) &&
                    !Exception.class.getName().equals(typeName)){
                return ANY_TYPE;
            }
        }
        return qName;
    }

    /**
     * Return whether the given type is a simple type or not
     * @param typeName the name of the type
     * @return  true if the type is a simple type
     */
    public boolean isSimpleType(String typeName) {
        
        if (simpleTypetoxsd.keySet().contains(typeName)){
            return true;
        }else if(typeName.startsWith("java.lang")||typeName.startsWith("javax.")){
            return true;
        }
        return false;
    }

    /**
     * Return the complex type map
     * @return  the map with complex types
     */
    public Map getComplexSchemaMap() {
        return complexTypeMap;
    }

    public void addComplexSchema(String name, QName schemaType) {
        complexTypeMap.put(name, schemaType);
    }

    public QName getComplexSchemaType(String name) {
        return (QName) complexTypeMap.get(name);
    }

    public String getClassNameForQName(QName qname) {
        return this.qNameToClassMap.get(qname);
    }

    public void addClassNameForQName(QName qname, String className) {
        this.qNameToClassMap.put(qname, className);
    }

    /**
     * Get the qname for a type
     * first try the simple types if not try the complex types
     * @param typeName  name of the type
     * @return  the Qname for this type
     */
    public QName getQNamefortheType(String typeName) {
        QName type = getSimpleSchemaTypeName(typeName);
        if (type == null) {
            type = getComplexSchemaType(typeName);
        }
        return type;
    }
}


