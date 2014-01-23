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

import org.apache.axiom.util.UIDGenerator;

/**
 * Content ID generator interface. Implementations of this interface are used by
 * {@link XOPEncodingStreamReader} to generate content IDs for use in <tt>xop:Include</tt>
 * elements.
 */
public interface ContentIDGenerator {
    /**
     * Default content ID generator that preserves any existing content ID.
     */
    ContentIDGenerator DEFAULT = new ContentIDGenerator() {
        public String generateContentID(String existingContentID) {
            if (existingContentID == null) {
                return UIDGenerator.generateContentId();
            } else {
                return existingContentID;
            }
        }
    };
    
    /**
     * Generate a content ID.
     * 
     * @param existingContentID
     *            An existing content ID for the {@link javax.activation.DataHandler} being
     *            processed, as returned by
     *            {@link org.apache.axiom.ext.stax.datahandler.DataHandlerReader#getContentID()},
     *            or <code>null</code> if no existing content ID is known. The implementation is
     *            free to use this information or not.
     * @return the content ID; may not be <code>null</code>
     */
    String generateContentID(String existingContentID);
}
