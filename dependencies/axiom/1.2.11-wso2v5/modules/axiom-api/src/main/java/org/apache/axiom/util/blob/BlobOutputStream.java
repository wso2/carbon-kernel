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

package org.apache.axiom.util.blob;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.axiom.ext.io.ReadFromSupport;
import org.apache.axiom.ext.io.StreamCopyException;

/**
 * Output stream that is used to write to a blob. Instances of this class are returned by the
 * {@link WritableBlob#getOutputStream()} method.
 */
public abstract class BlobOutputStream extends OutputStream implements ReadFromSupport {
    /**
     * Get the blob to which this output stream belongs.
     * 
     * @return the blob
     */
    public abstract WritableBlob getBlob();

    public long readFrom(InputStream inputStream, long length) throws StreamCopyException {
        return getBlob().readFrom(inputStream, length);
    }
}
