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

package org.apache.axis2.tool.core;

import org.apache.axis2.wsdl.codegen.writer.FileWriter;
import org.apache.axis2.wsdl.codegen.writer.ServiceXMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ServiceFileCreator {
    
    public File createServiceFile(String serviceName,String implementationClassName,ArrayList methodList) throws Exception {
        
        String currentUserDir = System.getProperty("user.dir");
        String fileName = "services.xml";
        
        FileWriter serviceXmlWriter = new ServiceXMLWriter(currentUserDir);
        writeFile(getServiceModel(serviceName,implementationClassName,methodList),serviceXmlWriter,fileName);

        return new File(currentUserDir + File.separator + fileName);
    }

    private Document getServiceModel(String serviceName,String className,ArrayList methods){

        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document doc = builder.newDocument();
        
        Element rootElement = doc.createElement("interface");
        rootElement.setAttribute("classpackage","");
        rootElement.setAttribute("name",className);
        rootElement.setAttribute("servicename",serviceName);
        Element methodElement = null;
        int size = methods.size();
        for(int i=0;i<size;i++){
            methodElement = doc.createElement("method");
            rootElement.setAttribute("name",methods.get(i).toString());
            rootElement.appendChild(methodElement);
        }
        doc.appendChild(rootElement);
        return doc;
    }
    
    /**
     * A resusable method for the implementation of interface and implementation writing
     * @param model
     * @param writer
     * @throws IOException
     * @throws Exception
     */
    private void writeFile(Document model, FileWriter writer,String fileName) throws IOException,Exception {
        
        Source source = new DOMSource(model);
        ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
        Result result = new StreamResult(memoryStream);
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
        
        //TODO: Doesn't really output stuff from the memorystream to file...hmm.
        
        writer.loadTemplate();
        writer.createOutFile(null,
                 fileName);
    }
    
   

}
