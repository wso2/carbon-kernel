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

import java.io.IOException;
import java.io.InputStream;

import org.apache.axis2.jaxws.catalog.JAXWSCatalogManager;
import org.apache.axis2.jaxws.description.impl.URIResolverImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.resolver.Catalog;
import org.xml.sax.InputSource;

/**
 * This resolver provides the means of resolving the imports and includes of a
 * given schema document. It allows the use of the Apache Commons Resolver API
 * to redirect resource requests to alternative locations.
 */
public class CatalogURIResolver extends URIResolverImpl {

    private static Log log = LogFactory.getLog(CatalogURIResolver.class);
    private Catalog catalogResolver;
    
    /**
     * CatalogURIResolver constructor.  Resolves WSDL URIs using Apache Commons Resolver API.
     * @param catalogManager
     *            the OASISCatalogManager which will determine the settings for the XML catalog
     */
    public CatalogURIResolver(JAXWSCatalogManager catalogManager) {
        this(catalogManager, null);
    }
    
    /**
     * CatalogURIResolver constructor.  Resolves WSDL URIs using Apache Commons Resolver API.
     * @param catalogManager
     *            the OASISCatalogManager which will determine the settings for the XML catalog
     * @param classLoader
     */    
    public CatalogURIResolver(JAXWSCatalogManager catalogManager, ClassLoader classLoader) {
        super(classLoader);
        if (log.isDebugEnabled()) {
            log.debug("init: catalogManager :"+ catalogManager);
        }
        if (catalogManager != null) {
            this.catalogResolver = catalogManager.getCatalog();
        }
    }
    
    /**
     * Resolves URIs using Apache Commons Resolver API.
     * 
     * @param namespace a URI specifying the namespace of the document 
     * @param schemaLocation a URI specifying the document to import
     * @param baseURI a URI specifying the location of the parent document doing
     * the importing
     * @return the resolved import location, or null if no indirection is performed
     */
    public String getRedirectedURI(String namespace,
                                   String schemaLocation,
                                   String baseUri) {
        String resolvedImportLocation = null;
        try {
            resolvedImportLocation = this.catalogResolver.resolveSystem(schemaLocation);
            if (resolvedImportLocation == null) {
                resolvedImportLocation = catalogResolver.resolveSystem(namespace);
            }
            if (resolvedImportLocation == null) {
                resolvedImportLocation = catalogResolver.resolveURI(schemaLocation);
            }
            if (resolvedImportLocation == null) {
                resolvedImportLocation = catalogResolver.resolvePublic(namespace, namespace);
            }
        
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("getRedirectedURI error: Catalog resolution failed");
            }
            throw new RuntimeException("Catalog resolution failed", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("getRedirectedURI exit: redirected location: "+ resolvedImportLocation);
        }
        return resolvedImportLocation;
    }
    
    /**
     * As for the resolver the public ID is the target namespace of the
     * schema and the schemaLocation is the value of the schema location
     * @param namespace
     * @param schemaLocation
     * @param baseUri
     */
    public InputSource resolveEntity(String namespace,
                                     String schemaLocation,
                                     String baseUri) {
        if (log.isDebugEnabled()) {
            log.debug("resolveEntity: ["+ namespace + "]["+ schemaLocation + "][ " + baseUri+ "]");
        }

        InputSource returnInputSource = null;
        
        if (this.catalogResolver != null) {
            if(log.isDebugEnabled()) {
                log.debug("catalogResolver found, calling CatalogURIResolver.getRedirectedURI.");
            }
            String redirectedURI = getRedirectedURI(namespace, schemaLocation, baseUri);
            // first make redirectedURI is valid for retrieving an input stream 
            if (redirectedURI != null) {
                returnInputSource = getInputSourceFromRedirectedURI(redirectedURI);
            }
        } 
        // If we were able to get an InputSource from the redirectedURI, just return that
        // else call up to parent to resolve with original location
        if (returnInputSource != null) {
            return returnInputSource;
        } else {
            return super.resolveEntity(namespace, schemaLocation, baseUri);
        }
    }
    
    /**
     * Given a redirecteURI from a static XML catalog, attempt to get the InputSource.
     * @param redirectedURI URI string from static XML catalog
     * @return InputSource or null if we were not able to load the resource
     */
    private InputSource getInputSourceFromRedirectedURI(String redirectedURI) {
        InputStream is = null;
        String validatedURI = null;
        InputSource returnInputSource = null;
        // If we have an absolute path, try to get the InputStream directly
        if (isAbsolute(redirectedURI)) {
            is = getInputStreamForURI(redirectedURI);
            if (is != null) {
                validatedURI = redirectedURI;
            }
        }
        // If we couldn't get the inputstream try using the classloader
        if (is == null && classLoader != null) {
            try {
                is = classLoader
                        .getResourceAsStream(redirectedURI);
                if (is != null) {
                    validatedURI = redirectedURI;
                }
            } catch (Throwable t) {
                if(log.isDebugEnabled()) {
                    log.debug("Exception occured in validateRedirectedURI, ignoring exception continuing processing: "+t.getMessage());
                }
            }
            // If we failed to get an InputStream using the entire redirectedURI,
            //  try striping off the protocol.  This may be necessary for some cases
            //  if a non-standard protocol is used.
            if (is == null) {
                redirectedURI = stripProtocol(redirectedURI);
                if (log.isDebugEnabled()) {
                    log.debug("getInputSourceFromRedirectedURI: new redirected location: "+ redirectedURI);
                }
                try {
                    is = classLoader
                            .getResourceAsStream(redirectedURI);
                    if (is != null) {
                        validatedURI = redirectedURI;
                    }
                } catch (Throwable t) {
                    if(log.isDebugEnabled()) {
                        log.debug("Exception occured in validateRedirectedURI, ignoring exception continuing processing: "+t.getMessage());
                    }                   
                }
            }
        }

        if (is != null) {
            if(log.isDebugEnabled()) {
                log.debug("getInputSourceFromRedirectedURI: XSD input stream is not null after resolving import for: " + 
                        redirectedURI);
            }
            returnInputSource = new InputSource(is);
            // We need to set the systemId. XmlSchema will use this value to
            // maintain a collection of
            // imported XSDs that have been read. If this value is null, then
            // circular XSDs will
            // cause infinite recursive reads.
            returnInputSource.setSystemId(validatedURI != null ? validatedURI : redirectedURI);

            if (log.isDebugEnabled()) {
                log.debug("returnInputSource :" + returnInputSource.getSystemId());
            }
        }
        return returnInputSource;
    }
    
    private String stripProtocol(String uriStr) {
        String retURI = uriStr.replace('\\', '/');
        int index = retURI.indexOf("://");
        if (index != -1) {
            retURI = retURI.substring(index + 3);
        }
        return retURI;
    }
    



}
