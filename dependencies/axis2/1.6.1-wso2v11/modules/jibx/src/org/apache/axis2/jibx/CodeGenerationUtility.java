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

package org.apache.axis2.jibx;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.extension.JiBXExtension;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.util.Constants;
import org.apache.axis2.wsdl.util.MessagePartInformationHolder;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.jibx.binding.model.BindingElement;
import org.jibx.binding.model.ElementBase;
import org.jibx.binding.model.FormatElement;
import org.jibx.binding.model.IncludeElement;
import org.jibx.binding.model.MappingElementBase;
import org.jibx.binding.model.ModelVisitor;
import org.jibx.binding.model.NamespaceElement;
import org.jibx.binding.model.ValidationContext;
import org.jibx.binding.model.ValidationProblem;
import org.jibx.runtime.JiBXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Framework-linked code used by JiBX data binding support. This is accessed via reflection from the
 * JiBX code generation extension when JiBX data binding is selected. JiBX uses a different approach
 * to unwrapping method parameters from that implemented by ADB, and since the ADB technique is
 * assumed by all code generation templates this has to create the same data structures. These data
 * structures are undocumented, and can only be determined by going through the {@link
 * org.apache.axis2.wsdl.codegen.extension.SchemaUnwrapperExtension} and {@link
 * org.apache.axis2.wsdl.codegen.emitter.AxisServiceBasedMultiLanguageEmitter} code.
 */
public class CodeGenerationUtility {
    private static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    private final CodeGenConfiguration codeGenConfig;

    private static HashSet s_primitiveSet = new HashSet();

    static {
        s_primitiveSet.add("boolean");
        s_primitiveSet.add("byte");
        s_primitiveSet.add("char");
        s_primitiveSet.add("double");
        s_primitiveSet.add("float");
        s_primitiveSet.add("int");
        s_primitiveSet.add("long");
        s_primitiveSet.add("short");
        s_primitiveSet.add("void");
    }

    private static HashMap s_wrapperMap = new HashMap();

    static {
        s_wrapperMap.put("boolean", "Boolean");
        s_wrapperMap.put("byte", "Byte");
        s_wrapperMap.put("char", "Character");
        s_wrapperMap.put("double", "Double");
        s_wrapperMap.put("float", "Float");
        s_wrapperMap.put("int", "Integer");
        s_wrapperMap.put("long", "Long");
        s_wrapperMap.put("short", "Short");
    }

    /** Reserved words for Java (keywords and literals). */
    private static final HashSet s_reservedWords = new HashSet();

    static {

        // keywords
        s_reservedWords.add("abstract");
        s_reservedWords.add("assert");
        s_reservedWords.add("boolean");
        s_reservedWords.add("break");
        s_reservedWords.add("byte");
        s_reservedWords.add("case");
        s_reservedWords.add("catch");
        s_reservedWords.add("char");
        s_reservedWords.add("class");
        s_reservedWords.add("const");
        s_reservedWords.add("continue");

        s_reservedWords.add("default");
        s_reservedWords.add("do");
        s_reservedWords.add("double");
        s_reservedWords.add("else");
        s_reservedWords.add("enum");
        s_reservedWords.add("extends");
        s_reservedWords.add("final");
        s_reservedWords.add("finally");
        s_reservedWords.add("float");
        s_reservedWords.add("for");
        s_reservedWords.add("goto");

        s_reservedWords.add("if");
        s_reservedWords.add("implements");
        s_reservedWords.add("import");
        s_reservedWords.add("instanceof");
        s_reservedWords.add("int");
        s_reservedWords.add("interface");
        s_reservedWords.add("long");
        s_reservedWords.add("native");
        s_reservedWords.add("new");
        s_reservedWords.add("package");

        s_reservedWords.add("private");
        s_reservedWords.add("protected");
        s_reservedWords.add("public");
        s_reservedWords.add("return");
        s_reservedWords.add("short");
        s_reservedWords.add("static");
        s_reservedWords.add("strictfp");
        s_reservedWords.add("super");
        s_reservedWords.add("switch");
        s_reservedWords.add("synchronized");

        s_reservedWords.add("this");
        s_reservedWords.add("throw");
        s_reservedWords.add("throws");
        s_reservedWords.add("transient");
        s_reservedWords.add("try");
        s_reservedWords.add("void");
        s_reservedWords.add("volatile");
        s_reservedWords.add("while");

        // literals
        s_reservedWords.add("true");
        s_reservedWords.add("false");
        s_reservedWords.add("null");

        // variable names used by template unwrapped code generation
        // TODO change templates to use $xxx names, block generation from WSDL
        s_reservedWords.add("true");
        s_reservedWords.add("uctx");
        s_reservedWords.add("child");
        s_reservedWords.add("wrapper");
    }

    /**
     * Constructor.
     *
     * @param config code generation configuration
     */
    public CodeGenerationUtility(CodeGenConfiguration config) {
        codeGenConfig = config;
    }

    /**
     * Configure the code generation based on the supplied parameters and WSDL. This first gets type
     * mappings from binding definition, then goes through the operations checking the input and
     * output messages. If unwrapping is disabled the message element must be handled directly by a
     * mapping. If unwrapping is enabled, this checks that the message element is of the proper form
     * (a sequence of other elements, which all have maxOccurs="1"). It then generates an unwrapping
     * description and adds it to the code generation configuration, where it'll be picked up and
     * included in the XML passed to code generation. This also constructs a type mapping, since
     * that's required by the base Axis2 code generation. In the case of unwrapped elements, the
     * type mapping includes a synthesized qname for each unwrapped parameter, and the detailed
     * information is set on the message information. Sound confusing? Welcome to Axis2 code
     * generation.
     *
     * @param path binding path (<code>null</code> if none)
     */
    public void engage(String path) {

        // make sure the binding definition file is present, if passed
        File file = null;
        if (path != null) {
            file = new File(path);
            if (!file.exists()) {
                throw new RuntimeException("jibx binding definition file " + path + " not found");
            }
        }
        try {

            // set flag for unwrapping
            boolean unwrap = !codeGenConfig.isParametersWrapped();

            // initialize the binding information
            BindingElement binding = null;
            if (file == null) {

                // unwrapped can be used without a binding, but wrapped requires one
                if (!unwrap) {
                    throw new RuntimeException(
                            "JiBX wrapped support requires a binding definition to be provided using the -E" +
                                    JiBXExtension.BINDING_PATH_OPTION + " {file-path} parameter");
                }

            } else {

                // Read the JiBX binding definition into memory. The binding definition
                // is only prevalidated so as not to require the user to have all
                // the referenced classes in the classpath, though this does make for
                // added work in finding the namespaces.
                ValidationContext vctx = BindingElement.newValidationContext();
                binding = BindingElement.readBinding(new FileInputStream(file), path, vctx);
                binding.setBaseUrl(file.toURL());
                vctx.setBindingRoot(binding);
                IncludePrevalidationVisitor ipv = new IncludePrevalidationVisitor(vctx);
                vctx.tourTree(binding, ipv);
                if (vctx.getErrorCount() != 0 || vctx.getFatalCount() != 0) {
                    ArrayList probs = vctx.getProblems();
                    System.err.println("Errors in generated binding:");
                    for (int j = 0; j < probs.size(); j++) {
                        ValidationProblem prob = (ValidationProblem)probs.get(j);
                        System.err.print(prob.getSeverity() >=
                                ValidationProblem.ERROR_LEVEL ? "Error: " : "Warning: ");
                        System.err.println(prob.getDescription());
                    }
                    throw new RuntimeException("invalid jibx binding definition file " + path);
                }
            }

            // create table with all built-in format definitions
            Map simpleTypeMap = new HashMap();
            buildFormat("byte", "byte",
                        "org.jibx.runtime.Utility.serializeByte",
                        "org.jibx.runtime.Utility.parseByte", "0", simpleTypeMap);
            buildFormat("unsignedShort", "char",
                        "org.jibx.runtime.Utility.serializeChar",
                        "org.jibx.runtime.Utility.parseChar", "0", simpleTypeMap);
            buildFormat("double", "double",
                        "org.jibx.runtime.Utility.serializeDouble",
                        "org.jibx.runtime.Utility.parseDouble", "0.0", simpleTypeMap);
            buildFormat("float", "float",
                        "org.jibx.runtime.Utility.serializeFloat",
                        "org.jibx.runtime.Utility.parseFloat", "0.0", simpleTypeMap);
            buildFormat("int", "int",
                        "org.jibx.runtime.Utility.serializeInt",
                        "org.jibx.runtime.Utility.parseInt", "0", simpleTypeMap);
            buildFormat("long", "long",
                        "org.jibx.runtime.Utility.serializeLong",
                        "org.jibx.runtime.Utility.parseLong", "0", simpleTypeMap);
            buildFormat("short", "short",
                        "org.jibx.runtime.Utility.serializeShort",
                        "org.jibx.runtime.Utility.parseShort", "0", simpleTypeMap);
            buildFormat("boolean", "boolean",
                        "org.jibx.runtime.Utility.serializeBoolean",
                        "org.jibx.runtime.Utility.parseBoolean", "false",
                        simpleTypeMap);
            buildFormat("dateTime", "java.util.Date",
                        "org.jibx.runtime.Utility.serializeDateTime",
                        "org.jibx.runtime.Utility.deserializeDateTime", null,
                        simpleTypeMap);
            buildFormat("date", "java.sql.Date",
                        "org.jibx.runtime.Utility.serializeSqlDate",
                        "org.jibx.runtime.Utility.deserializeSqlDate", null,
                        simpleTypeMap);
            buildFormat("time", "java.sql.Time",
                        "org.jibx.runtime.Utility.serializeSqlTime",
                        "org.jibx.runtime.Utility.deserializeSqlTime", null,
                        simpleTypeMap);
            buildFormat("base64Binary", "byte[]",
                        "org.jibx.runtime.Utility.serializeBase64",
                        "org.jibx.runtime.Utility.deserializeBase64", null,
                        simpleTypeMap);
            buildFormat("string", "java.lang.String", null, null, null,
                        simpleTypeMap);

            // collect all the top-level mapping and format definitions
            Map elementMap = new HashMap();
            Map complexTypeMap = new HashMap();
            Map bindingMap = new HashMap();
            if (binding != null) {
                collectTopLevelComponents(binding, "", elementMap,
                                          complexTypeMap, simpleTypeMap, bindingMap);
            }

            // make sure classes will be generated for abstract mappings
            if (unwrap && complexTypeMap.size() > 0 && (binding == null || !binding.isForceClasses())) {
                throw new RuntimeException(
                        "unwrapped binding must use force-classes='true' option in " + path);
            }

            // force off inappropriate option (set by error in options handling)
            codeGenConfig.setPackClasses(false);

            // configure handling for all operations of service
            codeGenConfig.setTypeMapper(new NamedParameterTypeMapper());
            Iterator operations = codeGenConfig.getAxisService().getOperations();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            int opindex = 0;
            Map typeMappedClassMap = new HashMap();
            String mappedclass = null;
            Set objins = new HashSet();
            Set objouts = new HashSet();
            Set objfaults = new HashSet();
            Map nsMap = new HashMap();
            ArrayList wrappers = new ArrayList();
            while (operations.hasNext()) {

                // get the basic operation information
                AxisOperation op = (AxisOperation)operations.next();
                String mep = op.getMessageExchangePattern();
                AxisMessage inmsg = null;
                AxisMessage outmsg = null;
                if (WSDLUtil.isInputPresentForMEP(mep)) {
                    inmsg = op.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                    if (inmsg == null) {
                        throw new RuntimeException(
                                "Expected input message not found for operation " + op.getName());
                    }
                    ArrayList headers = inmsg.getSoapHeaders();
                    for (int i = 0; i < headers.size(); i++) {
                        SOAPHeaderMessage header = (SOAPHeaderMessage)headers.get(i);
                        String cname = mapMessage(header, elementMap);
                        objins.add(cname);
                        if (mappedclass == null && isLookupClass(cname)) {
                            mappedclass = cname;
                        }
                    }
                }
                if (WSDLUtil.isOutputPresentForMEP(mep)) {
                    outmsg = op.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                    if (outmsg == null) {
                        throw new RuntimeException(
                                "Expected output message not found for operation " + op.getName());
                    }
                    ArrayList headers = outmsg.getSoapHeaders();
                    for (int i = 0; i < headers.size(); i++) {
                        SOAPHeaderMessage header = (SOAPHeaderMessage)headers.get(i);
                        String cname = mapMessage(header, elementMap);
                        objouts.add(cname);
                        if (mappedclass == null && isLookupClass(cname)) {
                            mappedclass = cname;
                        }
                    }
                }
                if (unwrap) {

                    // use unwrapping for both input and output
                    String receivername = "jibxReceiver" + opindex++;
                    Element dbmethod = doc.createElement("dbmethod");
                    dbmethod.setAttribute("receiver-name", receivername);
                    dbmethod.setAttribute("method-name", op.getName().getLocalPart());
                    Set nameset = new HashSet(s_reservedWords);
                    if (inmsg != null) {
                        Element wrapper = unwrapMessage(inmsg, false, simpleTypeMap, elementMap,
                                                        complexTypeMap, typeMappedClassMap,
                                                        bindingMap, nameset, nsMap, doc);
                        dbmethod.appendChild(wrapper);
                        wrappers.add(wrapper);
                    }
                    if (outmsg != null) {
                        Element wrapper = unwrapMessage(outmsg, true, simpleTypeMap, elementMap,
                                                        complexTypeMap, typeMappedClassMap,
                                                        bindingMap, nameset, nsMap, doc);
                        dbmethod.appendChild(wrapper);
                        wrappers.add(wrapper);
                    }

                    // save unwrapping information for use in code generation
                    op.addParameter(
                            new Parameter(Constants.DATABINDING_GENERATED_RECEIVER, receivername));
                    op.addParameter(new Parameter(Constants.DATABINDING_GENERATED_IMPLEMENTATION,
                                                  Boolean.TRUE));
                    op.addParameter(
                            new Parameter(Constants.DATABINDING_OPERATION_DETAILS, dbmethod));

                } else {

                    // concrete mappings, just save the mapped class name(s)
                    if (inmsg != null) {
                        String cname = mapMessage(inmsg, elementMap);
                        objins.add(cname);
                        if (mappedclass == null && isLookupClass(cname)) {
                            mappedclass = cname;
                        }
                    }
                    if (outmsg != null) {
                        String cname = mapMessage(outmsg, elementMap);
                        objouts.add(cname);
                        if (mappedclass == null && isLookupClass(cname)) {
                            mappedclass = cname;
                        }
                    }

                }

                // always handle faults as wrapped
                for (Iterator iter = op.getFaultMessages().iterator(); iter.hasNext();) {
                    String cname = mapMessage((AxisMessage)iter.next(), elementMap);
                    objfaults.add(cname);
                    if (mappedclass == null && isLookupClass(cname)) {
                        mappedclass = cname;
                    }
                }
            }

            // check for default namespace usage within bindings or wrappers
            //  (meaning we can't declare a conflicting default namespace)
            Collection prefixes = nsMap.values();
            boolean dfltns = prefixes.contains("");
            boolean wrapdflt = false;
            if (!dfltns) {
                for (int i = 0; i < wrappers.size(); i++) {
                    Element wrapper = (Element)wrappers.get(i);
                    if ("true".equals(wrapper.getAttribute("uses-default"))) {
                        wrapdflt = true;
                        break;
                    }
                }
            }

            // find a prefix that we can use where needed for extra namespace
            String xtrapref = "";
            if (dfltns || wrapdflt) {
                xtrapref = "_";
                int index = 0;
                while (prefixes.contains(xtrapref)) {
                    xtrapref = "_" + index++;
                }
            }

            // for each wrapper (input and output), determine what additional
            //  namespaces need to be declared, what prefix is to be used for
            //  the wrapper element, and what prefix to be used for each child
            //  element
            for (int i = 0; i < wrappers.size(); i++) {
                Element wrapper = (Element)wrappers.get(i);
                boolean addns = false;
                String ns = wrapper.getAttribute("ns");
                String prefix = "";
                if ("true".equals(wrapper.getAttribute("need-namespaces"))) {

                    // check extra definition needed for wrapper namespace
                    if (!"".equals(ns)) {
                        if (dfltns || wrapdflt) {

                            // need a namespace, can't be default, get or set it
                            prefix = (String)nsMap.get(ns);
                            if (prefix == null) {
                                prefix = xtrapref;
                                addns = true;
                            }

                        } else {

                            // just make the wrapper namespace the default
                            prefix = "";
                            addns = true;

                        }
                    }
                    wrapper.setAttribute("prefix", prefix);

                    // set prefixes for child elements of wrapper
                    Node node = wrapper.getFirstChild();
                    while (node != null) {
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element)node;
                            String lname = element.getNodeName();
                            if ("parameter-element".equals(lname) || "return-element".equals(lname))
                            {
                                String childns = element.getAttribute("ns");
                                if ("".equals(childns)) {
                                    element.setAttribute("prefix", "");
                                } else if (ns.equals(childns)) {
                                    element.setAttribute("prefix", prefix);
                                } else {
                                    String childprefix = (String)nsMap.get(childns);
                                    if (childprefix == null) {
                                        throw new RuntimeException("Unable to set namespace " +
                                                childns + " for child element");
                                    }
                                    element.setAttribute("prefix", childprefix);
                                }
                            }
                        }
                        node = node.getNextSibling();
                    }

                } else {

                    // check extra definition needed for wrapper namespace
                    if (!"".equals(ns)) {

                        // just make the wrapper namespace the default
                        prefix = "";
                        addns = true;

                    }
                    wrapper.setAttribute("prefix", prefix);

                    // set prefixes for child elements of wrapper
                    Node node = wrapper.getFirstChild();
                    while (node != null) {
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element)node;
                            String lname = element.getNodeName();
                            if ("parameter-element".equals(lname) || "return-element".equals(lname))
                            {
                                String childns = element.getAttribute("ns");
                                if ("".equals(childns)) {
                                    element.setAttribute("prefix", "");
                                } else if (ns.equals(childns)) {
                                    element.setAttribute("prefix", prefix);
                                } else {
                                    throw new RuntimeException("Unable to set namespace " +
                                            childns + " for child element");
                                }
                            }
                        }
                        node = node.getNextSibling();
                    }

                }
                if (addns) {
                    Element addedns = doc.createElement("extra-namespace");
                    addedns.setAttribute("ns", ns);
                    addedns.setAttribute("prefix", prefix);
                    wrapper.appendChild(addedns);
                }
            }

            // add type usage information for binding initialization
            List details = new ArrayList();
            Element bindinit = doc.createElement("initialize-binding");
            if (!typeMappedClassMap.isEmpty()) {
                for (Iterator iter = typeMappedClassMap.keySet().iterator(); iter.hasNext();) {
                    QName tname = (QName)iter.next();
                    String clsindex = ((Integer)typeMappedClassMap.get(tname)).toString();
                    Element detail = doc.createElement("abstract-type");
                    detail.setAttribute("ns", tname.getNamespaceURI());
                    detail.setAttribute("name", tname.getLocalPart());
                    detail.setAttribute("type-index", clsindex);
                    bindinit.appendChild(detail);
                    if (mappedclass == null) {
                        MappingElementBase mapping = (MappingElementBase)complexTypeMap.get(tname);
                        mappedclass = mapping.getClassName();
                    }
                }
            }

            // set binding lookup parameters
            if (binding != null && binding.getName() != null && binding.getTargetPackage() != null) {
                bindinit.setAttribute("binding-name", binding.getName());
                bindinit.setAttribute("binding-package", binding.getTargetPackage());
            } else {
                if (mappedclass == null) {
                    mappedclass = "";
                }
                bindinit.setAttribute("bound-class", mappedclass);
            }

            // include binding namespaces in initialization data
            for (Iterator iter = nsMap.keySet().iterator(); iter.hasNext();) {
                String ns = (String)iter.next();
                String prefix = (String)nsMap.get(ns);
                Element detail = doc.createElement("binding-namespace");
                detail.setAttribute("ns", ns);
                detail.setAttribute("prefix", prefix);
                bindinit.appendChild(detail);
            }
            details.add(bindinit);

            // add details for all objects used as inputs/outputs/faults
            for (Iterator iter = objins.iterator(); iter.hasNext();) {
                String classname = (String)iter.next();
                Element detail = doc.createElement("object-input");
                detail.setAttribute("type", classname);
                details.add(detail);
            }
            for (Iterator iter = objouts.iterator(); iter.hasNext();) {
                String classname = (String)iter.next();
                Element detail = doc.createElement("object-output");
                detail.setAttribute("type", classname);
                details.add(detail);
            }
            for (Iterator iter = objfaults.iterator(); iter.hasNext();) {
                String classname = (String)iter.next();
                Element detail = doc.createElement("object-fault");
                detail.setAttribute("type", classname);
                details.add(detail);
            }
            codeGenConfig.getAxisService()
                    .addParameter(new Parameter(Constants.DATABINDING_SERVICE_DETAILS, details));

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JiBXException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (AxisFault e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if a class is potentially usable for looking up binding information.
     *
     * @param type fully-qualified type name
     * @return <code>true</code> if potentially usable, <code>false</code> if not
     */
    private static boolean isLookupClass(String type) {
        return !type.startsWith("java.") && !type.startsWith("javax.") &&
                !type.startsWith("org.w3c.");
    }

    /**
     * Add format definition for type with built-in JiBX handling to map.
     *
     * @param stype schema type name
     * @param jtype java type name
     * @param sname serializer method name
     * @param dname deserializer method name
     * @param dflt  default value
     * @param map   schema type qname to format definition map
     */
    private static void buildFormat(String stype, String jtype, String sname,
                                    String dname, String dflt, Map map) {
        FormatElement format = new FormatElement();
        format.setTypeName(jtype);
        format.setSerializerName(sname);
        format.setDeserializerName(dname);
        format.setDefaultText(dflt);
        map.put(new QName(SCHEMA_NAMESPACE, stype), format);
    }

    /**
     * Handles unwrapping a message. This generates and returns the detailed description of how the
     * message is to be unwrapped. It also creates the data structures expected by the code
     * generation in order to be somewhat compatible with ADB unwrapping.
     *
     * @param msg                message to be unwrapped
     * @param isout              output message flag (wrapper inherits inner type, for XSLTs)
     * @param simpleTypeMap      binding formats
     * @param elementMap         map from element names to concrete mapping components of binding
     * @param complexTypeMap     binding mappings
     * @param typeMappedClassMap map from type qname to index
     * @param bindingMap         map from mapping components to containing binding definition
     * @param nameset            parameter variable names used in method
     * @param nsmap              mapr from URI to prefix for namespaces to be defined on wrapper
     *                           element
     * @param doc                document used for DOM components
     * @return detailed description element for code generation
     */
    private Element unwrapMessage(AxisMessage msg, boolean isout,
                                  Map simpleTypeMap, Map elementMap, Map complexTypeMap, Map typeMappedClassMap,
                                  Map bindingMap, Set nameset, Map nsmap, Document doc) {

        // find the schema definition for this message element
        QName qname = msg.getElementQName();
        if (qname == null) {
            throw new RuntimeException("No element reference in message " + msg.getName());
        }
        XmlSchemaElement wrapdef = codeGenConfig.getAxisService().getSchemaElement(qname);
        if (wrapdef == null) {
            throw new RuntimeException("Cannot unwrap - no definition found for element " + qname);
        }
        XmlSchemaType type = wrapdef.getSchemaType();

        // create document to hold data binding details for element
        Element wrapdetail = doc.createElement(isout ? "out-wrapper" : "in-wrapper");
        wrapdetail.setAttribute("ns", qname.getNamespaceURI());
        wrapdetail.setAttribute("name", qname.getLocalPart());

        // dig down to the complexType particle
        List partNameList = new ArrayList();
        String wrappertype = "";
        boolean nons = qname.getNamespaceURI().length() == 0;
        boolean dfltns = false;
        boolean complex = false;
        if (type instanceof XmlSchemaComplexType) {
            XmlSchemaComplexType ctype = (XmlSchemaComplexType)type;
            if (ctype.getAttributes().getCount() != 0) {
                throw new RuntimeException("Cannot unwrap element " +
                        qname + ": attributes not allowed on type to be unwrapped");
            }
            XmlSchemaParticle particle = ctype.getParticle();
            if (particle != null) {

                // if there's a particle present, it must be a sequence
                if (!(particle instanceof XmlSchemaSequence)) {
                    throw new RuntimeException("Cannot unwrap element " +
                            qname + ": type to be unwrapped must be a sequence");
                }
                if (particle.getMinOccurs() != 1 || particle.getMaxOccurs() != 1) {
                    throw new RuntimeException("Cannot unwrap element " +
                            qname +
                            ": contained sequence must have minOccurs='1' and maxOccurs='1'");
                }
                XmlSchemaSequence sequence = (XmlSchemaSequence)particle;

                // add child param element matching each child of wrapper element
                QName opName = msg.getAxisOperation().getName();
                XmlSchemaObjectCollection items = sequence.getItems();
                boolean first = true;
                for (Iterator iter = items.getIterator(); iter.hasNext();) {

                    // check that child item obeys the unwrapping rules
                    XmlSchemaParticle item = (XmlSchemaParticle)iter.next();
                    if (!(item instanceof XmlSchemaElement)) {
                        throw new RuntimeException("Cannot unwrap element " +
                                qname + ": only element items allowed in sequence");
                    }
                    XmlSchemaElement element = (XmlSchemaElement)item;
                    QName refname = element.getRefName();
                    QName typename = element.getSchemaTypeName();
                    if (refname == null && typename == null) {
                        throw new RuntimeException("Cannot unwrap element " +
                                qname +
                                ": all elements in contained sequence must be element references or reference a named type");
                    }
                    if (first) {
                        first = false;
                    } else if (isout) {
                        throw new RuntimeException("Cannot unwrap element " +
                                qname +
                                ": only one child element allowed in sequence for wrapped output");
                    }

                    // add element to output with details of this element handling
                    Element param =
                            doc.createElement(isout ? "return-element" : "parameter-element");
                    QName itemname = (refname == null) ? element.getQName() : refname;
                    nons = nons || itemname.getNamespaceURI().length() == 0;
                    param.setAttribute("ns", itemname.getNamespaceURI());
                    param.setAttribute("name", itemname.getLocalPart());
                    String javaname = toJavaName(itemname.getLocalPart(), nameset);
                    param.setAttribute("java-name", javaname);
                    param.setAttribute("nillable", Boolean.toString(element.isNillable()));
                    boolean optional = element.getMinOccurs() == 0;
                    param.setAttribute("optional", Boolean.toString(optional));
                    boolean isarray = element.getMaxOccurs() > 1;
                    param.setAttribute("array", Boolean.toString(isarray));
                    String javatype;
                    String createtype = null;
                    if (element.getSchemaType() instanceof XmlSchemaSimpleType) {

                        // simple type translates to format element in binding
                        FormatElement format = (FormatElement)simpleTypeMap.get(typename);
                        if (format == null) {
                            
                            // check for restriction with simple base, and treat as base if so
                            XmlSchemaSimpleType stype = (XmlSchemaSimpleType)element.getSchemaType();
                            XmlSchemaSimpleTypeContent content = stype.getContent();
                            if (content instanceof XmlSchemaSimpleTypeRestriction) {
                                QName tname = ((XmlSchemaSimpleTypeRestriction)content).getBaseTypeName();
                                if (SCHEMA_NAMESPACE.equals(tname.getNamespaceURI())) {
                                    format = (FormatElement)simpleTypeMap.get(tname);
                                    if (format != null) {
                                        typename = tname;
                                    }
                                }
                            }
                        }
                        if (format == null) {
                            throw new RuntimeException("Cannot unwrap element " +
                                qname + ": no format definition found for type " +
                                typename + " (used by element " + itemname + ')');
                        }
                        javatype = format.getTypeName();
                        param.setAttribute("form", "simple");
                        param.setAttribute("serializer", format.getSerializerName());
                        param.setAttribute("deserializer", format.getDeserializerName());

                        // convert primitive types to wrapper types for nillable
                        if ((optional || element.isNillable()) &&
                                s_wrapperMap.containsKey(javatype)) {
                            param.setAttribute("wrapped-primitive", "true");
                            param.setAttribute("value-method", javatype + "Value");
                            javatype = (String)s_wrapperMap.get(javatype);
                        } else {
                            param.setAttribute("wrapped-primitive", "false");
                            String dflt = element.getDefaultValue();
                            if (dflt == null) {
                                dflt = format.getDefaultText();
                                if (javatype.equals("float")) {
                                    dflt = dflt + 'F';
                                }
                            }
                            if (dflt != null) {
                                param.setAttribute("default", dflt);
                            }
                        }

                    } else {
                        
                        // conversion must be defined by mapping
                        MappingElementBase mapping;
                        if (refname == null) {

                            // complex type reference translates to abstract mapping in binding
                            mapping = (MappingElementBase)complexTypeMap.get(typename);
                            if (mapping == null) {
                                throw new RuntimeException("Cannot unwrap element " +
                                        qname + ": no abstract mapping definition found for type " +
                                        typename + " (used by element " + itemname + ')');
                            }
                            Integer tindex = (Integer)typeMappedClassMap.get(typename);
                            if (tindex == null) {
                                tindex = new Integer(typeMappedClassMap.size());
                                typeMappedClassMap.put(typename, tindex);
                            }
                            param.setAttribute("type-index", tindex.toString());
                            
                        } else {
                            
                            // element reference translates to concrete mapping
                            mapping = (MappingElementBase)elementMap.get(refname);
                            if (mapping == null) {
                                throw new RuntimeException("Cannot unwrap element " +
                                        qname + ": no concrete mapping definition found for element " +
                                        refname + " (used by element " + itemname + ')');
                            }
                            param.setAttribute("type-index", "");
                            
                        }

                        // configure based on the mapping information
                        param.setAttribute("form", "complex");
                        complex = true;
                        javatype = mapping.getClassName();
                        createtype = mapping.getCreateType();
                        if (createtype == null && mapping.isAbstract() &&
                            mapping.getExtensionTypes().isEmpty()) {
                            
                            // abstract mapping with no extensions requires instance
                            //  this assumes the mapped type can be created, but no easy way to check
                            createtype = javatype;
                        }

                        // merge contained namespace definitions into set for operation
                        Iterator citer = mapping.topChildIterator();
                        while (citer.hasNext()) {
                            ElementBase child = (ElementBase)citer.next();
                            if (child.type() == ElementBase.NAMESPACE_ELEMENT) {
                                dfltns = mapNamespace((NamespaceElement)child, dfltns, nsmap);
                            } else {
                                break;
                            }
                        }

                        // also merge namespace definitions from binding
                        BindingElement binding = (BindingElement)bindingMap.get(mapping);
                        citer = binding.topChildIterator();
                        while (citer.hasNext()) {
                            ElementBase child = (ElementBase)citer.next();
                            if (child.type() == ElementBase.NAMESPACE_ELEMENT) {
                                dfltns = mapNamespace((NamespaceElement)child, dfltns, nsmap);
                            } else if (child.type() != ElementBase.INCLUDE_ELEMENT) {
                                break;
                            }
                        }
                    }
                    param.setAttribute("java-type", javatype);
                    if (createtype != null) {
                        param.setAttribute("create-type", createtype);
                    }

                    boolean isobj = !s_primitiveSet.contains(javatype);
                    String fulltype = javatype;
                    if (isarray) {
                        fulltype += "[]";
                        isobj = false;
                    }
                    param.setAttribute("object", Boolean.toString(isobj));
                    if (isout) {
                        wrappertype = fulltype;
                    } else {
                        wrappertype = "java.lang.Object";
                    }
                    wrapdetail.appendChild(param);

                    // this magic code comes from org.apache.axis2.wsdl.codegen.extension.SchemaUnwrapperExtension
                    //  it's used here to fit into the ADB-based code generation model
                    QName partqname = WSDLUtil.getPartQName(opName.getLocalPart(),
                                                            WSDLConstants.INPUT_PART_QNAME_SUFFIX,
                                                            javaname);
                    partNameList.add(partqname);

                    // add type mapping so we look like ADB
                    codeGenConfig.getTypeMapper().addTypeMappingName(partqname, fulltype);
                }

                // check namespace prefix usage
                if (nons && dfltns) {
                    throw new RuntimeException("Cannot unwrap element " + qname +
                            ": no-namespace element(s) conflict with default namespace use in binding");
                }
                wrapdetail.setAttribute("uses-default", Boolean.toString(nons));

                // set flag for namespace declarations needed on wrapper
                wrapdetail.setAttribute("need-namespaces", Boolean.toString(complex));

            }

        } else if (type != null) {
            throw new RuntimeException("Cannot unwrap element " + qname +
                    ": not a complexType definition");
        }
        if (wrapdetail.getFirstChild() == null) {
            wrapdetail.setAttribute("empty", "true");
            wrapdetail.setAttribute("need-namespaces", "false");
            wrappertype = "";
        } else {
            wrapdetail.setAttribute("empty", "false");
        }

        // this magic code comes from org.apache.axis2.wsdl.codegen.extension.SchemaUnwrapperExtension
        //  it's used here to fit into the ADB-based code generation model
        MessagePartInformationHolder infoHolder = new MessagePartInformationHolder();
        infoHolder.setOperationName(msg.getAxisOperation().getName());
        infoHolder.setPartsList(partNameList);
        try {
            msg.addParameter(new Parameter(Constants.UNWRAPPED_DETAILS, infoHolder));
        } catch (AxisFault e) {
            throw new RuntimeException(e);
        }

        // set indication for unwrapped message
        try {
            msg.addParameter(new Parameter(Constants.UNWRAPPED_KEY, Boolean.TRUE));
        } catch (AxisFault e) {
            throw new RuntimeException(e);
        }

        // add fake mapping for wrapper name (necessary for current XSLTs)
        codeGenConfig.getTypeMapper().addTypeMappingName(qname, wrappertype);

        // return the unwrapping details
        return wrapdetail;
    }

    /**
     * Add mapping from namespace URI to prefix. In the case where multiple prefixes are used with a
     * single URI, this will preserve the last non-empty prefix for that URI.
     *
     * @param ns     namespace definition
     * @param dfltns flag for default namespace used in binding
     * @param nsmap  map from namespace URIs to prefixes
     * @return flag for default namespace used in binding
     */
    private boolean mapNamespace(NamespaceElement ns, boolean dfltns, Map nsmap) {
        String prefix = ns.getPrefix();
        if (prefix == null) {
            prefix = "";
        }
        String prior = (String)nsmap.get(ns.getUri());
        if (prior != null) {
            if (prefix.length() == 0) {
                return dfltns;
            } else if (prior.length() == 0) {
                dfltns = false;
            }
        }
        nsmap.put(ns.getUri(), prefix);
        return dfltns || prefix.length() == 0;
    }

    private static String toJavaName(String name, Set nameset) {
        StringBuffer buff = new StringBuffer(name.length());
        for (int i = 0; i < name.length(); i++) {
            char chr = name.charAt(i);
            if ((i == 0 && Character.isJavaIdentifierStart(chr)) ||
                    (i > 0 && Character.isJavaIdentifierPart(chr))) {
                buff.append(chr);
            } else if (chr == ':' || chr == '.') {
                buff.append('$');
            } else {
                buff.append('_');
            }
        }
        int count = 0;
        String jname = buff.toString();
        while (!nameset.add(jname)) {
            jname = buff.toString() + count++;
        }
        return jname;
    }

    private String mapMessage(AxisMessage msg, Map complexTypeMap) {
        QName qname = msg.getElementQName();
        if (qname == null) {
            throw new RuntimeException("No element reference in message " + msg.getName());
        }
        return mapQName(qname, complexTypeMap);
    }

    private String mapMessage(SOAPHeaderMessage msg, Map complexTypeMap) {
        QName qname = msg.getElement();
        if (qname == null) {
            throw new RuntimeException("No element reference in header");
        }
        return mapQName(qname, complexTypeMap);
    }

    private String mapQName(QName qname, Map complexTypeMap) throws RuntimeException {
        Object obj = complexTypeMap.get(qname);
        if (obj == null) {
            throw new RuntimeException("No mapping defined for element " + qname);
        }
        MappingElementBase mapping = (MappingElementBase)obj;
        String cname = mapping.getClassName();
        codeGenConfig.getTypeMapper().addTypeMappingName(qname, cname);
        return cname;
    }

    /**
     * Collect mapping from qnames to classes for top level mappings in JiBX binding.
     *
     * @param binding
     * @param dns            default namespace to be used unless overridden (empty string if none)
     * @param elementMap     map from element names to concrete mapping components of binding
     * @param complexTypeMap map from type names to abstract mapping components of binding
     * @param simpleTypeMap  map from type names to format definition components of binding
     * @param bindingMap     map from mapping components to containing binding definition
     */
    private static void collectTopLevelComponents(BindingElement binding,
                                                  String dns, Map elementMap, Map complexTypeMap,
                                                  Map simpleTypeMap,
                                                  Map bindingMap) {

        // check default namespace set at top level of binding
        String defaultns = findDefaultNS(binding.topChildIterator(), dns);

        // add all top level mapping and format definitions to maps
        for (Iterator iter = binding.topChildIterator(); iter.hasNext();) {
            ElementBase child = (ElementBase)iter.next();
            if (child.type() == ElementBase.INCLUDE_ELEMENT) {

                // recurse to process included binding definitions
                IncludeElement include = (IncludeElement)child;
                if(include.getBinding() != null) {
                    collectTopLevelComponents(include.getBinding(), defaultns,
                                            elementMap, complexTypeMap, simpleTypeMap, bindingMap);
                }

            } else if (child.type() == ElementBase.FORMAT_ELEMENT) {

                // register named formats as simple type conversions
                FormatElement format = (FormatElement)child;
                registerElement(format.getQName(), format, simpleTypeMap);
                bindingMap.put(format, binding);

            } else if (child.type() == ElementBase.MAPPING_ELEMENT) {
                
                // record only abstract mappings with type names, and mappings with names
                MappingElementBase mapping = (MappingElementBase)child;
                bindingMap.put(mapping, binding);
                if (mapping.isAbstract() && mapping.getTypeQName() != null) {

                    // register named abstract mappings as complex type conversions
                    registerElement(mapping.getTypeQName(), mapping,
                                    complexTypeMap);

                } else if (mapping.getName() != null) {

                    // register concrete mappings as element conversions
                    String uri = mapping.getUri();
                    if (uri == null) {
                        uri = findDefaultNS(mapping.topChildIterator(),
                                            defaultns);
                    }
                    elementMap.put(new QName(uri, mapping.getName()), mapping);
                }
            }
        }
    }

    /**
     * Register binding element by qualified name. This converts the qualified name format used by
     * the JiBX binding model to that used by Axis2.
     *
     * @param qname   qualified name in JiBX format (<code>null</code> if none)
     * @param element corresponding element of binding definition
     * @param map     qualified name to element map
     */
    private static void registerElement(org.jibx.runtime.QName qname,
                                        ElementBase element, Map map) {
        if (qname != null) {
            map.put(new QName(qname.getUri(), qname.getName()), element);
        }
    }

    /**
     * Find the default namespace within a list of JiBX binding model elements possibly including
     * namespace definitions. Once a non-namespace definition element is seen in the list, this just
     * returns (since the namespace definitions always come first in JiBX's binding format).
     *
     * @param iter iterator for elements in list
     * @param dns  default namespace if not overridden
     * @return default namespace
     */
    private static String findDefaultNS(Iterator iter, String dns) {
        while (iter.hasNext()) {
            ElementBase child = (ElementBase)iter.next();
            if (child.type() == ElementBase.NAMESPACE_ELEMENT) {
                NamespaceElement namespace = (NamespaceElement)child;
                String defaultName = namespace.getDefaultName();
                if ("elements".equals(defaultName) || "all".equals(defaultName)) {
                    return namespace.getUri();
                }
            } else {
                break;
            }
        }
        return dns;
    }

    /**
     * Inner class for handling prevalidation of include elements only. Unlike the normal JiBX
     * binding definition prevalidation step, this visitor ignores everything except include
     * elements.
     */
    private class IncludePrevalidationVisitor extends ModelVisitor {
        private final ValidationContext m_context;

        private IncludePrevalidationVisitor(ValidationContext vctx) {
            m_context = vctx;
        }

        /* (non-Javadoc)
        * @see org.jibx.binding.model.ModelVisitor#visit(org.jibx.binding.model.ElementBase)
        */
        public boolean visit(IncludeElement node) {
            try {
                // force creation of defintions context for containing binding
                m_context.getFormatDefinitions();
                node.prevalidate(m_context);
            } catch (Throwable t) {
                m_context.addFatal("Error during validation: " +
                        t.getMessage());
                t.printStackTrace();
                return false;
            }
            return true;
        }
    }

    private static class NamedParameterTypeMapper extends JavaTypeMapper {
        /**
         * Return the real parameter name, not a dummy.
         *
         * @param qname
         * @return local part of name
         */
        public String getParameterName(QName qname) {
            return qname.getLocalPart();
        }
    }
}