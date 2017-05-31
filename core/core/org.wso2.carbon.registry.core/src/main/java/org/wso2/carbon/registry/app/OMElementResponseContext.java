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

package org.wso2.carbon.registry.app;

import org.apache.abdera.protocol.server.context.SimpleResponseContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.IOException;
import java.io.Writer;
import java.net.HttpURLConnection;

/**
 * This is an extension of the {@link SimpleResponseContext} in Abdera. This is used in Dump
 * requests.
 */
public class OMElementResponseContext extends SimpleResponseContext {

    private Registry registry;
    private String path;

    /**
     * Creates response context.
     *
     * @param registry the registry.
     * @param path     the resource path.
     */
    public OMElementResponseContext(Registry registry, String path) {
        this.registry = registry;
        this.path = path;
        setStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * Method to dump the registry content in to the writer.
     *
     * @param writer the Writer connected to the HTTP response
     *
     * @throws IOException if an error occurred.
     */
    protected void writeEntity(Writer writer) throws IOException {
        try {
            registry.dump(path, writer);
        } catch (RegistryException e) {
            throw new IOException("Failed in dumping the path " + path + ".", e);
        }
    }

    /**
     * Whether the response contains an entity.
     *
     * @return true if the response contains an entity
     */
    public boolean hasEntity() {
        return true;
    }
}
