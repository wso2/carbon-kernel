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
import org.apache.axiom.c14n.omwrapper.interfaces.NamedNodeMap;
import org.apache.axiom.c14n.omwrapper.interfaces.Node;
import org.apache.axiom.om.OMElement;

import java.util.List;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class NamedNodeMapImpl implements NamedNodeMap {
    private WrapperFactory fac = null;
    private List list = null;
    private OMElement parent = null;

    public NamedNodeMapImpl(List list, OMElement parent, WrapperFactory fac){
        this.fac = fac;
        this.list = list;
        this.parent = parent;
    }
    public int getLength() {
        return list.size();
    }

    public Node item(int index) {
        try {
            // returns an appropriate AttrImpl, wrapping either an OMNamespace or OMAttribute
            return fac.getAttribute(list.get(index), parent);
        } catch (IndexOutOfBoundsException e) {
            // returns null if index is invalid
            return null;
        }
    }
}
