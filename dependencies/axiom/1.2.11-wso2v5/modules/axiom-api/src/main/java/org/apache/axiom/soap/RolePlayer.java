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

package org.apache.axiom.soap;

import java.util.List;

/**
 * This interface represents the thing which provides us with the SOAP roles in a given
 * context.  Used to search through SOAP header blocks.
 */
public interface RolePlayer {
    /**
     * Get a list of the roles supported.  NOTE: the "next" role is automatically supported,
     * and the "none" role (for SOAP 1.2) is automatically disallowed.  The roles returned
     * should only be the non-standard ones, since the ultimate destination role is also
     * handled by the isUltimateDestination method below.
     *
     * @return a List containing URI Strings, one per custom role supported, or null
     */
    List getRoles();

    /**
     * Are we the ultimate destination?
     *
     * @return true if this is the ultimate destination, false if an intermediary.
     */
    boolean isUltimateDestination();
}
