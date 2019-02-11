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
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

/**
 * Handles paths of the form <pure resource path>;comments e.g. /projects/foo/config.xml;comments
 */
public class CommentCollectionURLHandler extends Handler {

    public Resource get(RequestContext requestContext) throws RegistryException {

        ResourcePath resourcePath = requestContext.getResourcePath();

        Comment[] comments = requestContext.getRegistry().getComments(resourcePath.getPath());
        CollectionImpl resource = new CollectionImpl();
        resource.setDescription("Comments for '" + resourcePath.getPath() + "'");
        resource.setPath(resourcePath.getPath());
        resource.setContent(comments);

        requestContext.setProcessingComplete(true);
        return resource;

    }
}
