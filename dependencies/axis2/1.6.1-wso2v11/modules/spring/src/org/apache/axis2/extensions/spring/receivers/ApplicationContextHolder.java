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

package org.apache.axis2.extensions.spring.receivers;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Implementation of a Spring interface who is configured in Spring's applicationContext.xml or some
 * other Spring type of way. This class and the spring bean needed to wire it could be used as an
 * alternative to getting the ApplicationContext from the ServletContext.
 */
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContext appCtx;

    public ApplicationContextHolder() {
    }

    /** Spring supplied interface method for injecting app context. */
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        appCtx = applicationContext;
    }

    /** Access to spring wired beans. */
    public static ApplicationContext getContext() {
        return appCtx;
    }

}
