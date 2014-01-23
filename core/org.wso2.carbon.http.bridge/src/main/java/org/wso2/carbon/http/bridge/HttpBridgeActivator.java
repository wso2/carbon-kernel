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
package org.wso2.carbon.http.bridge;

import org.eclipse.equinox.http.servlet.HttpServiceServlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.bridge.BridgeServlet;

/**
 *
 */
public class HttpBridgeActivator implements BundleActivator {

    private HttpServiceServlet httpServiceServlet;

    public void start(BundleContext context) throws Exception {
        httpServiceServlet = new HttpServiceServlet();
		BridgeServlet.registerServletDelegate(httpServiceServlet);
    }

    public void stop(BundleContext context) throws Exception {
        BridgeServlet.unregisterServletDelegate(httpServiceServlet);
		httpServiceServlet = null;
    }
}