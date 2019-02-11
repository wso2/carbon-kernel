/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.core;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.Map;

/**
 * Aspects are using to associate custom behaviors with resources; Aspects differ form handlers, in
 * that handlers are automatically applied to a resource, whereas, aspects are needed to be invoked
 * manually through user action (e.g. by clicking a button in the user interface).
 */
public abstract class Aspect {

    public static final String AVAILABLE_ASPECTS = "registry.Aspects";

    /**
     * Associate a new Resource with this aspect.  This could set custom properties, create
     * sub-directories, etc.  If this throws an Exception, the association has FAILED.
     *
     * @param resource Resource which we want to change the state
     * @param registry Current registry instance
     *
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          If the condition is not met or some thing is wrong
     */
    public abstract void associate(Resource resource, Registry registry) throws RegistryException;

    /**
     * Do something - action names are aspect-specific, and it's up to the implementation to decide
     * if a given action is allowed, and what to do if so. Action invocations can (and often do)
     * have persistent side-effects in the Registry.
     *
     * @param context the RequestContext containing all the state about this request
     * @param action  action to perform
     *
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          If the condition is not met or some thing is wrong
     */
    public abstract void invoke(RequestContext context, String action) throws RegistryException;


    /**
     * Do something - action names are aspect-specific, and it's up to the implementation to decide
     * if a given action is allowed, and what to do if so. Action invocations can (and often do)
     * have persistent side-effects in the Registry.
     *
     * @param context    the RequestContext containing all the state about this request
     * @param action     action to perform
     * @param parameters parameters to be used for the operation
     *
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          If the condition is not met or some thing is wrong
     */
    public void invoke(RequestContext context, String action,
                                Map<String, String> parameters) throws RegistryException {
        throw new UnsupportedOperationException("This operation is not supported.");
    }

    /**
     * Get a list of available actions for the resource in the RequestContext, taking into account
     * current state, user, etc.
     *
     * @param context the RequestContext containing info about the Resource, Registry, User, etc.
     *
     * @return a String[] of the names of valid actions for this aspect on the specified resource
     */
    public abstract String[] getAvailableActions(RequestContext context);

    /**
     * Remove this Aspect from the referenced resource.
     *
     * @param context the RequestContext containing all the state about this request
     */
    @SuppressWarnings("unused")
    public abstract void dissociate(RequestContext context);
}
