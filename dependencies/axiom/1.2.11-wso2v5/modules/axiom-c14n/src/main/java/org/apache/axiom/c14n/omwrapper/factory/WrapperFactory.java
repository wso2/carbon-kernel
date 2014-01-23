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

package org.apache.axiom.c14n.omwrapper.factory;

import org.apache.axiom.c14n.omwrapper.*;
import org.apache.axiom.c14n.omwrapper.interfaces.Attr;
import org.apache.axiom.c14n.omwrapper.interfaces.Node;
import org.apache.axiom.om.*;

import java.util.HashMap;
import java.util.Map;

/**
 * class WrapperFactory
 *
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class WrapperFactory {
    private Map map = null;

    public WrapperFactory(){
        map = new HashMap();
    }
//    public static Map map = new HashMap();
    public Node getNode(Object o){
        Node n = (Node)map.get(o);
        if (n == null) {
            if(o instanceof OMElement){
                n = new ElementImpl((OMElement)o, this);
            } else if (o instanceof OMDocument) {
                n = new DocumentImpl((OMDocument)o, this);
            } else if (o instanceof OMComment) {
                n = new CommentImpl((OMComment)o, this);
            } else if (o instanceof OMDocType) {
                n = new DoctypeImpl((OMDocType)o, this);
            } else if (o instanceof OMText){
                n = new TextImpl((OMText)o, this);
            } else if (o instanceof OMProcessingInstruction){
                n = new ProcessingInstructionImpl((OMProcessingInstruction)o, this);
            }
            map.put(o, n);
        }
        return n;
    }

    public Attr getAttribute(Object o, OMElement parent){
        Attr at = (Attr)map.get(o);
        if (at == null){
            if (o instanceof OMAttribute || o instanceof OMNamespace) {
                at = new AttrImpl(o, parent, this);
            }
            map.put(o, at);
        }
        return at;
    }
}
