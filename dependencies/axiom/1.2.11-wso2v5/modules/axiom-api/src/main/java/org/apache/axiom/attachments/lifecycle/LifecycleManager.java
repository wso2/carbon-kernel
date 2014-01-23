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

package org.apache.axiom.attachments.lifecycle;

import java.io.File;
import java.io.IOException;

import org.apache.axiom.attachments.lifecycle.impl.FileAccessor;

/**
 * Lifecycle Manager will be used to manage the lifecycle of attachment files.
 * 
 * Axiom forks attachment processing based on the size of attachment to be processed.
 * Attachments are either processed by storing them in memory or by storing them in 
 * file system based on the size of the attachment file. 
 *
 * Lifecycle Manager provides and organized way of managing attachments, by providing
 * calls to create and delete attachments.
 */
public interface LifecycleManager {

    /**
     * Create a unique file in the designated directory
     * @param attachmentDir
     * @return TODO
     * @throws IOException
     */    
    public FileAccessor create(String attachmentDir) throws IOException;
    
    /** 
     * Deletes attachment file
     * @param file
     * @throws IOException
     */
    public void delete(File file) throws IOException;
    
    /**
     * Mark the file for deletion on application/VM exit 
     * @param file
     * @throws IOException
     */
    public void deleteOnExit(File file) throws IOException;
    
    /**
     * Mark attachment file for deletion when designated time interval in seconds 
     * has elapsed.
     * @param interval
     * @param file
     * @throws IOException
     */
    public void deleteOnTimeInterval(int interval, File file) throws IOException;
    
    /**
     * This method will return the file accessor associated with this file.
     * @param file
     * @return TODO
     * @throws IOException
     */
    public FileAccessor getFileAccessor(String file) throws IOException;
}
