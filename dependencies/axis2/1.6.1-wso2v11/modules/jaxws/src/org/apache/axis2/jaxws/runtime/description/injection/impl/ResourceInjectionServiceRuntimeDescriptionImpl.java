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

package org.apache.axis2.jaxws.runtime.description.injection.impl;

import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.runtime.description.injection.ResourceInjectionServiceRuntimeDescription;

import java.lang.reflect.Method;

public class ResourceInjectionServiceRuntimeDescriptionImpl implements
        ResourceInjectionServiceRuntimeDescription {

    private ServiceDescription serviceDesc;
    private String key;
    private boolean _hasResourceAnnotation;
    private Method _postConstructMethod;
    private Method _preDestroyMethod;

    protected ResourceInjectionServiceRuntimeDescriptionImpl(String key,
                                                             ServiceDescription serviceDesc) {
        this.serviceDesc = serviceDesc;
        this.key = key;
    }

    public boolean hasResourceAnnotation() {
        return _hasResourceAnnotation;
    }

    public Method getPostConstructMethod() {
        return _postConstructMethod;
    }

    public Method getPreDestroyMethod() {
        return _preDestroyMethod;
    }

    public ServiceDescription getServiceDescription() {
        return serviceDesc;
    }

    public String getKey() {
        return key;
    }

    /**
     * Called by Builder code
     *
     * @param value
     */
    void setResourceAnnotation(boolean value) {
        _hasResourceAnnotation = value;
    }

    void setPostConstructMethod(Method method) {
        _postConstructMethod = method;
    }

    void setPreDestroyMethod(Method method) {
        _preDestroyMethod = method;
    }

    public String toString() {
        final String newline = "\n";
        StringBuffer string = new StringBuffer();

        string.append(newline);
        string.append("  ResourceInjectionServiceRuntime:" + getKey());
        string.append(newline);
        string.append("    @Resource Annotation = " + hasResourceAnnotation());
        string.append(newline);
        string.append("    PostConstruct Method = " + getPostConstructMethod());
        string.append(newline);
        string.append("    PreDestroy Method    = " + getPreDestroyMethod());

        return string.toString();
    }


}
