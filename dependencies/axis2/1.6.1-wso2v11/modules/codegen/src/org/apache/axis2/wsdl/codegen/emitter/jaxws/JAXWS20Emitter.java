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

package org.apache.axis2.wsdl.codegen.emitter.jaxws;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.WSDL2Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class JAXWS20Emitter extends JAXWSEmitter {

    /**
     * Creates the XML model for the Service Endpoint interface
     *
     * @return DOM Document
     */
    protected Document createDOMDocumentForSEI() throws AxisFault {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("javaConstruct");

        Element importList = doc.createElement("importList");
        rootElement.appendChild(importList);

        String packageName = codeGenConfiguration.getPackageName();
        String targetNS = codeGenConfiguration.getTargetNamespace();
        String portTypeName = (String) axisService.getParameterValue(WSDL2Constants.INTERFACE_LOCAL_NAME);
        portTypeName = resolveNameCollision(portTypeName, packageName, TYPE_SUFFIX);
        this.axisService.addParameter(JAXWS_PORT_TYPE_NAME, portTypeName);

        addAttribute(doc, "package", packageName, rootElement);
        addAttribute(doc, "targetNamespace", targetNS, rootElement);
        addAttribute(doc, "name", portTypeName, rootElement);

        Element annotationElement = AnnotationElementBuilder.buildWebServiceAnnotationElement(portTypeName, targetNS,
                "", doc);
        rootElement.appendChild(annotationElement);

        loadOperations(doc, rootElement, null);

        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));
        doc.appendChild(rootElement);
        //////////////////////////////////////////////////////////
//        System.out.println(DOM2Writer.nodeToString(rootElement));
        ////////////////////////////////////////////////////////////
        return doc;

    }

    /**
     * Creates the XML model for the Service Endpoint interface
     *
     * @return DOM Document
     */
    protected Document createDOMDocumentForSEIImpl() throws AxisFault {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("javaConstruct");

        //Element importList = doc.createElement("importList");
        //rootElement.appendChild(importList);

        String packageName = codeGenConfiguration.getPackageName();
        String targetNS = codeGenConfiguration.getTargetNamespace();
        String portTypeName = (String) axisService.getParameterValue(WSDL2Constants.INTERFACE_LOCAL_NAME);
        portTypeName = resolveNameCollision(portTypeName, packageName, TYPE_SUFFIX);
        this.axisService.addParameter(JAXWS_PORT_TYPE_NAME, portTypeName);

        addAttribute(doc, "package", packageName, rootElement);
        addAttribute(doc, "targetNamespace", targetNS, rootElement);
        addAttribute(doc, "name", axisService.getParameter(JAXWS_PORT_TYPE_NAME).getValue() + JAXWS_IMPL_SUFFIX,
                rootElement);

        Element annotationElement = AnnotationElementBuilder.buildWebServiceAnnotationElement(
               packageName +"." +axisService.getParameter(JAXWS_PORT_TYPE_NAME).getValue(), doc);
        rootElement.appendChild(annotationElement);

        loadOperations(doc, rootElement, null);

        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));
        doc.appendChild(rootElement);
        //////////////////////////////////////////////////////////
//        System.out.println(DOM2Writer.nodeToString(rootElement));
        ////////////////////////////////////////////////////////////
        return doc;

    }

    /**
     * Creates the XML model for the Service Class
     *
     * @return DOM Document
     */
    protected Document createDOMDocumentForServiceClass() {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("javaConstruct");

        Element importList = doc.createElement("importList");
        rootElement.appendChild(importList);

        String serviceName = axisService.getName();
        String capitalizedServiceName = serviceName.toUpperCase();
        String wsdlLocation = "Needs to be fixed";
        String packageName = codeGenConfiguration.getPackageName();
        String targetNS = codeGenConfiguration.getTargetNamespace();

        serviceName = resolveNameCollision(serviceName, packageName, TYPE_SUFFIX);

        addAttribute(doc, "package", packageName, rootElement);
        addAttribute(doc, "targetNamespace", targetNS, rootElement);
        addAttribute(doc, "name", serviceName, rootElement);
        addAttribute(doc, "wsdlLocation", wsdlLocation, rootElement);
        addAttribute(doc, "capitalizedServiceName", capitalizedServiceName, rootElement);

        Element annotationElement = AnnotationElementBuilder.buildWebServiceClientAnnotationElement(serviceName,
                targetNS, wsdlLocation, doc);
        rootElement.appendChild(annotationElement);

        //Building portType Elements -- think of a suitable solution
        for (Iterator portIterator = axisService.getEndpoints().keySet().iterator(); portIterator.hasNext();) {
            String portName = (String) portIterator.next();

            Element portElement = doc.createElement("port");
            addAttribute(doc, "portName", portName, portElement);
            addAttribute(doc, "portTypeName", (String) this.axisService.getParameter(JAXWS_PORT_TYPE_NAME).getValue(),
                    portElement);

            Element endPointAnnoElement = AnnotationElementBuilder.buildWebEndPointAnnotationElement(portName, doc);
            portElement.appendChild(endPointAnnoElement);

            rootElement.appendChild(portElement);
        }

        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));
        doc.appendChild(rootElement);
        //////////////////////////////////////////////////////////
//        System.out.println(DOM2Writer.nodeToString(rootElement));
        ////////////////////////////////////////////////////////////
        return doc;
    }

    /**
     * Creates the XML model for a Exception Class
     *
     * @param key String
     * @return DOM Document
     */
    protected Document createDOMDocumentForException(String key) {
        Document doc = getEmptyDocument();
        Element faultElement;

        faultElement = doc.createElement("javaConstruct");
        Element importList = doc.createElement("importList");
        faultElement.appendChild(importList);

        String packageName = codeGenConfiguration.getPackageName();
        String targetNS = codeGenConfiguration.getTargetNamespace();

        addAttribute(doc, "package", packageName, faultElement);
        addAttribute(doc, "targetNamespace", targetNS, faultElement);

        String exceptionClassName = (String) faultClassNameMap.get(key);
        String resolvedExpClass = resolveNameCollision(exceptionClassName, packageName, EXCEPTION_SUFFIX);

        if (!resolvedExpClass.equals(exceptionClassName))
            faultClassNameMap.put(key, resolvedExpClass);

        addAttribute(doc, "name", resolvedExpClass, faultElement);

        String typeMapping =
                this.mapper.getTypeMappingName((QName) faultElementQNameMap.get(key));
        String shortType = extratClassName(typeMapping);

        addAttribute(doc, "type", (typeMapping == null)
                ? ""
                : typeMapping, faultElement);

        addAttribute(doc, "shortType", (shortType == null)
                ? ""
                : shortType, faultElement);

        Element importElement;
        importElement = doc.createElement("import");
        addAttribute(doc, "value", typeMapping, importElement);
        importList.appendChild(importElement);

        if (mapper.getDefaultMappingName().equals(typeMapping)) {
            addAttribute(doc, "default", "yes", faultElement);
        }

        addAttribute(doc, "value", getParamInitializer(typeMapping),
                faultElement);

        Element annotationElement = AnnotationElementBuilder.buildWebFaultAnnotationElement(typeMapping,
                codeGenConfiguration.getTargetNamespace(), doc);
        faultElement.appendChild(annotationElement);
        doc.appendChild(faultElement);
        //////////////////////////////////////////////////////////
//        System.out.println(DOM2Writer.nodeToString(faultElement));
        ////////////////////////////////////////////////////////////
        return doc;
    }
}