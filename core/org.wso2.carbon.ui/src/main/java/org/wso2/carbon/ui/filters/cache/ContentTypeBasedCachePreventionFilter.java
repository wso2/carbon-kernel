/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.ui.filters.cache;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * This filter implements browser/proxy cache prevention based on content types.
 * That is done by whitelisting or blacklisting a defined set of content type
 * patterns.
 *
 * It would prevent sensitive information being cached on web browsers and
 * intermediate network devices that adhere to protocols HTTP 1.0 through HTTP
 * 1.1. In order to accommodate future modifications to protocol specifications
 * and introduction of custom cache prevention headers and values, the filter
 * can also utilize headers and values that are defined in the web.xml file.
 *
 * @since 4.2.0
 */
public class ContentTypeBasedCachePreventionFilter extends AbstractCachePreventionFilter {

    @Override
    /**
     * {@inheritDoc}
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        chain.doFilter(request, new HttpServletResponseWrapper((HttpServletResponse) response) {

            @Override
            /**
             * {@inheritDoc}
             */
            public void setContentType(String contentType) {
                if (canApplyCachePreventionHeaders(contentType)) {
                    applyCachePreventionHeaders(this);
                }
                super.setContentType(contentType);
            }
        });
    }
}