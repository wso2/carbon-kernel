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

package org.apache.axis2.wsdl.codegen.schema;

import javax.xml.namespace.QName;

/**
 * this class is used to represent the top element
 * details of the schema
 */
public class TopElement {

    private QName elementQName;
    private QName typeQName;

    public TopElement() {
    }

    public TopElement(QName elementQName) {
        this.elementQName = elementQName;
    }

    public QName getElementQName() {
        return elementQName;
    }

    public void setElementQName(QName elementQName) {
        this.elementQName = elementQName;
    }

    public QName getTypeQName() {
        return typeQName;
    }

    public void setTypeQName(QName typeQName) {
        this.typeQName = typeQName;
    }

    /**
     * top elements should have a unique name
     * @param obj
     * @return whether two objects are equal or not
     */
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (obj instanceof TopElement){
            isEqual = ((TopElement)obj).getElementQName().equals(this.elementQName);
        }
        return isEqual;
    }
}
