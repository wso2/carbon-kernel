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
package org.wso2.carbon.bridge;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is the servlet that bridges the servlet container and OSGi framework. All the requests
 * comming into this servlet will be delegated to HttpService.
 */
public class BridgeServlet extends HttpServlet {

    private static final String CARBON_HOME = "carbon.home";
    
    private static final String INCLUDE_REQUEST_URI_ATTRIBUTE =
            "javax.servlet.include.request_uri";

    private static final String INCLUDE_SERVLET_PATH_ATTRIBUTE =
            "javax.servlet.include.servlet_path";

    private static final String INCLUDE_PATH_INFO_ATTRIBUTE =
            "javax.servlet.include.path_info";

    private FrameworkLauncher frameworkLauncher;
    
    private static BridgeServlet instance;

    private HttpServlet delegate;

    private static Lock lock = new ReentrantLock();

    private boolean enableFrameworkControls;

    private boolean initiated = false;

    public void init() throws ServletException {
        if(initiated){
            return;
        }
        initiated = true;
        setInstance(this);

        /**
         * Set the carbon.home system property if it is not already set. This happens when
         * Carbon runs on other app servers. Eg: Tomcat
         */
        String carbonHome = System.getProperty(CARBON_HOME);
        if (carbonHome == null) {
            if ((carbonHome = System.getenv("CARBON_HOME")) != null) {
                System.setProperty(CARBON_HOME, carbonHome);
            } else {
                throw new ServletException(
                        "CARBON_HOME environment variable is not set. Can't proceed.");
            }
        }

        //NOTE: CarbonContext initialization removed
        String carbonRepo = getInitParameter("carbonRepository");
        if (carbonRepo != null) {
            System.setProperty("carbon.repository", carbonRepo);
            instance.getServletContext().log("Carbon Repository : " + carbonRepo);
        }

        String axis2Repo = getInitParameter("axis2Repository");
        if (axis2Repo != null) {
            System.setProperty("axis2.repo", axis2Repo);
            instance.getServletContext().log("Axis2 Repository : " + axis2Repo);
        }

        try {
            String enableFrameworkControlsParameter =
                    getServletConfig().getInitParameter("enableFrameworkControls");
            enableFrameworkControls = (enableFrameworkControlsParameter != null &&
                                       enableFrameworkControlsParameter.equals("true"));
            frameworkLauncher = FrameworkLauncherFactory.getFrameworkLauncher();
            frameworkLauncher.init(getServletConfig());
            frameworkLauncher.deploy();
            frameworkLauncher.start();
        } catch (Exception e) {                              
            throw new ServletException(e);
        }
    }

    /**
     * service is called by the Servlet Container and will first determine if the request is a
     * framework control and will otherwise try to delegate to the registered servlet delegate
     */
    protected void service(HttpServletRequest req,
                           HttpServletResponse resp) throws ServletException, IOException {
        if(!FrameworkLauncherFactory.getFrameworkLauncher().isRunning()){
//            throw new ServletException("Carbon server has been stopped");
            // TODO: A temporary hack until we figure out the reason for requests being received after
            // TODO: the Tomcat connectors have been stopped
            return;
        }

        try {
            // We wrap the original HttpRequest & HttpSession so that attribute serialization
            // issues that occur on app servers like WebLogic can be overcome
            req = new CarbonHttpServletRequest(req);

            //This is applicable only for the Management Console. This property become true if the UI framework
            // is configured to run on the local transport - performance enhancement


            if (req.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE) == null) {
                String pathInfo = req.getPathInfo();
                // Check if this is being handled by an extension mapping
                if (pathInfo == null && isExtensionMapping(req.getServletPath()))
                    req = new ExtensionMappingRequest(req);

                if (enableFrameworkControls) {
                    if (pathInfo != null && pathInfo.startsWith("/sp_")) { //$NON-NLS-1$
                        if (serviceFrameworkControls(req, resp)) {
                            return;
                        }
                    }
                }
            } else {
                String pathInfo = (String) req.getAttribute(INCLUDE_PATH_INFO_ATTRIBUTE);
                // Check if this is being handled by an extension mapping
                if (pathInfo == null || pathInfo.length() == 0) {
                    String servletPath = (String) req.getAttribute(INCLUDE_SERVLET_PATH_ATTRIBUTE);
                    if (isExtensionMapping(servletPath))
                        req = new IncludedExtensionMappingRequest(req);
                }
            }

            ClassLoader original = Thread.currentThread().getContextClassLoader();
            HttpServlet servletReference = acquireDelegateReference();
            if (servletReference == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                               "BridgeServlet: " + req.getRequestURI());
                return;
            }
            try {
                ClassLoader frameworkContextClassLoader =
                        frameworkLauncher.getFrameworkContextClassLoader();
                if (frameworkContextClassLoader != null) {
                    Thread.currentThread()
                            .setContextClassLoader(frameworkContextClassLoader);
                }
                servletReference.service(req, resp);
            } finally {
    //            releaseDelegateReference();
                Thread.currentThread().setContextClassLoader(original);
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    private synchronized HttpServlet acquireDelegateReference() {
        if (delegate != null) {
        }
//			++delegateReferenceCount;
        return delegate;
    }

    private static class ExtensionMappingRequest extends HttpServletRequestWrapper {

        public ExtensionMappingRequest(HttpServletRequest req) {
            super(req);
        }

        public String getPathInfo() {
            return super.getServletPath();
        }

        public String getServletPath() {
            return "";
        }
    }

    private static class IncludedExtensionMappingRequest extends HttpServletRequestWrapper {

        public IncludedExtensionMappingRequest(HttpServletRequest req) {
            super(req);
        }

        public Object getAttribute(String attributeName) {
            if (attributeName.equals(INCLUDE_SERVLET_PATH_ATTRIBUTE)) {
                return "";
            } else if (attributeName.equals(INCLUDE_PATH_INFO_ATTRIBUTE)) {
                String servletPath = (String) super.getAttribute(INCLUDE_SERVLET_PATH_ATTRIBUTE);
                return servletPath;
            }
            return super.getAttribute(attributeName);
        }
    }

    private boolean isExtensionMapping(String servletPath) {
        if (servletPath == null)
            return false;

        String lastSegment = servletPath;
        int lastSlash = servletPath.lastIndexOf('/');
        if (lastSlash != -1)
            lastSegment = servletPath.substring(lastSlash + 1);

        return lastSegment.indexOf('.') != -1;
    }

    private static synchronized void setInstance(BridgeServlet servlet) {
        if ((instance != null) && (servlet != null)) {
            throw new IllegalStateException("instance already set");
        }
        instance = servlet;
    }

    public static synchronized void registerServletDelegate(HttpServlet servletDelegate) {
        if (instance == null) {
            // shutdown already
            return;
        }

        if (servletDelegate == null) {
            throw new NullPointerException("cannot register a null servlet delegate");
        }
        lock.lock();
        try {
            if (instance.delegate != null)
                throw new IllegalStateException(
                        "A Servlet Proxy is already registered");

            try {
                servletDelegate.init(instance.getServletConfig());
            } catch (ServletException e) {
                instance.getServletContext()
                        .log("Error initializing servlet delegate", e);
                return;
            }
            instance.delegate = servletDelegate;
        } finally {
            lock.unlock();
        }
    }

    public static synchronized void unregisterServletDelegate(HttpServlet servletDelegate) {
        if (instance == null) {
            // shutdown already
            return;
        }
        lock.lock();
        try {
            if (instance.delegate == null)
                throw new IllegalStateException("No servlet delegate is registered"); //$NON-NLS-1$

            if (instance.delegate != servletDelegate)
                throw new IllegalStateException(
                        "Servlet delegate does not match registered servlet delegate"); //$NON-NLS-1$

            HttpServlet oldProxy = instance.delegate;
            instance.delegate = null;
            /*while (instance.delegateReferenceCount != 0) {
                try {
                    instance.wait();
                } catch (InterruptedException e) {
                    // keep waiting for all requests to finish
                }
            }*/
            oldProxy.destroy();
        } finally {
            lock.unlock();
        }
    }

    public void destroy() {
        frameworkLauncher.stop();
        setInstance(null);
        super.destroy();
    }

    /**
     * serviceFrameworkControls currently supports the following commands (identified by the request's pathinfo)
     * sp_deploy - Copies the contents of /platform to the install area
     * sp_undeploy - Removes the copy of Eclipse from the install area
     * sp_redeploy - Resets the platform (e.g. stops, undeploys, deploys, starts)
     * sp_start - Starts a deployed platform
     * sp_stop - Stops the platform
     * @param req req
     * @param resp response
     * @return status
     * @throws java.io.IOException IOException
     */
    private boolean serviceFrameworkControls(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo.equals("/sp_start")) {
            frameworkLauncher.start();
            resp.getWriter().write("Platform Started");
            return true;
        } else if (pathInfo.equals("/sp_stop")) {
            frameworkLauncher.stop();
            resp.getWriter().write("Platform Stopped");
            return true;
        } else if (pathInfo.equals("/sp_deploy")) {
//            frameworkLauncher.deploy();
            resp.getWriter().write("Platform Deployed");
            return true;
        } else if (pathInfo.equals("/sp_undeploy")) {
            frameworkLauncher.undeploy();
            resp.getWriter().write("Platform Undeployed");
            return true;
        } else if (pathInfo.equals("/sp_reset")) {
            frameworkLauncher.stop();
            frameworkLauncher.start();
            resp.getWriter().write("Platform Reset");
            return true;
        } else if (pathInfo.equals("/sp_redeploy")) {
            frameworkLauncher.stop();
            frameworkLauncher.undeploy();
//            frameworkLauncher.deploy();
            frameworkLauncher.start();
            resp.getWriter().write("Platform Redeployed");
            return true;
        } else if (pathInfo.equals("/sp_test")) {
            if (delegate == null)
                resp.getWriter().write("Servlet delegate not registered.");
            else
                resp.getWriter().write("Servlet delegate registered - " +
                                       delegate.getClass().getName());
            return true;
        }
        return false;
    }

}
