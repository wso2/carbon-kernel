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

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

/**
 * Optional interface implemented by {@link javax.xml.stream.XMLStreamReader}
 * implementations that support writing character data directly to a
 * {@link Writer}.
 * <p>
 * All the requirements outlined in {@link org.apache.axiom.ext.stax} apply to
 * this extension interface. In particular, to get a reference to the extension,
 * the consumer MUST call {@link javax.xml.stream.XMLStreamReader#getProperty(String)}
 * with {@link #PROPERTY} as the property name.
 */
public interface CharacterDataReader {
    /**
     * The name of the property used to look up this extension interface from a
     * {@link javax.xml.stream.XMLStreamReader} implementation.
     */
    String PROPERTY = CharacterDataReader.class.getName();
    
    /**
     * Output the character data for the current event to the given writer. In
     * general, the implementation behaves such that
     * <code>reader.writeTextTo(writer)</code> has the same effect as
     * <code>writer.write(reader.getText())</code>. However, the implementation
     * MAY choose to split the character data differently. E.g. it MAY write the
     * character data in multiple chunks or it MAY choose to process more
     * character data in a single event than would be returned by
     * {@link javax.xml.stream.XMLStreamReader#getText()}. Therefore, using this
     * method together with {@link javax.xml.stream.XMLStreamReader#getText()},
     * {@link javax.xml.stream.XMLStreamReader#getTextCharacters()},
     * {@link javax.xml.stream.XMLStreamReader#getTextStart()},
     * {@link javax.xml.stream.XMLStreamReader#getTextLength()} or
     * {@link javax.xml.stream.XMLStreamReader#getTextCharacters(int, char[], int, int)}
     * is not supported and may lead to undefined results.
     * <p>
     * The implementation SHOULD avoid any unnecessary conversions between
     * strings and character arrays.
     * 
     * @param writer
     *            the writer to write the character data to
     * @throws XMLStreamException
     *             if the underlying XML source is not well-formed
     * @throws IOException
     *             if an I/O error occurs when writing the character data
     * @throws IllegalStateException
     *             if this state is not a valid text state.
     */
    void writeTextTo(Writer writer) throws XMLStreamException, IOException;
}
