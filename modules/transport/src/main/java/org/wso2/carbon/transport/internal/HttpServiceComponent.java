/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.transport.internal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.servlet.SampleServlet;

import javax.servlet.ServletException;

/**
 * This service  component is responsible for retrieving the HttpService
 * OSGi service and register servlets
 */

@Component(
        name = "org.wso2.carbon.transport.HttpServiceComponent",
        immediate = true
)
public class HttpServiceComponent {

    private static final Logger logger = LoggerFactory.getLogger(HttpServiceComponent.class);

    private HttpService httpService;

    @Activate
    protected void start() {
        SampleServlet servlet = new SampleServlet();
        String context = "/sample";
        try {
            logger.info("Registering a sample servlet : {}", context);
            httpService.registerServlet(context, servlet, null,
                                        httpService.createDefaultHttpContext());
        } catch (ServletException | NamespaceException e) {
            logger.error("Error while registering servlet", e);
        }
    }


    @Reference(
            name = "http.service",
            service = HttpService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.STATIC,
            unbind = "unsetHttpService"
    )
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }
}
