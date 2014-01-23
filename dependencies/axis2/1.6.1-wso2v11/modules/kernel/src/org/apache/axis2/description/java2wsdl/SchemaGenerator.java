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

import org.apache.axis2.description.AxisService;
import org.apache.ws.commons.schema.XmlSchema;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public interface SchemaGenerator {
    void setNsGen(NamespaceGenerator nsGen);

    Collection<XmlSchema> generateSchema() throws Exception;

    TypeTable getTypeTable();

    Method[] getMethods();

    void setExcludeMethods(ArrayList<String> excludeMethods);

    String getSchemaTargetNameSpace();

    void setAttrFormDefault(String attrFormDefault);

    void setElementFormDefault(String elementFormDefault);

    void setExtraClasses(ArrayList<String> extraClasses);

    void setUseWSDLTypesNamespace(boolean useWSDLTypesNamespace);

    void setPkg2nsmap(Map<String,String> pkg2nsmap);

    String getTargetNamespace();

    void setNonRpcMethods(ArrayList<String> nonRpcMethods);

    void setAxisService(AxisService service);

    String getCustomSchemaLocation();


    void setCustomSchemaLocation(String customSchemaLocation);

    String getMappingFileLocation() ;

    void setMappingFileLocation(String mappingFileLocation) ;
}
