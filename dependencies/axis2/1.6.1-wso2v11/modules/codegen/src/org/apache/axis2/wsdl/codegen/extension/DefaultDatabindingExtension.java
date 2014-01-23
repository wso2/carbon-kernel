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

package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.databinding.CDefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.i18n.CodegenMessages;

public class DefaultDatabindingExtension extends AbstractDBProcessingExtension {


    public void engage(CodeGenConfiguration configuration) throws CodeGenerationException {
        TypeMapper mapper = configuration.getTypeMapper();
        if (testFallThrough(configuration.getDatabindingType())) {
            //if it's fall through for the default databinding extension and a mapper has
            //not yet being set, then there's a problem.
            //Hence check the mapper status here

            if (mapper == null) {
                //this shouldn't happen
                throw new CodeGenerationException(
                        CodegenMessages.getMessage("extension.noProperDatabinding"));
            }
            return;
        }
        //the mapper has not been populated yet. since this extension is
        //registered for -d none, we have to generate a new type mapper
        //that serves only the default types
        if (mapper == null) {
            if (configuration.getOutputLanguage() != null &&
                    !configuration.getOutputLanguage().trim().equals("") &&
                    configuration.getOutputLanguage().toLowerCase().equals("c")) {
                configuration.setTypeMapper(new CDefaultTypeMapper());

            } else {
                configuration.setTypeMapper(new DefaultTypeMapper());
            }

        }
    }
}
