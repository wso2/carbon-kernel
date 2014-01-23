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

package org.apache.axiom.attachments.impl;

import org.apache.axiom.attachments.lifecycle.LifecycleManager;
import org.apache.axiom.attachments.lifecycle.impl.FileAccessor;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * PartOnFile stores that attachment in a file.
 * This implementation is used for very large attachments to reduce
 * the in-memory footprint.
 * 
 * The PartOnFile object is created by the PartFactory
 * @see PartFactory
 */
public class PartOnFile extends AbstractPart {

    FileAccessor fileAccessor;
    LifecycleManager manager;
    
    
    /**
     * Create a PartOnFile from the specified InputStream
     * @param headers Hashtable of javax.mail.Headers
     * @param in1 InputStream containing data
     * @param in2 InputStream containing data
     * @param attachmentDir String 
     */
    PartOnFile(LifecycleManager manager, Hashtable headers, InputStream is1, InputStream is2, String attachmentDir) throws IOException {
        super(headers);
        fileAccessor = manager.create(attachmentDir);
        
        // Now write the data to the backing file
        OutputStream fos = fileAccessor.getOutputStream();
        BufferUtils.inputStream2OutputStream(is1, fos);
        BufferUtils.inputStream2OutputStream(is2, fos);
        fos.flush();
        fos.close();
        
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.attachments.impl.AbstractPart#getDataHandler()
     */
    public DataHandler getDataHandler() throws MessagingException {
        return fileAccessor.getDataHandler(getContentType());
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.attachments.impl.AbstractPart#getFileName()
     */
    public String getFileName() throws MessagingException {
        return fileAccessor.getFileName();
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.attachments.impl.AbstractPart#getInputStream()
     */
    public InputStream getInputStream() throws IOException, MessagingException {
        return fileAccessor.getInputStream();
    }
    
    /* (non-Javadoc)
     * @see org.apache.axiom.attachments.impl.AbstractPart#getSize()
     */
    public long getSize() {
        return fileAccessor.getSize();
    }

}
