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

package org.apache.axis2.wsdl.databinding;

import org.apache.axis2.namespace.Constants;
import org.apache.axis2.wsdl.i18n.CodegenMessages;

import javax.xml.namespace.QName;

public class CTypeMapper extends TypeMappingAdapter {

    private String defaultStructName = "axiom_node_t*";

    public CTypeMapper() {
    }

    public String getTypeMappingName(QName qname) {

        if ((qname != null)) {
            Object o = qName2NameMap.get(qname);
            if (o != null) {
                return (String)o;
            } else if (Constants.XSD_ANYTYPE.equals(qname) ||
                    Constants.XSD_ANY.equals(qname)) {
                return defaultStructName;
            } else {
                throw new UnmatchedTypeException(
                        CodegenMessages.getMessage("databinding.typemapper.typeunmatched",
                                                   qname.getLocalPart(),
                                                   qname.getNamespaceURI())
                );
            }
        } else {
            return null;
        }
    }

    public String getDefaultMappingName() {
        return defaultStructName;
    }

    public void setDefaultMappingName(String defaultMapping) {
        this.defaultStructName = defaultMapping;
    }

}
