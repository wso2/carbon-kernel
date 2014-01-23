/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.client;

import javax.xml.namespace.QName;

/**
 * this class is intended to use as the key for fault maps used
 * in generated stubs.
 */

public class FaultMapKey {

    private QName elementQname;
    private String operationName;

    public FaultMapKey(QName elementQname, String operationName) {
        this.elementQname = elementQname;
        this.operationName = operationName;
    }

    public boolean equals(Object obj) {
        // it is safe to assume elementQname and operationName not null since it use
        // with proper values
        boolean isEqual = false;
        if (obj instanceof FaultMapKey){
            FaultMapKey faultMapKey = (FaultMapKey) obj;
            isEqual = this.elementQname.equals(faultMapKey.getElementQname()) &&
                          this.operationName.equals(faultMapKey.getOperationName());
        }
        return isEqual;
    }

    public int hashCode() {
        return this.elementQname.hashCode() + this.operationName.hashCode();
    }

    public QName getElementQname() {
        return elementQname;
    }

    public void setElementQname(QName elementQname) {
        this.elementQname = elementQname;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
}
