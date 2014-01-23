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

package org.apache.axis2.jaxws.description.impl;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.java.security.AccessController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

/** This class is used to locate xml schemas that are imported by wsdl documents. */
public class URIResolverImpl implements URIResolver {

    private static final String HTTP_PROTOCOL = "http";

    private static final String HTTPS_PROTOCOL = "https";

    private static final String FILE_PROTOCOL = "file";

    private static final String JAR_PROTOCOL = "jar";
    
    private static final String BUNDLE_RESOURCE_PROTOCOL = "bundleresource";

    protected ClassLoader classLoader;
    
    private static final Log log = LogFactory.getLog(URIResolverImpl.class);

    public URIResolverImpl() {
    }

    public URIResolverImpl(ClassLoader cl) {
        classLoader = cl;
    }

    public InputSource resolveEntity(String namespace, String schemaLocation,
                                     String baseUri) {
        if (log.isDebugEnabled()) {
            log.debug("resolveEntity: ["+ namespace + "]["+ schemaLocation + "][ " + baseUri+ "]");
        }
        
        InputStream is = null;
        URI pathURI = null;
        String pathURIStr = null;
        if(log.isDebugEnabled()) {
            log.debug("Import location: " + schemaLocation + " parent document: " + 
                    baseUri);
        }
        if (baseUri != null) {
            try {
                // if the location is an absolute path, build a URL directly
                // from it
            	if(log.isDebugEnabled()){
            		log.debug("Base URI not null");
            	}
                if (isAbsolute(schemaLocation)) {
                    if(log.isDebugEnabled()) {
                        log.debug("Retrieving input stream for absolute schema location: "
                                + schemaLocation);
                    }
                    is = getInputStreamForURI(schemaLocation);
                }

                else {
                    if(log.isDebugEnabled()){
                        log.debug("schemaLocation not in absolute path");
                    }
                    try{
                        pathURI = new URI(baseUri);
                    }catch(URISyntaxException e){
                        // Got URISyntaxException, Creation of URI requires 
                        // that we use special escape characters in path.
                        // The URI constructor below does this for us, so lets use that.
                        if(log.isDebugEnabled()){
                            log.debug("Got URISyntaxException. Exception Message = "+e.getMessage());
                            log.debug("Implementing alternate way to create URI");
                        }
                       pathURI = new URI(null, null, baseUri, null);
                     }
                    pathURIStr = schemaLocation;
                    // If this is absolute we need to resolve the path without the 
                    // scheme information
                    if (pathURI.isAbsolute()) {
                        if(log.isDebugEnabled()) {
                            log.debug("Parent document is at absolute location: " + 
                                    pathURI.toString());
                        }
                        URL url = new URL(baseUri);
                        if (url != null) {
                            URI tempURI;
                            try{
                                tempURI = new URI(url.getPath());
                            }catch(URISyntaxException e){
                                //Got URISyntaxException, Creation of URI requires 
                                // that we use special escape characters in path.
                                // The URI constructor below does this for us, so lets use that.
                                if(log.isDebugEnabled()){
                                    log.debug("Got URISyntaxException. Exception Message = "+e.getMessage());
                                    log.debug("Implementing alternate way to create URI");
                                }
                                tempURI = new URI(null, null, url.getPath(), null);
                            }
                            URI resolvedURI = tempURI.resolve(schemaLocation);
                            // Add back the scheme to the resolved path
                            pathURIStr = constructPath(url, resolvedURI);
                            if(log.isDebugEnabled()) {
                                log.debug("Resolved this path to imported document: " + 
                                        pathURIStr);
                            }
                        }
                    } else {
                        if(log.isDebugEnabled()) {
                            log.debug("Parent document is at relative location: " + 
                                    pathURI.toString());
                        }
                        pathURI = pathURI.resolve(schemaLocation);
                        pathURIStr = pathURI.toString();
                        if(log.isDebugEnabled()) {
                            log.debug("Resolved this path to imported document: " + 
                                    pathURIStr);
                        }
                    }
                    // If path is absolute, build URL directly from it
                    if (isAbsolute(pathURIStr)) {
                        is = getInputStreamForURI(pathURIStr);
                    }

                    // if the location is relative, we need to resolve the
                    // location using
                    // the baseURI, then use the loadStrategy to gain an input
                    // stream
                    // because the URI will still be relative to the module
                    if(is == null) {
                        is = classLoader
                                .getResourceAsStream(pathURI.toString());
                    }
                }
            } catch (Exception e) {
                if(log.isDebugEnabled()) {
                	log.debug("Exception occured in resolveEntity, ignoring exception continuing processing "+e.getMessage());
                    log.debug(e);
                }
            }
        }
        if(is == null) {
            if(log.isDebugEnabled()) {
                log.debug("XSD input stream is null after resolving import for: " + 
                        schemaLocation + " from parent document: " + baseUri);
            }
        }
        else {
            if(log.isDebugEnabled()) {
                log.debug("XSD input stream is not null after resolving import for: " + 
                        schemaLocation + " from parent document: " + baseUri);
            }
        }

        InputSource returnInputSource = new InputSource(is);
        // We need to set the systemId.  XmlSchema will use this value to maintain a collection of
        // imported XSDs that have been read.  If this value is null, then circular XSDs will 
        // cause infinite recursive reads.
        returnInputSource.setSystemId(pathURIStr != null ? pathURIStr : schemaLocation);
        
        if (log.isDebugEnabled()) {
            log.debug("returnInputSource :" + returnInputSource.getSystemId());    
        }
        
        return returnInputSource;
    }

    /**
     * Checks to see if the location given is an absolute (actual) or relative path.
     *
     * @param location
     * @return
     */
    protected boolean isAbsolute(String location) {
        boolean absolute = false;
        if (location.indexOf(":/") != -1) {
            absolute = true;
        } else if (location.indexOf(":\\") != -1) {
            absolute = true;
        } else if (location.indexOf("file:") != -1) {
            absolute = true;
        }
        return absolute;
    }

    /**
     * Gets input stream from the uri given. If we cannot find the stream, <code>null</code> is
     * returned.
     *
     * @param uri
     * @return
     */
    protected InputStream getInputStreamForURI(String uri) {
        URL streamURL = null;
        InputStream is = null;
        URI pathURI = null;

        try {
            streamURL = new URL(uri);
            is = openStream_doPriv(streamURL);
        } catch (Throwable t) {
            //Exception handling not needed
            if (log.isDebugEnabled()) {
                log.debug("Exception occured in getInputStreamForURI, ignoring exception continuing processing: "+t.getMessage());
            }
        }

        if (is == null) {
            try {
                pathURI = new URI(uri);
                streamURL = pathURI.toURL();
                is = openStream_doPriv(streamURL);
            } catch (Throwable t) {
                //Exception handling not needed
                if (log.isDebugEnabled()) {
                    log.debug("Exception occured in getInputStreamForURI, ignoring exception continuing processing: "+t.getMessage());
                }
            }
        }

        if (is == null) {
            try {
                final File file = new File(uri);
                streamURL = (URL) AccessController.doPrivileged(
                        new PrivilegedExceptionAction() {
                            public Object run() throws MalformedURLException {
                                return file.toURL();
                            }
                        }
                );
                is = openStream_doPriv(streamURL);
            } catch (Throwable t) {
                //Exception handling not needed
                if (log.isDebugEnabled()) {
                    log.debug("Exception occured in getInputStreamForURI, ignoring exception continuing processing: "+t.getMessage());
                }
            }
        }
        return is;
    }

    private InputStream openStream_doPriv(final URL streamURL) throws IOException {
        try {
            return (InputStream) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws IOException {
                            return streamURL.openStream();
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getException();
        }
    }

    private String constructPath(URL baseURL, URI resolvedURI) {
        String importLocation;
        URL url = null;
        try {
            // Allow for http or https
            if (baseURL.getProtocol() != null && 
                    (baseURL.getProtocol().equals(HTTP_PROTOCOL) || 
                     baseURL.getProtocol().equals(HTTPS_PROTOCOL) || 
                     baseURL.getProtocol().equals(BUNDLE_RESOURCE_PROTOCOL))) {
            	if(log.isDebugEnabled()){
            		log.debug("Constructing path with http/https protocol");
            	}
                url = new URL(baseURL.getProtocol(), baseURL.getHost(), baseURL.getPort(),
                              resolvedURI.toString());
                if (log.isDebugEnabled()) {
					log.debug("URL = " + url);
				}
               
            }
            // Check for file
            else if (baseURL.getProtocol() != null && baseURL.getProtocol().equals(FILE_PROTOCOL)) {
            	if(log.isDebugEnabled()){
            		log.debug("Constructing path with file protocol");
            	}
                url = new URL(baseURL.getProtocol(), baseURL.getHost(), resolvedURI.toString());
            }
            //Check for jar
            else if (baseURL.getProtocol() != null && baseURL.getProtocol().equals(JAR_PROTOCOL)) {
            	if(log.isDebugEnabled()){
            		log.debug("Constructing path with jar protocol");
            	}
            	url = new URL(baseURL.getProtocol(), baseURL.getHost(), resolvedURI.toString());
            }
            else{
                if(baseURL != null) {
                    
                    // try constructing it with unknown protocol
                    if(log.isDebugEnabled()){
                        log.debug("Constructing path with unknown protocol: " + baseURL.getProtocol());
                    }
                    url = new URL(baseURL.getProtocol(), baseURL.getHost(), resolvedURI.toString());
                }
                else {
                    if(log.isDebugEnabled()) {
                        log.debug("baseURL is NULL");
                    }
                }
            }
                        
        }
        catch (MalformedURLException e) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("schemaImportError",
                                                                               resolvedURI.toString(),
                                                                               baseURL.toString()),
                                                           e);
        }
        if (url == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("schemaImportError",
                                                                               resolvedURI.toString(),
                                                                               baseURL.toString()));
        }
        importLocation = url.toString();
        return importLocation;
    }

}
