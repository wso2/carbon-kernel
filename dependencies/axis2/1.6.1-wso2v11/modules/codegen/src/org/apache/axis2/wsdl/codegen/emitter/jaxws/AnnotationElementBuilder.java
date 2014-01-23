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

import org.apache.axis2.util.XSLTUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AnnotationElementBuilder {

    static Element buildWebServiceAnnotationElement(String name, String targetNS, String wsdlLocation,
                                                    Document doc) {

        Element annotationElement = doc.createElement("annotation");
        XSLTUtils.addAttribute(doc, "name", "javax.jws.WebService", annotationElement);

        Element paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "name", paramElement);
        XSLTUtils.addAttribute(doc, "value", name, paramElement);
        annotationElement.appendChild(paramElement);

        paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "targetNamespace", paramElement);
        XSLTUtils.addAttribute(doc, "value", targetNS, paramElement);
        annotationElement.appendChild(paramElement);

        return annotationElement;
    }

    static Element buildWebServiceAnnotationElement(String endpointInterface, Document doc) {

        Element annotationElement = doc.createElement("annotation");
        XSLTUtils.addAttribute(doc, "name", "javax.jws.WebService", annotationElement);

        Element paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "endpointInterface", paramElement);
        XSLTUtils.addAttribute(doc, "value", endpointInterface, paramElement);
        annotationElement.appendChild(paramElement);

        return annotationElement;
    }

    static Element buildWebFaultAnnotationElement(String name, String targetNS, Document doc) {
        Element annotationElement = doc.createElement("annotation");
        XSLTUtils.addAttribute(doc, "name", "javax.xml.ws.WebFault", annotationElement);

        Element paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "name", paramElement);
        XSLTUtils.addAttribute(doc, "value", name, paramElement);
        annotationElement.appendChild(paramElement);

        paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "targetNamespace", paramElement);
        XSLTUtils.addAttribute(doc, "value", targetNS, paramElement);
        annotationElement.appendChild(paramElement);

        return annotationElement;
    }

    static Element buildWebServiceClientAnnotationElement(String name, String targetNS, String wsdlLocation,
                                                          Document doc) {

        Element annotationElement = doc.createElement("annotation");
        XSLTUtils.addAttribute(doc, "name", "javax.xml.ws.WebServiceClient", annotationElement);

        Element paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "name", paramElement);
        XSLTUtils.addAttribute(doc, "value", name, paramElement);
        annotationElement.appendChild(paramElement);

        paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "targetNamespace", paramElement);
        XSLTUtils.addAttribute(doc, "value", targetNS, paramElement);
        annotationElement.appendChild(paramElement);

        paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "wsdlLocation", paramElement);
        XSLTUtils.addAttribute(doc, "value", wsdlLocation, paramElement);
        annotationElement.appendChild(paramElement);

        return annotationElement;
    }

    static Element buildWebEndPointAnnotationElement(String name, Document doc) {
        Element annotationElement = doc.createElement("annotation");
        XSLTUtils.addAttribute(doc, "name", "javax.xml.ws.WebEndpoint", annotationElement);

        Element paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "name", paramElement);
        XSLTUtils.addAttribute(doc, "value", name, paramElement);
        annotationElement.appendChild(paramElement);

        return annotationElement;
    }
}

