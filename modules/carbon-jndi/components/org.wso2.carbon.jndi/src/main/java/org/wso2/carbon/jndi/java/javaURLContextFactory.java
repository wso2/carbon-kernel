/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.wso2.carbon.jndi.java;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ObjectFactory;

import org.wso2.carbon.jndi.internal.impl.NamingContext;

/**
 * Context factory for the "java:" namespace.
 * <p>
 * <b>Important note</b> : This factory MUST be associated with the "java" URL
 * prefix, which can be done by either :
 * <ul>
 * <li>Adding a
 * java.jndi.factory.url.pkgs=org.apache.jndi property
 * to the JNDI properties file</li>
 * <li>Setting an environment variable named Context.URL_PKG_PREFIXES with
 * its value including the org.apache.jndi package name.
 * More detail about this can be found in the JNDI documentation :
 * {@link javax.naming.spi.NamingManager#getURLContext(java.lang.String, java.util.Hashtable)}.</li>
 * </ul>
 */
public class javaURLContextFactory implements ObjectFactory {

    public static final String MAIN = "Java";

    protected static volatile Context javaInitialContext = null;

    /**
     * Create a new Context's instance.
     */
    @SuppressWarnings("unchecked")
    @Override
    public synchronized Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                                 Hashtable<?, ?> environment) throws NamingException {

        if (javaInitialContext == null) {
            javaInitialContext = new NamingContext((Hashtable<String, Object>) environment, "Java");
        }
        return javaInitialContext;
    }
}

