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

package org.apache.axis2.json.gson.factory;

import java.util.ArrayList;
import java.util.List;

public class XmlNode {

    private String name;
    private boolean isAttribute;
    private boolean isArray;
    private List<XmlNode> childrenList = new ArrayList<XmlNode>();
    private String valueType;
    private String namespaceUri;

    public XmlNode(String name,String namespaceUri, boolean attribute, boolean array , String valueType) {
        this.name = name;
        this.namespaceUri = namespaceUri;
        isAttribute = attribute;
        isArray = array;
        this.valueType = valueType;
    }


    public void addChildToList(XmlNode child) {
        childrenList.add(child);
    }


    public String getName() {
        return name;
    }

    public boolean isAttribute() {
        return isAttribute;
    }

    public boolean isArray() {
        return isArray;
    }

    public List<XmlNode> getChildrenList() {
        return childrenList;
    }

    public String getValueType() {
        return valueType;
    }

    public String getNamespaceUri() {
        return namespaceUri;
    }
}
