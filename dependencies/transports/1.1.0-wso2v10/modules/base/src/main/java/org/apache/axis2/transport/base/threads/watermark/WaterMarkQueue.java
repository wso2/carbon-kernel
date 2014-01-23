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

package org.apache.axis2.transport.base.threads.watermark;

import java.util.concurrent.BlockingQueue;

/**
 * This queue acts as a queue with a mark. The methods exposed by the <code>BlockingQueue</code>
 * interface will add elements up to the mark. We call this mark the waterMark. After the
 * water mark the all the insertion operations will fails as if the queue is bounded by
 * this waterMark. After this to add values to the queue the offerAfter method should be called.
 *
 * @param <T> The object
 */
public interface WaterMarkQueue<T> extends BlockingQueue<T> {
    /**
     * Offer the element after the water mark.
     *
     * @param object object to be inserted
     * @return true if the insert is successful
     */
    public boolean offerAfter(T object);
}

