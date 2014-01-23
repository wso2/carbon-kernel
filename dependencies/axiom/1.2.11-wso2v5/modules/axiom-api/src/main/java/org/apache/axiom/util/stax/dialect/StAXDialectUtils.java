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

import javax.xml.stream.XMLInputFactory;

class StAXDialectUtils {
    /**
     * Default implementation for the {@link StAXDialect#disallowDoctypeDecl(XMLInputFactory)}
     * method. This method assumes that if {@link XMLInputFactory#SUPPORT_DTD} is set to false, the
     * underlying parser
     * <ol>
     * <li>still reports DTD events and
     * <li>doesn't attempt to load the external DTD subset (if present).
     * </ol>
     * These assumptions may be false for some StAX implementations.
     * 
     * @param factory
     *            the factory to configure
     * @return the configured factory
     */
    public static XMLInputFactory disallowDoctypeDecl(XMLInputFactory factory) {
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        return new DisallowDoctypeDeclInputFactoryWrapper(factory);
    }
}
