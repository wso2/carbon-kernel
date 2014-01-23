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

import javax.servlet.http.HttpServlet;
import java.util.Hashtable;

/**
 *
 * ServletDescriptor a utility class for describing Servlets to be deployed into a WebApp
 */

public class ServletDescriptor {
	protected Hashtable initParameters;

	protected HttpServlet servlet;

	protected String subContext;

	public ServletDescriptor(String subContext, HttpServlet servlet) {
		this.subContext = subContext;
		this.servlet = servlet;
	}

    public Hashtable getInitParameters() {
        return initParameters;
    }

    public void setInitParameters(Hashtable initParameters) {
        this.initParameters = initParameters;
    }

    public HttpServlet getServlet() {
        return servlet;
    }

    public void setServlet(HttpServlet servlet) {
        this.servlet = servlet;
    }

    public String getSubContext() {
        return subContext;
    }

    public void setSubContext(String subContext) {
        this.subContext = subContext;
    }
}
