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

package org.apache.axiom.om;

/**
 * Interface OMNamespace.
 * <p>
 * Implementations of this interface must be immutable or behave as if they were immutable,
 * i.e. {@link #getPrefix()} and {@link #getNamespaceURI()} must always return the same
 * values when invoked on the same instance.
 */
public interface OMNamespace {
    /**
     * Method equals.
     *
     * @param uri
     * @param prefix
     * @return Returns boolean.
     */
    boolean equals(String uri, String prefix);

    /**
     * Method getPrefix.
     *
     * @return Returns String.
     */
    String getPrefix();

    /**
     * Method getName.
     *
     * @return Returns String.
     * @deprecated This method is deprecated. Please use getNamespaceURI() method instead.
     */
    String getName();

    /**
     * Provides the namespace URI of this namespace.
     *
     * @return - the namespace URI of the namespace.
     */
    String getNamespaceURI();
}
