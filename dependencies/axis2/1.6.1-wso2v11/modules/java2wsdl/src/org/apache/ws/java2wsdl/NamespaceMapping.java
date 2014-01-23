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

package org.apache.ws.java2wsdl;

import org.apache.axiom.om.util.StAXUtils;
import org.apache.tools.ant.BuildException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Used for nested package definitions.
 * The file format used for storing mappings is a list of package=namespace
 */
public class NamespaceMapping implements Mapper {
    public static final QName PKG2NS_MAPPINGS = new QName("http://ws.apache.org/axis2", "pkg2ns_mapping");
    public static final QName MAPPING = new QName("http://ws.apache.org/axis2", "mapping");
    public static final String NAMESPACE = "namespace";
    public static final String PACKAGE = "package";

    private String namespace = null;
    private String packageName = null;
    private File mappingFile;
    private InputStream mapInputStream = null;
    private XMLStreamReader mapXmlReader = null;

    /**
     * pass in the namespace to map to
     */
    public NamespaceMapping() {
    }

    /**
     * the namespace in the WSDL. Required.
     * @param value new uri of the mapping
     */
    public void setNamespace(String value) {
        namespace = value;
    }

    /**
     * the Java package to bind to. Required.
     * @param value java package name
     */
    public void setPackage(String value) {
        packageName = value;
    }

    /**
     * name of a property file that contains mappings in
     * package=namespace format
     * @param file file to load
     */
    public void setFile(File file) {
        mappingFile = file;
    }

    /**
     * map a namespace to a package
     * @param map map to assign to
     * @param packName package name
     * @param nspace namespace
     * @param packageIsKey if the package is to be the key for the map
     */
    protected void map(HashMap map,
                       String packName,
                       String nspace,
                       boolean packageIsKey) {
        if(packageIsKey) {
            map.put(packName,nspace);
        } else {
            map.put(nspace, packName);
        }
    }

    /**
     * validate the option set
     */
    private void validate() {
        if (mappingFile != null || mapInputStream != null || mapXmlReader != null ) {
            if (namespace != null || packageName != null) {
                throw new BuildException(
                        "Namespace or Package cannot be used with a File attribute");
            }
        } else {
            if (namespace == null) {
                throw new BuildException("namespace must be defined");
            }
            if (packageName == null) {
                throw new BuildException("package must be defined");
            }
        }
    }

    /**
     * Load a mapping xml reader and save it to the map
     * @param map target map file
     * @param packageIsKey if the package is to be the key for the map
     * @throws BuildException if an IOException needed swallowing
     */
    protected void mapXmlReader(HashMap map, boolean packageIsKey) throws BuildException {
        try {
            loadMappingFromXMLReader(map, packageIsKey); 
        } catch ( Exception e ) {
            throw new BuildException(e);
        }
    }
   
    
    /**
     * Load a mapping input stream  and save it to the map
     * @param map target map file
     * @param packageIsKey if the package is to be the key for the map
     * @throws BuildException if an IOException needed swallowing
     */
    protected void mapXmlStream(HashMap map, boolean packageIsKey) throws BuildException {
        try {
            mapXmlReader = StAXUtils.createXMLStreamReader(mapInputStream);
            mapXmlReader(map, packageIsKey);
            //ensure this clean up so that the next invocation does not have any stale state
            mapXmlReader = null;
        } catch ( Exception e ) {
            throw new BuildException(e);
        }
    }
    
    /**
     * Load a mapping file and save it to the map
     * @param map target map file
     * @param packageIsKey if the package is to be the key for the map
     * @throws BuildException if an IOException needed swallowing
     */
    protected void mapFile(HashMap map, boolean packageIsKey) throws BuildException {
        try {
            mapInputStream = new FileInputStream(mappingFile);
            mapXmlStream(map, packageIsKey); 
            //need to do this since the file was opened here
            mapInputStream.close();
            //ensure this clean up so that the next invocation does not have any stale state
            mapInputStream = null;
        } catch ( Exception e ) {
            throw new BuildException(e);
        }
    }
    
    /**
     * load from an xml reader containing mapping info
     * @return a properties file with zero or more mappings
     * @throws BuildException if the load failed
     */
    private void loadMappingFromXMLReader(HashMap map, boolean packageIsKey) throws BuildException {
        try {
                QName parentElement = null;
                //if the reader is in the fragment that deals with package to namespace mappings
                while (mapXmlReader.hasNext()) {
                   // mapXmlReader.next();mapXmlReader.next();
                   //  if ( mapXmlReader.getName().equals(PKG2NS_MAPPINGS) ) {
                        switch (mapXmlReader.next()) {
                            case XMLStreamConstants.START_ELEMENT :
                                QName qname = mapXmlReader.getName();
                                if (MAPPING.equals(qname) && parentElement.equals(PKG2NS_MAPPINGS)) {
                                    map(map, 
                                        mapXmlReader.getAttributeValue(null, PACKAGE), 
                                        mapXmlReader.getAttributeValue(null, NAMESPACE), 
                                        packageIsKey);
                                } else {
                                    parentElement = qname;
                                }
                                
                                mapXmlReader.next();
                                break;
                            case XMLStreamConstants.END_ELEMENT:
                                if (mapXmlReader.getName().equals(PKG2NS_MAPPINGS)) {
                                    //ensure this clean up
                                    return;
                                }
                                break;
                   //     }
                }
            }
        } catch ( Exception e ) {
            System.out.println("Exception while loading package to namespace mappings... " + e);
        }
    }

    /**
     * execute the mapping
     * @param map map to map to
     * @param packageIsKey if the package is to be the key for the map
     * @throws BuildException in case of emergency
     */
    public void execute(HashMap map, boolean packageIsKey) throws BuildException {
        validate();
        if (mappingFile != null ) {
            mapFile(map,packageIsKey);
            //ensure this clean up so that the next invocation does not have any stale state
            mappingFile = null;
        } else if ( mapInputStream != null ) {
            mapXmlStream(map, packageIsKey);   
        } else if ( mapXmlReader != null ) {
            mapXmlReader(map, packageIsKey);
        } else {
            map(map, packageName, namespace, packageIsKey);
        }
    }

    public InputStream getMapInputStream() {
        return mapInputStream;
    }

    public void setMapInputStream(InputStream mapInputStream) {
        this.mapInputStream = mapInputStream;
    }

    public File getMappingFile() {
        return mappingFile;
    }

    public void setMappingFile(File mappingFile) {
        this.mappingFile = mappingFile;
    }

    public XMLStreamReader getXmlReader() {
        return mapXmlReader;
    }

    public void setXmlReader(XMLStreamReader xmlReader) {
        this.mapXmlReader = xmlReader;
    }


}