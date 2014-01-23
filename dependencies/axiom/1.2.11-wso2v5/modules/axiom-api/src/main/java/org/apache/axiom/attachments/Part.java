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

package org.apache.axiom.attachments;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

/**
 * Abstract for Part.  A Part can be the SOAP Part or an Attachment Part.
 * There are several implementations for part, which are optimized for 
 * space and time.
 * 
 * A Part is created with the PartFactory.
 * 
 * @see org.apache.axiom.attachments.impl.PartFactory
 */
public interface Part {


    /**
     * @return DataHandler representing this part
     * @throws MessagingException
     */
    public DataHandler getDataHandler() throws MessagingException;
    
    /**
     * @return size
     * @throws MessagingException
     */
    public long getSize() throws MessagingException;

    /**
     * @return content type of the part
     * @throws MessagingException
     */
    public String getContentType() throws MessagingException;

    /**
     * @return content id of the part
     * @throws MessagingException
     */
    public String getContentID() throws MessagingException;

    /**
     * The part may be backed by a file.  If that is the case,
     * this method returns the file name.
     * 
     * @return the name of the file
     * @throws MessagingException
     * @deprecated The callers should not no how the part 
     * is implemented.
     */
    public String getFileName() throws MessagingException;

    /**
     * @return Get the part data as an input stream
     * @throws IOException
     * @throws MessagingException
     */
    public InputStream getInputStream() throws IOException, MessagingException;

    /**
     * Add a Header (name, value) to the part
     * @param name
     * @param value
     * @throws MessagingException
     */
    public void addHeader(String name, String value) throws MessagingException;

    /**
     * Get the value of a specific header
     * @param name
     * @return value or null
     * @throws MessagingException
     */
    public String getHeader(String name) throws MessagingException;

    /**
     * @return Enumeration of javax.mail.Header
     * @throws MessagingException
     */
    public Enumeration getAllHeaders() throws MessagingException;

}
