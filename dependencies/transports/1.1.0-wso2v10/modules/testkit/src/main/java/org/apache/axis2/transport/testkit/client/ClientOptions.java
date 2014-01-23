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

package org.apache.axis2.transport.testkit.client;

import javax.mail.internet.ContentType;

import org.apache.axiom.util.UIDGenerator;

public class ClientOptions {
    private final ContentType transportContentType;
    private final String charset;
    private String mimeBoundary;
    private String rootContentId;

    // TODO: this is ugly; find a better solution
    public ClientOptions(TestClient client, ContentType baseContentType, String charset) throws Exception {
        this.charset = charset;
        transportContentType = client.getContentType(this, baseContentType);
    }

    public ContentType getTransportContentType() {
        return transportContentType;
    }

    public String getCharset() {
        return charset;
    }
    
    public String getMimeBoundary() {
        if (mimeBoundary == null) {
            mimeBoundary = UIDGenerator.generateMimeBoundary();
        }
        return mimeBoundary;
    }

    public String getRootContentId() {
        if (rootContentId == null) {
            rootContentId = UIDGenerator.generateContentId();
        }
        return rootContentId;
    }
}
