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

package org.apache.axis2.schema.extension;

import org.apache.axis2.databinding.types.Language;
import org.apache.axis2.schema.AbstractTestCase;

public class SimpleExtensionTest extends AbstractTestCase {

    public void testSimpleTypeComplexExtension() throws Exception {
        FullName fullName = new FullName();
        fullName.setFirst("amila");
        fullName.setMiddle("chinthaka");
        fullName.setLast("suriarachchi");
        fullName.setLanguage(new Language("singhala"));
        fullName.setAttribute1(BaseType.Factory.fromString(BaseType._s1, ""));

        fullName.setAttribute2(SimpleType.Factory.fromString("ATTRIBUTE", ""));

        testSerializeDeserialize(fullName, false);
    }


}
