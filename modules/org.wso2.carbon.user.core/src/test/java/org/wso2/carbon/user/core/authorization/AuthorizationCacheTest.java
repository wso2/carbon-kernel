/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.user.core.authorization;

import org.wso2.carbon.user.core.BaseTestCase;

/**
 * Date: Oct 1, 2010 Time: 1:35:11 PM
 */

public class AuthorizationCacheTest extends BaseTestCase {

    public void testAddToCache() {
        AuthorizationCache cache = AuthorizationCache.getInstance();
        try {
            cache.isUserAuthorized(null, 1,"roadrunner","/x/y", "read");
            fail("This user is not yet added. Should be a cache miss");
        } catch (AuthorizationCacheException e) {
            assertTrue(true);            
        }
    }

    public void testAddCacheHit() {
        AuthorizationCache cache = AuthorizationCache.getInstance();
        try {
            cache.isUserAuthorized(null, 1,"roadrunner", "/x/y", "read");
            fail("This user is not yet added. Should be a cache miss");
        } catch (AuthorizationCacheException e) {
            assertTrue(true);
        }

        try {
            cache.addToCache(null,1,"roadrunner", "/x/y", "read", true);

            boolean b = cache.isUserAuthorized(null, 1,"roadrunner", "/x/y", "read");
            assertTrue(b);
            
        } catch (AuthorizationCacheException e) {
            fail("Should be a cache hit.");
        }
    }


}
