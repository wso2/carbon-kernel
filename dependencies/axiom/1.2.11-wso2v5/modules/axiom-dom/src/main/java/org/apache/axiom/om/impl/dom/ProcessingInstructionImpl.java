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

package org.apache.axiom.om.impl.dom;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMProcessingInstruction;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

public class ProcessingInstructionImpl extends ChildNode implements ProcessingInstruction, OMProcessingInstruction {
    private String target;
    private String value;

    public ProcessingInstructionImpl(DocumentImpl ownerDocument, String target, String value,
            OMFactory factory) {
        
        super(ownerDocument, factory);
        this.target = target;
        this.value = value;
        done = true;
    }

    public int getType() {
        return OMNode.PI_NODE;
    }

    public void setType(int nodeType) throws OMException {
        if (nodeType != OMNode.PI_NODE) {
            throw new OMException("Can't change the type of a ProcessingInstruction node");
        }
    }
    
    public short getNodeType() {
        return Node.PROCESSING_INSTRUCTION_NODE;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getValue() {
        return value;
    }
    
    public void setValue(String text) {
        this.value = text;
    }
    
    public String getData() {
        return value;
    }
    
    public void setData(String data) throws DOMException {
        if (!isReadonly()) {
            value = data;
        } else {
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   DOMMessageFormatter.formatMessage(
                                           DOMMessageFormatter.DOM_DOMAIN,
                                           DOMException.NO_MODIFICATION_ALLOWED_ERR, null));
        }
    }
    
    public String getNodeName() {
        return target;
    }

    public String getNodeValue() throws DOMException {
        return value;
    }

    public void internalSerialize(XMLStreamWriter writer, boolean cache) throws XMLStreamException {
        writer.writeProcessingInstruction(target + " ", value);
    }
}
