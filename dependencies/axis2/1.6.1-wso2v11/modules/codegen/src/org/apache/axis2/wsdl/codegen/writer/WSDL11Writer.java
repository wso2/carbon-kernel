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

package org.apache.axis2.wsdl.codegen.writer;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.FileWriter;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class WSDL11Writer {

    public static final String IMPORT_TAG = "import";
    public static final String INCLUDE_TAG = "include";
    public static final String SCHEMA_LOCATION = "schemaLocation";

    private File baseFolder = null;
    private int count;


    public WSDL11Writer(File baseFolder) {
        this.baseFolder = baseFolder;
        this.count = 0;
    }

    public void writeWSDL(AxisService axisService) {
        try {
            if (axisService != null) {
                //create a output file
                File outputFile = FileWriter.createClassFile(baseFolder,
                                                             null,
                                                             axisService.getName(),
                                                             ".wsdl");
                FileOutputStream fos = new FileOutputStream(outputFile);
                axisService.printWSDL(fos);
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("WSDL writing failed!", e);
        }
    }

    public void writeWSDL(AxisService axisService, Definition definition, Map changedMap) {
        try {
            if (axisService != null) {
                Map baseURIwsdlNameMap = new HashMap();
                // add the initial definition to the map
                baseURIwsdlNameMap.put(definition.getDocumentBaseURI(),axisService.getName() + ".wsdl");
                writeWSDL(definition, axisService.getName() + ".wsdl", changedMap, baseURIwsdlNameMap);
            }
        } catch (Exception e) {
            throw new RuntimeException("WSDL writing failed!", e);
        }
    }

    private void writeWSDL(Definition definition,
                           String fileName,
                           Map changedMap,
                           Map baseURIwsdlNameMap) throws Exception {
        // first process the imports and save them.
        Map imports = definition.getImports();
        if (imports != null && (imports.size() > 0)) {
            Vector importsVector = null;
            Import wsdlImport = null;
            String wsdlName = null;
            String wsdlLocation = null;
            for (Iterator improtsVectorIter = imports.values().iterator();
                 improtsVectorIter.hasNext();) {
                importsVector = (Vector)improtsVectorIter.next();
                for (Iterator importsIter = importsVector.iterator(); importsIter.hasNext();) {
                    wsdlImport = (Import)importsIter.next();
                    wsdlLocation = wsdlImport.getDefinition().getDocumentBaseURI();
                    // we have to process this wsdl file only if it has not been processed earlier
                    if (!baseURIwsdlNameMap.containsKey(wsdlLocation)) {
                        wsdlName = wsdlLocation.substring(wsdlLocation.lastIndexOf('/') + 1);
                        if (!wsdlName.endsWith(".wsdl") && !wsdlName.endsWith(".xsd")){
                           // this seems to be an online wsdl so we generate a dummy name
                           if (wsdlName.indexOf("xsd") > -1){
                               wsdlName = "xsd" + count++ + ".xsd";
                           } else {
                               wsdlName = "wsdl" + count++ + ".wsdl";
                           }
                        }

                        //trim the wsdl part
                        while (baseURIwsdlNameMap.containsValue(wsdlName)) {
                            // import file name can either be xsd or wsdl
                            String fileNamePart = wsdlName.substring(0,wsdlName.lastIndexOf("."));
                            String extension = wsdlName.substring(wsdlName.lastIndexOf("."));
                            wsdlName = fileNamePart + count++ + extension;
                        }
                        baseURIwsdlNameMap.put(wsdlLocation, wsdlName);
                        Definition innerDefinition = wsdlImport.getDefinition();
                        writeWSDL(innerDefinition, wsdlName, changedMap, baseURIwsdlNameMap);
                    }

                    wsdlImport.setLocationURI((String)baseURIwsdlNameMap.get(wsdlLocation));
                }
            }
        }
        // change the locations on the imported schemas
        adjustWSDLSchemaLocations(definition, changedMap);
        // finally save the file
        WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
        File outputFile = FileWriter.createClassFile(baseFolder,
                                                     null, fileName, null);
        FileOutputStream out = new FileOutputStream(outputFile);

        // we have a catch here
        // if there are multimple services in the definition object
        // we have to write only the relavent service.


        if (definition.getServices().size() > 1){
           List removedServices = new ArrayList();
           List servicesList = new ArrayList();

           Map services = definition.getServices();
           // populate the services list
           for (Iterator iter = services.values().iterator();iter.hasNext();){
               servicesList.add(iter.next());
           }
           Service service;
           String serviceNameFromFileName = fileName;
           if (fileName.indexOf(".wsdl") > -1){
                serviceNameFromFileName = fileName.substring(0,fileName.lastIndexOf(".wsdl"));
           }

           if (fileName.indexOf(".xsd") > -1){
               serviceNameFromFileName = fileName.substring(0,fileName.lastIndexOf(".xsd"));
           }
           for (Iterator iter = servicesList.iterator();iter.hasNext();){
               service = (Service) iter.next();
               if (!service.getQName().getLocalPart().equals(serviceNameFromFileName)){
                   definition.removeService(service.getQName());
                   removedServices.add(service);
               }
           }

           //now we have only the required service so write it
           wsdlWriter.writeWSDL(definition, out);

           // again add the removed services
           for (Iterator iter = removedServices.iterator(); iter.hasNext();){
               service = (Service) iter.next();
               definition.addService(service);
           }
        } else {
           // no problem proceed normaly
           wsdlWriter.writeWSDL(definition, out);
        }
        out.flush();
        out.close();
    }

    /**
     * @deprecated  please use adjustWSDLSchemaLocations
     * @param definition
     * @param changedSchemaLocations
     */
    public void adjustWSDLSchemaLocatins(Definition definition, Map changedSchemaLocations) {
        adjustWSDLSchemaLocations(definition, changedSchemaLocations);
    }

    /**
     * adjust the schema locations in the original wsdl
     *
     * @param definition
     * @param changedSchemaLocations
     */
    public void adjustWSDLSchemaLocations(Definition definition, Map changedSchemaLocations) {
        Types wsdlTypes = definition.getTypes();
        if (wsdlTypes != null) {
            List extensibilityElements = wsdlTypes.getExtensibilityElements();
            Object currentObject;
            Schema schema;
            for (Iterator iter = extensibilityElements.iterator(); iter.hasNext();) {
                currentObject = iter.next();
                if (currentObject instanceof Schema) {
                    schema = (Schema)currentObject;
                    changeLocations(schema.getElement(), changedSchemaLocations);
                }
            }
        }
    }

    private void changeLocations(Element element, Map changedSchemaLocations) {
        NodeList nodeList = element.getChildNodes();
        String tagName;
        for (int i = 0; i < nodeList.getLength(); i++) {
            tagName = nodeList.item(i).getLocalName();
            if (IMPORT_TAG.equals(tagName) || INCLUDE_TAG.equals(tagName)) {
                processImport(nodeList.item(i), changedSchemaLocations);
            }
        }
    }

    private void processImport(Node importNode, Map changedSchemaLocations) {
        NamedNodeMap nodeMap = importNode.getAttributes();
        Node attribute;
        String attributeValue;
        for (int i = 0; i < nodeMap.getLength(); i++) {
            attribute = nodeMap.item(i);
            if (attribute.getNodeName().equals("schemaLocation")) {
                attributeValue = attribute.getNodeValue();
                if (changedSchemaLocations.get(attributeValue) != null) {
                    attribute.setNodeValue(
                            (String)changedSchemaLocations.get(attributeValue));
                }
            }
        }
    }


}
