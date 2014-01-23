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

package org.apache.axiom.util.stax;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

/**
 * {@link IOException} that wraps an {@link XMLStreamException}.
 */
public class XMLStreamIOException extends IOException {
    private static final long serialVersionUID = -2209565480803762583L;

    /**
     * Constructor.
     * 
     * @param cause the {@link XMLStreamException} to wrap
     */
    public XMLStreamIOException(XMLStreamException cause) {
        initCause(cause);
    }
    
    /**
     * Get the wrapped {@link XMLStreamException}.
     * 
     * @return the wrapped exception
     */
    public XMLStreamException getXMLStreamException() {
        return (XMLStreamException)getCause();
    }
}
