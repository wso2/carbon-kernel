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

package org.apache.axiom.om.impl.jaxp;

import javax.xml.transform.sax.SAXResult;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.SAXOMBuilder;

/**
 * Implementation of {@link javax.xml.transform.Result} for AXIOM.
 * The implementation is based on {@link SAXResult} and transforms the stream
 * of SAX events to an AXIOM tree using {@link SAXOMBuilder}.
 * <p>
 * Note that building {@link org.apache.axiom.om.OMDocType} nodes from DTD information is
 * not supported.
 */
public class OMResult extends SAXResult {
    private final SAXOMBuilder builder;
    
    public OMResult(OMFactory omFactory) {
        builder = new SAXOMBuilder(omFactory);
        setHandler(builder);
    }
    
    public OMResult() {
        this(OMAbstractFactory.getOMFactory());
    }

    public OMDocument getDocument() {
        return builder.getDocument();
    }

    public OMElement getRootElement() {
        return builder.getRootElement();
    }
}
