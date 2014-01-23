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

package org.apache.axis2.xmlbeans;

import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.SchemaUtil;
import org.apache.axis2.util.URLProcessor;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.extension.XMLBeansExtension;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.xmlbeans.BindingConfig;
import org.apache.xmlbeans.Filer;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Framework-linked code used by XMLBeans data binding support. This is accessed via reflection from
 * the XMLBeans code generation extension when XMLBeans data binding is selected.
 */
public class CodeGenerationUtility {
    public static final String SCHEMA_FOLDER = "schemas";

    public static String MAPPINGS = "mappings";
    public static String MAPPING = "mapping";
    public static String MESSAGE = "message";
    public static String JAVA_NAME = "javaclass";

    public static final String MAPPING_FOLDER = "Mapping";
    public static final String MAPPER_FILE_NAME = "mapper";
    public static final String SCHEMA_PATH = "/org/apache/axis2/wsdl/codegen/schema/";

    private static final Log log = LogFactory.getLog(CodeGenerationUtility.class);
    boolean debug = false;

    /**
     * @param additionalSchemas
     * @throws RuntimeException
     */
    public static TypeMapper processSchemas(List schemas,
                                            Element[] additionalSchemas,
                                            CodeGenConfiguration cgconfig,
                                            String typeSystemName) throws RuntimeException {
        try {

            //check for the imported types. Any imported types are supposed to be here also
            if (schemas == null || schemas.isEmpty()) {
                //there are no types to be code generated
                //However if the type mapper is left empty it will be a problem for the other
                //processes. Hence the default type mapper is set to the configuration
                return new DefaultTypeMapper();
            }

            SchemaTypeSystem sts;
            List completeSchemaList = new ArrayList();
            List topLevelSchemaList = new ArrayList();

            //create the type mapper
            //First try to take the one that is already there
            TypeMapper mapper = cgconfig.getTypeMapper();
            if (mapper == null) {
                mapper = new JavaTypeMapper();
            }

            //change the  default class name of the mapper to
            //xmlbeans specific XMLObject
            mapper.setDefaultMappingName(XmlObject.class.getName());

            Map nameSpacesMap = new HashMap();
            List axisServices = cgconfig.getAxisServices();
            AxisService axisService;
            for (Iterator iter = axisServices.iterator(); iter.hasNext();) {
                axisService = (AxisService)iter.next();
                nameSpacesMap.putAll(axisService.getNamespaceMap());
            }

            // process all the schemas and make a list of all of them for
            // resolving entities
            for (int i = 0; i < schemas.size(); i++) {
                XmlSchema schema = (XmlSchema)schemas.get(i);
                XmlOptions options = new XmlOptions();
                options.setLoadAdditionalNamespaces(
                        nameSpacesMap); //add the namespaces
                XmlSchema[] allSchemas = SchemaUtil.getAllSchemas(schema);
                for (int j = 0; j < allSchemas.length; j++) {
                    completeSchemaList.add(allSchemas[j]);
                }
            }

            //make another list of top level schemas for passing into XMLbeans
            for (int i = 0; i < schemas.size(); i++) {
                XmlSchema schema = (XmlSchema)schemas.get(i);
                XmlOptions options = new XmlOptions();
                options.setLoadAdditionalNamespaces(
                        nameSpacesMap); //add the namespaces
                topLevelSchemaList.add(
                        XmlObject.Factory.parse(
                                getSchemaAsString(schema)
                                , options));

            }

            XmlSchemaCollection extras = new XmlSchemaCollection();
            // add the third party schemas
            //todo perhaps checking the namespaces would be a good idea to
            //make the generated code work efficiently
            for (int i = 0; i < additionalSchemas.length; i++) {
                completeSchemaList.add(extras.read(additionalSchemas[i]));
                topLevelSchemaList.add(XmlObject.Factory.parse(
                        additionalSchemas[i]
                        , null));
            }

            //compile the type system
            Axis2EntityResolver er = new Axis2EntityResolver();
            er.setSchemas((XmlSchema[])completeSchemaList
                    .toArray(new XmlSchema[completeSchemaList.size()]));
            er.setBaseUri(cgconfig.getBaseURI());

            String xsdConfigFile = (String)cgconfig.getProperties().get(XMLBeansExtension.XSDCONFIG_OPTION);

            //-Ejavaversion switch to XmlOptions to generate 1.5 compliant code
            XmlOptions xmlOptions	=	new XmlOptions();
            xmlOptions.setEntityResolver(er);
            //test if javaversion property in CodeGenConfig
            if(null!=cgconfig.getProperty("javaversion")){
            	xmlOptions.put(XmlOptions.GENERATE_JAVA_VERSION,cgconfig.getProperty("javaversion"));
            }
            sts = XmlBeans.compileXmlBeans(
                    // set the STS name; defaults to null, which makes the generated class
                    // include a unique (but random) STS name
                    typeSystemName,
                    null,
                    convertToSchemaArray(topLevelSchemaList),
                    new Axis2BindingConfig(cgconfig.getUri2PackageNameMap(),
                                           xsdConfigFile),
                    XmlBeans.getContextTypeLoader(),
                    new Axis2Filer(cgconfig),
                    xmlOptions);

            // prune the generated schema type system and add the list of base64 types
            cgconfig.putProperty(Constants.BASE_64_PROPERTY_KEY,
                                 findBase64Types(sts));
            cgconfig.putProperty(Constants.PLAIN_BASE_64_PROPERTY_KEY,
                                 findPlainBase64Types(sts));

            SchemaTypeSystem internal = XmlBeans.getBuiltinTypeSystem();
            SchemaType[] schemaTypes = internal.globalTypes();
            for (int j = 0; j < schemaTypes.length; j++) {
                mapper.addTypeMappingName(schemaTypes[j].getName(),
                                          schemaTypes[j].getFullJavaName());

            }

            //get the schematypes and add the document types to the type mapper
            schemaTypes = sts.documentTypes();
            for (int j = 0; j < schemaTypes.length; j++) {
                mapper.addTypeMappingName(schemaTypes[j].getDocumentElementName(),
                                          schemaTypes[j].getFullJavaName());

            }

            //process the unwrapped parameters
            if (!cgconfig.isParametersWrapped()) {
                //figure out the unwrapped operations
                axisServices = cgconfig.getAxisServices();
                for (Iterator servicesIter = axisServices.iterator(); servicesIter.hasNext();) {
                    axisService = (AxisService)servicesIter.next();
                    for (Iterator operations = axisService.getOperations();
                         operations.hasNext();) {
                        AxisOperation op = (AxisOperation)operations.next();

                        if (WSDLUtil.isInputPresentForMEP(op.getMessageExchangePattern())) {
                            AxisMessage message = op.getMessage(
                                    WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                            if (message != null &&
                                    message.getParameter(Constants.UNWRAPPED_KEY) != null) {
                                SchemaGlobalElement xmlbeansElement =
                                        sts.findElement(message.getElementQName());
                                SchemaType sType = xmlbeansElement.getType();

                                SchemaProperty[] elementProperties = sType.getElementProperties();
                                for (int i = 0; i < elementProperties.length; i++) {
                                    SchemaProperty elementProperty = elementProperties[i];

                                    QName partQName =
                                            WSDLUtil.getPartQName(op.getName().getLocalPart(),
                                                                  WSDLConstants.INPUT_PART_QNAME_SUFFIX,
                                                                  elementProperty
                                                                          .getName().getLocalPart());

                                    //this type is based on a primitive type- use the
                                    //primitive type name in this case
                                    String fullJaveName =
                                            elementProperty.getType().getFullJavaName();
                                    if (elementProperty.extendsJavaArray()) {
                                        fullJaveName = fullJaveName.concat("[]");
                                    }
                                    mapper.addTypeMappingName(partQName, fullJaveName);
                                    SchemaType primitiveType =
                                            elementProperty.getType().getPrimitiveType();


                                    if (primitiveType != null) {
                                        mapper.addTypeMappingStatus(partQName, Boolean.TRUE);
                                    }
                                    if (elementProperty.extendsJavaArray()) {
                                        mapper.addTypeMappingStatus(partQName,
                                                                    Constants.ARRAY_TYPE);
                                    }
                                }
                            }
                        }

                        if (WSDLUtil.isOutputPresentForMEP(op.getMessageExchangePattern())) {
                            AxisMessage message = op.getMessage(
                                    WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                            if (message != null &&
                                    message.getParameter(Constants.UNWRAPPED_KEY) != null) {
                                SchemaGlobalElement xmlbeansElement =
                                        sts.findElement(message.getElementQName());
                                SchemaType sType = xmlbeansElement.getType();

                                SchemaProperty[] elementProperties = sType.getElementProperties();
                                for (int i = 0; i < elementProperties.length; i++) {
                                    SchemaProperty elementProperty = elementProperties[i];

                                    QName partQName =
                                            WSDLUtil.getPartQName(op.getName().getLocalPart(),
                                                                  WSDLConstants.OUTPUT_PART_QNAME_SUFFIX,
                                                                  elementProperty
                                                                          .getName().getLocalPart());

                                    //this type is based on a primitive type- use the
                                    //primitive type name in this case
                                    String fullJaveName =
                                            elementProperty.getType().getFullJavaName();
                                    if (elementProperty.extendsJavaArray()) {
                                        fullJaveName = fullJaveName.concat("[]");
                                    }
                                    mapper.addTypeMappingName(partQName, fullJaveName);
                                    SchemaType primitiveType =
                                            elementProperty.getType().getPrimitiveType();


                                    if (primitiveType != null) {
                                        mapper.addTypeMappingStatus(partQName, Boolean.TRUE);
                                    }
                                    if (elementProperty.extendsJavaArray()) {
                                        mapper.addTypeMappingStatus(partQName,
                                                                    Constants.ARRAY_TYPE);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //return mapper to be set in the config
            return mapper;


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Populate the base64 types The algo is to look for simpletypes that have base64 content, and
     * then step out of that onestep and get the element. For now there's an extended check to see
     * whether the simple type is related to the Xmime:contentType!
     *
     * @param sts
     */
    private static List findBase64Types(SchemaTypeSystem sts) {
        List allSeenTypes = new ArrayList();
        List base64ElementQNamesList = new ArrayList();
        SchemaType outerType;
        //add the document types and global types
        allSeenTypes.addAll(Arrays.asList(sts.documentTypes()));
        allSeenTypes.addAll(Arrays.asList(sts.globalTypes()));

        for (int i = 0; i < allSeenTypes.size(); i++) {
            SchemaType sType = (SchemaType)allSeenTypes.get(i);

            if (sType.getContentType() == SchemaType.SIMPLE_CONTENT &&
                    sType.getPrimitiveType() != null) {
                if (org.apache.axis2.namespace.Constants.BASE_64_CONTENT_QNAME
                        .equals(sType.getPrimitiveType().getName())) {
                    outerType = sType.getOuterType();
                    //check the outer type further to see whether it has the contenttype attribute from
                    //XMime namespace
                    SchemaProperty[] properties = sType.getProperties();
                    for (int j = 0; j < properties.length; j++) {
                        if (org.apache.axis2.namespace.Constants.XMIME_CONTENT_TYPE_QNAME
                                .equals(properties[j].getName())) {
                            //add this only if it is a document type ??
                            if (outerType.isDocumentType()) {
                                base64ElementQNamesList.add(outerType.getDocumentElementName());
                            }
                            break;
                        }
                    }
                }
            }
            //add any of the child types if there are any
            allSeenTypes.addAll(Arrays.asList(sType.getAnonymousTypes()));
        }

        return base64ElementQNamesList;
    }

    /**
     * @param sts
     * @return array list
     */
    private static List findPlainBase64Types(SchemaTypeSystem sts) {
        ArrayList allSeenTypes = new ArrayList();

        allSeenTypes.addAll(Arrays.asList(sts.documentTypes()));
        allSeenTypes.addAll(Arrays.asList(sts.globalTypes()));

        ArrayList base64Types = new ArrayList();

        for (Iterator iterator = allSeenTypes.iterator(); iterator.hasNext();) {
            SchemaType stype = (SchemaType)iterator.next();
            findPlainBase64Types(stype, base64Types, new ArrayList());
        }

        return base64Types;
    }

    /**
     * @param stype
     * @param base64Types
     */
    private static void findPlainBase64Types(SchemaType stype,
                                             ArrayList base64Types,
                                             ArrayList processedTypes) {

        SchemaProperty[] elementProperties = stype.getElementProperties();
        QName name;
        SchemaType schemaType;
        for (int i = 0; i < elementProperties.length; i++) {
            schemaType = elementProperties[i].getType();
            name = elementProperties[i].getName();
            if (!base64Types.contains(name) && !processedTypes.contains(schemaType.getName())) {
                processedTypes.add(stype.getName());
                if (schemaType.isPrimitiveType()) {
                    SchemaType primitiveType = schemaType.getPrimitiveType();
                    if (org.apache.axis2.namespace.Constants.BASE_64_CONTENT_QNAME
                            .equals(primitiveType.getName())) {
                        base64Types.add(name);
                    }

                } else {
                    findPlainBase64Types(schemaType, base64Types, processedTypes);
                }
            }
        }


    }


    /** Private class to generate the filer */
    private static class Axis2Filer implements Filer {

        private File location;
        private boolean flatten = false;
        private String resourceDirName;
        private String srcDirName;
        private static final String JAVA_FILE_EXTENSION = ".java";

        private Axis2Filer(CodeGenConfiguration config) {
            location = config.getOutputLocation();
            flatten = config.isFlattenFiles();
            resourceDirName = config.getResourceLocation();
            srcDirName = config.getSourceLocation();
        }

        public OutputStream createBinaryFile(String typename)
                throws IOException {
            File resourcesDirectory =
                    flatten ?
                            location :
                            new File(location, resourceDirName);

            if (!resourcesDirectory.exists()) {
                resourcesDirectory.mkdirs();
            }
            File file = new File(resourcesDirectory, typename);
            file.getParentFile().mkdirs();
            file.createNewFile();
            return new FileOutputStream(file);
        }

        public Writer createSourceFile(String typename)
                throws IOException {
            typename =
                    typename.replace('.', File.separatorChar);

            File outputDir =
                    flatten ?
                            location :
                            new File(location, srcDirName);

            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            File file = new File(outputDir,
                                 typename + JAVA_FILE_EXTENSION);
            file.getParentFile().mkdirs();
            file.createNewFile();
            return new FileWriter(file);
        }
    }

    /**
     * Convert schema into a String
     *
     * @param schema
     */
    private static String getSchemaAsString(XmlSchema schema) throws IOException {
        StringWriter writer = new StringWriter();
        schema.write(writer);
        return writer.toString();
    }

    /**
     * Custom binding configuration for the code generator. This controls how the namespaces are
     * suffixed/prefixed
     */
    private static class Axis2BindingConfig extends BindingConfig {

        private Map uri2packageMappings = null;
        private XSDConfig xsdConfig = null;

        public Axis2BindingConfig(Map uri2packageMappings, String xsdConfigfile) {
            this.uri2packageMappings = uri2packageMappings;
            if (this.uri2packageMappings == null) {
                //make an empty one to avoid nasty surprises
                this.uri2packageMappings = new HashMap();
            }

            // Do we have an xsdconfig file?
            if (xsdConfigfile != null) {
                xsdConfig = new XSDConfig(xsdConfigfile);
            }
        }

        public String lookupPackageForNamespace(String uri) {
            /* If the xsdconfig file has mappings, we'll use them instead of the -p option.
             * If we have an xsdconfig file but no namespace to package mappings, then we'll
             * defer to the -p option.
             */
            if (xsdConfig != null) {
                if (xsdConfig.hasNamespaceToJavaPackageMappings) {
                    log.debug("RETURNING " + uri + " = " +
                            xsdConfig.getNamespacesToJavaPackages().get(uri));
                    return (String)xsdConfig.getNamespacesToJavaPackages().get(uri);
                }
            }

            if (uri2packageMappings.containsKey(uri)) {
                return (String)uri2packageMappings.get(uri);
            } else {
                return URLProcessor.makePackageName(uri);
            }
        }

        public String lookupJavanameForQName(QName qname) {
            /* The mappings are stored in the format:
            * NAMESPACE:LOCAL_NAME, i.e.
            * urn:weegietech:minerva:moduleType
            */
            if (xsdConfig != null) {
                String key = qname.getNamespaceURI() + ":" + qname.getLocalPart();
                if (xsdConfig.getSchemaTypesToJavaNames().containsKey(key)) {
                    log.debug("RETURNING " + qname.getLocalPart() + " = " +
                            xsdConfig.getSchemaTypesToJavaNames().get(key));
                    return (String)xsdConfig.getSchemaTypesToJavaNames().get(key);
                } else {
                    return null;
                }
            } else {
                return super.lookupJavanameForQName(qname);
            }

        }
    }

    /**
     * Converts a given vector of schemaDocuments to XmlBeans processable schema objects. One
     * drawback we have here is the non-inclusion of untargeted namespaces
     *
     * @param vec
     * @return schema array
     */
    private static SchemaDocument.Schema[] convertToSchemaArray(List vec) {
        SchemaDocument[] schemaDocuments =
                (SchemaDocument[])vec.toArray(new SchemaDocument[vec.size()]);
        //remove duplicates
        Vector uniqueSchemas = new Vector(schemaDocuments.length);
        Vector uniqueSchemaTns = new Vector(schemaDocuments.length);
        SchemaDocument.Schema s;
        for (int i = 0; i < schemaDocuments.length; i++) {
            s = schemaDocuments[i].getSchema();
            if (!uniqueSchemaTns.contains(s.getTargetNamespace())) {
                uniqueSchemas.add(s);
                uniqueSchemaTns.add(s.getTargetNamespace());
            } else if (s.getTargetNamespace() == null) {
                uniqueSchemas.add(s);
            }
        }
        return (SchemaDocument.Schema[])
                uniqueSchemas.toArray(
                        new SchemaDocument.Schema[uniqueSchemas.size()]);
    }

    /** Axis2 specific entity resolver */
    private static class Axis2EntityResolver implements EntityResolver {
        private XmlSchema[] schemas;
        private String baseUri;

        /**
         * @param publicId - this is the target namespace
         * @param systemId - this is the location (value of schemaLocation)
         * @return
         * @see EntityResolver#resolveEntity(String, String)
         */
        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException, IOException {
            if (systemId.startsWith("project://local/")) {
                systemId = systemId.substring("project://local/".length());
            }

            // if the system id has // final system id gives only one / after tokenizing process
            // to avoid this we check whether it is started with http:// or not
            if (!systemId.startsWith("http://")) {
                StringTokenizer pathElements = new StringTokenizer(systemId, "/");
                Stack pathElementStack = new Stack();
                while (pathElements.hasMoreTokens()) {
                    String pathElement = pathElements.nextToken();
                    if (".".equals(pathElement)) {
                    } else if ("..".equals(pathElement)) {
                        if (!pathElementStack.isEmpty())
                            pathElementStack.pop();
                    } else {
                        pathElementStack.push(pathElement);
                    }
                }
                StringBuffer pathBuilder = new StringBuffer();
                for (Iterator iter = pathElementStack.iterator(); iter.hasNext();) {
                    pathBuilder.append(File.separator + iter.next());
                }
                systemId = pathBuilder.toString().substring(1);
            }
            

            log.info("Resolving schema with publicId [" + publicId + "] and systemId [" + systemId + "]");
            try {
                for (int i = 0; i < schemas.length; i++) {
                    XmlSchema schema = schemas[i];

                    if (schema.getSourceURI() != null &&
                            schema.getSourceURI().endsWith(systemId.replaceAll("\\\\", "/"))) {

                        // if source uri does not contain any / then it should match
                        // with the final part of the system ID
                        if (schema.getSourceURI().indexOf("/") == -1){
                            String systemIDFinalPath = systemId.substring(systemId.indexOf("/") + 1);
                            if (!systemIDFinalPath.equals(schema.getSourceURI())){
                                continue;
                            }
                        }

                        String path = schema.getSourceURI();
                        File f = getFileFromURI(path);
                        if(f.exists()){
                            InputSource source = new InputSource();
                            source.setSystemId(schema.getSourceURI());
                            return source;
                        } else {
                            return new InputSource(getSchemaAsInputStream(schemas[i]));
                        }
                    }

                }
                for (int i = 0; i < schemas.length; i++) {
                    XmlSchema schema = schemas[i];
                    if (schema.getTargetNamespace() != null &&
                            schema.getTargetNamespace().equals(publicId)) {
                        return new InputSource(getSchemaAsInputStream(schemas[i]));
                    }
                }
                if (systemId.indexOf(':') == -1) {
                    //if the base URI is missing then attache the file:/// to it
                    //if the systemId actually had a scheme then as per the URL
                    //constructor, the context URL scheme should be ignored
                    baseUri = (baseUri == null) ? "file:///" : baseUri;
                    URL url = new URL(baseUri + systemId);
                    InputSource source = new InputSource();
                    source.setSystemId(url.toString());
                    return source;
                }
                return XMLUtils.getEmptyInputSource();
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        private File getFileFromURI(String path) {
            if(path.startsWith("file:///")){
                            path = path.substring(8);
            } else if(path.startsWith("file://")){
                path = path.substring(7);
            } else if(path.startsWith("file:/")){
                path = path.substring(6);
            }
            return new File(path);
        }

        public XmlSchema[]  getSchemas() {
            return schemas;
        }

        public void setSchemas(XmlSchema[] schemas) {
            this.schemas = schemas;
        }

        public String getBaseUri() {
            return baseUri;
        }

        public void setBaseUri(String baseUri) {
            this.baseUri = baseUri;
        }

        /**
         * Convert schema into a InputStream
         *
         * @param schema
         */
        private InputStream getSchemaAsInputStream(XmlSchema schema){
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         schema.write(baos);
         return new ByteArrayInputStream(baos.toByteArray());
        }
    }


}



