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

package org.apache.axis2.databinding.utils.reader;

import org.apache.axis2.databinding.utils.Constants;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamReader;

public interface ADBXMLStreamReader extends XMLStreamReader, Constants {

    // this will help to handle Text within the current element.
    // user should pass the element text to the property list as this
    // ELEMENT_TEXT as the key. This key deliberately has a space in it
    // so that it is not a valid XML name
    static final String ELEMENT_TEXT = "Element Text";

    /** Extra method to query the state of the pullparser */
    boolean isDone();

    /** add the parent namespace context to this parser */
    void addNamespaceContext(NamespaceContext nsContext);

    /**
     * Initiate the parser - this will do whatever the needed tasks to initiate the parser and must
     * be called before attempting any specific parsing using this parser
     */
    void init();
}
