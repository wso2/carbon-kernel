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
package org.apache.axis2.osgi;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.osgi.deployment.OSGiConfigurationContextFactory;
import org.apache.axis2.transport.http.AxisServlet;
import org.apache.axis2.transport.http.ListingAgent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * This servlet is used with the association of HttpService.
 * This is the entry point to all requests.
 */
public class OSGiAxisServlet extends AxisServlet {

    private BundleContext context;

    /**
     * OSGiAxisServlet needs an referenc to OSGi environmentb
     *
     * @param context BundleContext
     */
    public OSGiAxisServlet(BundleContext context) {
        this.context = context;
    }


    public void init(ServletConfig servletConfig) throws ServletException {
        this.servletConfig = servletConfig;
        ServiceReference reference =
                context.getServiceReference(ConfigurationContext.class.getName());
        if (reference == null) {
            throw new ServletException(
                    "An instance of ConfigurationContext is not available to continue the proccess.");
        }
        configContext = (ConfigurationContext) context.getService(reference);
        axisConfiguration = configContext.getAxisConfiguration();
        agent = new ListingAgent(configContext);
        initParams();
        ServletContext servletContext = servletConfig.getServletContext();
        if (servletContext != null) {
            servletContext.setAttribute(this.getClass().getName(), this);
        }

    }
}
