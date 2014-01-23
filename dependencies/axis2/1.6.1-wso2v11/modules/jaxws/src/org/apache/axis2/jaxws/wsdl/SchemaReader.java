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

/**
 * 
 */
package org.apache.axis2.jaxws.wsdl;

import javax.wsdl.Definition;
import java.util.Set;

/**
 * Schema Reader interface is used to read schema from wsdl and derive appropriate package names
 * from targetnamespace. The algorithm for deriving reading packages names from schema is as
 * follows: 1) Read the inline schema defined in the wsdl 2) check if there is any jaxb
 * customization/binding defined namely schemaBinding. if(schemaBinding defined) then read the
 * package name and add that to the package set. else read the targetnamespace convert tns to
 * package and add to pkg set. 3) check if there are any xsd imports or includes then for(each
 * import) read the inline schema or perfor step 1 check if there is any jaxb schemaBinding
 * customization defined if(schemaBinding defined) then read the package name and add that to the
 * package set. else read the targetnamespace convert tns to package and add to pkg set. Do Step 3
 * recursively so we cover Schema imports within import n times.
 */
public interface SchemaReader {
    public Set<String> readPackagesFromSchema(Definition wsdlDefinition)
            throws SchemaReaderException;
}
