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

package org.wso2.carbon.registry.core;

/**
 * This class is to store resource associations. Association can be made between 1. Registry
 * Resource Paths. 2. Path with web url In addition to the source and target paths associations has
 * a type. Dependency is a special case of association where the type field is always equal to
 * Association.DEPENDS constant.
 */
public class Association extends org.wso2.carbon.registry.api.Association {

    public static final String DEPENDS = org.wso2.carbon.registry.api.Association.DEPENDS;
    @SuppressWarnings("unused")
    public static final String USED_BY = org.wso2.carbon.registry.api.Association.USED_BY;

    public Association() {
    }

    public Association(String sourcePath, String destinationPath, String associationType) {
        super(sourcePath, destinationPath, associationType);
    }
}
