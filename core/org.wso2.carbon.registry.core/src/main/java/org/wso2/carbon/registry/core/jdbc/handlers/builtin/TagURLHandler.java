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
import org.wso2.carbon.registry.core.jdbc.dataobjects.TaggingDO;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.Date;

/**
 * Handles paths of the form <b>pure resource path</b>;tags:<b>tag name</b>:<b>username</b> e.g.
 * /projects/ids/config.xml;tags:foo:bar
 */
public class TagURLHandler extends Handler {

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

        String tagDetails = resourcePath.getParameterValue("tags");
        if (tagDetails != null) {

            String[] queries = tagDetails.split(":");

            if (queries.length != 2) {
                return null;
            }

            String path = resourcePath.getPath();
            String tagName = queries[0];
            String userName = queries[1];

            TaggingDO taggingDO = null;
            ResourceIDImpl resourceIDImpl = resourceDAO.getResourceID(path);
            if (resourceIDImpl != null) {
                ResourceImpl resourceImpl = resourceDAO.getResourceMetaData(resourceIDImpl);
                if (resourceImpl != null) {
                    TaggingDO[] taggingDOs = tagsDAO.getTagging(resourceImpl, tagName, userName);
                    taggingDO = taggingDOs[0];
                }
            }


            ResourceImpl resource = new ResourceImpl();
            resource.setMediaType(RegistryConstants.TAG_MEDIA_TYPE);
            resource.setPath(resourcePath.getCompletePath());
            if (taggingDO != null) {
                resource.setContent(taggingDO.getTagName());
                resource.setAuthorUserName(taggingDO.getTaggedUserName());
                final Date taggedTime = taggingDO.getTaggedTime();
                resource.setCreatedTime(taggedTime);
                resource.setLastModified(taggedTime);
                resource.addProperty("resourcePath", taggingDO.getResourcePath());
            }
            requestContext.setProcessingComplete(true);

            return resource;
        }

        return null;
    }
}
