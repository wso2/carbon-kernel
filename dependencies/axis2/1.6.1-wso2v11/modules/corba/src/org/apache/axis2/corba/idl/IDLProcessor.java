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

package org.apache.axis2.corba.idl;

import antlr.collections.AST;
import org.apache.axis2.corba.exceptions.IDLProcessorException;
import org.apache.axis2.corba.idl.parser.IDLLexer;
import org.apache.axis2.corba.idl.parser.IDLParser;
import org.apache.axis2.corba.idl.parser.IDLVisitor;
import org.apache.axis2.corba.idl.types.IDL;

import java.io.InputStream;

public class IDLProcessor {

    protected InputStream stream;

    public IDLProcessor(InputStream stream) {
        this.stream = stream;
    }

    public IDL process() throws IDLProcessorException {
        IDL idl;
        try {
            IDLParser parser = new IDLParser(new IDLLexer(stream));
            parser.specification();
            AST idlTree = parser.getAST();

            // new DumpASTVisitor().visit(idlTree); // print the AST structure

            IDLVisitor visitor = new IDLVisitor();
            visitor.visit(idlTree);
            idl = visitor.getIDL();
        } catch (Exception ex) {
            throw new IDLProcessorException(ex.getMessage(), ex);
        }
        return idl;
    }
}
