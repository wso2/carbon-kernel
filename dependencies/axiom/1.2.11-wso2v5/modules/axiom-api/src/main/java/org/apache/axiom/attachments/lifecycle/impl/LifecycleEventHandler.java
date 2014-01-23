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

package org.apache.axiom.attachments.lifecycle.impl;

import java.io.IOException;

/**
 * The attachment life cycle manager supports create and delete operations on the FileAccessor(which holds attachment file).
 * These operations are coupled to events, the LifecycleManager needs to execute an operation when an even
 * causes that operation to trigger. For example a delete operation should execute when a deleteOnExit 
 * or deleteOnTimeInterval event occur on FileAccessor.
 * 
 * The LifecycleManager should execute operation on FileAccessor based on the Events that trigger them. 
 * EventHandler defines methods to execute LifecycleManager operation when a event occurs.
 *
 */
public interface LifecycleEventHandler {
    
    /**
     * When a Event occurs in FileAccessor, execute the LifecycleManager Operation
     * For example, if the delete behaviour is readOnce and if the inputstream on attachment is read 
     * and closed the first time, the delete operation in LifecycleManager should be executed. 
     * @param eventId
     */
    public void handleEvent(int eventId) throws IOException;
}
