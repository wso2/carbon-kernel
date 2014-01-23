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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.schema.BeanWriterMetaInfoHolder;
import org.apache.axis2.schema.CompilerOptions;
import org.apache.axis2.schema.SchemaCompilationException;
import org.apache.axis2.schema.SchemaConstants;
import org.apache.axis2.schema.i18n.SchemaCompilerMessages;
import org.apache.axis2.schema.typemap.JavaTypeMap;
import org.apache.axis2.schema.util.PrimitiveTypeFinder;
import org.apache.axis2.schema.util.PrimitiveTypeWrapper;
import org.apache.axis2.schema.util.SchemaPropertyLoader;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.PrettyPrinter;
import org.apache.axis2.util.URLProcessor;
import org.apache.axis2.util.XSLTTemplateProcessor;
import org.apache.axis2.util.XSLTUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Java Bean writer for the schema compiler.
 */
public class JavaBeanWriter implements BeanWriter {

    private static final Log log = LogFactory.getLog(JavaBeanWriter.class);

    public static final String WRAPPED_DATABINDING_CLASS_NAME = "WrappedDatabinder";

    private String javaBeanTemplateName = null;

    private boolean templateLoaded = false;

    private Templates templateCache;

    private List<String> nameList;

    private Map<String,List<String>> packageNameToClassNamesMap;

    private static int count = 0;

    private boolean wrapClasses = false;

    private boolean writeClasses = false;

    private boolean isUseWrapperClasses = false;

    private String packageName = null;

    private File rootDir;

    private Document globalWrappedDocument;

    private Map modelMap = new HashMap();

    private static final String DEFAULT_PACKAGE = "adb";

    private Map baseTypeMap = new JavaTypeMap().getTypeMap();

    private Map<String,String> ns2packageNameMap = new HashMap<String,String>();

    private boolean isHelperMode = false;

    private boolean isSuppressPrefixesMode = false;

    /**
     * package for the mapping class
     */
    private String mappingClassPackage = null;

    public static final String EXTENSION_MAPPER_CLASSNAME = "ExtensionMapper";
// a list of externally identified QNames to be processed. This becomes
    // useful when  only a list of external elements need to be processed

    public static final String DEFAULT_CLASS_NAME = OMElement.class.getName();
    public static final String DEFAULT_CLASS_ARRAY_NAME = "org.apache.axiom.om.OMElement[]";

    public static final String DEFAULT_ATTRIB_CLASS_NAME = OMAttribute.class.getName();
    public static final String DEFAULT_ATTRIB_ARRAY_CLASS_NAME = "org.apache.axiom.om.OMAttribute[]";


    /**
     * Default constructor
     */
    public JavaBeanWriter() {
    }

    /**
     * This returns a map of Qnames vs DOMDocument models. One can use this
     * method to obtain the raw DOMmodels used to write the classes. This has no
     * meaning when the classes are supposed to be wrapped so the
     *
     * @return Returns Map.
     * @see BeanWriter#getModelMap()
     */
    public Map getModelMap() {
        return modelMap;
    }

    public String getDefaultClassName() {
        return DEFAULT_CLASS_NAME;
    }

    public String getDefaultClassArrayName() {
        return DEFAULT_CLASS_ARRAY_NAME;
    }

    public String getDefaultAttribClassName() {
        return DEFAULT_ATTRIB_CLASS_NAME;
    }

    public String getDefaultAttribArrayClassName() {
        return DEFAULT_ATTRIB_ARRAY_CLASS_NAME;
    }


    public void init(CompilerOptions options) throws SchemaCompilationException {
        try {

            // set all state variables to default values
            modelMap = new HashMap();
            ns2packageNameMap = new HashMap<String,String>();
            mappingClassPackage = null;

            initWithFile(options.getOutputLocation());
            packageName = options.getPackageName();
            writeClasses = options.isWriteOutput();
            isUseWrapperClasses = options.isUseWrapperClasses();

            if (!writeClasses) {
                wrapClasses = false;
            } else {
                wrapClasses = options.isWrapClasses();
            }

            // if the wrap mode is set then create a global document to keep the
            // wrapped
            // element models
            if (options.isWrapClasses()) {
                globalWrappedDocument = XSLTUtils.getDocument();
                Element rootElement = XSLTUtils.getElement(
                        globalWrappedDocument, "beans");
                globalWrappedDocument.appendChild(rootElement);
                XSLTUtils.addAttribute(globalWrappedDocument, "name",
                        WRAPPED_DATABINDING_CLASS_NAME, rootElement);
                String tempPackageName;

                if (packageName != null && packageName.endsWith(".")) {
                    tempPackageName = this.packageName.substring(0,
                            this.packageName.lastIndexOf("."));
                } else {
                    tempPackageName = DEFAULT_PACKAGE;
                }

                XSLTUtils.addAttribute(globalWrappedDocument, "package",
                        tempPackageName, rootElement);
            }

            // add the ns mappings
            this.ns2packageNameMap = options.getNs2PackageMap();
            //set helper mode
            this.isHelperMode = options.isHelperMode();
            // set suppress prefixes mode
            this.isSuppressPrefixesMode = options.isSuppressPrefixesMode();

            //set mapper class package if present
            if (options.isMapperClassPackagePresent()) {
                this.mappingClassPackage = options.getMapperClassPackage();
            }

        } catch (IOException e) {
            throw new SchemaCompilationException(e);
        } catch (ParserConfigurationException e) {
            throw new SchemaCompilationException(e); // todo need to put
            // correct error
            // messages
        }
    }

    /**
     * @param element
     * @param typeMap
     * @param metainf
     * @return Returns String.
     * @throws SchemaCompilationException
     */
    public String write(XmlSchemaElement element,
                        Map<QName,String> typeMap,
                        Map<QName,String> groupTypeMap,
                        BeanWriterMetaInfoHolder metainf) throws SchemaCompilationException {

        try {
            QName qName = element.getQName();

            return process(qName, metainf, typeMap, groupTypeMap, true, false);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SchemaCompilationException(e);
        }

    }

    /**
     * `
     * @param qName
     * @param typeMap
     * @param metainf
     * @param isAbstract
     * @return
     * @throws SchemaCompilationException
     */
    public String write(QName qName,
                        Map<QName,String> typeMap,
                        Map<QName,String> groupTypeMap,
                        BeanWriterMetaInfoHolder metainf,
                        boolean isAbstract)
            throws SchemaCompilationException {

        try {
            // determine the package for this type.
            return process(qName, metainf, typeMap, groupTypeMap, false,isAbstract);

        } catch (SchemaCompilationException e) {
            throw e;
        } catch (Exception e) {
            throw new SchemaCompilationException(e);
        }

    }



    /**
     * @throws Exception
     * @see BeanWriter#writeBatch()
     */
    public void writeBatch() throws SchemaCompilationException {
        try {
            if (wrapClasses) {
                String tempPackage;
                if (packageName == null) {
                    tempPackage = DEFAULT_PACKAGE;
                } else {
                    tempPackage = packageName;
                }
                File out = createOutFile(tempPackage,
                        WRAPPED_DATABINDING_CLASS_NAME);
                // parse with the template and create the files

                parse(globalWrappedDocument, out);
            }
        } catch (Exception e) {
            throw new SchemaCompilationException(e);
        }
    }

    /**
     * @param simpleType
     * @param typeMap
     * @param metainf
     * @return Returns String.
     * @throws SchemaCompilationException
     */
    public String write(XmlSchemaSimpleType simpleType,
                        Map<QName,String> typeMap,
                        Map<QName,String> groupTypeMap,
                        BeanWriterMetaInfoHolder metainf) throws SchemaCompilationException {
        try {
            QName qName = simpleType.getQName();
            if (qName == null) {
                qName = (QName) simpleType.getMetaInfoMap().get(SchemaConstants.SchemaCompilerInfoHolder.FAKE_QNAME);
            }
            metainf.addtStatus(qName, SchemaConstants.SIMPLE_TYPE_OR_CONTENT);
            return process(qName, metainf, typeMap, groupTypeMap, true, false);
        } catch (Exception e) {
            throw new SchemaCompilationException(e);
        }
    }

    /**
     * @param rootDir
     * @throws IOException
     * @see BeanWriter#init(java.io.File)
     */
    private void initWithFile(File rootDir) throws IOException {
        if (rootDir == null) {
            this.rootDir = new File(".");
        } else if (!rootDir.isDirectory()) {
            throw new IOException(SchemaCompilerMessages
                    .getMessage("schema.rootnotfolderexception"));
        } else {
            this.rootDir = rootDir;
        }
        this.nameList = new ArrayList<String>();
        this.packageNameToClassNamesMap = new HashMap<String,List<String>>();
        javaBeanTemplateName = SchemaPropertyLoader.getBeanTemplate();
    }

    /**
     * Make the fully qualified class name for an element or named type
     *
     * @param qName the qualified Name for this element or type in the schema
     * @return the appropriate fully qualified class name to use in generated
     *         code
     */
    public String makeFullyQualifiedClassName(QName qName) {

        String namespaceURI = qName.getNamespaceURI();

        String packageName = getPackage(namespaceURI);

        String originalName = qName.getLocalPart();
        String className = null;

        // when wrapping classes all the data binding and exception class should have
        // a unique name since package name is not being applied.
        // otherewise we can make unique with the package name
        if (!wrapClasses){
            className = makeUniqueJavaClassName(this.nameList, originalName);
        } else {
            if (!this.packageNameToClassNamesMap.containsKey(packageName)) {
                this.packageNameToClassNamesMap.put(packageName, new ArrayList<String>());
            }
            className = makeUniqueJavaClassName(this.packageNameToClassNamesMap.get(packageName), originalName);
        }


        String packagePrefix = null;

        String fullyqualifiedClassName;

        if (wrapClasses)
            packagePrefix = (this.packageName == null ? DEFAULT_PACKAGE + "."
                    : this.packageName)
                    + WRAPPED_DATABINDING_CLASS_NAME;
        else if (writeClasses)
            packagePrefix = packageName;
        if (packagePrefix != null)
            fullyqualifiedClassName = packagePrefix
                    + (packagePrefix.endsWith(".") ? "" : ".") + className;
        else
            fullyqualifiedClassName = className;
        // return the fully qualified class name
        return fullyqualifiedClassName;
    }

    private String getPackage(String namespaceURI) {
        String basePackageName;
        if ((ns2packageNameMap != null) && ns2packageNameMap.containsKey(namespaceURI)) {
            basePackageName = ns2packageNameMap.get(namespaceURI);
        } else {
            basePackageName = URLProcessor.makePackageName(namespaceURI);
        }

        return this.packageName == null ? basePackageName
                : this.packageName + basePackageName;
    }

    /**
     * A util method that holds common code for the complete schema that the
     * generated XML complies to look under other/beanGenerationSchema.xsd
     *
     * @param qName
     * @param metainf
     * @param typeMap
     * @param isElement
     * @param fullyQualifiedClassName the name returned by makeFullyQualifiedClassName() or null if
     *                                it wasn't called
     * @return Returns String.
     * @throws Exception
     */
    private String process(QName qName,
                           BeanWriterMetaInfoHolder metainf,
                           Map<QName,String> typeMap,
                           Map<QName,String> groupTypeMap,
                           boolean isElement,
                           boolean isAbstract)
            throws Exception {
        String fullyQualifiedClassName = metainf.getOwnClassName();
        if (fullyQualifiedClassName == null)
            fullyQualifiedClassName = makeFullyQualifiedClassName(qName);
        String className = fullyQualifiedClassName
                .substring(1 + fullyQualifiedClassName.lastIndexOf('.'));
        String basePackageName;
        if (fullyQualifiedClassName.lastIndexOf('.') == -1) {// no 'dots' so
            // the package
            // is not there
            basePackageName = "";
        } else {
            basePackageName = fullyQualifiedClassName.substring(0,
                    fullyQualifiedClassName.lastIndexOf('.'));
        }

        String originalName = qName == null ? "" : qName.getLocalPart();
        ArrayList<String> propertyNames = new ArrayList<String>();

        if (!templateLoaded) {
            loadTemplate();
        }

        // if wrapped then do not write the classes now but add the models to a
        // global document. However in order to write the
        // global class that is generated, one needs to call the writeBatch()
        // method
        if (wrapClasses) {
            globalWrappedDocument.getDocumentElement().appendChild(
                    getBeanElement(globalWrappedDocument, className,
                            originalName, basePackageName, qName, isElement,isAbstract,
                            metainf, propertyNames, typeMap, groupTypeMap));

        } else {
            // create the model
            Document model = XSLTUtils.getDocument();
            // make the XML
            model.appendChild(getBeanElement(model, className, originalName,
                    basePackageName, qName, isElement,isAbstract, metainf, propertyNames,
                    typeMap, groupTypeMap));

            if (writeClasses) {
                // create the file
                File out = createOutFile(basePackageName, className);
                // parse with the template and create the files

                if (isHelperMode) {

                    XSLTUtils.addAttribute(model, "helperMode", "yes", model.getDocumentElement());

                    // Generate bean classes
                    parse(model, out);

                    // Generating the helper classes
                    out = createOutFile(basePackageName, className + "Helper");
                    XSLTUtils.addAttribute(model, "helper", "yes", model
                            .getDocumentElement());
                    parse(model, out);

                } else {
                    //No helper mode - just generate the classes
                    parse(model, out);
                }
            }

            // add the model to the model map
            modelMap.put(new QName(qName.getNamespaceURI(), className), model);

        }

        // return the fully qualified class name
        return fullyQualifiedClassName;

    }

    /**
     * @param model
     * @param className
     * @param originalName
     * @param packageName
     * @param qName
     * @param isElement
     * @param metainf
     * @param propertyNames
     * @param typeMap
     * @return Returns Element.
     * @throws SchemaCompilationException
     */
    private Element getBeanElement(Document model,
                                   String className,
                                   String originalName,
                                   String packageName,
                                   QName qName,
                                   boolean isElement,
                                   boolean isAbstract,
                                   BeanWriterMetaInfoHolder metainf,
                                   ArrayList<String> propertyNames,
                                   Map<QName,String> typeMap,
                                   Map<QName,String> groupTypeMap)
            throws SchemaCompilationException {

        Element rootElt = XSLTUtils.getElement(model, "bean");
        XSLTUtils.addAttribute(model, "name", className, rootElt);
        XSLTUtils.addAttribute(model, "originalName", originalName, rootElt);
        XSLTUtils.addAttribute(model, "package", packageName, rootElt);
        XSLTUtils.addAttribute(model, "nsuri", qName.getNamespaceURI(), rootElt);
       XSLTUtils.addAttribute(model, "nsprefix", isSuppressPrefixesMode ? "" : getPrefixForURI(qName
                .getNamespaceURI(), qName.getPrefix()), rootElt);

        if (!wrapClasses) {
            XSLTUtils.addAttribute(model, "unwrapped", "yes", rootElt);
        }

        if (isAbstract){
           XSLTUtils.addAttribute(model, "isAbstract", "yes", rootElt);
        }

        if (!writeClasses) {
            XSLTUtils.addAttribute(model, "skip-write", "yes", rootElt);
        }

        if (!isElement) {
            XSLTUtils.addAttribute(model, "type", "yes", rootElt);
        }

        if (metainf.isAnonymous()) {
            XSLTUtils.addAttribute(model, "anon", "yes", rootElt);
        }

        if (isUseWrapperClasses){
            XSLTUtils.addAttribute(model, "usewrapperclasses", "yes", rootElt);
        }

        if (metainf.isExtension()) {
            XSLTUtils.addAttribute(model, "extension", metainf
                    .getExtensionClassName(), rootElt);

        }
        if (metainf.isRestriction()) {
            XSLTUtils.addAttribute(model, "restriction", metainf
                    .getRestrictionClassName(), rootElt);

        }
        //add the mapper class name
        XSLTUtils.addAttribute(model, "mapperClass", getFullyQualifiedMapperClassName(), rootElt);

        if (metainf.isChoice()) {
            XSLTUtils.addAttribute(model, "choice", "yes", rootElt);
        }

        if (metainf.isSimple()) {
            XSLTUtils.addAttribute(model, "simple", "yes", rootElt);
        }

        if (metainf.isUnion()) {
            XSLTUtils.addAttribute(model, "union", "yes", rootElt);
        }

        if (metainf.isList()) {
            XSLTUtils.addAttribute(model, "list", "yes", rootElt);
        }

        if (metainf.isOrdered()) {
            XSLTUtils.addAttribute(model, "ordered", "yes", rootElt);
        }

        if (isElement && metainf.isNillable(qName)) {
            XSLTUtils.addAttribute(model, "nillable", "yes", rootElt);
        }

        if (metainf.isParticleClass()) {
            XSLTUtils.addAttribute(model, "particleClass", "yes", rootElt);
        }

        if (metainf.isHasParticleType()){
            XSLTUtils.addAttribute(model, "hasParticleType", "yes", rootElt);
        }

        // populate all the information
        populateInfo(metainf, model, rootElt, propertyNames, typeMap, groupTypeMap, false);

        if (metainf.isSimple() && metainf.isUnion()) {
            populateMemberInfo(metainf, model, rootElt, typeMap);
        }

        if (metainf.isSimple() && metainf.isList()) {
            populateListInfo(metainf, model, rootElt, typeMap, groupTypeMap);
        }
        //////////////////////////////////////////////////////////
//        System.out.println(DOM2Writer.nodeToString(rootElt));
        ////////////////////////////////////////////////////////////

        return rootElt;
    }

    protected void populateListInfo(BeanWriterMetaInfoHolder metainf,
                                    Document model,
                                    Element rootElement,
                                    Map<QName,String> typeMap,
                                    Map<QName,String> groupTypeMap) {

        String javaName = makeUniqueJavaClassName(new ArrayList<String>(), metainf.getItemTypeQName().getLocalPart());
        Element itemType = XSLTUtils.addChildElement(model, "itemtype", rootElement);
        XSLTUtils.addAttribute(model, "type", metainf.getItemTypeClassName(), itemType);
        XSLTUtils.addAttribute(model, "nsuri", metainf.getItemTypeQName().getNamespaceURI(), itemType);
        XSLTUtils.addAttribute(model, "originalName", metainf.getItemTypeQName().getLocalPart(), itemType);
        XSLTUtils.addAttribute(model, "javaname", javaName, itemType);


        if (typeMap.containsKey(metainf.getItemTypeQName()) ||
                groupTypeMap.containsKey(metainf.getItemTypeClassName())) {
            XSLTUtils.addAttribute(model, "ours", "true", itemType);
        }
        if (PrimitiveTypeFinder.isPrimitive(metainf.getItemTypeClassName())) {
            XSLTUtils.addAttribute(model, "primitive", "yes", itemType);
        }

        String shortTypeName = getShortTypeName(metainf.getItemTypeClassName());
        XSLTUtils.addAttribute(model, "shorttypename", shortTypeName, itemType);

    }

    protected void populateMemberInfo(BeanWriterMetaInfoHolder metainf,
                                      Document model,
                                      Element rootElement,
                                      Map<QName,String> typeMap) {
        Map<QName,String> memberTypes = metainf.getMemberTypes();
        for (QName memberQName : metainf.getMemberTypesKeys()) {
            String memberClass = memberTypes.get(memberQName);
            if (PrimitiveTypeFinder.isPrimitive(memberClass)) {
                memberClass = PrimitiveTypeWrapper.getWrapper(memberClass);
            }

            // add member type element
            Element memberType = XSLTUtils.addChildElement(model, "memberType", rootElement);
            XSLTUtils.addAttribute(model, "type", memberClass, memberType);
            XSLTUtils.addAttribute(model, "nsuri", memberQName.getNamespaceURI(), memberType);
            XSLTUtils.addAttribute(model, "originalName", memberQName.getLocalPart(), memberType);
            if (typeMap.containsKey(memberQName)) {
                XSLTUtils.addAttribute(model, "ours", "true", memberType);
            }
            String shortTypeName = getShortTypeName(memberClass);
            XSLTUtils.addAttribute(model, "shorttypename", shortTypeName, memberType);

        }
    }

    /**
     * @param metainf
     * @param model
     * @param rootElt
     * @param propertyNames
     * @param typeMap
     * @throws SchemaCompilationException
     */
    private void populateInfo(BeanWriterMetaInfoHolder metainf,
                              Document model,
                              Element rootElt,
                              ArrayList<String> propertyNames,
                              Map<QName,String> typeMap,
                              Map<QName,String> groupTypeMap,
                              boolean isInherited) throws SchemaCompilationException {
        // we should add parent class details only if it is
        // an extension or simple restriction
        // should not in complex restrictions
        if (metainf.getParent() != null && (!metainf.isRestriction() || (metainf.isRestriction() && metainf.isSimple())))
        {
            populateInfo(metainf.getParent(), model, rootElt, propertyNames,
                    typeMap, groupTypeMap, true);
        }
        addPropertyEntries(metainf, model, rootElt, propertyNames, typeMap, groupTypeMap,
                isInherited);

    }

    /**
     * @param metainf
     * @param model
     * @param rootElt
     * @param propertyNames
     * @param typeMap
     * @throws SchemaCompilationException
     */
    private void addPropertyEntries(BeanWriterMetaInfoHolder metainf,
                                    Document model, Element rootElt,
                                    ArrayList<String> propertyNames,
                                    Map<QName,String> typeMap,
                                    Map<QName,String> groupTypeMap,
                                    boolean isInherited) throws SchemaCompilationException {
        // go in the loop and add the part elements
        QName[] qName;
        String javaClassNameForElement;
        ArrayList<QName> missingQNames = new ArrayList<QName>();
        ArrayList<QName> qNames = new ArrayList<QName>();

        BeanWriterMetaInfoHolder parentMetaInf = metainf.getParent();

        if (metainf.isOrdered()) {
            qName = metainf.getOrderedQNameArray();
        } else {
            qName = metainf.getQNameArray();
        }
        
        for (int i = 0; i < qName.length; i++) {
            qNames.add(qName[i]);
        }
        
        //adding missing QNames to the end, including elements & attributes.
        // for the simple types we have already add the parent elements
        // it is almost consider as an extension
        if (metainf.isRestriction() && !metainf.isSimple()) {
            addMissingQNames(metainf, qNames, missingQNames);
        }
        
    	List<BeanWriterMetaInfoHolder> parents=new ArrayList<BeanWriterMetaInfoHolder>();
    	BeanWriterMetaInfoHolder immediateParent=metainf.getParent();
    	while(immediateParent != null){
    		parents.add(immediateParent);
    		immediateParent=immediateParent.getParent();
    	}
        
        for (QName name : qNames) {
            Element property = XSLTUtils.addChildElement(model, "property", rootElt);

            String xmlName = name.getLocalPart();
            
			String xmlNameNew=identifyUniqueNameForQName(parents,xmlName, metainf, name,name);
			while(!xmlName.equalsIgnoreCase(xmlNameNew)){
				xmlName=xmlNameNew;
				xmlNameNew=identifyUniqueNameForQName(parents,xmlNameNew, metainf, name,new QName(xmlNameNew));
			}
            
            XSLTUtils.addAttribute(model, "name", xmlName, property);
            XSLTUtils.addAttribute(model, "nsuri", name.getNamespaceURI(), property);
            

            String javaName;
            if (metainf.isJavaNameMappingAvailable(xmlName)) {
                javaName = metainf.getJavaName(xmlName);
            } else {
                javaName = makeUniqueJavaClassName(propertyNames, xmlName);
                // in a restriction if this element already there and array status have changed
                // then we have to generate a new  name for this
                if (parentMetaInf != null && metainf.isRestriction() && !missingQNames.contains(name) &&
                        (parentMetaInf.getArrayStatusForQName(name) && !metainf.getArrayStatusForQName(name))) {
                    javaName = makeUniqueJavaClassName(propertyNames, xmlName);
                }
                metainf.addXmlNameJavaNameMapping(xmlName,javaName);
            }

            XSLTUtils.addAttribute(model, "javaname", javaName, property);

            if (parentMetaInf != null && metainf.isRestriction() && missingQNames.contains(name)) {
                javaClassNameForElement = parentMetaInf.getClassNameForQName(name);
            } else {
                javaClassNameForElement = metainf.getClassNameForQName(name);
            }


            if (javaClassNameForElement == null) {
                javaClassNameForElement = getDefaultClassName();
                log.warn(SchemaCompilerMessages
                        .getMessage("schema.typeMissing", name.toString()));
            }

            if (metainf.isRestriction() && typeChanged(name, missingQNames, metainf)) {
                XSLTUtils.addAttribute(model, "typeChanged", "yes", property);
                //XSLTUtils.addAttribute(model, "restricted", "yes", property);
            }

            long minOccurs = metainf.getMinOccurs(name);
            if (PrimitiveTypeFinder.isPrimitive(javaClassNameForElement)
                    && isUseWrapperClasses && ((minOccurs == 0) || metainf.isNillable(name))) {
                 // if this is an primitive class and user wants to use the
                 // wrapper type we change the type to wrapper type.
                 javaClassNameForElement = PrimitiveTypeWrapper.getWrapper(javaClassNameForElement);
            }

            XSLTUtils.addAttribute(model, "type", javaClassNameForElement, property);


            if (PrimitiveTypeFinder.isPrimitive(javaClassNameForElement)) {

                XSLTUtils.addAttribute(model, "primitive", "yes", property);
            }

             // add the default value
            if (metainf.isDefaultValueAvailable(name)){
                QName schemaQName = metainf.getSchemaQNameForQName(name);
                if (baseTypeMap.containsKey(schemaQName)){
                    XSLTUtils.addAttribute(model, "defaultValue",
                            metainf.getDefaultValueForQName(name), property);
                }
            }
            
            
            //in the case the original element is an array but the derived one is not.
            if (parentMetaInf != null && metainf.isRestriction() && !missingQNames.contains(name) &&
                    (parentMetaInf.getArrayStatusForQName(name) && !metainf.getArrayStatusForQName(name))) {

                XSLTUtils.addAttribute(model, "rewrite", "yes", property);
                XSLTUtils.addAttribute(model, "occuranceChanged", "yes", property);
            } else if (metainf.isRestriction() && !missingQNames.contains(name) &&
                    (minOccursChanged(name, missingQNames, metainf) || maxOccursChanged(name, missingQNames, metainf)))
            {

                XSLTUtils.addAttribute(model, "restricted", "yes", property);
                XSLTUtils.addAttribute(model, "occuranceChanged", "yes", property);
            }

            // set the is particle class
            if (metainf.getParticleTypeStatusForQName(name)){
                XSLTUtils.addAttribute(model, "particleClassType", "yes", property);
            }

            // if we have an particle class in a extension class then we have
            // to consider the whole class has a particle type.

            if (metainf.isHasParticleType()) {
                XSLTUtils.addAttribute(model, "hasParticleType", "yes", rootElt);
            }

            // what happed if this contain attributes
            // TODO: check the meaning of this removed property

            if (metainf.isRestriction() && missingQNames.contains(name) && !metainf.isSimple()) {
                //XSLTUtils.addAttribute(model, "restricted", "yes", property);
                XSLTUtils.addAttribute(model, "removed", "yes", property);
            }

            if (isInherited) {
                XSLTUtils.addAttribute(model, "inherited", "yes", property);
            }

            if (metainf.getInnerChoiceStatusForQName(name)){
                XSLTUtils.addAttribute(model, "innerchoice", "yes", property);
            }



            if ((parentMetaInf != null) && metainf.isRestriction() && missingQNames.contains(name)) {
                // this element details should be there with the parent meta Inf
                addAttributesToProperty(
                        parentMetaInf,
                        name,
                        model,
                        property,
                        typeMap,
                        groupTypeMap,
                        javaClassNameForElement);

            } else {
                addAttributesToProperty(
                        metainf,
                        name,
                        model,
                        property,
                        typeMap,
                        groupTypeMap,
                        javaClassNameForElement);
            }
        }  // end of foo
    }

    private void addAttributesToProperty(BeanWriterMetaInfoHolder metainf,
                                         QName name,
                                         Document model,
                                         Element property,
                                         Map<QName,String> typeMap,
                                         Map<QName,String> groupTypeMap,
                                         String javaClassNameForElement) {
        // add an attribute that says the type is default
        if (metainf.getDefaultStatusForQName(name)) {
            XSLTUtils.addAttribute(model, "default", "yes", property);
        }

        if (typeMap.containsKey(metainf.getSchemaQNameForQName(name)) ||
                groupTypeMap.containsKey(metainf.getSchemaQNameForQName(name))) {
            XSLTUtils.addAttribute(model, "ours", "yes", property);
        }

        if (metainf.getAttributeStatusForQName(name)) {
            XSLTUtils.addAttribute(model, "attribute", "yes", property);
        }

        if (metainf.isNillable(name)) {
            XSLTUtils.addAttribute(model, "nillable", "yes", property);
        }

        if (metainf.getOptionalAttributeStatusForQName(name)) {
            XSLTUtils.addAttribute(model, "optional", "yes", property);
        }

        String shortTypeName;
        if (metainf.getSchemaQNameForQName(name) != null) {
            // see whether the QName is a basetype
            if (baseTypeMap.containsKey(metainf.getSchemaQNameForQName(name))) {
                shortTypeName = metainf.getSchemaQNameForQName(name).getLocalPart();
            } else {
                shortTypeName = getShortTypeName(javaClassNameForElement);
            }
        } else {
            shortTypeName = getShortTypeName(javaClassNameForElement);
        }
        XSLTUtils.addAttribute(model, "shorttypename", shortTypeName, property);


        if (metainf.getAnyStatusForQName(name)) {
            XSLTUtils.addAttribute(model, "any", "yes", property);
        }

        if (metainf.getBinaryStatusForQName(name)) {
            XSLTUtils.addAttribute(model, "binary", "yes", property);
        }

        if (metainf.isSimple() || metainf.getSimpleStatusForQName(name)) {
            XSLTUtils.addAttribute(model, "simple", "yes", property);
        }

        // put the min occurs count irrespective of whether it's an array or
        // not
        long minOccurs = metainf.getMinOccurs(name);
        XSLTUtils.addAttribute(model, "minOccurs", minOccurs + "", property);


        if (metainf.getArrayStatusForQName(name)) {

            XSLTUtils.addAttribute(model, "array", "yes", property);

            int endIndex = javaClassNameForElement.indexOf("[");
            if (endIndex >= 0) {
                XSLTUtils.addAttribute(model, "arrayBaseType",
                        javaClassNameForElement.substring(0, endIndex), property);
            } else {
                XSLTUtils.addAttribute(model, "arrayBaseType",
                        javaClassNameForElement, property);
            }

            long maxOccurs = metainf.getMaxOccurs(name);
            if (maxOccurs == Long.MAX_VALUE) {
                XSLTUtils.addAttribute(model, "unbound", "yes", property);
            } else {
                XSLTUtils.addAttribute(model, "maxOccurs", maxOccurs + "", property);
            }
        }
        if (metainf.isRestrictionBaseType(name)) {
            XSLTUtils.addAttribute(model, "restrictionBaseType", "yes", property);
        }

        if (metainf.isExtensionBaseType(name)) {
            XSLTUtils.addAttribute(model, "extensionBaseType", "yes", property);
        }

        if (metainf.isRestrictionBaseType(name) && metainf.getLengthFacet() != -1) {
            XSLTUtils.addAttribute(model, "lenFacet", metainf.getLengthFacet() + "", property);
        }

        if (metainf.isRestrictionBaseType(name) && metainf.getMaxLengthFacet() != -1) {
            XSLTUtils.addAttribute(model, "maxLenFacet", metainf.getMaxLengthFacet() + "", property);
        }

        if (metainf.isRestrictionBaseType(name) && metainf.getMinLengthFacet() != -1) {
            XSLTUtils.addAttribute(model, "minLenFacet", metainf.getMinLengthFacet() + "", property);
        }

        if (metainf.isRestrictionBaseType(name) && metainf.getMaxExclusiveFacet() != null) {
            XSLTUtils.addAttribute(model, "maxExFacet", metainf.getMaxExclusiveFacet() + "", property);
        }

        if (metainf.isRestrictionBaseType(name) && metainf.getMinExclusiveFacet() != null) {
            XSLTUtils.addAttribute(model, "minExFacet", metainf.getMinExclusiveFacet() + "", property);
        }

        if (metainf.isRestrictionBaseType(name) && metainf.getMaxInclusiveFacet() != null) {
            XSLTUtils.addAttribute(model, "maxInFacet", metainf.getMaxInclusiveFacet() + "", property);
        }

        if (metainf.isRestrictionBaseType(name) && metainf.getMinInclusiveFacet() != null) {
            XSLTUtils.addAttribute(model, "minInFacet", metainf.getMinInclusiveFacet() + "", property);
        }

        if (!metainf.getEnumFacet().isEmpty()) {
            boolean validJava = true;    // Assume all enum values are valid ids

            // Walk the values looking for invalid ids
            for (String value : metainf.getEnumFacet()) {
                if (!JavaUtils.isJavaId(value)) {
                    validJava = false;
                }
            }

            int id = 0;
            for (String attribValue : metainf.getEnumFacet()) {
                Element enumFacet = XSLTUtils.addChildElement(model, "enumFacet", property);
                XSLTUtils.addAttribute(model, "value", attribValue, enumFacet);
                if (validJava) {
                    XSLTUtils.addAttribute(model, "id", attribValue, enumFacet);
                } else {
                    id++;
                    XSLTUtils.addAttribute(model, "id", "value" + id, enumFacet);
                }
            }
        }

        if (metainf.isRestrictionBaseType(name) && metainf.getPatternFacet() != null) {
            XSLTUtils.addAttribute(model, "patternFacet", metainf.getPatternFacet(), property);
        }
    }

    private void addMissingQNames(BeanWriterMetaInfoHolder metainf, ArrayList<QName> qName, ArrayList<QName> missingQNames) {

        QName[] qNames = null;
        QName[] pQNames = null;

        BeanWriterMetaInfoHolder parentMetaInf = metainf.getParent();

        if (metainf.isOrdered()) {
            qNames = metainf.getOrderedQNameArray();
        } else {
            qNames = metainf.getQNameArray();
        }

        if (parentMetaInf != null) {
            if (parentMetaInf.isOrdered()) {
                pQNames = parentMetaInf.getOrderedQNameArray();
            } else {
                pQNames = parentMetaInf.getQNameArray();
            }
        }


        for (int i = 0; pQNames != null && i < pQNames.length; i++) {
            if (qNameNotFound(pQNames[i], metainf)) {
                missingQNames.add(pQNames[i]);
            }
        }
        //adding missing QNames to the end of list.
        if (!missingQNames.isEmpty()) {
            for (int i = 0; i < missingQNames.size(); i++) {
                qName.add(missingQNames.get(i));
            }
        }

    }

    private boolean qNameNotFound(QName qname, BeanWriterMetaInfoHolder metainf) {

        boolean found = false;
        QName[] qNames;

        if (metainf.isOrdered()) {
            qNames = metainf.getOrderedQNameArray();
        } else {
            qNames = metainf.getQNameArray();
        }

        for (int j = 0; j < qNames.length; j++) {
            if (qname.getLocalPart().equals(qNames[j].getLocalPart())) {
                found = true;
            }
        }
        return !found;
    }

    private boolean typeChanged(QName qname, ArrayList<QName> missingQNames, BeanWriterMetaInfoHolder metainf) {

        boolean typeChanged = false;
        QName[] pQNames;

        BeanWriterMetaInfoHolder parentMetainf = metainf.getParent();

        if (parentMetainf != null && !missingQNames.contains(qname)) {

            if (parentMetainf.isOrdered()) {
                pQNames = parentMetainf.getOrderedQNameArray();
            } else {
                pQNames = parentMetainf.getQNameArray();
            }

            for (int j = 0; j < pQNames.length; j++) {
                if (qname.getLocalPart().equals(pQNames[j].getLocalPart())) {

                    String javaClassForParentElement = parentMetainf.getClassNameForQName(pQNames[j]);
                    String javaClassForElement = metainf.getClassNameForQName(qname);

                    if (!javaClassForParentElement.equals(javaClassForElement)) {
                        if (javaClassForParentElement.endsWith("[]")) {
                            if ((javaClassForParentElement.substring(0, javaClassForParentElement.indexOf('['))).equals(javaClassForElement))
                            {
                                continue;
                            }
                        } else if (javaClassForElement.endsWith("[]")) {
                            if ((javaClassForElement.substring(0, javaClassForElement.indexOf('['))).equals(javaClassForParentElement))
                            {
                                continue;
                            }
                        } else {
                            typeChanged = true;
                        }
                    }
                }
            }
        }
        return typeChanged;
    }

    private boolean minOccursChanged(QName qname, ArrayList<QName> missingQNames, BeanWriterMetaInfoHolder metainf) throws SchemaCompilationException {

        boolean minChanged = false;
        QName[] pQNames;

        BeanWriterMetaInfoHolder parentMetainf = metainf.getParent();

        if (parentMetainf != null && !missingQNames.contains(qname)) {

            if (parentMetainf.isOrdered()) {
                pQNames = parentMetainf.getOrderedQNameArray();
            } else {
                pQNames = parentMetainf.getQNameArray();
            }

            for (int j = 0; j < pQNames.length; j++) {
                if (qname.getLocalPart().equals(pQNames[j].getLocalPart())) {

                    if (metainf.getMinOccurs(qname) > parentMetainf.getMinOccurs(pQNames[j])) {
                        minChanged = true;
                    } else if (metainf.getMinOccurs(qname) < parentMetainf.getMinOccurs(pQNames[j])) {
                        throw new SchemaCompilationException(SchemaCompilerMessages.getMessage("minOccurs Wrong!"));
                    }

                }
            }
        }
        return minChanged;
    }

    private boolean maxOccursChanged(QName qname, ArrayList<QName> missingQNames, BeanWriterMetaInfoHolder metainf) throws SchemaCompilationException {

        boolean maxChanged = false;
        QName[] pQNames;

        BeanWriterMetaInfoHolder parentMetainf = metainf.getParent();

        if (parentMetainf != null && !missingQNames.contains(qname)) {
            if (parentMetainf.isOrdered()) {
                pQNames = parentMetainf.getOrderedQNameArray();
            } else {
                pQNames = parentMetainf.getQNameArray();
            }

            for (int j = 0; j < pQNames.length; j++) {
                if (qname.getLocalPart().equals(pQNames[j].getLocalPart())) {

                    if (metainf.getMaxOccurs(qname) < parentMetainf.getMaxOccurs(pQNames[j])) {
                        maxChanged = true;
                    } else if (metainf.getMaxOccurs(qname) > parentMetainf.getMaxOccurs(pQNames[j])) {
                        throw new SchemaCompilationException(SchemaCompilerMessages.getMessage("maxOccurs Wrong!"));
                    }
                }
            }
        }
        return maxChanged;
    }

    /**
     * Test whether the given class name matches the default
     *
     * @param javaClassNameForElement
     */
    private boolean isDefault(String javaClassNameForElement) {
        return getDefaultClassName()
                .equals(javaClassNameForElement)
                || getDefaultClassArrayName()
                .equals(javaClassNameForElement);
    }

    /**
     * Given the xml name, make a unique class name taking into account that
     * some file systems are case sensitive and some are not. -Consider the
     * Jax-WS spec for this
     *
     * @param listOfNames
     * @param xmlName
     * @return Returns String.
     */
    private String makeUniqueJavaClassName(List<String> listOfNames, String xmlName) {
        String javaName;
        if (JavaUtils.isJavaKeyword(xmlName)) {
            javaName = JavaUtils.makeNonJavaKeyword(xmlName);
        } else {
            javaName = JavaUtils.capitalizeFirstChar(JavaUtils
                    .xmlNameToJava(xmlName));
        }

        while (listOfNames.contains(javaName.toLowerCase())) {
            if (!listOfNames.contains((javaName + "E").toLowerCase())){
                javaName = javaName + "E";
            } else {
                javaName = javaName + count++;
            }
        }

        listOfNames.add(javaName.toLowerCase());
        return javaName;
    }

    /**
     * A bit of code from the old code generator. We are better off using the
     * template engines and such stuff that's already there. But the class
     * writers are hard to be reused so some code needs to be repeated (atleast
     * a bit)
     */
    private void loadTemplate() throws SchemaCompilationException {

        // first get the language specific property map
        Class<?> clazz = this.getClass();
        String templateName = javaBeanTemplateName;
        if (templateName != null) {
            try {
                // Use URL instead of InputStream here, so that the processor may resolve
                // imports/includes with relative hrefs.
                URL xsl = clazz.getResource(templateName);
                templateCache = TransformerFactory.newInstance().newTemplates(
                        new StreamSource(xsl.toExternalForm()));
                templateLoaded = true;
            } catch (TransformerConfigurationException e) {
                throw new SchemaCompilationException(SchemaCompilerMessages
                        .getMessage("schema.templateLoadException"), e);
            }
        } else {
            throw new SchemaCompilationException(SchemaCompilerMessages
                    .getMessage("schema.templateNotFoundException"));
        }
    }

    /**
     * Creates the output file
     *
     * @param packageName
     * @param fileName
     * @throws Exception
     */
    private File createOutFile(String packageName, String fileName)
            throws Exception {
        return org.apache.axis2.util.FileWriter.createClassFile(this.rootDir,
                packageName, fileName, ".java");
    }

    /**
     * Writes the output file
     *
     * @param doc
     * @param outputFile
     * @throws Exception
     */
    private void parse(Document doc, File outputFile) throws Exception {
        OutputStream outStream = new FileOutputStream(outputFile);
        XSLTTemplateProcessor.parse(outStream, doc, getTransformer());
        outStream.flush();
        outStream.close();

        PrettyPrinter.prettify(outputFile);
    }

    private Transformer getTransformer() throws TransformerConfigurationException, SchemaCompilationException {
        try {
            return this.templateCache
                    .newTransformer();
        } catch (Exception e){
            // Under some peculiar conditions (classloader issues), just scrap the old templateCache,
            // create a new one and try again.
            loadTemplate();
            return this.templateCache
                    .newTransformer();
        }
    }

    /**
     * Get a prefix for a namespace URI. This method will ALWAYS return a valid
     * prefix - if the given URI is already mapped in this serialization, we
     * return the previous prefix. If it is not mapped, we will add a new
     * mapping and return a generated prefix of the form "ns<num>".
     *
     * @param uri is the namespace uri
     * @return Returns prefix.
     */
    public String getPrefixForURI(String uri) {
        return getPrefixForURI(uri, null);
    }

    /**
     * Last used index suffix for "ns"
     */
    private int lastPrefixIndex = 1;

    /**
     * Map of namespaces URI to prefix(es)
     */
    HashMap<String,String> mapURItoPrefix = new HashMap<String,String>();

    HashMap<String,String> mapPrefixtoURI = new HashMap<String,String>();

    /**
     * Get a prefix for the given namespace URI. If one has already been defined
     * in this serialization, use that. Otherwise, map the passed default prefix
     * to the URI, and return that. If a null default prefix is passed, use one
     * of the form "ns<num>"
     */
    public String getPrefixForURI(String uri, String defaultPrefix) {
        if ((uri == null) || (uri.length() == 0))
            return null;
        String prefix = mapURItoPrefix.get(uri);
        if (prefix == null) {
            if (defaultPrefix == null || defaultPrefix.length() == 0) {
                prefix = "ns" + lastPrefixIndex++;
                while (mapPrefixtoURI.get(prefix) != null) {
                    prefix = "ns" + lastPrefixIndex++;
                }
            } else {
                prefix = defaultPrefix;
            }
            mapPrefixtoURI.put(prefix, uri);
            mapURItoPrefix.put(uri, prefix);
        }
        return prefix;
    }

    private String getShortTypeName(String typeClassName) {
        if (typeClassName.endsWith("[]")) {
            typeClassName = typeClassName.substring(0, typeClassName
                    .lastIndexOf("["));
        }

        return typeClassName.substring(typeClassName.lastIndexOf(".") + 1,
                typeClassName.length());

    }

    /**
     * Get the mapper class name - there is going to be only one
     * mapper class for the whole
     */
    private String getFullyQualifiedMapperClassName() {
        if (wrapClasses || !writeClasses || mappingClassPackage == null) {
            return EXTENSION_MAPPER_CLASSNAME;
        } else {
            return mappingClassPackage + "." + EXTENSION_MAPPER_CLASSNAME;
        }
    }

    /**
     * get the mapper class package name
     * May be ignored by the implementer
     */
    public String getExtensionMapperPackageName() {
        return mappingClassPackage;
    }

    /**
     * Sets the mapping class name of this writer. A mapping class
     * package set by the options may be overridden at the this point
     *
     * @param mapperPackageName
     */
    public void registerExtensionMapperPackageName(String mapperPackageName) {
        this.mappingClassPackage = mapperPackageName;
    }

    /**
     * Write the extension classes - this is needed to process
     * the hierarchy of classes
     *
     * @param metainfArray
     */
    public void writeExtensionMapper(BeanWriterMetaInfoHolder[] metainfArray) throws SchemaCompilationException {
        //generate the element
        try {


            String mapperClassName = getFullyQualifiedMapperClassName();

            Document model = XSLTUtils.getDocument();
            Element rootElt = XSLTUtils.getElement(model, "mapper");
            String mapperName = mapperClassName.substring(mapperClassName.lastIndexOf(".") + 1);
            XSLTUtils.addAttribute(model, "name", mapperName, rootElt);
            String basePackageName = "";
            if (mapperClassName.indexOf(".") != -1) {
                basePackageName = mapperClassName.substring(0, mapperClassName.lastIndexOf("."));
                XSLTUtils.addAttribute(model, "package", basePackageName, rootElt);
            } else {
                XSLTUtils.addAttribute(model, "package", "", rootElt);
            }

            if (!wrapClasses) {
                XSLTUtils.addAttribute(model, "unwrapped", "yes", rootElt);
            }

            if (!writeClasses) {
                XSLTUtils.addAttribute(model, "skip-write", "yes", rootElt);
            }

            if (isHelperMode) {
                XSLTUtils.addAttribute(model, "helpermode", "yes", rootElt);
            }

            for (int i = 0; i < metainfArray.length; i++) {
                QName ownQname = metainfArray[i].getOwnQname();
                String className = metainfArray[i].getOwnClassName();
                //do  not add when the qname is not availble
                if (ownQname != null) {
                    Element typeChild = XSLTUtils.addChildElement(model, "type", rootElt);
                    XSLTUtils.addAttribute(model, "nsuri", ownQname.getNamespaceURI(), typeChild);
                    XSLTUtils.addAttribute(model, "classname", className == null ? "" : className, typeChild);
                    XSLTUtils.addAttribute(model, "shortname", ownQname == null ? "" :
                            ownQname.getLocalPart(), typeChild);
                }
            }

            model.appendChild(rootElt);

            if (!templateLoaded) {
                loadTemplate();
            }

            if (wrapClasses) {
                rootElt = (Element) globalWrappedDocument.importNode(rootElt, true);
                //add to the global wrapped document
                globalWrappedDocument.getDocumentElement().appendChild(rootElt);
            } else {
                if (writeClasses) {
                    // create the file
                    File out = createOutFile(basePackageName, mapperName);
                    // parse with the template and create the files
                    parse(model, out);

                }

                // add the model to the model map
                modelMap.put(new QName(mapperName), model);
            }

        } catch (ParserConfigurationException e) {
            throw new SchemaCompilationException(SchemaCompilerMessages.getMessage("schema.docuement.error"), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SchemaCompilationException(e);
        }


    }
    
    /**
     * This method is used to generate a unique name for a given QName and a metainf.
     * First we check the parents whether they have the given QName, If yes, then we check the java type for QName.
     * If they are similar, no issue. If they are different, java name has to be changed. 
     * After changing also, we need to check whether the changed name contains in the parentinfs.
     * @param parents
     * @param xmlName
     * @param metainf
     * @param original
     * @param modified
     * @return
     */
    private String identifyUniqueNameForQName(List<BeanWriterMetaInfoHolder> parents, String xmlName,BeanWriterMetaInfoHolder metainf,QName original,QName modified){

    	int count=0;
		for (BeanWriterMetaInfoHolder beanWriterMetaInfoHolder : parents) {
			QName[] pQNmame = null;
			if (beanWriterMetaInfoHolder.isOrdered()) {
				pQNmame = beanWriterMetaInfoHolder.getOrderedQNameArray();
			} else {
				pQNmame = beanWriterMetaInfoHolder.getQNameArray();
			}

			List<QName> pQNameList = null;
			if (pQNmame != null) {
				pQNameList = Arrays.asList(pQNmame);
			}
			
			if (pQNameList != null
					&& pQNameList.contains(modified)
					&& metainf.getClassNameForQName(original) != null
					&& !metainf.getClassNameForQName(original).equalsIgnoreCase(
							beanWriterMetaInfoHolder.getClassNameForQName(modified))) {
				xmlName += count;
				break;
			}
			count++;
		}
    	return xmlName;
    }

}