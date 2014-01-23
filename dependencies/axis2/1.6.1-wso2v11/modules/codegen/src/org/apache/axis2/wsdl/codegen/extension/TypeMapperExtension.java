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
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.databinding.CTypeMapper;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * The purpose of this extension is to populate the type mapper from the type mapping file. The
 * format of the type mapping file is as follows <mappings dbf="adb"> <mapping> <qname
 * namespace="ns" prefix="p1">localName</qname> <value>type</value> </mapping> </mappings>
 * <p/>
 * In any case it is best that the type mapper extension be after all the databinding extensions
 */

public class TypeMapperExtension implements CodeGenExtension {


    private static final String MAPPING_ELEMENT_NAME = "mapping";
    private static final String NAMESPACE_ATTRIBUTE_NAME = "namespace";
    private static final String QNAME_ELEMENT_NAME = "qname";
    private static final String VALUE_ELEMENT_NAME = "value";
    private static final String DB_FRAMEWORK_ATTRIBUTE_NAME = "dbf";


    /** @throws CodeGenerationException  */
    public void engage(CodeGenConfiguration configuration) throws CodeGenerationException {
        if (configuration.getTypeMappingFile() != null) {
            //a type mapping is present. try building the
            //mapping from it

            // if the configuration already has a mapping then take it
            // the external mappings will override the currently available
            // mappings
            TypeMapper mapper = configuration.getTypeMapper();
            // there is no mapper present - so just create a new one
            if (mapper == null) {
                if (configuration.getOutputLanguage() != null &&
                    !configuration.getOutputLanguage().trim().equals("") &&
                    configuration.getOutputLanguage().toLowerCase().equals("c")) {
                    mapper = new CTypeMapper();
    
                }  else {
                    mapper = new DefaultTypeMapper();
                }
            }

            //read the file as a DOM
            Document mappingDocument = buildDocument(configuration);
            Element rootMappingsElement = mappingDocument.getDocumentElement();

            //override the databinding framework name. If a mapping file is
            //present then the databinding framework name will be overridden
            //if present. If a user wants to mix types then it must be
            //from the same databinding framework!

            //first do a sanity check to see whether the user is trying to
            //mix databinding types!

            String databindingName = rootMappingsElement.
                    getAttribute(DB_FRAMEWORK_ATTRIBUTE_NAME);
            if (!databindingName.equals(configuration.getDatabindingType())) {
                throw new CodeGenerationException(
                        CodegenMessages.
                                getMessage("extension.databindingMismatch")
                );
            }
            configuration.
                    setDatabindingType(
                            databindingName);


            NodeList mappingList = rootMappingsElement.
                    getElementsByTagName(MAPPING_ELEMENT_NAME);
            int length = mappingList.getLength();
            for (int i = 0; i < length; i++) {
                Element mappingNode = (Element)mappingList.item(i);
                //we know this is only one - if there are multiple then
                //it is invalid
                Element qNameChild =
                        (Element)mappingNode.
                                getElementsByTagName(QNAME_ELEMENT_NAME).item(0);
                Element valueChild =
                        (Element)mappingNode.
                                getElementsByTagName(VALUE_ELEMENT_NAME).item(0);
                //generate a Qname and add to the type mapping
                mapper.addTypeMappingName(new QName(
                        qNameChild.getAttribute(NAMESPACE_ATTRIBUTE_NAME),
                        getTextFromElement(qNameChild)),
                                          getTextFromElement(valueChild));

            }

            //set the type mapper to the configurtion
            configuration.setTypeMapper(mapper);
        }
    }

    /**
     * Build a dom document from the mapping file
     *
     * @throws CodeGenerationException
     */
    private Document buildDocument(CodeGenConfiguration configuration)
            throws CodeGenerationException {
        try {
            DocumentBuilderFactory documentBuilderFactory
                    = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder =
                    documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(configuration.getTypeMappingFile());
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    /**
     * Gets the string content from an element. returns null if there are no test nodes found
     *
     * @param elt
     * @return text cotent of the element
     */
    private String getTextFromElement(Element elt) {
        NodeList children = elt.getChildNodes();
        String returnString = null;
        int length = children.getLength();
        for (int i = 0; i < length; i++) {
            Node node = children.item(i);
            if (Node.TEXT_NODE == node.getNodeType()) {
                returnString = (returnString == null ? "" : returnString) + node.getNodeValue();
            }

        }

        return returnString;
    }
}
