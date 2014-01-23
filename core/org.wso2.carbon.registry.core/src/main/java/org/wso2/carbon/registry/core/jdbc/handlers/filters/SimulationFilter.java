/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.jdbc.handlers.filters;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

/**
 * This is a built-in Filter that is used for simulation operations. The handler simulator uses an
 * instance of this filter. When in simulation mode, this filter will run the associated simulation
 * handler, and when not, the handler methods won't get called. The simulation mode can be set by
 * accessing the instance of this Filter.
 */
public class SimulationFilter extends Filter {

    /**
     * This field stores whether the filter is in simulation mode or not.
     */
    private static ThreadLocal<Boolean> simulation =
            new ThreadLocal<Boolean>() {
                protected Boolean initialValue() {
                    return false;
                }
            };

    /**
     * Method to obtain whether in simulation mode or not.
     *
     * @return whether in simulation mode or not.
     */
    public static Boolean isSimulation() {
        return simulation.get();
    }

    /**
     * Method to set whether in simulation mode or not.
     *
     * @param input whether in simulation mode or not.
     */
    public static void setSimulation(Boolean input) {
        simulation.set(input);
    }

    @Override
    public boolean handleGet(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handlePut(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleImportResource(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleDelete(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handlePutChild(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleImportChild(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleInvokeAspect(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleMove(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleCopy(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleRename(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleCreateLink(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleRemoveLink(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleAddAssociation(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleRemoveAssociation(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleGetAllAssociations(RequestContext requestContext)
            throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleGetAssociations(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleApplyTag(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleRemoveTag(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleRateResource(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleRestoreVersion(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleCreateVersion(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleEditComment(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleRemoveComment(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleAddComment(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleGetComments(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleGetResourcePathsWithTag(RequestContext requestContext)
            throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleGetTags(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleGetAverageRating(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleGetRating(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleGetVersions(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleExecuteQuery(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleSearchContent(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleResourceExists(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleDump(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }

    @Override
    public boolean handleRestore(RequestContext requestContext) throws RegistryException {
        return isSimulation();
    }
}
