/*
 * Copyright 2009-2010 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.feature.mgt.module.handler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.feature.mgt.services.CompMgtConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ComponentMgtHandler extends AbstractHandler {
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        if (!msgContext.isEngaged(CompMgtConstants.COMP_MGT_MODULE_NAME)) {
            return InvocationResponse.CONTINUE;
        }

        HttpSession httpSession = getHttpSession(msgContext);
        msgContext.setProperty(CompMgtConstants.COMP_MGT_SERVELT_SESSION, httpSession);
        return InvocationResponse.CONTINUE;
    }

    public HttpSession getHttpSession(MessageContext messageContext) {
        HttpServletRequest request =
                (HttpServletRequest) messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        return request.getSession(true);
    }
}
