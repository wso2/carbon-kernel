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

import java.io.IOException;

/**
 * Signals that an I/O exception occurred while copying data from an input stream (or other data
 * source) to an output stream (or other data sink). The exception wraps the original
 * {@link IOException} together with information about the type of operation (read or write) that
 * failed.
 */
public class StreamCopyException extends IOException {
    private static final long serialVersionUID = -6489101119109339448L;
    
    /**
     * Indicates that the wrapped exception was triggered while reading from the input stream
     * (or data source).
     */
    public static final int READ = 1;
    
    /**
     * Indicates that the wrapped exception was triggered while writing to the output stream
     * (or data sink).
     */
    public static final int WRITE = 2;
    
    private final int operation;
    
    /**
     * Constructor.
     * 
     * @param operation
     *            indicates the type of operation that caused the exception; must be {@link #READ}
     *            or {@link #WRITE}
     * @param cause
     *            the wrapped exception
     */
    public StreamCopyException(int operation, IOException cause) {
        this.operation = operation;
        initCause(cause);
    }

    /**
     * Get information about the type of operation that fails.
     * 
     * @return one of {@link #READ} or {@link #WRITE}
     */
    public int getOperation() {
        return operation;
    }

    public String getMessage() {
        return operation == READ ? "Error reading from source"
                                 : "Error writing to destination";
    }
}
