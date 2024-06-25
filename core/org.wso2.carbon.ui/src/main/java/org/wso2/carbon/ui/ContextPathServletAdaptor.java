package org.wso2.carbon.ui;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class ContextPathServletAdaptor implements Servlet {
    private Servlet delegate;
    String contextPath;

    public ContextPathServletAdaptor(Servlet delegate, String contextPath) {
        this.delegate = delegate;
        this.contextPath = contextPath != null && !contextPath.equals("/") ? contextPath : "";
    }

    public void init(ServletConfig config) throws ServletException {
        this.delegate.init(new ContextPathServletAdaptor.ServletConfigAdaptor(config));
    }

    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        this.delegate.service(new ContextPathServletAdaptor.HttpServletRequestAdaptor((HttpServletRequest)request), response);
    }

    public void destroy() {
        this.delegate.destroy();
    }

    public ServletConfig getServletConfig() {
        return this.delegate.getServletConfig();
    }

    public String getServletInfo() {
        return this.delegate.getServletInfo();
    }

    private class RequestDispatcherAdaptor implements RequestDispatcher {
        private RequestDispatcher requestDispatcher;

        public RequestDispatcherAdaptor(RequestDispatcher requestDispatcher) {
            this.requestDispatcher = requestDispatcher;
        }

        public void forward(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
            if (req instanceof ContextPathServletAdaptor.HttpServletRequestAdaptor) {
                req = ((ContextPathServletAdaptor.HttpServletRequestAdaptor)req).getRequest();
            }

            this.requestDispatcher.forward(req, resp);
        }

        public void include(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
            if (req instanceof ContextPathServletAdaptor.HttpServletRequestAdaptor) {
                req = ((ContextPathServletAdaptor.HttpServletRequestAdaptor)req).getRequest();
            }

            this.requestDispatcher.include(req, resp);
        }
    }

    private class HttpServletRequestAdaptor extends HttpServletRequestWrapper {
        static final String INCLUDE_REQUEST_URI_ATTRIBUTE = "javax.servlet.include.request_uri";
        static final String INCLUDE_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.include.context_path";
        static final String INCLUDE_SERVLET_PATH_ATTRIBUTE = "javax.servlet.include.servlet_path";
        static final String INCLUDE_PATH_INFO_ATTRIBUTE = "javax.servlet.include.path_info";
        private boolean isRequestDispatcherInclude;

        public HttpServletRequestAdaptor(HttpServletRequest req) {
            super(req);
            this.isRequestDispatcherInclude = req.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE) != null;
        }

        public String getServletPath() {
            if (this.isRequestDispatcherInclude) {
                return super.getServletPath();
            } else {
                String fullPath = super.getServletPath();
                return fullPath.substring(
                        ContextPathServletAdaptor.this.contextPath.length());
            }
        }

        public String getContextPath() {
            return this.isRequestDispatcherInclude ? super.getContextPath() : super.getContextPath() + ContextPathServletAdaptor.this.contextPath;
        }

        public Object getAttribute(String attributeName) {
            if (this.isRequestDispatcherInclude) {
                String servletPath;
                if (attributeName.equals(INCLUDE_CONTEXT_PATH_ATTRIBUTE)) {
                    servletPath = (String)super.getAttribute(INCLUDE_CONTEXT_PATH_ATTRIBUTE);
                    if (servletPath != null && !servletPath.equals("/")) {
                        return servletPath + ContextPathServletAdaptor.this.contextPath;
                    }

                    return ContextPathServletAdaptor.this.contextPath;
                }

                if (attributeName.equals(INCLUDE_SERVLET_PATH_ATTRIBUTE)) {
                    servletPath = (String)super.getAttribute(INCLUDE_SERVLET_PATH_ATTRIBUTE);
                    return servletPath.substring(
                            ContextPathServletAdaptor.this.contextPath.length());
                }
            }

            return super.getAttribute(attributeName);
        }

        public RequestDispatcher getRequestDispatcher(String arg0) {
            return ContextPathServletAdaptor.this.new RequestDispatcherAdaptor(super.getRequestDispatcher(
                    ContextPathServletAdaptor.this.contextPath + arg0));
        }
    }

    private class ServletContextAdaptor implements ServletContext {
        private ServletContext delegate;

        public ServletContextAdaptor(ServletContext delegate) {
            this.delegate = delegate;
        }

        public RequestDispatcher getRequestDispatcher(String path) {
            return ContextPathServletAdaptor.this.contextPath.equals("/") ? ContextPathServletAdaptor.this.new RequestDispatcherAdaptor(this.delegate.getRequestDispatcher(path)) : ContextPathServletAdaptor.this.new RequestDispatcherAdaptor(this.delegate.getRequestDispatcher(
                    ContextPathServletAdaptor.this.contextPath + path));
        }

        public URL getResource(String name) throws MalformedURLException {
            return this.delegate.getResource(name);
        }

        public InputStream getResourceAsStream(String name) {
            return this.delegate.getResourceAsStream(name);
        }

        public Set getResourcePaths(String name) {
            return this.delegate.getResourcePaths(name);
        }

        public Object getAttribute(String arg0) {
            return this.delegate.getAttribute(arg0);
        }

        public Enumeration getAttributeNames() {
            return this.delegate.getAttributeNames();
        }

        public ServletContext getContext(String arg0) {
            return this.delegate.getContext(arg0);
        }

        public String getInitParameter(String arg0) {
            return this.delegate.getInitParameter(arg0);
        }

        public Enumeration getInitParameterNames() {
            return this.delegate.getInitParameterNames();
        }

        public boolean setInitParameter(String s, String s1) {
            return this.delegate.setInitParameter(s, s1);
        }

        public int getMajorVersion() {
            return this.delegate.getMajorVersion();
        }

        public String getMimeType(String arg0) {
            return this.delegate.getMimeType(arg0);
        }

        public int getMinorVersion() {
            return this.delegate.getMinorVersion();
        }

        public int getEffectiveMajorVersion() {
            return this.delegate.getEffectiveMajorVersion();
        }

        public int getEffectiveMinorVersion() {
            return this.delegate.getEffectiveMinorVersion();
        }

        public RequestDispatcher getNamedDispatcher(String arg0) {
            return ContextPathServletAdaptor.this.new RequestDispatcherAdaptor(this.delegate.getNamedDispatcher(arg0));
        }

        public String getRealPath(String arg0) {
            return this.delegate.getRealPath(arg0);
        }

        public String getServerInfo() {
            return this.delegate.getServerInfo();
        }

        /** @deprecated */
        public Servlet getServlet(String arg0) throws ServletException {
            return this.delegate.getServlet(arg0);
        }

        public String getServletContextName() {
            return this.delegate.getServletContextName();
        }

        public ServletRegistration.Dynamic addServlet(String s, String s1) {
            return this.delegate.addServlet(s, s1);
        }

        public ServletRegistration.Dynamic addServlet(String s, Servlet servlet) {
            return this.delegate.addServlet(s, servlet);
        }

        public ServletRegistration.Dynamic addServlet(String s, Class<? extends Servlet> aClass) {
            return this.delegate.addServlet(s, aClass);
        }

        public <T extends Servlet> T createServlet(Class<T> tClass) throws ServletException {
            return this.delegate.createServlet(tClass);
        }

        public ServletRegistration getServletRegistration(String s) {
            return this.delegate.getServletRegistration(s);
        }

        public Map<String, ? extends ServletRegistration> getServletRegistrations() {
            return this.delegate.getServletRegistrations();
        }

        public FilterRegistration.Dynamic addFilter(String s, String s1) {
            return this.delegate.addFilter(s, s1);
        }

        public FilterRegistration.Dynamic addFilter(String s, Filter filter) {
            return this.delegate.addFilter(s, filter);
        }

        public FilterRegistration.Dynamic addFilter(String s, Class<? extends Filter> aClass) {
            return this.delegate.addFilter(s, aClass);
        }

        public <T extends Filter> T createFilter(Class<T> tClass) throws ServletException {
            return this.delegate.createFilter(tClass);
        }

        public FilterRegistration getFilterRegistration(String s) {
            return this.delegate.getFilterRegistration(s);
        }

        public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
            return this.delegate.getFilterRegistrations();
        }

        public SessionCookieConfig getSessionCookieConfig() {
            return this.delegate.getSessionCookieConfig();
        }

        public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) throws IllegalStateException, IllegalArgumentException {
            this.delegate.setSessionTrackingModes(sessionTrackingModes);
        }

        public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            return this.delegate.getDefaultSessionTrackingModes();
        }

        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            return this.delegate.getEffectiveSessionTrackingModes();
        }

        public void addListener(Class<? extends EventListener> aClass) {
            this.delegate.addListener(aClass);
        }

        public void addListener(String s) {
            this.delegate.addListener(s);
        }

        public <T extends EventListener> void addListener(T t) {
            this.delegate.addListener(t);
        }

        public <T extends EventListener> T createListener(Class<T> tClass) throws ServletException {
            return this.delegate.createListener(tClass);
        }

        public void declareRoles(String... strings) {
            this.delegate.declareRoles(strings);
        }

        public ClassLoader getClassLoader() {
            return this.delegate.getClassLoader();
        }

        public JspConfigDescriptor getJspConfigDescriptor() {
            return this.delegate.getJspConfigDescriptor();
        }

        /** @deprecated */
        public Enumeration getServletNames() {
            return this.delegate.getServletNames();
        }

        /** @deprecated */
        public Enumeration getServlets() {
            return this.delegate.getServlets();
        }

        /** @deprecated */
        public void log(Exception arg0, String arg1) {
            this.delegate.log(arg0, arg1);
        }

        public void log(String arg0, Throwable arg1) {
            this.delegate.log(arg0, arg1);
        }

        public void log(String arg0) {
            this.delegate.log(arg0);
        }

        public void removeAttribute(String arg0) {
            this.delegate.removeAttribute(arg0);
        }

        public void setAttribute(String arg0, Object arg1) {
            this.delegate.setAttribute(arg0, arg1);
        }

        public String getContextPath() {
            try {
                Method getContextPathMethod = this.delegate.getClass().getMethod("getContextPath", (Class[])null);
                return (String)getContextPathMethod.invoke(this.delegate, (Object[])null);
            } catch (Exception var2) {
                return null;
            }
        }
    }

    private class ServletConfigAdaptor implements ServletConfig {
        private ServletConfig config;
        private ServletContext context;

        public ServletConfigAdaptor(ServletConfig config) {
            this.config = config;
            this.context = ContextPathServletAdaptor.this.new ServletContextAdaptor(config.getServletContext());
        }

        public String getInitParameter(String arg0) {
            return this.config.getInitParameter(arg0);
        }

        public Enumeration getInitParameterNames() {
            return this.config.getInitParameterNames();
        }

        public ServletContext getServletContext() {
            return this.context;
        }

        public String getServletName() {
            return this.config.getServletName();
        }
    }
}
