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
package org.apache.axis2.osgi.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.osgi.OSGiAxisServlet;
import static org.apache.axis2.osgi.deployment.OSGiAxis2Constants.AXIS2_OSGi_ROOT_CONTEXT;
import org.apache.axis2.osgi.deployment.OSGiConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.*;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import javax.servlet.ServletException;

/**
 * Activator will set the necessary parameters that initiate Axis2 OSGi integration
 */
public class Activator implements BundleActivator{

    private static Log log = LogFactory.getLog(Activator.class);

    private HttpServiceTracker tracker;

    private final OSGiConfigurationContextFactory managedService;

    public Activator() {
        managedService = new OSGiConfigurationContextFactory();
    }

    public void start(BundleContext context) throws Exception {
        managedService.start(context);
        managedService.updated(null);
        tracker = new HttpServiceTracker(context);
        tracker.open();
    }

    public void stop(BundleContext context) {
        tracker.close();
        managedService.stop();
        //ungetService ConfigurationContext.class.getName()
        ServiceReference configCtxRef =
                context.getServiceReference(ConfigurationContext.class.getName());
        if (configCtxRef != null) {
            context.ungetService(configCtxRef);
        }
    }

    //HttpServiceTracker

    class HttpServiceTracker extends ServiceTracker {

        public HttpServiceTracker(BundleContext context) {
            super(context, HttpService.class.getName(), null);
        }

        public Object addingService(ServiceReference serviceReference) {

            HttpService httpService = (HttpService) context.getService(serviceReference);
            try {
                OSGiAxisServlet axisServlet = new OSGiAxisServlet(context);
                ServiceReference configCtxRef =
                        context.getServiceReference(ConfigurationContext.class.getName());
                ConfigurationContext configCtx =
                        (ConfigurationContext) context.getService(configCtxRef);
                String propServiceContextRoot = context.getProperty(AXIS2_OSGi_ROOT_CONTEXT);
                String serviceContextRoot = "services";
                if (propServiceContextRoot != null && propServiceContextRoot.length() != 0) {
                    if (propServiceContextRoot.startsWith("/")) {
                        serviceContextRoot = propServiceContextRoot.substring(1);
                    } else {
                        serviceContextRoot = propServiceContextRoot;
                    }
                }
                configCtx.setServicePath(serviceContextRoot);
                String contextRoot = "/" + serviceContextRoot;
                log.info("Registering SOAP message listener servlet to context : " + contextRoot);
                httpService.registerServlet(contextRoot, axisServlet, null, null);
            } catch (ServletException e) {
                String msg = "Error while registering servlets";
                log.error(msg, e);
            } catch (NamespaceException e) {
                String msg = "Namespace missmatch when registering servlets";
                log.error(msg, e);
            }
            return httpService;
        }

    }
}
