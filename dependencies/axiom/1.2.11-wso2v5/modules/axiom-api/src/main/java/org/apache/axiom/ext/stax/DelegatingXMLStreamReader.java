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
package org.apache.axiom.ext.stax;

import javax.xml.stream.XMLStreamReader;

/**
 * Interface implemented by {@link XMLStreamReader} classes that delegate to another stream reader
 * and that can be safely unwrapped.
 * <p>
 * This interface gives access to the parent reader, i.e. it allows to unwrap a stream reader
 * wrapper. It should be noted that in general, unwrapping a wrapper and accessing the parent object
 * is not a safe operation because it may invalidate the state of the wrapper (which may still be
 * used later). Therefore this interface must only be implemented by {@link XMLStreamReader} classes
 * for which unwrapping is a safe operation. In particular, this interface should not be implemented
 * by wrappers that alter the sequence of events produced by the stream reader.
 * <p>
 * This interface is used by
 * {@link org.apache.axiom.util.stax.XMLStreamReaderUtils#getOriginalXMLStreamReader(XMLStreamReader)}
 * to get access to the original parser.
 */
public interface DelegatingXMLStreamReader extends XMLStreamReader {
    XMLStreamReader getParent();
}
