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

package org.apache.axiom.om;

/**
 * Thrown if an object model operation would lead to a hierarchy that is not allowed in the
 * given object model implementation.
 * <p>
 * If this exception is encountered by a builder when creating an OM node from an event received
 * by the parser and if the corresponding content can be ignored (i.e. is not semantically
 * relevant), the builder should ignore the exception and skip the event. An example is whitespace
 * appearing before or after the root element of a document. This would be represented as an
 * {@link OMText} node below the {@link OMDocument}. If the OM implementation doesn't allow text
 * nodes as children of a document (as for example in DOM), it should throw this exception so
 * that the builder can discard the event. 
 */
public class OMHierarchyException extends OMException {
    private static final long serialVersionUID = 8391435427221729190L;

    public OMHierarchyException(String message) {
        super(message);
    }
}
