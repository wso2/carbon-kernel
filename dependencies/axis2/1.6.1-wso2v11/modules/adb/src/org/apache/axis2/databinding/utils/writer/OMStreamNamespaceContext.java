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

package org.apache.axis2.databinding.utils.writer;

import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class OMStreamNamespaceContext implements NamespaceContext {

    private Map namespaceToPrefixMap;
    private Map prefixToNamespaceMap;

    public OMStreamNamespaceContext() {
        this.namespaceToPrefixMap = new HashMap();
        this.prefixToNamespaceMap = new HashMap();
    }

    public void registerNamespace(String namespace,String prefix){
        this.namespaceToPrefixMap.put(namespace,prefix);
        this.prefixToNamespaceMap.put(prefix,namespace);
    }

    public String getNamespaceURI(String prefix) {
        return (String) prefixToNamespaceMap.get(prefix);
    }

    public String getPrefix(String namespaceURI) {
        return (String) namespaceToPrefixMap.get(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI) {
        return prefixToNamespaceMap.keySet().iterator();
    }

    public void removeNamespce(String namespaceURI){
        String prefix = (String) this.namespaceToPrefixMap.remove(namespaceURI);
        this.prefixToNamespaceMap.remove(prefix);
    }


}
