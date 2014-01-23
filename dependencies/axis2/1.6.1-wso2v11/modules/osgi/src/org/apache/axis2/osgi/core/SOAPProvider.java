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

package org.apache.axis2.osgi.core;

import org.apache.axis2.osgi.core.web.WebApp;
import org.osgi.framework.Bundle;

/**
 * SOAPProvider is an interface for generic SOAP functionality.
 * Use this interface to register SOAPProvider services with the OSGi
 * runtime using the <code>BundleContext.registerService()</code> methods.
 */

public interface SOAPProvider {

    /**
     * The name of the SOAPProvider implementation. Use this as the key when constructing
     * the service's properties for registration with the OSGi runtime.
     */
    public static final String PROVIDER_NAME = "Name";

    /**
     * The major version of the SOAPProvider implementation. Use this as the key when constructing
     * the service's properties for registration with the OSGi runtime.
     */
    public static final String PROVIDER_MAJOR_VERSION = "MajorVersion";

    /**
     * The minor version of the SOAPProvider implementation. Use this as the key when constructing
     * the service's properties for registration with the OSGi runtime.
     */
    public static final String PROVIDER_MINOR_VERSION = "MinorVersion";

    /**
     * Getter method for the implementation's provider name. This name should be the same
     * as the one used during the registration of the SOAPProvider service
     *
     * @return the Provider Name
     */
    public String getProviderName();

    /**
     * Getter method for the implementation's version. This name should be constructed
     * from the major and minor versions used during registration of the SOAPProvider service.
     *
     * @return the Provider Version
     */
    public String getProviderVersion();

    public Object getProviderEngine();

    public Object getProviderDeployer();

    public Object getProviderDeployer(WebApp webApp);

    /**
     * Deploys an Object as a WebService using the implementation's default binding type.
     * The service is deployed into the provider's default application context.
     *
     * @param srvName   the display name of the service
     * @param srvClass  the class or interface that should be exposed. Specifying an interface
     *                  allows only the desired methods of the service object to be published.
     * @param srvObject the actual implementation
     * @throws Exception
     */
    public void deployService(String srvName, Class srvClass, Object srvObject, String handlerChain) throws Exception;

    /**
     * Deploys an Object as a WebService using a specified binding type
     * The service is deployed into the provider's default application context.
     *
     * @param srvName     the display name of the service
     * @param bindingType the name of the desired binding type
     * @param srvClass    the class or interface that should be exposed. Specifying an interface
     *                    allows only the desired methods of the service object to be published.
     * @param srvObject   the actual implementation
     * @throws Exception
     */
    public void deployService(String srvName, String bindingType, Class srvClass, Object srvObject, String handlerChain) throws Exception;

    /**
     * Deploys an Object as a WebService using the provider's default binding type.
     * The service is deployed into the specified <code>WebApp</code> context
     *
     * @param webApp    the target web application context
     * @param srvName   the display name of the service
     * @param srvClass  the class or interface that should be exposed. Specifying an interface
     *                  allows only the desired methods of the service object to be published.
     * @param srvObject the actual implementation
     * @throws Exception
     */
    public void deployService(WebApp webApp, String srvName, Class srvClass, Object srvObject, String handlerChain) throws Exception;

    /**
     * Deploys an Object as a WebService using a specified binding type
     * The service is deployed into the specified <code>WebApp</code> context
     *
     * @param webApp      the target web application context
     * @param srvName     the display name of the service
     * @param bindingType the name of the desired binding type
     * @param srvClass    the class or interface that should be exposed. Specifying an interface
     *                    allows only the desired methods of the service object to be published.
     * @param srvObject   the actual implementation
     * @throws Exception
     */
    public void deployService(WebApp webApp, String srvName, String bindingType, Class srvClass, Object srvObject, String handlerChain) throws Exception;

    public void undeployService(String srvName, Class srvClass) throws Exception;

    public void undeployService(WebApp webApp, String srvName, Class srvClass) throws Exception;

    /**
     * Gets a web application from the provider for the given context path.
     *
     * @param contextPath the context path of the desired WebApp
     * @param create      if <code>true</code>, create the WebApp if it does not exits.
     * @return return the WebApp
     * @throws Exception
     */
    public WebApp getWebApp(String contextPath, boolean create) throws Exception;

    /**
     * Gets a web application from the provider for the given context path, using
     * the provided bundle as the location for the engine's configuration information
     *
     * @param contextPath the context path of the desired WebApp
     * @param create      if <code>true</code>, create the WebApp if it does not exits.
     * @return return the WebApp
     * @throws Exception
     */
	public WebApp getWebApp(Bundle bundle, String contextPath, boolean create) throws Exception;

}
