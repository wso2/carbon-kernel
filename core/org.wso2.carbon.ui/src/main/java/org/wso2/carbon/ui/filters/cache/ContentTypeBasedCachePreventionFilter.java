package org.wso2.carbon.ui.filters.cache;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

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
                if (isApplyCachePreventionHeaders(contentType)) {
                    applyCachePreventionHeaders(this);
                }
                super.setContentType(contentType);
            }
        });
    }
}