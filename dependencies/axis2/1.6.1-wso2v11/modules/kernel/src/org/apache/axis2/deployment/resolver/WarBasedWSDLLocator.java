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

package org.apache.axis2.deployment.resolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.woden.WSDLException;
import org.apache.woden.resolver.URIResolver;
import org.apache.ws.commons.schema.resolver.DefaultURIResolver;
import org.xml.sax.InputSource;

import javax.wsdl.xml.WSDLLocator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class WarBasedWSDLLocator extends DefaultURIResolver implements WSDLLocator, URIResolver {
    protected static final Log log = LogFactory
            .getLog(WarBasedWSDLLocator.class);

    private InputStream baseInputStream;
    private URI lastImportLocation;
    private String baseURI;
    private ClassLoader classLoader;

    public WarBasedWSDLLocator(String baseURI, ClassLoader classLoader,
                               InputStream baseInputStream) {
        this.baseURI = baseURI;
        this.baseInputStream = baseInputStream;
        this.classLoader = classLoader;
    }

    public InputSource getBaseInputSource() {
        return new InputSource(baseInputStream);
    }

    /**
     * @param parentLocation
     * @param importLocation
     */
    public InputSource getImportInputSource(String parentLocation, String importLocation) {
        lastImportLocation = URI.create(parentLocation).resolve(importLocation);

        if (isAbsolute(importLocation)) {
            return super.resolveEntity(
                    null, importLocation, parentLocation);
        } else {
            String searchingStr = lastImportLocation.toString();
            return new InputSource(classLoader.getResourceAsStream(searchingStr));
        }
    }

    /**
     * As for the zip there is no point in returning
     * a base URI
     */
    public String getBaseURI() {
        // we don't care
        return baseURI;
    }

    /**
     * returns the latest import
     */
    public String getLatestImportURI() {
        //we don't care about this either
        return lastImportLocation.toString();
    }

    public void close() {
        //TODO: FIXME:
    }

    public URI resolveURI(URI uri) throws WSDLException, IOException {

        if (isAbsolute(uri.toString())) {
            return uri;
        } else {
            lastImportLocation = URI.create(baseURI).resolve(uri.toString());
            String searchingStr = lastImportLocation.toString();
            URL resource = classLoader.getResource(searchingStr);
            if (resource != null) {
                try {
                    return new URI(resource.toString());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            log.info("AARBasedWSDLLocator: Unable to resolve " + lastImportLocation);
            return null;
        }
    }
}