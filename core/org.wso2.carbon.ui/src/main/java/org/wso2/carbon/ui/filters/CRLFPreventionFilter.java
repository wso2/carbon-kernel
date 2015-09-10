package org.wso2.carbon.ui.filters;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.ServerConfiguration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * Prevent Injection of Carriage Return (CR) and Line Feed (LF) characters in response headers.
 * The filter wraps the HttpServletResponse and sanitizes response header values for CR and LF.
 */
public class CRLFPreventionFilter implements Filter {
    private static final String CRLF_CONFIG_ENABLED_PROPERTY = "Security.CRLFPreventionConfig.Enabled";
    private static boolean CRLFPreventionEnabled = false;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Loads enabled configuration at /repository/conf/carbon.xml//Server/Security/CRLFPreventionConfig/Enabled
        ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
        if (serverConfiguration.getFirstProperty(CRLF_CONFIG_ENABLED_PROPERTY) != null && Boolean.parseBoolean(
                serverConfiguration.getFirstProperty(CRLF_CONFIG_ENABLED_PROPERTY))) {
            CRLFPreventionEnabled = true;
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                                   FilterChain filterChain) throws IOException, ServletException {

        if (CRLFPreventionEnabled && servletResponse instanceof HttpServletResponse) {
            CRLFResponseWrapper responseWrapper = new CRLFResponseWrapper((HttpServletResponse) servletResponse);
            filterChain.doFilter(servletRequest, responseWrapper);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        // Nothing to implement
    }

    protected static class CRLFResponseWrapper extends HttpServletResponseWrapper {

        public CRLFResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void addCookie(Cookie cookie) {
            cookie.setValue(sanitize(cookie.getValue()));
            super.addCookie(cookie);
        }

        @Override
        public void addHeader(String name, String value) {
            super.addHeader(sanitize(name), sanitize(value));
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(sanitize(name), sanitize(value));
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            super.sendRedirect(sanitize(location));
        }

        private String sanitize(String input) {

            if (StringUtils.isBlank(input)) {
                return input;
            }

            return input.replaceAll("(\\r|\\n|%0D|%0A|%0a|%0d)", "");
        }
    }

}
