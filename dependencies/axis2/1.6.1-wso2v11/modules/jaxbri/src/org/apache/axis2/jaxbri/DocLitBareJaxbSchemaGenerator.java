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

package org.apache.axis2.jaxbri;

import org.apache.axis2.description.java2wsdl.DocLitBareSchemaGenerator;

import java.util.Collection;

public class DocLitBareJaxbSchemaGenerator extends DocLitBareSchemaGenerator {

    private JaxbSchemaGenerator jaxbSchemaGenerator;

    public DocLitBareJaxbSchemaGenerator(ClassLoader loader, String className,
                               String schematargetNamespace,
                               String schematargetNamespacePrefix)
            throws Exception {
        super(loader, className, schematargetNamespace, schematargetNamespacePrefix,null);
        jaxbSchemaGenerator = new JaxbSchemaGenerator(loader,className,schematargetNamespace,schematargetNamespacePrefix);
    }


    public Collection generateSchema() throws Exception {
       jaxbSchemaGenerator.generateSchemaForParameters();
        return super.generateSchema();
    }
}
