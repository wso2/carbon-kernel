/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.utils;

import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
/*
 * 
 */

public class SessionContextUtil {
	
	private SessionContextUtil() {
	    //disable external instantiation
	}
	
    public static SessionContext createSessionContext(MessageContext messageContext) {

        HttpServletRequest request = (HttpServletRequest) messageContext.getProperty(
                HTTPConstants.MC_HTTP_SERVLETREQUEST);
        if (request == null) {
            // This can happen inside a HostObject accessed by a Jaggery application.
            return null;
        }                
        SessionContext sessionContext = null;
        
        HttpSession httpSession = request.getSession(true);
        if (httpSession != null) {
            sessionContext =
                    (SessionContext) httpSession.getAttribute(Constants.SESSION_CONTEXT_PROPERTY);
            if (sessionContext == null) {
                String cookieValueString = httpSession.getId();
                sessionContext = new SessionContext(null);
                sessionContext.setParent(messageContext.getConfigurationContext());
                sessionContext.setCookieID(cookieValueString);
                httpSession.setAttribute(Constants.SESSION_CONTEXT_PROPERTY, sessionContext);
                messageContext.setSessionContext(sessionContext);
                messageContext.setProperty("SessionId", cookieValueString);
            } else if (sessionContext.getParent() != messageContext.getConfigurationContext()) {
                httpSession.removeAttribute(Constants.SESSION_CONTEXT_PROPERTY);
                sessionContext = new SessionContext(null);
                sessionContext.setParent(messageContext.getConfigurationContext());
                String cookieValueString = httpSession.getId();
                sessionContext.setCookieID(cookieValueString);
                messageContext.setProperty("SessionId", cookieValueString);
                httpSession.setAttribute(Constants.SESSION_CONTEXT_PROPERTY,
                                         sessionContext);

            }
        }
        return sessionContext;
    }
}
