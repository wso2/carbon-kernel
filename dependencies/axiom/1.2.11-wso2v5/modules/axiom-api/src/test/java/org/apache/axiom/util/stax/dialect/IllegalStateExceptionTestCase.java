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
package org.apache.axiom.util.stax.dialect;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public abstract class IllegalStateExceptionTestCase extends EventSpecificTestCase {
    private final boolean expectException;
    
    public IllegalStateExceptionTestCase(int event, boolean expectException) {
        super(event);
        this.expectException = expectException;
    }

    protected final void runTest(XMLStreamReader reader) throws Throwable {
        boolean exception;
        try {
            invoke(reader);
            exception = false;
        } catch (IllegalStateException ex) {
            exception = true;
        }
        if (exception && !expectException) {
            fail("Didn't expect IllegalStateException");
        } else if (!exception && expectException) {
            fail("Expected IllegalStateException");
        }
    }
    
    protected abstract void invoke(XMLStreamReader reader) throws XMLStreamException;
}
