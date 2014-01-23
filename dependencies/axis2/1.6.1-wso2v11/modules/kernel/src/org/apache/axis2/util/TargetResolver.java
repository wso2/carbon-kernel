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

package org.apache.axis2.util;

import org.apache.axis2.context.MessageContext;

/**
 * TargetResolver
 * <p/>
 * Interface to be implemented by code to update the invocation target URL
 * before the transport is selected and the engine invoked.
 * <p/>
 * Examples of use:
 * 1. wsa:To set to a URN value which needs translated to a targetable URL
 * 2. support clustering where a single URI may repesent multiple servers and one must be selected
 */
public interface TargetResolver {
    /**
     * resolveTarget examines the MessageContext and updates the MessageContext
     * in order to resolve the target.
     */
    public void resolveTarget(MessageContext messageContext);
}