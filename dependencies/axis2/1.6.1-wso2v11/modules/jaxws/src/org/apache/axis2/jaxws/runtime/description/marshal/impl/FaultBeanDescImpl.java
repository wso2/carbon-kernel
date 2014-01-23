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

package org.apache.axis2.jaxws.runtime.description.marshal.impl;

import org.apache.axis2.jaxws.runtime.description.marshal.FaultBeanDesc;

class FaultBeanDescImpl implements FaultBeanDesc {

    String faultBeanClassName;
    String faultBeanLocalName;
    String faultBeanNamespace;

    FaultBeanDescImpl(String faultBeanClassName, String faultBeanLocalName,
                      String faultBeanNamespace) {
        this.faultBeanClassName = faultBeanClassName;
        this.faultBeanLocalName = faultBeanLocalName;
        this.faultBeanNamespace = faultBeanNamespace;
    }

    public String getFaultBeanClassName() {
        return faultBeanClassName;
    }

    public String getFaultBeanLocalName() {
        return faultBeanLocalName;
    }

    public String getFaultBeanNamespace() {
        return faultBeanNamespace;
    }

    public String toString() {
        final String newline = "\n";
        StringBuffer string = new StringBuffer();
        string.append(newline);
        string.append("      FaultBean Class Name :" + faultBeanClassName);
        string.append(newline);
        string.append("      FaultBean Namespace  :" + faultBeanNamespace);
        string.append(newline);
        string.append("      FaultBean Local Name :" + faultBeanLocalName);
        string.append(newline);
        return string.toString();
    }
}
