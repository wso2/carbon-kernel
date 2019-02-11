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
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.SimpleTarget;

/**
 * A Target which contains a ready-made ResponseContext.  The RegistryAdapter just notices this and
 * returns the contained ResponseContext immediately.
 */
public class ResponseTarget extends SimpleTarget {

    /**
     * The type of response target.
     */
    public static final TargetType RESPONSE_TYPE = TargetType.get("response", true);

    private ResponseContext response;

    /**
     * Creates a response target.
     *
     * @param context  the request context.
     * @param response the response context.
     */
    public ResponseTarget(RequestContext context, ResponseContext response) {
        super(RESPONSE_TYPE, context);
        this.response = response;
    }

    /**
     * Method to obtain the response context.
     *
     * @return the response context.
     */
    public ResponseContext getResponse() {
        return response;
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
        return (obj instanceof ResponseTarget) && super.equals(obj);
    }
}
