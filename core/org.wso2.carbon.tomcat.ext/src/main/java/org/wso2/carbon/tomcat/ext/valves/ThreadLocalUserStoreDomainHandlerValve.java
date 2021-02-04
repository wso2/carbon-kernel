/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.tomcat.ext.valves;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.io.IOException;

import javax.servlet.ServletException;

/**
 * During authentication flows userstore managers, some authenticators, and some listners set authenticated user's
 * userstore domain to a thread local variable via UserCoreUtil#setDomainInThreadLocal method. This valve will
 * ensure that this variable will be cleared upon the completion of each request flow.
 */
@SuppressWarnings("unused")
public class ThreadLocalUserStoreDomainHandlerValve extends ValveBase {

    private static final Log log = LogFactory.getLog(ThreadLocalUserStoreDomainHandlerValve.class);

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {

        try {
            getNext().invoke(request, response);
        } finally {
            if (UserCoreUtil.getDomainFromThreadLocal() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Clearing the user store domain : " + UserCoreUtil.getDomainFromThreadLocal()
                            + ", from the thread local variable.");
                }
                UserCoreUtil.setDomainInThreadLocal(null);
            }
        }
    }
}
