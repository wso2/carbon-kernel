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

package org.apache.axis2.transport.testkit.message;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class RESTMessage {
    public static class Parameter {
        private final String key;
        private final String value;
        
        public Parameter(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object _obj) {
            if (_obj instanceof Parameter) {
                Parameter obj = (Parameter)_obj;
                return ObjectUtils.equals(key, obj.key) && ObjectUtils.equals(value, obj.value);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(key).append(value).toHashCode();
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
    
    private final Parameter[] parameters;

    public RESTMessage(Parameter[] parameters) {
        this.parameters = parameters;
    }
    
    public Parameter[] getParameters() {
        return parameters;
    }

    public String getQueryString() {
        StringBuilder buffer = new StringBuilder();
        for (Parameter parameter : parameters) {
            if (buffer.length() > 0) {
                buffer.append('&');
            }
            buffer.append(parameter.getKey());
            buffer.append('=');
            try {
                buffer.append(URLEncoder.encode(parameter.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new Error("JRE doesn't know UTF-8!", e);
            }
        }
        return buffer.toString();
    }
}
