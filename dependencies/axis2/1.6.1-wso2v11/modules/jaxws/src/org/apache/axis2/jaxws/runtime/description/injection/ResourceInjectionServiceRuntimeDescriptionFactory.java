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

package org.apache.axis2.jaxws.runtime.description.injection;

import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.runtime.description.injection.impl.ResourceInjectionServiceRuntimeDescriptionBuilder;

public class ResourceInjectionServiceRuntimeDescriptionFactory {

    /** intentionally private */
    private ResourceInjectionServiceRuntimeDescriptionFactory() {
    }

    /**
     * Get or create MarshalServiceRuntimeDescription
     *
     * @param serviceDesc
     * @param implClass
     * @return OperationRuntimeDescription
     */
    public static ResourceInjectionServiceRuntimeDescription get(ServiceDescription serviceDesc,
                                                                 Class implClass) {
        String key = ResourceInjectionServiceRuntimeDescriptionBuilder.getKey(implClass);
        ResourceInjectionServiceRuntimeDescription risrDesc =
                (ResourceInjectionServiceRuntimeDescription)
                        serviceDesc.getServiceRuntimeDesc(key);

        if (risrDesc == null) {
            risrDesc = ResourceInjectionServiceRuntimeDescriptionBuilder
                    .create(serviceDesc, implClass);
            serviceDesc.setServiceRuntimeDesc(risrDesc);
        }
        return risrDesc;
    }
}
