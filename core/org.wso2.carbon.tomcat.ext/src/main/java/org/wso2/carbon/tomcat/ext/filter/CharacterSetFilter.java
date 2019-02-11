/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.tomcat.ext.filter;

import javax.servlet.*;
import java.io.IOException;

/***
 * This is a filter class to force the java webapp to handle all requests and responses as UTF-8 encoded by default.
 * This requires that we define a character set filter.
 * This filter makes sure that if the browser hasn't set the encoding used in the request, that it's set to UTF-8.
 */

public class CharacterSetFilter implements Filter {

    private static final String UTF8 = "UTF-8";
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    private String encoding;

    public String getEncoding () {
        return this.encoding;
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        encoding = config.getInitParameter("requestEncoding");

        if (encoding == null) {
            encoding = UTF8;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Respect the client-specified character encoding
        // (see HTTP specification section 3.4.1)
        if (null == request.getCharacterEncoding())  {
            request.setCharacterEncoding(encoding);
        }

        /**
         * Set the default response content type and encoding
         */
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(UTF8);


        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
