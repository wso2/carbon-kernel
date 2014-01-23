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

package org.apache.axis2.transport.testkit;

import org.apache.axis2.transport.testkit.name.Key;

public class MessageTestData {
    private final String name;
    private final String text;
    private final String charset;
    
    public MessageTestData(String name, String text, String charset) {
        this.name = name;
        this.text = text;
        this.charset = charset;
    }
    
    @Key("data")
    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getCharset() {
        return charset;
    }
}
