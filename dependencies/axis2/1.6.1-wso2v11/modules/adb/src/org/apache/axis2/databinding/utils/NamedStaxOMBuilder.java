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

package org.apache.axis2.databinding.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class NamedStaxOMBuilder {

    //wrap a StAXOMBuilder
    private StAXOMBuilder builder;
    private XMLStreamReader reader;
    private QName nameToMatch;

    /**
     * @param xmlStreamReader
     * @param nameToMatch
     */
    public NamedStaxOMBuilder(XMLStreamReader xmlStreamReader, QName nameToMatch) {
        reader = xmlStreamReader;
        builder = new StAXOMBuilder(xmlStreamReader);
        this.nameToMatch = nameToMatch;
    }

    /**
     *
     */
    public OMElement getOMElement() {
        //force to build within the given QName
        boolean done = false;
        int depth = 0;
        while (!done) {
            if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                depth--;
            } else if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                depth++;
            }

            if (depth == 0 && reader.getEventType() == XMLStreamConstants.END_ELEMENT &&
                    nameToMatch.equals(reader.getName())) {
                done = true;
            } else {
                builder.next();
            }

        }

        return builder.getDocumentElement();
    }


}
