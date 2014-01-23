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

package org.apache.ideaplugin.bean;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;

public class ArchiveBean {

    private boolean singleService = false;
    private boolean generetServiceDesc = false;
    private ArrayList classLocation = new ArrayList();
    private File classLoc;
    private ClassLoader classLoader;
    private String ServiceXML;
    public String fileSeparator = System.getProperty("file.separator");
    private ArrayList libs = new ArrayList();
    private ArrayList tempLibs = new ArrayList();
    private ArrayList tempWsdls = new ArrayList();
    private ArrayList wsdls = new ArrayList();
    private String outPath;
    private String archiveName;
    private ArrayList servicelsit = new ArrayList();
    private boolean includeClass=false;

    public ArrayList getTempWsdls() {
        return tempWsdls;
    }

    public void setTempWsdls(ArrayList tempWsdls) {
        this.tempWsdls = tempWsdls;
    }

    public ArrayList getTempLibs() {
        return tempLibs;
    }

    public void setTempLibs(ArrayList tempLibs) {
        this.tempLibs = tempLibs;
    }

    public File getClassLoc() {
        return classLoc;
    }

    public void setClassLoc(File classLoc) {
        this.classLoc = classLoc;
    }

    public ArrayList getServicelsit() {
        return servicelsit;
    }

    public void addToServicelsit(ServiceObj service) {
        for (int count =0;count<servicelsit.size();count++)
        {
            if (((ServiceObj)servicelsit.get(count)).getServiceName().equalsIgnoreCase(service.getServiceName()))
            {
                servicelsit.remove(count);
                servicelsit.add(service);
                return;
            }

        }
        servicelsit.add(service);
    }

    public boolean isSingleService() {
        return singleService;
    }

    public void setSingleService(boolean singleService) {
        this.singleService = singleService;
    }

    public boolean isGeneretServiceDesc() {
        return generetServiceDesc;
    }

    public void setGeneretServiceDesc(boolean generetServiceDesc) {
        this.generetServiceDesc = generetServiceDesc;
    }

    public ArrayList getClassLocation() {
        return classLocation;
    }

    public void addClassLocation(File classLocation) {
        this.classLocation.add(classLocation);
    }

    public String getServiceXML() {
        return ServiceXML;
    }

    public void setServiceXML(String serviceXML) {
        ServiceXML = serviceXML;
    }

    public ArrayList getLibs() {
        return libs;
    }

    public void addLibs(ArrayList libs) {
        this.libs.addAll(libs);
    }

    public ArrayList getWsdls() {
        return wsdls;
    }

    public void addWsdls(File wsdl) {
        this.wsdls.add(wsdl);
    }

    public String getOutPath() {
        return outPath;
    }

    public void setOutPath(String outPath) {
        this.outPath = outPath;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }
    public void setIncludeClass(boolean includeClass){
        this.includeClass=includeClass;
    }
    public boolean getIncludeClass(){
        return this.includeClass;
    }


    public void finsh() throws Exception {
        //Creating out File
        try {
            File outFile = new File(getOutPath());
            String time = Calendar.getInstance().getTime().toString().replace(':', '-');
            File tempfile = new File(outFile, "temp-" + time);
            tempfile.mkdir();
            //creating META-INF
            File metainf = new File(tempfile, "META-INF");
            if (!metainf.exists()) {
                metainf.mkdir();
            }

            // Writing services.xml
            File servicexml = new File(metainf, "services.xml");
            FileWriter writer = new FileWriter(servicexml);
            writer.write(getServiceXML());
            writer.flush();
            writer.close();

            //Coping class files
            FileCopier fc = new FileCopier();
            if(includeClass){
                for (int count=0;count<classLocation.size();count++)
                    fc.copyFiles((File)classLocation.get(count), tempfile, ".class");
            }else{
                for (int count=0;count<classLocation.size();count++)
                    fc.copyFiles((File)classLocation.get(count), tempfile, null);
            }
            // Coping wsdl files
            File lib = new File(tempfile, "lib");
            if (!lib.exists()) {
                lib.mkdir();
            }
            if (libs!=null)
            {
            for (int i = 0; i < libs.size(); i++) {
                      String libname = (String) libs.get(i);
                      fc.copyFiles(new File(libname), lib, null);
            }
            }

            //coping wsdl files
            if (wsdls!=null)
            for (int i = 0; i < wsdls.size(); i++) {
                File libname = (File)wsdls.get(i);
                fc.copyFiles(libname, metainf, null);
            }

            String arcivename = getArchiveName();
            if (arcivename.indexOf(".aar") < 0) {
                arcivename = arcivename + ".aar";
            }
            JarFileWriter jwriter = new JarFileWriter();
            jwriter.writeJarFile(outFile, arcivename, tempfile);
            //craeting the jar file
            deleteDir(tempfile);

        } catch (Exception e) {
            throw e;
        }
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }


}
