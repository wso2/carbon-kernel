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

package org.apache.axis2.corba.idl.types;

public abstract class AbstractCollectionType extends CompositeDataType {
    protected int elementCount;
    protected DataType dataType;
    protected String elementModule;
    protected String elementName;
    protected int depth;

    public void setElementCount(int elementCount) {
        this.elementCount = elementCount;
    }

    public int getElementCount() {
        return elementCount;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public boolean isArray() {
        return (this instanceof ArrayType);
    }

    public boolean isSequence() {
        return (this instanceof SequenceType);
    }

    public String getElementModule() {
        return elementModule;
    }

    public void setElementModule(String elementModule) {
        this.elementModule = elementModule;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
