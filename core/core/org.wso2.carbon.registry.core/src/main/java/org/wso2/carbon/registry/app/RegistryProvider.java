/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.app;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.RequestProcessor;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.AbstractWorkspaceProvider;
import org.apache.abdera.protocol.server.impl.SimpleWorkspaceInfo;
import org.apache.abdera.protocol.server.impl.TemplateTargetBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.app.targets.ResponseTarget;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;

/**
 * An implementation of a workspace provider. This is registered as a servlet param when registering
 * the abdera servlet.
 */
@SuppressWarnings("unused")
// This class is used outside the registry kernel, by component code.
public class RegistryProvider extends AbstractWorkspaceProvider {

    private static final Log log = LogFactory.getLog(RegistryProvider.class);

    private RegistryAdapter adapter;

    /**
     * The base context of the registry in the world of APP.
     */
    public static final String BASE_CONTEXT = "/registry";

    /**
     * Method to obtain the collection adapter.
     *
     * @param request the request context.
     *
     * @return the collection adapter.
     */
    public CollectionAdapter getCollectionAdapter(RequestContext request) {
        return adapter;
    }

    /**
     * Constructor accepting an abdera instance.
     *
     * @param abdera the abdera instance.
     *
     * @throws Exception if an exception occurs.
     */
    public RegistryProvider(Abdera abdera) throws Exception {
        RegistryContext context = RegistryContext.getBaseInstance();
        if (context == null) {

            String msg = "Registry is not properly initialized. Registry context is not found.";
            log.error(msg);
            throw new RegistryException(msg);

            //InputStream is = null;
            //try {
            //    is = new FileInputStream("registry.xml");
            //} catch (FileNotFoundException e) {
            //    // no problem
            //}
            //context = new RegistryContext(is);
            //RegistryContext.setSingleton(context);
        }
        TargetType[] types = {RegistryResolver.TAGS_TYPE, RegistryResolver.LOGS_TYPE,
                RegistryResolver.RATINGS_TYPE, RegistryResolver.COMMENTS_TYPE,
                RegistryResolver.RENAME_TYPE, RegistryResolver.COPY_TYPE,
                RegistryResolver.MOVE_TYPE, RegistryResolver.TAG_URL_TYPE,
                RegistryResolver.ASSOCIATIONS_TYPE, RegistryResolver.RESTORE_TYPE,
                RegistryResolver.VERSIONS_TYPE, RegistryResolver.CHECKPOINT_TYPE,
                RegistryResolver.QUERY_TYPE, RegistryResolver.IMPORT_TYPE,
                RegistryResolver.DUMP_TYPE, RegistryResolver.COLLECTION_CUSTOM_TYPE,
                RegistryResolver.DELETE_TYPE, RegistryResolver.ASPECT_TYPE, 
                ResponseTarget.RESPONSE_TYPE};
        RequestProcessor processor = new RegistryRequestProcessor();
        for (TargetType type : types) {
            this.requestProcessors.put(type, processor);
        }

        EmbeddedRegistryService embeddedRegistryService;
        try {
            embeddedRegistryService = Utils.getEmbeddedRegistryService();
        } catch (RegistryException e) {
            embeddedRegistryService = context.getEmbeddedRegistryService();
        }

        RegistryResolver resolver = new RegistryResolver(embeddedRegistryService, BASE_CONTEXT);
        adapter = new RegistryAdapter();

        this.setTargetResolver(resolver);

        this.setTargetBuilder(
                new TemplateTargetBuilder().setTemplate(TargetType.TYPE_COLLECTION,
                        "{target_base}/atom/{collection}").
                        setTemplate(RegistryResolver.TAGS_TYPE,
                        "{target_base}/atom/{collection};tags"));
        SimpleWorkspaceInfo workspace = new SimpleWorkspaceInfo();
        workspace.setTitle("A Simple Workspace");
        workspace.addCollection(adapter);
        addWorkspace(workspace);
    }
}
