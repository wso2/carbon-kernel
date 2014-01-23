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

import javax.xml.stream.XMLStreamConstants;

/**
 * Contains utility methods related to StAX events.
 */
public final class XMLEventUtils {
    private XMLEventUtils() {}
    
    /**
     * Get the string representation of a given StAX event type. The returned
     * value is the name of the constant in {@link XMLStreamConstants}
     * corresponding to the event type.
     * 
     * @param event
     *            the event type as returned by
     *            {@link javax.xml.stream.events.XMLEvent#getEventType()},
     *            {@link javax.xml.stream.XMLStreamReader#getEventType()} or
     *            {@link javax.xml.stream.XMLStreamReader#next()}
     * @return a string representation of the event type
     */
    public static String getEventTypeString(int event) {
        String state = null;
        switch(event) {
        case XMLStreamConstants.START_ELEMENT:
            state = "START_ELEMENT";
            break;
        case XMLStreamConstants.START_DOCUMENT:
            state = "START_DOCUMENT";
            break;
        case XMLStreamConstants.CHARACTERS:
            state = "CHARACTERS";
            break;
        case XMLStreamConstants.CDATA:
            state = "CDATA";
            break;
        case XMLStreamConstants.END_ELEMENT:
            state = "END_ELEMENT";
            break;
        case XMLStreamConstants.END_DOCUMENT:
            state = "END_DOCUMENT";
            break;
        case XMLStreamConstants.SPACE:
            state = "SPACE";
            break;
        case XMLStreamConstants.COMMENT:
            state = "COMMENT";
            break;
        case XMLStreamConstants.DTD:
            state = "DTD";
            break;
        case XMLStreamConstants.PROCESSING_INSTRUCTION:
            state = "PROCESSING_INSTRUCTION";
            break;
        case XMLStreamConstants.ENTITY_REFERENCE:
            state = "ENTITY_REFERENCE";
            break;
        default :
            state = "UNKNOWN_STATE: " + event;
        }
        return state;
    }
}
