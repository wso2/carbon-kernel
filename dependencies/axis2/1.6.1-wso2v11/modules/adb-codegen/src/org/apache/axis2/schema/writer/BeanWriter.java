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

package org.apache.axis2.schema.writer;

import org.apache.axis2.schema.BeanWriterMetaInfoHolder;
import org.apache.axis2.schema.CompilerOptions;
import org.apache.axis2.schema.SchemaCompilationException;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Map;

/**
 * The bean writer interface. The schema compiler expects one of these to be
 * presented to it and calls the appropriate methods
 */
public interface BeanWriter {


    /**
     * Initializes the writer with compiler options.
     *
     * @param options
     * @throws IOException
     */
    public void init(CompilerOptions options) throws SchemaCompilationException;

    /**
     * Writes a wrapped class. This will have effect only if the CompilerOptions wrapclassses
     * returns true.
     */
    public void writeBatch() throws SchemaCompilationException;

    /**
     * Gets a map of models. This is useful for tight integrations where the internal workings
     * of the schema compiler may be exposed.
     */
    public Map getModelMap();

    /** Make the fully qualified class name for an element or named type
     * @param qName the qualified Name for this element or type in the schema
     * @return the appropriate fully qualified class name to use in generated code
     */
    public String makeFullyQualifiedClassName(QName qName);

    /**
     * Write a complex type
     *
     * @param complexType
     * @param typeMap
     * @param metainf
     * @param fullyQualifiedClassName the name returned by makeFullyQualifiedClassName() or null if it wasn't called
     * @return Returns String.
     * @throws SchemaCompilationException
     */
    public String write(QName qname,
                        Map<QName,String> typeMap,
                        Map<QName,String> groupTypeMap,
                        BeanWriterMetaInfoHolder metainf,
                        boolean isAbstract)
            throws SchemaCompilationException;

    /**
     * Write a element
     *
     * @param element
     * @param typeMap
     * @param metainf
     * @return Returns String.
     * @throws SchemaCompilationException
     */
    public String write(XmlSchemaElement element,
                        Map<QName,String> typeMap,
                        Map<QName,String> groupTypeMap,
                        BeanWriterMetaInfoHolder metainf) throws SchemaCompilationException;


    /**
     * Write a simple type
     *
     * @param simpleType
     * @param typeMap
     * @param metainf
     * @return Returns String.
     * @throws SchemaCompilationException
     */
    public String write(XmlSchemaSimpleType simpleType,
                        Map<QName,String> typeMap,
                        Map<QName,String> groupTypeMap,
                        BeanWriterMetaInfoHolder metainf) throws SchemaCompilationException;


     /**
     * Find whether the mapper class name is present
     * @param mapperPackageName
     */
    public String getExtensionMapperPackageName();

    /**
     * Registers the mapper package name - this is relevant to languages
     * that enforce packaging such as Java or C#. May be ignored in other
     * languages
     * @param mapperPackageName
     */
    public void registerExtensionMapperPackageName(String mapperPackageName);


    /**
     * Write the extensions mapper component - this is relevant to only the OOP languages
     * and a particular implementation may ignore this
     * @param metainfArray
     * @param namespaceToUse
     */
    public void writeExtensionMapper(BeanWriterMetaInfoHolder[] metainfArray) throws SchemaCompilationException;

    public String getDefaultClassName();

    public String getDefaultClassArrayName();

    public String getDefaultAttribClassName();

    public String getDefaultAttribArrayClassName();

}
