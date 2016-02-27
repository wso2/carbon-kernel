/*
 * Copyright (c) OSGi Alliance (2009, 2010). All Rights Reserved.
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

package org.osgi.service.jndi;

import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

/**
 * This interface defines the OSGi service interface for the JNDIContextManager.
 * <p>
 * This service provides the ability to create new JNDI Context instances
 * without relying on the InitialContext constructor.
 */
public interface JNDIContextManager {

    /**
     * Creates a new JNDI initial context with the default JNDI environment
     * properties.
     *
     * @return an instance of javax.jndi.Context
     * @throws NamingException upon any error that occurs during context
     *                         creation
     */
    public Context newInitialContext() throws NamingException;

    /**
     * Creates a new JNDI initial context with the specified JNDI environment
     * properties.
     *
     * @param environment JNDI environment properties specified by caller
     * @return an instance of javax.jndi.Context
     * @throws NamingException upon any error that occurs during context
     *                         creation
     */
    public Context newInitialContext(Map<?, ?> environment) throws NamingException;

    /**
     * Creates a new initial DirContext with the default JNDI environment
     * properties.
     *
     * @return an instance of javax.jndi.directory.DirContext
     * @throws NamingException upon any error that occurs during context
     *                         creation
     */
    public DirContext newInitialDirContext() throws NamingException;

    /**
     * Creates a new initial DirContext with the specified JNDI environment
     * properties.
     *
     * @param environment JNDI environment properties specified by the caller
     * @return an instance of javax.jndi.directory.DirContext
     * @throws NamingException upon any error that occurs during context
     *                         creation
     */
    public DirContext newInitialDirContext(Map<?, ?> environment)
            throws NamingException;
}
