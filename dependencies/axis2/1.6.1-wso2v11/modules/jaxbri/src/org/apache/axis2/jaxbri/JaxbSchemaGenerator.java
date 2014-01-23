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

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import org.apache.axis2.description.java2wsdl.DefaultSchemaGenerator;
import org.apache.axis2.util.Loader;
import org.apache.axis2.deployment.util.BeanExcludeInfo;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

public class JaxbSchemaGenerator extends DefaultSchemaGenerator {
    public JaxbSchemaGenerator(ClassLoader loader, String className,
                               String schematargetNamespace,
                               String schematargetNamespacePrefix)
            throws Exception {
        super(loader, className, schematargetNamespace, schematargetNamespacePrefix,null);
    }

    public Collection generateSchema() throws Exception {
        generateSchemaForParameters();
        return super.generateSchema();
    }

    /**
     * collects all the method parameters and the extra classes from the command line
     * creates a JAXBContext and generates schemas from the JAXBContext
     * 
     * @throws Exception
     */
    public void generateSchemaForParameters() throws Exception {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.addAll(processJaxBeMethods(serviceClass.getMethods()));

        if (extraClasses != null) {
            for (Object extraClass : extraClasses) {
                classes.add(Loader.loadClass(classLoader, (String) extraClass));
            }
        }

        String jaxbNamespace = null;
        if(isUseWSDLTypesNamespace()){
            jaxbNamespace = (String) pkg2nsmap.get("all");
        }
        if(jaxbNamespace == null) {
            jaxbNamespace = this.getSchemaTargetNameSpace();
        }

        JAXBContextImpl context = (JAXBContextImpl) createJAXBContext(classes, jaxbNamespace);

        for (DOMResult r : generateJaxbSchemas(context)) {
            Document d = (Document) r.getNode();
            String targetNamespace = d.getDocumentElement().getAttribute("targetNamespace");
            if ("".equals(targetNamespace)) {
                targetNamespace = this.getSchemaTargetNameSpace();
                d.getDocumentElement().setAttribute("targetNamespace", targetNamespace);
            }

            NodeList nodes = d.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);
                if (n instanceof Element) {
                    Element e = (Element) n;
                    if (e.getLocalName().equals("import")) {
                        d.getDocumentElement().removeChild(e);
                    }
                }
            }

            XmlSchema xmlSchema = xmlSchemaCollection.read(d.getDocumentElement());

            for (Class clazz : classes) {
                JaxBeanInfo<?> beanInfo = context.getBeanInfo(clazz);
                QName qName = getTypeName(beanInfo);
                if(qName != null) {
                    typeTable.addComplexSchema(clazz.getName(), qName);
                }
            }
            schemaMap.put(targetNamespace, xmlSchema);
        }
    }

    private QName getTypeName(JaxBeanInfo<?> beanInfo) {
        Iterator<QName> itr = beanInfo.getTypeNames().iterator();
        if (!itr.hasNext()) {
            return null;
        }

        return itr.next();
    }


    protected List<Class<?>> processJaxBeMethods(Method[] declaredMethods) throws Exception {
        List<Class<?>> list = new ArrayList<Class<?>>();

        for (int i = 0; i < declaredMethods.length; i++) {
            Method jMethod = declaredMethods[i];

            if (jMethod.getExceptionTypes().length > 0) {
                Class[] extypes = jMethod.getExceptionTypes();
                for (int j = 0; j < extypes.length; j++) {
                    Class extype = extypes[j];
                    if(!extype.getName().startsWith("java")||extype.getName().startsWith("javax.")){
                        list.add(extype);
                    }
                    generateSchemaForType(list, extype);
                }
            }
            Class[] paras = jMethod.getParameterTypes();
            for (int j = 0; j < paras.length; j++) {
                Class paraType = paras[j];
                generateSchemaForType(list, paraType);
            }
            // for its return type
            Class returnType = jMethod.getReturnType();

            if (!(returnType == void.class)) {
                generateSchemaForType(list, returnType);

            }
        }
        return list;
    }

    private void generateSchemaForType(List<Class<?>> list, Class type)
            throws Exception {

        boolean isArrayType = false;
        if (type != null) {
            isArrayType = type.isArray();
        }
        if (isArrayType) {
            type = type.getComponentType();
        }
        String classTypeName;
        if (type == null) {
            classTypeName = "java.lang.Object";
        } else {
            classTypeName = type.getName();
        }
        if (isArrayType && "byte".equals(classTypeName)) {
            classTypeName = "base64Binary";
            isArrayType = false;
        }
        if ("javax.activation.DataHandler".equals(classTypeName)) {
            classTypeName = "base64Binary";
        }
        QName schemaTypeName = typeTable.getSimpleSchemaTypeName(classTypeName);
        if (schemaTypeName == null) {
            list.add(type);
        }
        addImport(getXmlSchema(schemaTargetNameSpace), schemaTypeName);
    }

    protected List<DOMResult> generateJaxbSchemas(JAXBContext context) throws IOException {
        final List<DOMResult> results = new ArrayList<DOMResult>();

        context.generateSchema(new SchemaOutputResolver() {
            @Override
            public Result createOutput(String ns, String file) throws IOException {
                DOMResult result = new DOMResult();
                result.setSystemId(file);
                results.add(result);
                return result;
            }
        });

        return results;
    }

    protected static JAXBContext createJAXBContext(Set<Class<?>> classes,
                                                   String defaultNs) throws JAXBException {
        Iterator it = classes.iterator();
        String className = "";
        Object remoteExceptionObject;
        while (it.hasNext()) {
            remoteExceptionObject = it.next();
            className = remoteExceptionObject.toString();
            if (!("".equals(className)) && className.contains("RemoteException")) {
                it.remove();
            }
        }

        for (Class<?> cls : classes) {
            if (cls.getName().endsWith("ObjectFactory")) {
                //kind of a hack, but ObjectFactories may be created with empty namespaces
                defaultNs = null;
            }
        }

        Map<String, Object> map = new HashMap<String, Object>();
        if (defaultNs != null) {
            map.put("com.sun.xml.bind.defaultNamespaceRemap", defaultNs);
        }

        for (Class<?> cls : classes) {
            System.out.println(">>>> :" + cls);
        }
        return JAXBContext.newInstance(classes.toArray(new Class[classes.size()]), map);
    }

    protected QName generateSchema(Class javaType) throws Exception {
        String name = getClassName(javaType);
        QName schemaTypeName = typeTable.getComplexSchemaType(name);
        if (schemaTypeName == null) {
            String simpleName = javaType.getSimpleName();

            String packageName = getQualifiedName(javaType.getPackage());
            String targetNameSpace = resolveSchemaNamespace(packageName);

            XmlSchema xmlSchema = getXmlSchema(targetNameSpace);
            String targetNamespacePrefix = (String) targetNamespacePrefixMap.get(targetNameSpace);
            if (targetNamespacePrefix == null) {
                targetNamespacePrefix = generatePrefix();
                targetNamespacePrefixMap.put(targetNameSpace, targetNamespacePrefix);
            }

            XmlSchemaComplexType complexType = new XmlSchemaComplexType(xmlSchema);
            XmlSchemaSequence sequence = new XmlSchemaSequence();
            XmlSchemaComplexContentExtension complexExtension =
                    new XmlSchemaComplexContentExtension();

            XmlSchemaElement eltOuter = new XmlSchemaElement();
            schemaTypeName = new QName(targetNameSpace, simpleName, targetNamespacePrefix);
            eltOuter.setName(simpleName);
            eltOuter.setQName(schemaTypeName);

            Class sup = javaType.getSuperclass();
            if ((sup != null) && !("java.lang.Object".compareTo(sup.getName()) == 0) &&
                    !(getQualifiedName(sup.getPackage()).indexOf("org.apache.axis2") > 0)
                    && !(getQualifiedName(sup.getPackage()).indexOf("java.util") > 0))
            {
                String superClassName = sup.getName();
                String superclassname = sup.getSimpleName();
                String tgtNamespace;
                String tgtNamespacepfx;
                QName qName = typeTable.getSimpleSchemaTypeName(superClassName);
                if (qName != null) {
                    tgtNamespace = qName.getNamespaceURI();
                    tgtNamespacepfx = qName.getPrefix();
                } else {
                    tgtNamespace = resolveSchemaNamespace(getQualifiedName(sup.getPackage()));
                    tgtNamespacepfx = (String) targetNamespacePrefixMap.get(tgtNamespace);
                    QName superClassQname = generateSchema(sup);
                    if (superClassQname != null) {
                        tgtNamespacepfx = superClassQname.getPrefix();
                        tgtNamespace = superClassQname.getNamespaceURI();
                    }
                }
                if (tgtNamespacepfx == null) {
                    tgtNamespacepfx = generatePrefix();
                    targetNamespacePrefixMap.put(tgtNamespace, tgtNamespacepfx);
                }
                //if the parent class package name is differ from the child
                if (!((NamespaceMap) xmlSchema.getNamespaceContext()).values().
                        contains(tgtNamespace)) {
                    XmlSchemaImport importElement = new XmlSchemaImport();
                    importElement.setNamespace(tgtNamespace);
                    xmlSchema.getItems().add(importElement);
                    ((NamespaceMap) xmlSchema.getNamespaceContext()).
                            put(generatePrefix(), tgtNamespace);
                }

                QName basetype = new QName(tgtNamespace, superclassname, tgtNamespacepfx);
                complexExtension.setBaseTypeName(basetype);
                complexExtension.setParticle(sequence);
                XmlSchemaComplexContent contentModel = new XmlSchemaComplexContent();
                contentModel.setContent(complexExtension);
                complexType.setContentModel(contentModel);

            } else {
                complexType.setParticle(sequence);
            }

            complexType.setName(simpleName);

//            xmlSchema.getItems().add(eltOuter);
            xmlSchema.getElements().add(schemaTypeName, eltOuter);
            eltOuter.setSchemaTypeName(complexType.getQName());

            xmlSchema.getItems().add(complexType);
            xmlSchema.getSchemaTypes().add(schemaTypeName, complexType);

            // adding this type to the table
            typeTable.addComplexSchema(name, eltOuter.getQName());
            // adding this type's package to the table, to support inheritance.
            typeTable.addComplexSchema(getQualifiedName(javaType.getPackage()), eltOuter.getQName());

            BeanExcludeInfo beanExcludeInfo = null;
            if (service.getExcludeInfo() != null) {
                beanExcludeInfo = service.getExcludeInfo().getBeanExcludeInfoForClass(getClassName(javaType));
            }

            // we need to get properties only for this bean. hence ignore the super
            // class properties
            BeanInfo beanInfo = Introspector.getBeanInfo(javaType, javaType.getSuperclass());
            PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
            PropertyDescriptor property ;
            String propertyName ;

            for (int i = 0; i < properties.length; i++) {
                property = properties[i];
                propertyName = property.getName();
                if (!property.getName().equals("class") && (property.getPropertyType() != null)) {
                    if ((beanExcludeInfo == null) || !beanExcludeInfo.isExcludedProperty(propertyName)) {

                        generateSchemaforFieldsandProperties(xmlSchema,
                                sequence,
                                property.getPropertyType(),
                                propertyName,
                                property.getPropertyType().isArray());
                    }
                }
            }
        }
        return schemaTypeName;
    }

}
