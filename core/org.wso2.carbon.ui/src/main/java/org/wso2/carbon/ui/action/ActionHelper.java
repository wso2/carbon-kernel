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
package org.wso2.carbon.ui.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tiles.Attribute;
import org.apache.tiles.AttributeContext;
import org.apache.tiles.TilesContainer;
import org.apache.tiles.access.TilesAccess;
import org.apache.tiles.request.ApplicationContext;
import org.apache.tiles.request.Request;
import org.apache.tiles.request.servlet.ServletRequest;
import org.apache.tiles.request.servlet.ServletUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class ActionHelper {
	private static Log log = LogFactory.getLog(ActionHelper.class);

    /**
     *
     * @param actionUrl url should be start with "/"
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws Exception Exception
     */
    public static void render(String actionUrl, HttpServletRequest request,
                       HttpServletResponse response) throws Exception {

        ServletContext servletContext = request.getSession().getServletContext();
        ApplicationContext applicationContext = ServletUtil.getApplicationContext(servletContext);
        TilesContainer container = TilesAccess.getContainer(applicationContext);
        if (log.isDebugEnabled()) {
            log.debug(
                    "Rendering tiles main.layout with page : " + actionUrl + "(" + request.getSession().getId() + ")");
        }
        Request requestContext = new ServletRequest(container.getApplicationContext(), request, response);
        AttributeContext attributeContext = container.startContext(requestContext);
        Attribute attr = new Attribute(actionUrl);
        attributeContext.putAttribute("body", attr);
        try {
            container.render("main.layout", requestContext);
            container.endContext(requestContext);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {  // Intentionally logged at debug level
                log.debug("Error occurred while rendering." +
                        " We generally see this 'harmless' exception on WebLogic. Hiding it.", e);
            }
        }
    }

}
