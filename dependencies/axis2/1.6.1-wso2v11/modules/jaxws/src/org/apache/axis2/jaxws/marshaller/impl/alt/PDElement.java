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

package org.apache.axis2.jaxws.marshaller.impl.alt;

import org.apache.axis2.jaxws.description.ParameterDescription;


/** A PDElement object holds a ParameterDescription  
 *  Element object.
 * 
 * If this Element represents an attachment, 
 *   1) The Element represents the xml element that references the attachment (null if SWA Attachment)
 *   2) The Attachment object represents the attachment
 */
public class PDElement {
    private ParameterDescription param;
    private Element element;
    private Attachment attachment; // Null if not an attachment
    private Class byJavaTypeClass;  // Class for "by java type" marshalling and unmarshalling is used....normally null

    public PDElement(ParameterDescription param, Element element, Class byType, Attachment attachment) {
        super();
        this.param = param;
        this.element = element;
        this.byJavaTypeClass = byType;
        this.attachment = attachment;
    }

    public PDElement(ParameterDescription param, Element element, Class byType) {
        this(param, element, byType, null);
    }
    
    public ParameterDescription getParam() {
        return param;
    }

    public Element getElement() {

        return element;
    }

    public Class getByJavaTypeClass() {
        return byJavaTypeClass;
    }

    public void setByJavaTypeClass(Class byType) {
        this.byJavaTypeClass = byType;
    }
    
    public Attachment getAttachment() {
        return attachment;
    }
}
