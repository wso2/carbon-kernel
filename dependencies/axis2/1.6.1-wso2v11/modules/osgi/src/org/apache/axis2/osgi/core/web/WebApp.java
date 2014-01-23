/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
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

package org.apache.axis2.osgi.core.web;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;

/**
 *
 * WebApp is a utility class for describing a WebApplication to be deployed into an OSGi
 * HTTP Service implementation. The WebApp implementation extends the OSGi <code>HttpContext</code>.
 */
public class WebApp implements HttpContext {
	protected static WebAppDescriptor webAppDescriptor = null;

	protected HttpService httpService;

	protected ServiceReference sRef;

	public WebApp(WebAppDescriptor descriptor) {
		webAppDescriptor = descriptor;
	}

	// Return null and let the HTTP determine the type
	public String getMimeType(String reqEntry) {
		return null;
	}

	// Get the resource from the jar file, use the class loader to do it
	public URL getResource(String name) {
		URL url = getClass().getResource(name);

		return url;
	}

	public boolean handleSecurity(HttpServletRequest request,
			HttpServletResponse response) throws java.io.IOException {
		return true;
	}

	/**
	 * Starts the WebApp
	 * @param bc the BundleContext of the WebApp host
	 * @throws BundleException
	 */
	public void start(BundleContext bc) throws BundleException {
		if ((sRef = bc.getServiceReference("org.osgi.service.http.HttpService")) == null)
			throw new BundleException("Failed to get HttpServiceReference");
		if ((httpService = (HttpService) bc.getService(sRef)) == null)
			throw new BundleException("Failed to get HttpService");
		try {
			WebAppDescriptor wad = webAppDescriptor;

			for (int i = 0; i < wad.servlet.length; i++) {
				ServletDescriptor servlet = wad.servlet[i];

				httpService.registerServlet(wad.context + servlet.subContext,
						servlet.servlet, servlet.initParameters, this);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BundleException("Failed to register servlets");
		}
	}

	/**
	 * Stops the WebApp
	 * @param bc the BundleContext of the WebApp host
	 * @throws BundleException
	 */
	public void stop(BundleContext bc) throws BundleException {
		try {
			for (int i = 0; i < webAppDescriptor.servlet.length; i++) {
				ServletDescriptor servlet = webAppDescriptor.servlet[i];

				httpService.unregister(webAppDescriptor.context
						+ servlet.subContext);
			}
			bc.ungetService(sRef);
			httpService = null;
			webAppDescriptor = null;
		} catch (Exception e) {
			throw new BundleException("Failed to unregister resources", e);
		}
	}

    public static WebAppDescriptor getWebAppDescriptor() {
        return webAppDescriptor;
    }

    public static void setWebAppDescriptor(WebAppDescriptor webAppDescriptor) {
        WebApp.webAppDescriptor = webAppDescriptor;
    }

    public HttpService getHttpService() {
        return httpService;
    }

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public ServiceReference getSRef() {
        return sRef;
    }

    public void setSRef(ServiceReference sRef) {
        this.sRef = sRef;
    }
}
