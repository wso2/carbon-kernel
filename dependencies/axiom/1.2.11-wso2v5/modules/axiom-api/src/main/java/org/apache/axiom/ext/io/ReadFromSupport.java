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

package org.apache.axiom.ext.io;

import java.io.InputStream;

/**
 * Optional interface implemented by {@link java.io.OutputStream} implementations that support
 * transferring data from an {@link InputStream}. This interface may be used to avoid allocating
 * a temporary buffer when there is a need to copy data from an input stream to an output stream.
 */
public interface ReadFromSupport {
    /**
     * Read data from the given input stream and write it to this output stream.
     * The method transfers data until one of the following conditions is met:
     * <ul>
     *   <li>The end of the input stream is reached.
     *   <li>The value of the <code>length</code> argument is different from <code>-1</code>
     *       and the number of bytes transferred is equal to <code>length</code>.
     * </ul>
     * 
     * @param inputStream
     *            An input stream to read data from. This method will not close the stream.
     * @param length
     *            the number of bytes to transfer, or <code>-1</code> if the method should
     *            transfer data until the end of the input stream is reached
     * @throws StreamCopyException
     * @return the number of bytes transferred
     */
    long readFrom(InputStream inputStream, long length) throws StreamCopyException;
}
