/**
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

package org.apache.ws.security.util;

import java.io.Serializable;

public class Mapping implements Serializable {
    private String namespaceURI;
    private int namespaceHash;

    private String prefix;
    private int prefixHash;

    public Mapping(String namespaceURI, String prefix) {
        setPrefix(prefix);
        setNamespaceURI(namespaceURI);
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public int getNamespaceHash() {
        return namespaceHash;
    }

    public void setNamespaceURI(String namespaceURI) {
        this.namespaceURI = namespaceURI;
        this.namespaceHash = namespaceURI.hashCode();
    }

    public String getPrefix() {
        return prefix;
    }

    public int getPrefixHash() {
        return prefixHash;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.prefixHash = prefix.hashCode();
    }

}
