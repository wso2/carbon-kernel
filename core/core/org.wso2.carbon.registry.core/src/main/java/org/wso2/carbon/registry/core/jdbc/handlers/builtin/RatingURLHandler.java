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
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.config.StaticConfiguration;
import org.wso2.carbon.registry.core.dao.ResourceDAO;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.dao.RatingsDAO;
import org.wso2.carbon.registry.core.jdbc.dataobjects.RatingDO;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.Date;

/**
 * Handles paths of the form <b>pure resource path</b>;ratings:<b>username</b> e.g.
 * /projects/ids/config.xml;ratings:foo
 */
public class RatingURLHandler extends Handler {

    public Resource get(RequestContext requestContext) throws RegistryException {
        RegistryContext registryContext = requestContext.getRegistryContext();
        if (registryContext == null) {
            registryContext = RegistryContext.getBaseInstance();
        }
        ResourceDAO resourceDAO = registryContext.getDataAccessManager().getDAOManager().
                getResourceDAO();
        RatingsDAO ratingsDAO = registryContext.getDataAccessManager().getDAOManager().
                getRatingsDAO(StaticConfiguration.isVersioningRatings());

        ResourcePath resourcePath = requestContext.getResourcePath();

        String ratedUserName = resourcePath.getParameterValue("ratings");
        if (ratedUserName != null) {
            ResourceImpl resourceImpl = resourceDAO.getResourceMetaData(resourcePath.getPath());

            RatingDO ratingDO = ratingsDAO.getRatingDO(resourceImpl, ratedUserName);
            int rating = ratingDO.getRating();
            Date ratedTime = ratingDO.getRatedTime();

            ResourceImpl resource = new ResourceImpl();
            resource.setMediaType(RegistryConstants.RATING_MEDIA_TYPE);
            resource.setContent(rating);
            resource.setAuthorUserName(ratedUserName);
            resource.setPath(resourcePath.getCompletePath());
            if (ratedTime != null) {
                resource.setCreatedTime(ratedTime);
                resource.setLastModified(ratedTime);
            }
            resource.addProperty("resourcePath", resourcePath.getPath());

            return resource;
        }

        return null;
    }
}
