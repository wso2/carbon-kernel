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
package org.wso2.carbon.registry.core.jdbc.handlers.builtin;

import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.TaggedResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.List;
import java.util.Map;

/**
 * This handler is used to capture results after a handler simulation has taken place. The
 * simulation handler is a reporting handler that runs in the reporting handler lifecycle phase.
 * Handler simulation is useful to debug and also to identify the various operations that take place
 * during the execution of the various handlers for a particular operation.
 */
public class SimulationHandler extends Handler {

    private static ThreadLocal<Map<String, List<String[]>>> status =
            new ThreadLocal<Map<String, List<String[]>>>() {
                protected Map<String, List<String[]>> initialValue() {
                    return null;
                }
            };

    /**
     * Method to obtain the simulation status
     *
     * @return the simulation status as a map.
     */
    public static Map<String, List<String[]>> getStatus() {
        return status.get();
    }

    private static void setStatus(Map<String, List<String[]>> input) {
        status.set(input);
    }


    @Override
    public Resource get(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.get(requestContext);
    }

    @Override
    public void put(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.put(requestContext);
    }

    @Override
    public void importResource(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.importResource(requestContext);
    }

    @Override
    public String move(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.move(requestContext);
    }

    @Override
    public String copy(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.copy(requestContext);
    }

    @Override
    public String rename(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.rename(requestContext);
    }

    @Override
    public void createLink(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.createLink(requestContext);
    }

    @Override
    public void removeLink(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.removeLink(requestContext);
    }

    @Override
    public void delete(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.delete(requestContext);
    }

    /*@Override
    public void putChild(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.putChild(requestContext);
    }*/

    @Override
    public void importChild(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.importChild(requestContext);
    }

    @Override
    public void invokeAspect(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.invokeAspect(requestContext);
    }

    @Override
    public void addAssociation(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.addAssociation(requestContext);
    }

    @Override
    public void removeAssociation(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.removeAssociation(requestContext);
    }

    @Override
    public Association[] getAllAssociations(RequestContext requestContext)
            throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.getAllAssociations(requestContext);
    }

    @Override
    public Association[] getAssociations(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.getAssociations(requestContext);
    }

    @Override
    public void applyTag(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.applyTag(requestContext);
    }

    @Override
    public void removeTag(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.removeTag(requestContext);
    }

    @Override
    public void rateResource(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.rateResource(requestContext);
    }

    @Override
    public void restoreVersion(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.restoreVersion(requestContext);
    }

    @Override
    public void createVersion(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.createVersion(requestContext);
    }

    @Override
    public void editComment(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.editComment(requestContext);
    }

    @Override
    public void removeComment(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.removeComment(requestContext);
    }

    @Override
    public String addComment(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.addComment(requestContext);
    }

    @Override
    public Comment[] getComments(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.getComments(requestContext);
    }

    @Override
    public float getAverageRating(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.getAverageRating(requestContext);
    }

    @Override
    public int getRating(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.getRating(requestContext);
    }

    @Override
    public String[] getVersions(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.getVersions(requestContext);
    }

    @Override
    public Tag[] getTags(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.getTags(requestContext);
    }

    @Override
    public TaggedResourcePath[] getResourcePathsWithTag(RequestContext requestContext)
            throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.getResourcePathsWithTag(requestContext);
    }

    @Override
    public Collection executeQuery(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.executeQuery(requestContext);
    }

    @Override
    public Collection searchContent(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.searchContent(requestContext);
    }

    @Override
    public boolean resourceExists(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        return super.resourceExists(requestContext);
    }

    @Override
    public void dump(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.dump(requestContext);
    }

    @Override
    public void restore(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        setStatus(requestContext.getHandlerExecutionStatusMap());
        super.restore(requestContext);
    }
}
