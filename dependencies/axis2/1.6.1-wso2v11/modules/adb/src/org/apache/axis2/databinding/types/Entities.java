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

package org.apache.axis2.databinding.types;

import java.util.StringTokenizer;

/**
 * Custom class for supporting XSD data type Entities
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#ENTITIES">XML Schema 3.3.12 ENTITIES</a>
 */
public class Entities extends NCName {

    private static final long serialVersionUID = -4511368195143560809L;

    private Entity[] entities;

    public Entities() {
        super();
    }

    /**
     * ctor for Entities
     *
     * @throws IllegalArgumentException will be thrown if validation fails
     */
    public Entities(String stValue) throws IllegalArgumentException {
        StringTokenizer tokenizer = new StringTokenizer(stValue);
        int count = tokenizer.countTokens();
        entities = new Entity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = new Entity(tokenizer.nextToken());
        }
    }
}
