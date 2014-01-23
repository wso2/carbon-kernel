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

import javax.activation.DataHandler;

/**
 * Interface used by {@link XOPDecodingStreamReader} to load MIME parts referenced by
 * <tt>xop:Include</tt> elements.
 */
public interface MimePartProvider {
    /**
     * Check whether the MIME part identified by a given content ID has already been loaded. A
     * return value of <code>true</code> means that a call to {@link #getDataHandler(String)} (for
     * the same content ID) will not block or will retrieve the {@link DataHandler} without
     * overhead.
     * 
     * @return <code>true</code> if the MIME part has already been loaded; <code>false</code>
     *         otherwise
     * @throws IllegalArgumentException
     *             Thrown if the MIME part specified by the content ID doesn't exist. Note that the
     *             implementation may be unable to determine this without loading all the MIME
     *             parts. In this case, it should return <code>false</code>.
     */
    boolean isLoaded(String contentID);
    
    /**
     * Get the {@link DataHandler} for the MIME part identified by a given content ID.
     * 
     * @param contentID
     *            a content ID referenced in an <tt>xop:Include</tt> element
     * @return the {@link DataHandler} for the MIME part identified by the content ID; may not be
     *         <code>null</code>
     * @throws IllegalArgumentException
     *             if the MIME part was not found
     * @throws IOException
     *             if an error occurred while loading the part
     */
    DataHandler getDataHandler(String contentID) throws IOException;
}
