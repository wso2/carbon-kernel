/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.http.client.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.http.client.service.HttpClientService;

/**
 * OSGi declarative services component which handles
 * registration and unregistration of HttpClientServiceComponent.
 */
@Component(
        name = "http.client.service.component",
        immediate = true
)
public class HttpClientServiceComponent {

    private static final Log log = LogFactory.getLog(HttpClientServiceComponent.class);
    private HttpClientDataHolder dataHolder = HttpClientDataHolder.getInstance();

    @Activate
    protected void activate(ComponentContext context) {

        BundleContext bundleContext = context.getBundleContext();
        try {
            bundleContext.registerService(HttpClientService.class.getName(),
                    new HttpClientService(), null);
            log.debug("HttpClient bundle is activated.");
        } catch (Throwable e) {
            log.error("Failed to activate HttpClient bundle.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        log.debug("HttpClient bundle is deactivated.");
    }
}
