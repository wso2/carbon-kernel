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

package org.apache.axiom.om.impl.builder;

import java.io.IOException;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

import org.apache.axiom.util.stax.xop.MimePartProvider;
import org.apache.axiom.util.stax.xop.XOPUtils;

public class AttachmentUnmarshallerImpl extends AttachmentUnmarshaller {
    private final MimePartProvider mimePartProvider;
    private boolean accessed;
    
    public AttachmentUnmarshallerImpl(MimePartProvider mimePartProvider) {
        this.mimePartProvider = mimePartProvider;
    }

    @Override
    public byte[] getAttachmentAsByteArray(String cid) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public DataHandler getAttachmentAsDataHandler(String cid) {
        try {
            accessed = true;
            return mimePartProvider.getDataHandler(XOPUtils.getContentIDFromURL(cid));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isXOPPackage() {
        return true;
    }

    public boolean isAccessed() {
        return accessed;
    }
}
