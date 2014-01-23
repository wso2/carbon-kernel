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

package org.apache.axis2.jaxws.description.builder;

import org.apache.axiom.om.OMDocument;

import javax.wsdl.Definition;
import java.util.HashMap;

public class WsdlComposite {

    HashMap<String, Definition> wsdlDefinitionsMap;

    HashMap<String, OMDocument> schemaMap;

    String wsdlFileName;

    public WsdlComposite() {
        super();
    }

    /** @return Returns the schemaMap. */
    public HashMap<String, OMDocument> getSchemaMap() {
        return schemaMap;
    }

    /** @return Returns the wsdlDefinition. */
    public HashMap<String, Definition> getWsdlDefinitionsMap() {
        return wsdlDefinitionsMap;
    }

    /** @return Returns the root WSDL Definition */
    public Definition getRootWsdlDefinition() {
        return wsdlDefinitionsMap.get(getWsdlFileName());
    }

    /** @return Returns the wsdlFileName. */
    public String getWsdlFileName() {
        return wsdlFileName;
    }

    /** @param schemaMap The schemaMap to set. */
    public void setSchemaMap(HashMap<String, OMDocument> schemaMap) {
        this.schemaMap = schemaMap;
    }

    /** @param wsdlDefinition The wsdlDefinition to set. */
    public void setWsdlDefinition(HashMap<String, Definition> wsdlDefinitionsMap) {
        this.wsdlDefinitionsMap = wsdlDefinitionsMap;
    }

    /**
     * @param wsdlFileName The wsdlFileName to set.
     */
    public void setWsdlFileName(String wsdlFileName) {
        this.wsdlFileName = wsdlFileName;
    }
	
	
}
