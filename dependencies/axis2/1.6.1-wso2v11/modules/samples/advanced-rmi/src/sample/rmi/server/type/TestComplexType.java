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
package sample.rmi.server.type;

import org.apache.axis2.rmi.metadata.AbstractType;
import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;

import java.util.Map;

import sample.rmi.server.dto.TestComplexBean;

import javax.xml.namespace.QName;


public class TestComplexType extends AbstractType {

    public void populateMetaData(Configurator configurator,
                                 Map processedTypeMap)
            throws MetaDataPopulateException {
        this.javaClass = TestComplexBean.class;
        this.name = "sequenceMaxOccresType";
        this.namespace = "http://ws.apache.org/axis2/rmi/samples/types";
    }

    public void generateSchema(Configurator configurator,
                               Map schemaMap)
            throws SchemaGenerationException {
        this.isSchemaGenerated = true;
        this.xmlType = new TestComplexXmlType();
        this.xmlType.setQname(new QName(this.namespace,this.name));
        registerXmlType(schemaMap);

    }
}
