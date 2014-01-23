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

package org.apache.axis2.jaxws.util;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.StringTokenizer;


/**
 * This class is the base for an implementation of a WSDL4J interface that 
 * will be supplied to a WSDLReader instance. Its primary goal is to assist 
 * with locating imported WSDL documents.
 */
public abstract class BaseWSDLLocator {
    
    private static Log log = LogFactory.getLog(BaseWSDLLocator.class);

    protected String baseURI, lastestImportURI;
    protected InputStream baseInputStream;

    /**
     * Returns an InputStream pointed at an imported wsdl pathname relative
     * to the parent resource or loadStrategy.
     * 
     * @param importPath identifies the WSDL file within the context 
     * @return an stream of the WSDL file
     */
    abstract protected InputStream getInputStream(String importPath) throws IOException;

    /**
     * Allows for a level of indirection, such as a catalog, when importing URIs.
     * 
     * @param importURI a URI specifying the document to import
     * @param parent a URI specifying the location of the parent document doing
     * the importing
     * @return the resolved import location, or null if no indirection is performed
     */
    abstract protected String getRedirectedURI(String importURI, String parent);
    
    /**
      * Returns an InputSource "pointed at" the base document.
      */
    public InputSource getBaseInputSource() {
        return new InputSource(baseInputStream);
    }

    /**
     * Returns an InputSource pointed at an imported wsdl document whose
     * parent document was located at parentLocation and whose
     * relative location to the parent document is specified by
     * relativeLocation.
     *
     * @param parentLocation a URI specifying the location of the
     * document doing the importing.
     * @param relativeLocation a URI specifying the location of the
     * document to import, relative to the parent document's location.
     */
    public InputSource getImportInputSource(String parentLocation, String relativeLocation) {
        if (log.isDebugEnabled()) {
            log.debug("getImportInputSource, parentLocation= " + parentLocation + 
                    " relativeLocation= " + relativeLocation);
        }
        InputStream is = null;
        URL absoluteURL = null;

        String redirectedURI = getRedirectedURI(relativeLocation, parentLocation);
        if  (redirectedURI != null)
        	relativeLocation = redirectedURI;
        
        try {
            if (isAbsoluteImport(relativeLocation)) {
                try{
                    absoluteURL = new URL(relativeLocation);
                    is = absoluteURL.openStream();
                    lastestImportURI = absoluteURL.toExternalForm();
                }
                catch(Throwable t){
                    if (relativeLocation.startsWith("file://")) {
                        try {
                            relativeLocation = "file:/" + relativeLocation.substring("file://".length());
                            absoluteURL = new URL(relativeLocation);
                            is = absoluteURL.openStream();
                            lastestImportURI = absoluteURL.toExternalForm();
                        } catch (Throwable t2) {
                        }
                    }
                }
                if(is == null){
                    try{
                        URI fileURI = new URI(relativeLocation);
                        absoluteURL = fileURI.toURL();
                        is = absoluteURL.openStream();  
                        lastestImportURI = absoluteURL.toExternalForm();
                    }
                    catch(Throwable t){
                        //No FFDC code needed  
                    }
                }
                if(is == null){
                    try{
                        File file = new File(relativeLocation);
                        absoluteURL = file.toURL();
                        is = absoluteURL.openStream();  
                        lastestImportURI = absoluteURL.toExternalForm();
                    }
                    catch(Throwable t){
                        //No FFDC code needed           
                    }
                }
                
            } else {
                String importPath = normalizePath(parentLocation, relativeLocation);
                is = getInputStream(importPath);
                lastestImportURI = importPath;
            }
        } catch (IOException ex) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("WSDLRelativeErr1", 
                                        relativeLocation, 
                                        parentLocation, 
                                        ex.toString()));
        }
        if(is == null){
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("WSDLRelativeErr2", 
                                        relativeLocation, 
                                        parentLocation));
        }
        if(log.isDebugEnabled()){
            log.debug("Loaded file: " + relativeLocation);
        }
        return new InputSource(is);
    }

    /**
     * Returns a URI representing the location of the base document.
     */
    public String getBaseURI() {
        return baseURI;

    }

    /**
     * Returns a URI representing the location of the last import document
     * to be resolved. This is useful when resolving nested imports.
     */
    public String getLatestImportURI() {
        return lastestImportURI;
    }

    /*
     * @param rawURI the uri for base wsdl file, which could be the form of
     *          META-INF/base.wsdl or just base.wsdl, but it can be /base.wsdl (no leading slash according to spec)
     * @return the uri which is one level up the raw uri with the trailing slash (if not empty)
     * 
     */
    protected String convertURI(String rawURI) {
    	int idx = rawURI.lastIndexOf('/');
        if(idx > 0) {
        	rawURI = rawURI.substring(0, idx + 1);
            return rawURI;
        }
        // this may be an absolute file reference
        else {
        	idx = rawURI.lastIndexOf('\\');
        	if(idx > 0) {
        		rawURI = rawURI.substring(0, idx + 1);
                return rawURI;
        	}
        	return "";
        }
    }

    protected boolean isAbsoluteImport(String uri) {
        boolean absolute = false;
        if(uri != null){
            if(uri.indexOf(":/") != -1){
                absolute = true;
            }
            else if(uri.indexOf(":\\") != -1){
                absolute = true;
            } 
        }
        
        return absolute;
    }

    /**
     * The ZipFile can not handle relative imports of that have directory components 
     * of the form "..".  Given a 'relativeLocation' relative to 'parentLocation', replace
     * any ".." with actual path component names.
     * @param parentLocation Path relative to the module root of the file that is doing the
     *                       importing.
     * @param relativeLocation Path relative to the parentLocation of the file that is being imported.
     * @return String contatining the path to the file being imported (i.e. relativeLocation) that is
     *         relative to the module root and has all ".." and "." path components removed and replaced
     *         with the corresponding actual directory name.
    */
    private static final char WSDL_PATH_SEPERATOR_CHAR = '/';
    private static final String WSDL_PATH_SEPERATOR =
        (Character.valueOf(WSDL_PATH_SEPERATOR_CHAR)).toString();

    protected String normalizePath(String parentLocation, String relativeLocation) {
        if (log.isDebugEnabled()) {
            log.debug("normalizePath, parentLocation= " + parentLocation + 
                    " relativeLocation= " + relativeLocation);
        }
        // Get the path from the module root to the directory containing the importing WSDL file.
        // Note this path will end in a "/" and will not contain any ".." path components.
        String pathFromRoot = convertURI(parentLocation);

        // Construct the path to the location relative to the module root based on the parent location, 
        // removing any ".." or "." path components.
        StringBuffer pathToRelativeLocation = new StringBuffer(pathFromRoot);
        StringTokenizer tokenizedRelativeLocation =
            new StringTokenizer(relativeLocation, WSDL_PATH_SEPERATOR);
        if (log.isDebugEnabled()) {
            log.debug("pathFromRoot = " + pathFromRoot);
            log.debug("relativeLocation = " + relativeLocation);
        }
        while (tokenizedRelativeLocation.hasMoreTokens()) {
            String nextToken = tokenizedRelativeLocation.nextToken();
            if (nextToken.equals("..")) {
                // Relative parent directory, so chop off the last path component in the path to back
                // up to the parent directory.  First delete the trailing "/" from the path if there 
                // is one, then delete characters from the end of the path until we find the next "/".
                int charToDelete = pathToRelativeLocation.length() - 1;
                if (pathToRelativeLocation.charAt(charToDelete) == WSDL_PATH_SEPERATOR_CHAR || 
                		pathToRelativeLocation.charAt(charToDelete) == '\\') {
                    pathToRelativeLocation.deleteCharAt(charToDelete--);
                }
                while (pathToRelativeLocation.charAt(charToDelete) != WSDL_PATH_SEPERATOR_CHAR && 
                		pathToRelativeLocation.charAt(charToDelete) != '\\') {
                    pathToRelativeLocation.deleteCharAt(charToDelete--);
                }
            } else if (nextToken.equals(".")) {
                // Relative current directory, do not add or delete any path components
            } else {
                // Make sure the current path ends in a "/"  or "\\" then append this path component
            	// This handles locations within the module and URIs
                if ((pathToRelativeLocation.indexOf(String.valueOf(WSDL_PATH_SEPERATOR_CHAR)) 
                		!= -1) && (pathToRelativeLocation.charAt(pathToRelativeLocation.length() 
                				- 1)!= WSDL_PATH_SEPERATOR_CHAR)) {
                    pathToRelativeLocation.append(WSDL_PATH_SEPERATOR_CHAR);
                }
                // This handles file based locations
                else if((pathToRelativeLocation.indexOf("\\") != -1) && (pathToRelativeLocation.
                		charAt(pathToRelativeLocation.length() -1) != '\\')) {
                	pathToRelativeLocation.append('\\');
                }
                pathToRelativeLocation.append(nextToken);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Built path = " + pathToRelativeLocation.toString());
        }
        return pathToRelativeLocation.toString();
    }
}
