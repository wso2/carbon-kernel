package org.wso2.carbon.tomcat.ext.servlet;

import org.wso2.carbon.base.ServletRequestHolder;
import org.wso2.carbon.tomcat.ext.internal.CarbonTomcatServiceHolder;
import org.wso2.carbon.utils.CarbonUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.equinox.http.servlet.HttpServiceServlet;

/**
 * This class register itself under a tomcat web-context and delegates all the calls to the
 * {@link HttpServiceServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
 * method
 */
public class DelegationServlet implements Servlet {

    private static Log log = LogFactory.getLog(DelegationServlet.class);
    private Servlet httpServiceServlet = new HttpServiceServlet();
    private boolean initiated = false;

    public void init(ServletConfig config) throws ServletException {
        if (log.isDebugEnabled()) {
            log.debug("within the init method of DelegationServlet ");
        }
        if (!initiated) {
            initiated = true;
            httpServiceServlet.init(config);
        }
    }

    public void destroy() {
        httpServiceServlet.destroy();
    }

    /**
     * All the service calls get delegated to httpProxy servlet implemented by equinox org.eclipse.equinox.http.servlet
     * bundle
     *
     * @param request  requestObject injected by servlet Engine
     * @param response responseObject injected by servlet Ending
     * @throws ServletException
     * @throws java.io.IOException
     */
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        if (CarbonUtils.isRunningOnLocalTransportMode()) {
            ServletRequestHolder.setServletRequest((HttpServletRequest) request);
        }

        // before invoking the service call we set the context classLoader to ContextFinder. Otherwise the default context
        // classLoader is web-app class loader. (the default CL of the http-nio listeners)
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader contextFinderClassLoader = CarbonTomcatServiceHolder.getTccl();
            if (contextFinderClassLoader != null) {
                Thread.currentThread().setContextClassLoader(contextFinderClassLoader);
            }
            httpServiceServlet.service(request, response);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    public ServletConfig getServletConfig() {
        return httpServiceServlet.getServletConfig();
    }

    public String getServletInfo() {
        return httpServiceServlet.getServletInfo();
    }
}
