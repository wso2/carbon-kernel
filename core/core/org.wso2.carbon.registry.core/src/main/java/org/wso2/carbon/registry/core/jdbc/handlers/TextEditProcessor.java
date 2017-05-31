/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.core.jdbc.handlers;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Built-in EditProcessor implementation to process text inputs.
 */
public class TextEditProcessor extends EditProcessor {

    /**
     * Extracts text based user input from a HTML control named "generic-text-input" and sets it as
     * the resource content.
     *
     * @param path        Path of the resource to be edited.
     * @param editViewKey UI key of the edit view. This parameter does not have a useful meaning in
     *                    text edit processor as only one view is supported.
     * @param request     HttpServletRequest instance.
     * @param response    HttpServletResponse instance.
     *
     * @throws RegistryException
     */
    public boolean processEditContent(
            String path,
            String editViewKey,
            HttpServletRequest request,
            HttpServletResponse response) throws RegistryException {

        String textContent = request.getParameter(RegistryConstants.TEXT_INPUT_NAME);

        Registry registry = getRegistry(request);
        Resource resource = registry.get(path);
        resource.setContent(textContent);
        registry.put(path, resource);
        resource.discard();

        return false;
    }

    public boolean processNewContent(
            String path,
            String newViewKey,
            HttpServletRequest request,
            HttpServletResponse response) throws RegistryException {

        return false;
    }
}
