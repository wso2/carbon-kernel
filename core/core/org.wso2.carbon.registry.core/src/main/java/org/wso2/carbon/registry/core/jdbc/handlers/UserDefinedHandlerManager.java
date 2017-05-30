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
package org.wso2.carbon.registry.core.jdbc.handlers;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.TaggedResourcePath;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.Filter;
import org.wso2.carbon.registry.core.session.CurrentSession;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is designed to manage user-defined handlers.
 */
public class UserDefinedHandlerManager extends HandlerManager {

    private Map<Integer, HandlerManager> userHandlerManagers =
            new HashMap<Integer, HandlerManager>();

    public HandlerManager getUserHandlerManager() {
        HandlerManager hm = userHandlerManagers.get(CurrentSession.getCallerTenantId());
        if (hm == null) {
            hm = new HandlerManager();
            userHandlerManagers.put(CurrentSession.getCallerTenantId(), hm);
        }
        return hm;
    }

    @Override
    public void addHandler(String[] methods, Filter filter, Handler handler) {
        getUserHandlerManager().addHandler(methods, filter, handler);
    }

    @Override
    public void addHandler(String[] methods, Filter filter, Handler handler,
                           String lifecyclePhase) {
        getUserHandlerManager().addHandler(methods, filter, handler, lifecyclePhase);
    }

    @Override
    public void addHandlerWithPriority(String[] methods, Filter filter, Handler handler) {
        getUserHandlerManager().addHandlerWithPriority(methods, filter, handler);
    }

    @Override
    public void addHandlerWithPriority(String[] methods, Filter filter, Handler handler,
                                       String lifecyclePhase) {
        getUserHandlerManager().addHandlerWithPriority(methods, filter, handler, lifecyclePhase);
    }

    @Override
    public void removeHandler(Handler handler) {
        getUserHandlerManager().removeHandler(handler);
    }

    @Override
    public void removeHandler(Handler handler, String lifecyclePhase) {
        getUserHandlerManager().removeHandler(handler, lifecyclePhase);
    }

    @Override
    public void removeHandler(String[] methods, Filter filter, Handler handler) {
        getUserHandlerManager().removeHandler(methods, filter, handler);
    }

    @Override
    public void removeHandler(String[] methods, Filter filter, Handler handler,
                              String lifecyclePhase) {
        getUserHandlerManager().removeHandler(methods, filter, handler, lifecyclePhase);
    }

    @Override
    public void editComment(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().editComment(requestContext);
    }

    @Override
    public void removeComment(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().removeComment(requestContext);
    }

    @Override
    public void createVersion(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().createVersion(requestContext);
    }

    @Override
    public void restoreVersion(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().restoreVersion(requestContext);
    }

    @Override
    public void rateResource(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().rateResource(requestContext);
    }

    @Override
    public void removeTag(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().removeTag(requestContext);
    }

    @Override
    public void applyTag(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().applyTag(requestContext);
    }

    @Override
    public void removeAssociation(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().removeAssociation(requestContext);
    }

    @Override
    public void addAssociation(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().addAssociation(requestContext);
    }

    @Override
    public Association[] getAllAssociations(RequestContext requestContext)
            throws RegistryException {
        return getUserHandlerManager().getAllAssociations(requestContext);
    }

    @Override
    public Association[] getAssociations(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().getAssociations(requestContext);
    }

    @Override
    public TaggedResourcePath[] getResourcePathsWithTag(RequestContext requestContext)
            throws RegistryException {
        return getUserHandlerManager().getResourcePathsWithTag(requestContext);
    }

    @Override
    public Tag[] getTags(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().getTags(requestContext);
    }

    @Override
    public Comment[] getComments(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().getComments(requestContext);
    }

    @Override
    public float getAverageRating(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().getAverageRating(requestContext);
    }

    @Override
    public int getRating(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().getRating(requestContext);
    }

    @Override
    public String[] getVersions(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().getVersions(requestContext);
    }

    @Override
    public Collection executeQuery(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().executeQuery(requestContext);
    }

    @Override
    public Collection searchContent(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().searchContent(requestContext);
    }

    @Override
    public String addComment(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().addComment(requestContext);
    }

    @Override
    public Resource get(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().get(requestContext);
    }

    @Override
    public String put(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().put(requestContext);
    }

    @Override
    public String importResource(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().importResource(requestContext);
    }

    @Override
    public void delete(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().delete(requestContext);
    }

    @Override
    public void putChild(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().putChild(requestContext);
    }

    @Override
    public void importChild(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().importChild(requestContext);
    }

    @Override
    public void invokeAspect(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().invokeAspect(requestContext);
    }

    @Override
    public String copy(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().copy(requestContext);
    }

    @Override
    public String move(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().move(requestContext);
    }

    @Override
    public String rename(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().rename(requestContext);
    }

    @Override
    public void createLink(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().createLink(requestContext);
    }

    @Override
    public void removeLink(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().removeLink(requestContext);
    }

    @Override
    public boolean resourceExists(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().resourceExists(requestContext);
    }

    @Override
    public RegistryContext getRegistryContext(RequestContext requestContext) {
        return getUserHandlerManager().getRegistryContext(requestContext);
    }

    @Override
    public OMElement dump(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().dump(requestContext);
    }

    @Override
    public void restore(RequestContext requestContext) throws RegistryException {
        getUserHandlerManager().restore(requestContext);
    }

    @Override
    public void setEvaluateAllHandlers(boolean evaluateAllHandlers) {
        getUserHandlerManager().setEvaluateAllHandlers(evaluateAllHandlers);
    }
    
    @Override
    public OMElement dumpLite(RequestContext requestContext) throws RegistryException {
        return getUserHandlerManager().dumpLite(requestContext);
    }
}
