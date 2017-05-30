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
package org.wso2.carbon.registry.app;

import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.RequestProcessor;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.WorkspaceManager;

/**
 * An implementation of a request processor for the registry.
 */
public class RegistryRequestProcessor implements RequestProcessor {

    /**
     * Method containing the logic to process a request.
     *
     * @param context           the request context.
     * @param workspaceManager  the workspace manager instance.
     * @param collectionAdapter the collection adapter to use.
     *
     * @return the response context if the request was handled, or null for an extension request.
     */
    public ResponseContext process(RequestContext context, WorkspaceManager workspaceManager,
                                   CollectionAdapter collectionAdapter) {
        // We return null here as we want to process an extension request.
        return null;
    }
}
