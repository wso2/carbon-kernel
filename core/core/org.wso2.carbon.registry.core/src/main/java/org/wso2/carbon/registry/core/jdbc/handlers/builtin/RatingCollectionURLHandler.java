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

import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.config.StaticConfiguration;
import org.wso2.carbon.registry.core.dao.ResourceDAO;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.dao.RatingsDAO;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles paths of the form <b>pure resource path</b>;ratings e.g. /projects/ids/config.xml;ratings
 */
public class RatingCollectionURLHandler extends Handler {

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

        if (resourcePath.parameterExists("ratings") &&
                resourcePath.getParameterValue("ratings") == null) {
            ResourceImpl resourceImpl = resourceDAO.getResourceMetaData(resourcePath.getPath());
            String[] ratedUserNames = ratingsDAO.getRatedUserNames(resourceImpl);

            CollectionImpl resource = new CollectionImpl();
            resource.setPath(resourcePath.getCompletePath());
            List<String> ratingPaths = new ArrayList<String>();
            for (String ratedUserName : ratedUserNames) {
                String ratingPath = resourcePath.getPath() + RegistryConstants.URL_SEPARATOR +
                        "ratings:" + ratedUserName;
                ratingPaths.add(ratingPath);
            }

            String[] ratingsContent = ratingPaths.toArray(new String[ratingPaths.size()]);
            resource.setContent(ratingsContent);

            requestContext.setProcessingComplete(true);

            return resource;
        }

        return null;
    }
}
