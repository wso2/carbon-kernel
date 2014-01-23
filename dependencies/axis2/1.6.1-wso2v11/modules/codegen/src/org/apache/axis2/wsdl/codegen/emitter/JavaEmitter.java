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

package org.apache.axis2.wsdl.codegen.emitter;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.TypeMapper;

public class JavaEmitter extends AxisServiceBasedMultiLanguageEmitter {

    public JavaEmitter() {
        super();
    }

    /** @param configuration  */
    public JavaEmitter(CodeGenConfiguration configuration) {
        super();
        this.codeGenConfiguration = configuration;
        this.mapper = new DefaultTypeMapper();


    }

    /**
     * @param configuration
     * @param mapper
     */
    public JavaEmitter(CodeGenConfiguration configuration, TypeMapper mapper) {
        super();
        this.codeGenConfiguration = configuration;
        this.mapper = mapper;


    }

}
