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

import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This XMLResolver is used whenever a secure XMLStreamReader
 * is needed.  Basically it throws an exception if an attempt
 * is made to read an entity.
 */
final class SecureXMLResolver implements XMLResolver {

    private static Log log = LogFactory.getLog(SecureXMLResolver.class);
    public Object resolveEntity(String arg0, String arg1, String arg2,
            String arg3) throws XMLStreamException {
        // Do not expose the name of the entity that was attempted to be 
        // read as this will reveal secure information to the client.
        if (log.isDebugEnabled()) {
            log.debug("resolveEntity is disabled because this is a secure XMLStreamReader(" + 
                    arg0 + ") (" + arg1 + ") (" + arg2   + ") (" + arg3 + ")");
        }
        throw new XMLStreamException("Reading external entities is disabled");
    }

}

