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

package org.apache.axiom.c14n.omwrapper;

import org.apache.axiom.c14n.omwrapper.factory.WrapperFactory;
import org.apache.axiom.c14n.omwrapper.interfaces.Node;
import org.apache.axiom.c14n.omwrapper.interfaces.NodeList;

import java.util.List;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class NodeListImpl implements NodeList {
    private WrapperFactory fac = null;
    private List nodes = null;

    public NodeListImpl(List nodes, WrapperFactory fac) {
        this.fac = fac;
        this.nodes = nodes;
    }

    public Node item(int index) {
        try {
            return fac.getNode(nodes.get(index));
        } catch(IndexOutOfBoundsException e) {
            return null;
        }
    }

    public int getLength() {
        return nodes.size();
    }

    // Object should represent an OMNode
    protected int indexOf(Object o){
        return nodes.indexOf(o);
    }
}
