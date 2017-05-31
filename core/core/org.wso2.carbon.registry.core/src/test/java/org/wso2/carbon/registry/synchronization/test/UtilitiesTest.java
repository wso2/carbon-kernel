/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.synchronization.test;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.registry.synchronization.Utils;

public class UtilitiesTest extends BaseTestCase {

    public void setUp() {
        super.setUp();

    }

    public void testURLDecomposition() throws RegistryException {
        assertEquals(Utils.getRegistryUrl("/x/y/z"), null);
        assertEquals(Utils.getPath("/x/y/z"), "/x/y/z");
        assertEquals(Utils.getPath("https://localhost:9443/registry/"), "/");
        assertEquals(Utils.getRegistryUrl("https://localhost:9443/registry/x/y/z"),
                "https://localhost:9443/registry");
        assertEquals(Utils.getPath("https://localhost:9443/registry/x/y/z"), "/x/y/z");
        assertEquals(Utils.getPath("https://localhost:9443/registry/registry/"), "/");
        assertEquals(Utils.getRegistryUrl("https://localhost:9443/registry/registry/x/y/z"),
                "https://localhost:9443/registry/registry");
        assertEquals(Utils.getPath("https://localhost:9443/registry/registry/x/y/z"), "/x/y/z");
        assertEquals(Utils.getPath("https://localhost:9443/greg/registry/"), "/");
        assertEquals(Utils.getRegistryUrl("https://localhost:9443/greg/registry/x/y/z"),
                "https://localhost:9443/greg/registry");
        assertEquals(Utils.getPath("https://localhost:9443/greg/registry/x/y/z"), "/x/y/z");
    }

}
