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

package org.apache.axiom.util.stax;

import javax.xml.stream.Location;

/**
 * Dummy {@link Location} implementation. It always returns -1 for the location
 * and <code>null</code> for the publicId and systemId. It may be used by
 * {@link javax.xml.stream.XMLStreamReader} implementations that don't support
 * the concept of location.
 */
public class DummyLocation implements Location {
    public static final DummyLocation INSTANCE = new DummyLocation();

    protected DummyLocation() {}
    
    public int getLineNumber() {
        return -1;
    }

    public int getColumnNumber() {
        return -1;
    }

    public int getCharacterOffset() {
        return 0;
    }

    public String getPublicId() {
        return null;
    }

    public String getSystemId() {
        return null;
    }
}
