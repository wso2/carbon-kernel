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

package org.apache.axis2.scripting.convertors;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;

/**
 * OMElementConvertor for Ruby scripts
 * 
 * TODO: Right now this goes via Strings and likely isn't very fast
 * There could well be much better ways to do this :)
 */
public class RBOMElementConvertor implements OMElementConvertor {

    protected BSFEngine bsfEngine;

    public RBOMElementConvertor() {
    }

    public Object toScript(OMElement omElement) {
        try {

            StringBuffer srcFragment = new StringBuffer("Document.new(<<EOF\n");
            srcFragment.append(omElement.toString());
            srcFragment.append("\nEOF\n");
            srcFragment.append(")");
            
            Object o = bsfEngine.eval("RBOMElementConvertor", 0, 0, srcFragment.toString());
            return o;

        } catch (BSFException e) {
            throw new RuntimeException(e);
        }
    }

    public OMElement fromScript(Object o) {
        try {

            byte[] xmlBytes = o.toString().getBytes();
            StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(xmlBytes));
            OMElement omElement = builder.getDocumentElement();

            return omElement;

        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public void setEngine(BSFEngine e) {
        this.bsfEngine = e;
    }
}
