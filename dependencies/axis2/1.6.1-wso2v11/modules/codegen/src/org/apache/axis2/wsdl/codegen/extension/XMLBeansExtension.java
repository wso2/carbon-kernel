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

import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.schema.AxisServiceTopElementSchemaGenerator;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class XMLBeansExtension extends AbstractDBProcessingExtension {
    /** Name of "extra" option used to supply package name for xsb files. */
    public static final String TYPESYSTEMNAME_OPTION = "typesystemname";
    public static final String SCHEMA_FOLDER = "schemas";
    public static final String XSDCONFIG_OPTION = "xc";
    public static final String XSDCONFIG_OPTION_LONG = "xsdconfig";


    public static String MAPPINGS = "mappings";
    public static String MAPPING = "mapping";
    public static String MESSAGE = "message";
    public static String JAVA_NAME = "javaclass";

    public static final String MAPPING_FOLDER = "Mapping";
    public static final String MAPPER_FILE_NAME = "mapper";
    public static final String SCHEMA_PATH = "/org/apache/axis2/wsdl/codegen/schema/";

    public static final String XMLBEANS_CONFIG_CLASS =
            "org.apache.xmlbeans.BindingConfig";
    public static final String XMLBEANS_UTILITY_CLASS =
            "org.apache.axis2.xmlbeans.CodeGenerationUtility";
    public static final String XMLBEANS_PROCESS_METHOD = "processSchemas";

    boolean debug = false;

    public void engage(CodeGenConfiguration configuration) {

        //test the databinding type. If not just fall through
        if (testFallThrough(configuration.getDatabindingType())) {
            return;
        }

        // check the JiBX binding definition file specified
        String typeSystemName = (String)configuration.getProperties().get(TYPESYSTEMNAME_OPTION);

        try {
            // try dummy load of framework class first to check missing jars
            try {
                getClass().getClassLoader().loadClass(XMLBEANS_CONFIG_CLASS);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("XMLBeans framework jars not in classpath");
            }

            // load the actual utility class
            Class clazz = null;
            try {
                clazz = getClass().getClassLoader().loadClass(XMLBEANS_UTILITY_CLASS);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("XMLBeans binding extension not in classpath");
            }

            // invoke utility class method for actual processing
            Method method = clazz.getMethod(XMLBEANS_PROCESS_METHOD,
                                            new Class[] { List.class, Element[].class,
                                                    CodeGenConfiguration.class, String.class });
            List schemas = new ArrayList();
            List axisServices = configuration.getAxisServices();
            AxisService axisService = null;
            AxisServiceTopElementSchemaGenerator schemaGenerator = null;
            for (Iterator iter = axisServices.iterator(); iter.hasNext();) {
                axisService = (AxisService)iter.next();
                if (configuration.getProperties().containsKey(
                        CommandLineOptionConstants.ExtensionArguments.WITHOUT_DATABIND_CODE)){
                    // use the dummy code
                    schemaGenerator = new AxisServiceTopElementSchemaGenerator(axisService);
                    schemas.addAll(schemaGenerator.getDummySchemaList());
                } else {
                    schemas.addAll(axisService.getSchema());
                }

            }

            Element[] additionalSchemas = loadAdditionalSchemas();
            TypeMapper mapper = (TypeMapper)method.invoke(null,
                                                          new Object[] { schemas, additionalSchemas,
                                                                  configuration, typeSystemName });

            // set the type mapper to the config
            configuration.setTypeMapper(mapper);

        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Loading the external schemas.
     *
     * @return element array consisting of the the DOM element objects that represent schemas
     */
    private Element[] loadAdditionalSchemas() {
        //load additional schemas
        String[] schemaNames = ConfigPropertyFileLoader.getThirdPartySchemaNames();
        Element[] schemaElements;

        try {
            ArrayList additionalSchemaElements = new ArrayList();
            DocumentBuilder documentBuilder = getNamespaceAwareDocumentBuilder();
            for (int i = 0; i < schemaNames.length; i++) {
                //the location for the third party schema;s is hardcoded
                if (!"".equals(schemaNames[i].trim())) {
                    InputStream schemaStream =
                            this.getClass().getResourceAsStream(SCHEMA_PATH + schemaNames[i]);
                    Document doc = documentBuilder.parse(schemaStream);
                    additionalSchemaElements.add(doc.getDocumentElement());
                }
            }

            //Create the Schema element array
            schemaElements = new Element[additionalSchemaElements.size()];
            for (int i = 0; i < additionalSchemaElements.size(); i++) {
                schemaElements[i] = (Element)additionalSchemaElements.get(i);

            }
        } catch (Exception e) {
            throw new RuntimeException(
                    CodegenMessages.getMessage("extension.additionalSchemaFailure"), e);
        }

        return schemaElements;
    }

    private DocumentBuilder getNamespaceAwareDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        return documentBuilderFactory.newDocumentBuilder();
    }
}
