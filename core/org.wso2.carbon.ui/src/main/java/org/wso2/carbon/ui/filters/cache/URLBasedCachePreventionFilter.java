package org.wso2.carbon.ui.filters.cache;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class URLBasedCachePreventionFilter extends AbstractCachePreventionFilter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        if (isApplyCachePreventionHeaders(((HttpServletRequest) request).getRequestURI())) {
            applyCachePreventionHeaders(httpServletResponse);
        }

        chain.doFilter(request, httpServletResponse);
    }
}