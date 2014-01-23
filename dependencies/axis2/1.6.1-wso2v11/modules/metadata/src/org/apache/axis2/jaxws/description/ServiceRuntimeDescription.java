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

package org.apache.axis2.jaxws.description;

/**
 * A ServiceRuntimeDescription object contains immutable data that is needed during the runtime
 * (i.e. to detect @Resource injection). The ServiceRuntimeDescription must be immutable so that it
 * can safely accessed by multiple theads without synchronization. It cannot be used to store
 * information that may not be available in different threads/ classloaders (i.e. it cannot have
 * references to Class objects)
 * <p/>
 * The actual ServiceRuntimeDescription objects are accessed via the key.
 */
public interface ServiceRuntimeDescription {

    /** @return OperationDesc parent */
    public ServiceDescription getServiceDescription();

    /** @return String */
    public String getKey();
}
