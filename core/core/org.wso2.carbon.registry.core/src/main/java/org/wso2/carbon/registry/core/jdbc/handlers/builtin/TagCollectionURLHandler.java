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

package org.wso2.carbon.registry.core.jdbc.handlers.builtin;

import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceIDImpl;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.config.StaticConfiguration;
import org.wso2.carbon.registry.core.dao.ResourceDAO;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.dao.TagsDAO;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

/**
 * Handles paths of the form <b>pure resource path</b>;tags e.g. /projects/wsf-php/config.xml;tags
 */
@Deprecated
public class TagCollectionURLHandler extends Handler {

    public Resource get(RequestContext requestContext) throws RegistryException {
        RegistryContext registryContext = requestContext.getRegistryContext();
        if (registryContext == null) {
            registryContext = RegistryContext.getBaseInstance();
        }
        ResourceDAO resourceDAO = registryContext.getDataAccessManager().getDAOManager().
                getResourceDAO();
        TagsDAO tagsDAO = registryContext.getDataAccessManager().getDAOManager().
                getTagsDAO(StaticConfiguration.isVersioningTags());

        ResourcePath resourcePath = requestContext.getResourcePath();

        if (resourcePath.parameterExists("tags") &&
                resourcePath.getParameterValue("tags") == null) {

            String path = resourcePath.getPath();
            String tagStr = "";

            ResourceIDImpl resourceIDImpl = resourceDAO.getResourceID(path);
            if (resourceIDImpl != null) {
                ResourceImpl resourceImpl = resourceDAO.getResourceMetaData(resourceIDImpl);
                if (resourceImpl != null) {
                    String[] tags = tagsDAO.getTags(resourceImpl);
                    StringBuffer sb = new StringBuffer(tagStr);
                    for (String tag : tags) {
                        sb.append(tag);
                    }
                    tagStr = sb.toString();
                }
            }

            ResourceImpl resource = new ResourceImpl();
            resource.setMediaType(RegistryConstants.TAG_MEDIA_TYPE);
            resource.setContent(tagStr);
            resource.setPath(resourcePath.getCompletePath());
            resource.addProperty("resourcePath", path);

            requestContext.setProcessingComplete(true);
            return resource;
        }

        return null;
    }
}
