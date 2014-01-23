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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.emitter.AxisServiceBasedMultiLanguageEmitter;
import org.apache.axis2.wsdl.codegen.writer.ExceptionWriter;
import org.apache.axis2.wsdl.codegen.writer.FileWriter;
import org.apache.axis2.wsdl.codegen.writer.InterfaceImplementationWriter;
import org.apache.axis2.wsdl.codegen.writer.SkeletonInterfaceWriter;
import org.apache.axis2.wsdl.codegen.writer.SkeletonWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.Map;

public abstract class JAXWSEmitter extends AxisServiceBasedMultiLanguageEmitter {

    protected final static String TYPE_SUFFIX = "Type";
    protected final static String SERVICE_SUFFIX = "Service";
    protected final static String EXCEPTION_SUFFIX = "Exception";
    protected final static String JAXWS_PORT_TYPE_NAME = "JaxwsPortTypeName";
    protected final static String JAXWS_IMPL_SUFFIX = "Impl";

    public void setCodeGenConfiguration(CodeGenConfiguration configuration) {
        super.setCodeGenConfiguration(configuration);
    }

    public void emitSkeleton() throws CodeGenerationException {

        try {

            Map originalMap = getNewCopy(this.mapper.getAllMappedNames());
            // we are going to generate following files seperately per service
            for (Iterator axisServicesIter = this.axisServices.iterator();
                 axisServicesIter.hasNext();) {
                this.axisService = (AxisService) axisServicesIter.next();
                this.axisBinding =
                        axisService.getEndpoint(axisService.getEndpointName()).getBinding();

                // see the comment at updateMapperClassnames for details and reasons for
                // calling this method
                if (mapper.isObjectMappingPresent()) {
                    copyMap(originalMap, this.mapper.getAllMappedNames());
                    updateMapperForMessageReceiver();
                } else {
                    copyToFaultMap();
                }

                //handle faults
                generateAndPopulateFaultNames();

                //write the Exceptions
                writeExceptions();

                //write the Service Endpoint Interface
                writeServiceEndpointInterface();

                //write the Service Endpoint Interface
                writeServiceEndpointInterfaceImpl();

                //write the Service Class
                writeServiceClass();
            }

            // save back type map
            if (this.mapper.isObjectMappingPresent()) {
                copyMap(originalMap, this.mapper.getAllMappedNames());
            }

            // write service xml
            // if asked
            if (codeGenConfiguration.isGenerateDeployementDescriptor()) {
                writeServiceXml();
            }

            //write the ant build
            //we skip this for the flattened case
            if (!codeGenConfiguration.isFlattenFiles()) {
                writeAntBuild();
            }


        } catch (CodeGenerationException cgExp) {
            throw cgExp;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    /**
     * Write the service endpoint interface
     *
     * @throws Exception
     */
    protected void writeServiceEndpointInterface() throws Exception {

        Document skeletonModel = createDOMDocumentForSEI();
        debugLogDocument("Document for Service Endpoint Interface:", skeletonModel);
        FileWriter skeletonInterfaceWriter = new SkeletonInterfaceWriter(
                codeGenConfiguration.isFlattenFiles() ?
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                codeGenConfiguration.getSourceLocation())
                , this.codeGenConfiguration.getOutputLanguage());

        writeFile(skeletonModel, skeletonInterfaceWriter);
    }

    protected void writeServiceEndpointInterfaceImpl() throws Exception {
        Document skeletonModel = createDOMDocumentForSEIImpl();
        debugLogDocument("Document for Service Endpoint Interface:", skeletonModel);
        FileWriter interfaceImplementationWriter = new InterfaceImplementationWriter(
                codeGenConfiguration.isFlattenFiles() ?
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                codeGenConfiguration.getSourceLocation())
                , this.codeGenConfiguration.getOutputLanguage());

        writeFile(skeletonModel, interfaceImplementationWriter);
    }

    /**
     * Writes the exception calsses.
     */
    protected void writeExceptions() throws Exception {
        String key;
        Iterator iterator = fullyQualifiedFaultClassNameMap.keySet().iterator();
        while (iterator.hasNext()) {
            key = (String) iterator.next();


            Document skeletonModel = createDOMDocumentForException(key);
            debugLogDocument("Document for Exception Class:", skeletonModel);
            ExceptionWriter exceptionWriter =
                    new ExceptionWriter(
                            codeGenConfiguration.isFlattenFiles() ?
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                            null) :
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                            codeGenConfiguration.getSourceLocation()),
                            codeGenConfiguration.getOutputLanguage());

            writeFile(skeletonModel, exceptionWriter);
        }
    }

    /**
     * Write the service class
     *
     * @throws Exception
     */
    protected void writeServiceClass() throws Exception {
        Document skeletonModel = createDOMDocumentForServiceClass();
        debugLogDocument("Document for Service Endpoint Interface:", skeletonModel);
        FileWriter skeletonInterfaceWriter = new SkeletonWriter(
                codeGenConfiguration.isFlattenFiles() ?
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                codeGenConfiguration.getSourceLocation())
                , this.codeGenConfiguration.getOutputLanguage());

        writeFile(skeletonModel, skeletonInterfaceWriter);
    }

    protected Document createDOMDocumentForServiceXML() {
        Document doc = getEmptyDocument();
        String className = null;
        String serviceName = null;
        Element rootElement = doc.createElement("interfaces");
        doc.appendChild(rootElement);

        for (Iterator iter = this.axisServices.iterator(); iter.hasNext();) {
            this.axisService = (AxisService) iter.next();
            this.axisBinding = axisService.getEndpoint(axisService.getEndpointName()).getBinding();
            serviceName = axisService.getName();
            className = (String)axisService.getParameter(JAXWS_PORT_TYPE_NAME).getValue() + JAXWS_IMPL_SUFFIX;
            rootElement.appendChild(getServiceElement(serviceName, className, doc));
        }

        return doc;
    }

    /**
     * Creates the XML model for the Service Endpoint interface
     *
     * @return DOM Document
     */
    protected abstract Document createDOMDocumentForSEI() throws AxisFault;

     /**
     * Creates the XML model for the Service Endpoint interface Implementation class
     *
     * @return DOM Document
     */
    protected abstract Document createDOMDocumentForSEIImpl() throws AxisFault;

    /**
     * Creates the XML model for the Service Class
     *
     * @return DOM Document
     */
    protected abstract Document createDOMDocumentForServiceClass();

    /**
     * Creates the XML model for a Exception Class
     *
     * @param key String
     * @return DOM Document
     */
    protected abstract Document createDOMDocumentForException(String key);

    /**
     * A resusable method to return the service element for creating the service xml
     *
     * @param serviceName
     * @param className
     * @param doc
     * @return DOM Element
     */
    protected Element getServiceElement(String serviceName, String className, Document doc) {

        if (allServiceInfoHolder.get(serviceName) != null) {
            this.infoHolder = (Map) allServiceInfoHolder.get(serviceName);
        }
        Element rootElement = doc.createElement("interface");

        addAttribute(doc, "package", "", rootElement);
        addAttribute(doc, "classpackage", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", className, rootElement);

        if (!codeGenConfiguration.isWriteTestCase()) {
            addAttribute(doc, "testOmit", "true", rootElement);
        }
        addAttribute(doc, "servicename", serviceName, rootElement);

        Iterator it = mepToClassMap.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();

            if (Boolean.TRUE.equals(infoHolder.get(key))) {
                Element elt = addElement(doc, "messagereceiver",
                        makeJavaClassName(serviceName) + mepToSuffixMap.get(key),
                        rootElement);
                addAttribute(doc, "mepURI", key.toString(), elt);
            }

        }

        loadOperations(doc, rootElement, null);

        return rootElement;
    }

    //Util methods
    public String extratClassName(String fullyQualifiedName) {
        if (fullyQualifiedName == null) {
            return "";
        }

        String className = fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.'),
                fullyQualifiedName.length());

        if (className.charAt(0) == '.') {
            return className.substring(1);
        }

        return className;
    }

    protected String getFullyQualifiedName(String className, String packageName) {
//        className = makeJavaClassName(className);
        return packageName + "." + className;
    }

    protected String resolveNameCollision(String className, String packageName, String suffix) {
        className = makeJavaClassName(className);
        String fullQualifiedName = getFullyQualifiedName(className, packageName);
        Map map = mapper.getAllMappedNames();
        if (map.containsValue(fullQualifiedName)) {
            return className + suffix;
        }
        return className;
    }
}
