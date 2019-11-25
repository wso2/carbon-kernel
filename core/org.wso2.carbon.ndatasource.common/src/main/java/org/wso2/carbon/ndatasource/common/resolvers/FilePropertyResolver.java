/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.ndatasource.common.resolvers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * File Property resolver can be used to resolve file property variables in the synapse config.
 */
public class FilePropertyResolver implements Resolver {

    private static final Log LOG = LogFactory.getLog(FilePropertyResolver.class);

    /**
     * input is the file property key value which needs to resolve
     */
    private String input;

    /**
     * set file property variable which needs to resolved
     *
     * @param input
     */
    @Override
    public void setVariable(String input) {
        this.input = input;
    }

    /**
     * file property variable is resolved in this function
     *
     * @return resolved value for the file property variable
     */
    @Override
    public String resolve() {
        FilePropertyLoader propertyLoader = FilePropertyLoader.getInstance();
        String PropertyValue = propertyLoader.getValue(input);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolving File Property value " + PropertyValue);
        }

        if (PropertyValue == null) {
            throw new ResolverException("File Property variable could not be found");
        }
        return PropertyValue;
    }
}
