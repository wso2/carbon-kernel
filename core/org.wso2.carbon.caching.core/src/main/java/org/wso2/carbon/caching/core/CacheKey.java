package org.wso2.carbon.caching.core;

import java.io.Serializable;

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

/**
 * Date: Oct 1, 2010 Time: 3:23:44 PM
 */

/**
 * Cache key class. Any value that acts as a cache key must encapsulated with a class
 * overriding from this class.
 */
public abstract class CacheKey implements Serializable {

    private static final long serialVersionUID = 1471805737633325514L;

    @Override
    public abstract boolean equals(Object otherObject);

    @Override
    public abstract int hashCode();
}
