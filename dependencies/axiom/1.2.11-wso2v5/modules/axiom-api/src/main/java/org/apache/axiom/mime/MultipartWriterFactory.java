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

package org.apache.axiom.mime;

import java.io.OutputStream;

/**
 * Factory for {@link MultipartWriter} instances.
 */
public interface MultipartWriterFactory {
    /**
     * Create a new {@link MultipartWriter} instance that writes a MIME multipart package to a given
     * output stream.
     * 
     * @param out
     *            The output stream to write the MIME package to.
     * @param boundary
     *            The MIME boundary to use. The value should not include the leading dashes, i.e. it
     *            is the same value as used in the <tt>boundary</tt> content type parameter.
     * @return the writer instance
     */
    MultipartWriter createMultipartWriter(OutputStream out, String boundary);
}
