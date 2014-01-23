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
package org.apache.axiom.om.impl;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.util.stax.wrapper.XMLStreamReaderWrapper;

public class RootWhitespaceFilter extends XMLStreamReaderWrapper {
    private int depth;
    
    public RootWhitespaceFilter(XMLStreamReader parent) {
        super(parent);
    }

    public int next() throws XMLStreamException {
        int event;
        loop: while (true) {
            event = super.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    depth++;
                    break loop;
                case XMLStreamConstants.END_ELEMENT:
                    depth--;
                    break loop;
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.SPACE:
                    if (depth > 0) {
                        break loop;
                    } else {
                        continue loop;
                    }
                default:
                    break loop;
            }
        }
        return event;
    }
}