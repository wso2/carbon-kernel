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
package org.wso2.carbon.registry.app.targets;

import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.SimpleTarget;
import org.wso2.carbon.registry.core.Resource;

/**
 * This is a target for a registry resource
 */
public class ResourceTarget extends SimpleTarget {

    private Resource resource;

    /**
     * Creates a resource target.
     *
     * @param type     the target type.
     * @param context  the request context.
     * @param resource the resource.
     */
    public ResourceTarget(TargetType type, RequestContext context, Resource resource) {
        super(type, context);
        this.resource = resource;
    }

    /**
     * Method to obtain a resource.
     *
     * @return the resource.
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        return (obj instanceof ResourceTarget) && super.equals(obj);
    }
}
