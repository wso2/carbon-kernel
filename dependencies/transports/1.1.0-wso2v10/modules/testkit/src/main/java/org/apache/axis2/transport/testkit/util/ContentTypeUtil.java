/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.util;

import java.util.Enumeration;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParameterList;

public class ContentTypeUtil {
    private ContentTypeUtil() {}
    
    public static ContentType addCharset(ContentType contentType, String charset) {
        ParameterList orgParamList = contentType.getParameterList();
        ParameterList paramList = new ParameterList();
        if (orgParamList != null) {
            for (Enumeration<?> e = orgParamList.getNames(); e.hasMoreElements(); ) {
                String name = (String)e.nextElement();
                paramList.set(name, orgParamList.get(name));
            }
        }
        paramList.set("charset", charset);
        return new ContentType(contentType.getPrimaryType(), contentType.getSubType(), paramList);
    }
    
    public static ContentType removeCharset(ContentType contentType) {
        ParameterList orgParamList = contentType.getParameterList();
        ParameterList paramList = new ParameterList();
        for (Enumeration<?> e = orgParamList.getNames(); e.hasMoreElements(); ) {
            String name = (String)e.nextElement();
            if (!name.equalsIgnoreCase("charset")) {
                paramList.set(name, orgParamList.get(name));
            }
        }
        return new ContentType(contentType.getPrimaryType(), contentType.getSubType(), paramList);
    }
}
