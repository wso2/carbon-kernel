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

package org.apache.axis2.tool.service.control;

import org.apache.axis2.tool.core.ClassFileHandler;
import org.apache.axis2.tool.core.FileCopier;
import org.apache.axis2.tool.core.JarFileWriter;
import org.apache.axis2.tool.core.ServiceXMLCreater;
import org.apache.axis2.tool.service.bean.ClassFileSelectionBean;
import org.apache.axis2.tool.service.bean.LibrarySelectionBean;
import org.apache.axis2.tool.service.bean.Page2Bean;
import org.apache.axis2.tool.service.bean.Page3Bean;
import org.apache.axis2.tool.service.bean.WSDLFileLocationBean;
import org.apache.axis2.tool.service.bean.WizardBean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    public ArrayList getMethodList(WizardBean bean) throws ProcessException {
        ArrayList returnList = null;
        try {
            returnList =
                    new ClassFileHandler().getMethodNamesFromClass(
                            bean.getPage2bean().getAutomaticClassName(),
                            bean.getPage1bean().getFileLocation());
        } catch (IOException e) {
            throw new ProcessException(
                    "IO Error, The class file location may be faulty!", e);
        } catch (ClassNotFoundException e) {
            throw new ProcessException(
                    " The specified class does not exist!!!");
        } catch (Exception e) {
            throw new ProcessException(
                    "Unknown Error! See whether all parameters are available");
        }
        return returnList;
    }


    public void process(WizardBean bean) throws ProcessException, Exception {

        ClassFileSelectionBean page1Bean = bean.getPage1bean();
        WSDLFileLocationBean wsdlBean = bean.getWsdlBean();
        LibrarySelectionBean libBean = bean.getLibraryBean();
        Page2Bean page2Bean = bean.getPage2bean();
        Page3Bean page3Bean = bean.getPage3bean();

        File serviceFile = null;
        File wsdlFile = null;
        File classFileFolder = null;
        File outputFolder = null;
        String outputFileName = null;
        boolean isServiceCreated = false;
        boolean isWSDLAvailable = false;

        //see if the class file location is valid
        classFileFolder = new File(page1Bean.getFileLocation());
        if (!classFileFolder.exists()) {
            throw new ProcessException(
                    "Specified Class file location is empty!!");
        }
        if (!classFileFolder.isDirectory()) {
            throw new ProcessException(
                    "The class file location must be a folder!");
        }

        //see if the  service.xml file is valid
        if (page2Bean.isManual()) {
            serviceFile = new File(page2Bean.getManualFileName());
            if (!serviceFile.exists()) {
                throw new ProcessException(
                        "Specified Service XML file is missing!");
            }
        } else {
            ArrayList methodList = page2Bean.getSelectedMethodNames();
            if (methodList.isEmpty()) {
                throw new ProcessException(
                        "There are no methods selected to generate the service!!");
            }
            String currentUserDir = System.getProperty("user.dir");
            String fileName = "services.xml";
            ServiceXMLCreater serviceXMLCreater = new ServiceXMLCreater(page2Bean.getServiceName(),
            												 page2Bean.getAutomaticClassName(),
            												 page2Bean.getSelectedMethodNames());
            String serviceFileString = serviceXMLCreater.toString();
            	serviceFile = new File(currentUserDir + File.separator + fileName);
            if (serviceFile.exists()){serviceFile.delete();}
            FileWriter serviceXMLFileWriter = new FileWriter(serviceFile, true);
            BufferedWriter writer = new BufferedWriter(serviceXMLFileWriter) ;
            writer.write(serviceFileString) ;
            writer.close() ;
            
//                    new ServiceFileCreator().createServiceFile(
//                            page2Bean.getServiceName(),
//                            page2Bean.getAutomaticClassName(),
//                            page2Bean.getSelectedMethodNames());//create the file here
            	
            isServiceCreated = true;
        }

        //see if the WSDL file is available
        if (!wsdlBean.isSkip()){
            wsdlFile = new File(wsdlBean.getWSDLFileName());
            if (!wsdlFile.exists()) {
                throw new ProcessException(
                        "Specified WSDL file is missing!");
            }else{
                isWSDLAvailable = true;
            }
        }
        
        List fileList = new ArrayList();
        //check the libs
        if (libBean!=null){
            String[] files = libBean.getFileList();
            File tempFile = null;
            if (files!=null){
               for (int i=0;i<files.length;i++){
                 tempFile = new File(files[i]); 
                 if (!tempFile.exists()||tempFile.isDirectory()){
                     throw new ProcessException("Invalid libraries");
                 }else{
                     fileList.add(tempFile);
                 }
               }
            }
        }
        
        outputFolder = new File(page3Bean.getOutputFolderName());
        outputFileName = page3Bean.getOutputFileName();
        if (!outputFileName.toLowerCase().endsWith(".jar") && !outputFileName.toLowerCase().endsWith(".aar")) {
            outputFileName = outputFileName + ".aar";
        }

        File tempFileFolder = null;
        String xmlFilter = ".xml";
        String wsdlFilter = ".wsdl";

        try {
        	String metaInfFolderName="META-INF";
        	String libFolderName="lib";
        	
            //create a temporary directory and copy the files
            tempFileFolder = File.createTempFile("temp",".tmp");
            tempFileFolder.deleteOnExit();
            if (tempFileFolder.exists()){deleteDir(tempFileFolder);}
            tempFileFolder.mkdir();
            
            File metaInfFolder = new File(tempFileFolder, metaInfFolderName);
            metaInfFolder.mkdir();
            
            File libFolder = new File(tempFileFolder,libFolderName);
            libFolder.mkdir();
            
            FileCopier classFilecopier = new FileCopier();
            //copy the classes
            classFilecopier.copyFiles(classFileFolder, tempFileFolder,page1Bean.getFilter());
           
            //copy the service.xml
            FileCopier serviceXMLcopier = new FileCopier();
            serviceXMLcopier.copyFiles(serviceFile, metaInfFolder,xmlFilter);
           
            //copy the libs
            FileCopier libCopier = new FileCopier();
            for (int i=0;i < fileList.size();i++){
            	libCopier.copyFiles((File)fileList.get(i),libFolder,null);
            }
            
            if (isWSDLAvailable){
                new FileCopier().copyFiles(wsdlFile, metaInfFolder,wsdlFilter);
            }
            //jar the temp directory. the output folder will be created if missing
            
            new JarFileWriter().writeJarFile(outputFolder,
                    outputFileName,
                    tempFileFolder);
        } catch (Exception e) {
            throw new ProcessException(e);
        } finally {
            deleteDir(tempFileFolder);
             if (isServiceCreated){
                serviceFile.delete();
             }
        }

    }
    
    private  boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }
}
