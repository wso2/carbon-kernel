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

package org.apache.axiom.util.stax.xop;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;

import org.apache.axiom.ext.stax.datahandler.DataHandlerProvider;

/**
 * Base class for {@link XOPEncodingStreamReader} and {@link XOPEncodingStreamWriter}.
 */
public abstract class XOPEncodingStreamWrapper implements MimePartProvider {
    private final Map dataHandlerObjects = new LinkedHashMap();
    private final ContentIDGenerator contentIDGenerator;
    private final OptimizationPolicy optimizationPolicy;

    public XOPEncodingStreamWrapper(ContentIDGenerator contentIDGenerator,
                                    OptimizationPolicy optimizationPolicy) {
        this.contentIDGenerator = contentIDGenerator;
        this.optimizationPolicy = optimizationPolicy;
    }

    private String addDataHandler(Object dataHandlerObject, String existingContentID) {
        String contentID = contentIDGenerator.generateContentID(existingContentID);
        dataHandlerObjects.put(contentID, dataHandlerObject);
        return contentID;
    }

    protected String processDataHandler(DataHandler dataHandler,
                                        String existingContentID,
                                        boolean optimize) throws IOException {
        if (optimizationPolicy.isOptimized(dataHandler, optimize)) {
            return addDataHandler(dataHandler, existingContentID);
        } else {
            return null;
        }
    }

    protected String processDataHandler(DataHandlerProvider dataHandlerProvider,
                                        String existingContentID,
                                        boolean optimize) throws IOException {
        if (optimizationPolicy.isOptimized(dataHandlerProvider, optimize)) {
            return addDataHandler(dataHandlerProvider, existingContentID);
        } else {
            return null;
        }
    }

    /**
     * Get the set of content IDs referenced in <tt>xop:Include</tt> element information items
     * produced by this wrapper.
     * 
     * @return The set of content IDs in their order of appearance in the infoset. If no
     *         <tt>xop:Include</tt> element information items have been produced yet, an empty
     *         set will be returned.
     */
    public Set/*<String>*/ getContentIDs() {
        return Collections.unmodifiableSet(dataHandlerObjects.keySet());
    }

    public boolean isLoaded(String contentID) {
        Object dataHandlerObject = dataHandlerObjects.get(contentID);
        if (dataHandlerObject == null) {
            throw new IllegalArgumentException("No DataHandler object found for content ID '" +
                    contentID + "'");
        } else if (dataHandlerObject instanceof DataHandler) {
            return true;
        } else {
            return ((DataHandlerProvider)dataHandlerObject).isLoaded();
        }
    }

    public DataHandler getDataHandler(String contentID) throws IOException {
        Object dataHandlerObject = dataHandlerObjects.get(contentID);
        if (dataHandlerObject == null) {
            throw new IllegalArgumentException("No DataHandler object found for content ID '" +
                    contentID + "'");
        } else if (dataHandlerObject instanceof DataHandler) {
            return (DataHandler)dataHandlerObject;
        } else {
            return ((DataHandlerProvider)dataHandlerObject).getDataHandler();
        }
    }
}
