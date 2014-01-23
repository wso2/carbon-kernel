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

package org.apache.axis2.transport.http;

import org.apache.commons.httpclient.methods.RequestEntity;

import java.io.IOException;
import java.io.OutputStream;

public class RESTRequestEntity2 implements RequestEntity {
    private String contentType;
    private String postRequestBody;

    public RESTRequestEntity2(String postRequestBody, String contentType) {
        this.postRequestBody = postRequestBody;
        this.contentType = contentType;
    }

    public void writeRequest(OutputStream output) throws IOException {
        output.write(postRequestBody.getBytes());
    }

    public long getContentLength() {
        return this.postRequestBody.getBytes().length;
    }

    public String getContentType() {
        return this.contentType;
    }

    public boolean isRepeatable() {
        return true;
    }
}
