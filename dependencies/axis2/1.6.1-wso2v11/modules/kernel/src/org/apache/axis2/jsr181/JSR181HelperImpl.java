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

package org.apache.axis2.jsr181;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

class JSR181HelperImpl extends JSR181Helper {
    @Override
    public WebServiceAnnotation getWebServiceAnnotation(Class<?> clazz) {
        WebService annotation = clazz.getAnnotation(WebService.class);
        return annotation == null ? null : new WebServiceAnnotation(annotation.targetNamespace(),
                annotation.serviceName());
    }

    @Override
    public WebMethodAnnotation getWebMethodAnnotation(Method method) {
        WebMethod annotation = method.getAnnotation(WebMethod.class);
        return annotation == null ? null : new WebMethodAnnotation(annotation.exclude(),
                annotation.action(),annotation.operationName());
    }

    @Override
    public WebParamAnnotation getWebParamAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof WebParam) {
                WebParam webParam = (WebParam)annotation;
                return new WebParamAnnotation(webParam.name());
            }
        }
        return null;
    }

    @Override
    public WebResultAnnotation getWebResultAnnotation(Method method) {
        WebResult annotation = method.getAnnotation(WebResult.class);
        return annotation == null ? null : new WebResultAnnotation(annotation.name());
    }
}
