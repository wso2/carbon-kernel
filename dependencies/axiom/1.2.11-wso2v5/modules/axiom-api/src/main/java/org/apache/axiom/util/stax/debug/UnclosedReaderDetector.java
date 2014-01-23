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

package org.apache.axiom.util.stax.debug;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.util.stax.wrapper.WrappingXMLInputFactory;
import org.apache.axiom.util.stax.wrapper.XMLStreamReaderWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link XMLInputFactory} wrapper that enabled detection of unclosed stream readers. An unclosed
 * reader will be detected when the {@link XMLStreamReader} instance is finalized by the virtual
 * machine. When this happens, a warning message will be logged. The log message contains the stack
 * trace of the instruction that created the reader.
 * <p>
 * Note that for this to work, the detector must compute the stack trace every time a reader is
 * created. Since this may have a significant performance impact, the wrapper should only used
 * during testing and debugging.
 */
public class UnclosedReaderDetector extends WrappingXMLInputFactory {
    static final Log log = LogFactory.getLog(UnclosedReaderDetector.class);
    
    private static class StreamReaderWrapper extends XMLStreamReaderWrapper {
        private final Throwable stackTrace;
        private boolean isClosed;
    
        public StreamReaderWrapper(XMLStreamReader parent) {
            super(parent);
            stackTrace = new Throwable();
        }
    
        public void close() throws XMLStreamException {
            super.close();
            isClosed = true;
        }
    
        protected void finalize() throws Throwable {
            if (!isClosed) {
                log.warn("Detected unclosed XMLStreamReader.", stackTrace);
            }
        }
    }

    /**
     * Constructor.
     * 
     * @param parent the parent factory
     */
    public UnclosedReaderDetector(XMLInputFactory parent) {
        super(parent);
    }

    protected XMLStreamReader wrap(XMLStreamReader reader) {
        return new StreamReaderWrapper(reader);
    }
}
