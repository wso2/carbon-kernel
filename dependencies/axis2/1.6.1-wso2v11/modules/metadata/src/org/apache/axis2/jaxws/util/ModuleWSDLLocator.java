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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.xml.WSDLLocator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * This class is an implementation of a WSDL4J interface and is the
 * implementation we supply to a WSDLReader instance. Its primary 
 * goal is to assist with locating imported WSDL documents.
 * 
 */
public class ModuleWSDLLocator extends BaseWSDLLocator implements WSDLLocator {

    private static Log log = LogFactory.getLog(ModuleWSDLLocator.class);
    
    private ClassLoader classLoader;

    /**
     * ModuleWSDLLocator constructor.
     * @param uri
     *            the path for the base wsdl file, relative to the module root
     * @param stream
     *            the InputStream for the base wsdl file
     * @param strategy
     *            the load strategy for the module
     */
    public ModuleWSDLLocator(String uri, InputStream stream,
            ClassLoader classLoader) {
        baseURI = convertURI(uri);
        baseInputStream = stream;
        this.classLoader = classLoader;
    }

    /**
     * Returns null because no URI indirection is performed when 
     * using the ModuleWSDLLocator.
     * 
     * @param importURI a URI specifying the document to import
     * @param parent a URI specifying the location of the parent document doing
     * the importing
     * @return null
     */
    protected String getRedirectedURI(String importURI, String parent) {
    	return null;
    }
    
    /**
     * Returns an InputStream pointed at an imported wsdl pathname relative to
     * the parent document.
     * 
     * @param importPath
     *            identifies the WSDL file within the context
     * @return a stream of the WSDL file
     */
    protected InputStream getInputStream(String importPath) throws IOException {
        URL importURL = null;
        InputStream is = null;
        try {
            importURL = new URL(importPath);
            is = importURL.openStream();
        }
        catch (Throwable t) {
            // No FFDC required
        }
        if (is == null) {
            try {
                is = classLoader.getResourceAsStream(importPath);
            }
            catch (Throwable t) {
                // No FFDC required
            }
        }
        if (is == null) {
            try {
                File file = new File(importPath);
                is = file.toURL().openStream();
            }
            catch (Throwable t) {
                // No FFDC required
            }
        }
        if (is == null) {
            try {
                URI uri = new URI(importPath);
                is = uri.toURL().openStream();
            }
            catch (Throwable t) {
                // No FFDC required
            }
        }
        return is;
    }

    /**
     * Return the wsdlLocation in URL form. WsdlLocation could be URL, relative
     * module path, full absolute path.
     * 
     * @param wsdlLocation
     *            the location of a WSDL document in the form of a URL string, a
     *            relative pathname (relative to the root of a module, or a
     *            full-qualified absolute pathname
     * @return the location of the WSDL document in the form of a URL
     */
    public URL getWsdlUrl(String wsdlLocation) {
        URL streamURL = null;
        InputStream is = null;
        URI pathURI = null;

        try {
            streamURL = new URL(wsdlLocation);
            is = streamURL.openStream();
            is.close();
        }
        catch (Throwable t) {
            // No FFDC required
        }

        if (is == null) {
            try {
                pathURI = new URI(wsdlLocation);
                streamURL = pathURI.toURL();
                is = streamURL.openStream();
                is.close();
            }
            catch (Throwable t) {
                // No FFDC required
            }
        }

        if (is == null) {
            try {
                File file = new File(wsdlLocation);
                streamURL = file.toURL();
                is = streamURL.openStream();
                is.close();
            }
            catch (Throwable t) {
                // No FFDC required
            }
        }

        if (log.isDebugEnabled() && streamURL == null) {
            log.debug("Absolute wsdlLocation could not be determined: "
                    + wsdlLocation);
        }

        return streamURL;
    }

    public void close() {
    }
}