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

import org.apache.axiom.om.OMException;

/**
 * Container for AttachmentStream s. This class provides an SwA like access mechanism, allowing
 * applications to access the streams directly. Access it intentionally restrictred to either SwA
 * like (stream access), or MTOM like (part/data handler access via blob id), not both.
 */
public abstract class IncomingAttachmentStreams {

    /**
     * Boolean indicating weather or not the next stream can be read (next stream cannot be read until
     * previous is consumed
     */
    protected boolean _readyToGetNextStream = true;

    /** @return True if the next stream can be read, false otherwise. */
    public final boolean isReadyToGetNextStream() {
        return _readyToGetNextStream;
    }

    /**
     * Set the ready flag. Intended for the inner class to use.
     *
     * @param ready
     */
    protected final void setReadyToGetNextStream(boolean ready) {
        _readyToGetNextStream = ready;
    }

    /**
     * Returns the next attachment stream in sequence.
     *
     * @return The next stream or null if no additional streams are left.
     */
    public abstract IncomingAttachmentInputStream getNextStream() throws OMException;
}