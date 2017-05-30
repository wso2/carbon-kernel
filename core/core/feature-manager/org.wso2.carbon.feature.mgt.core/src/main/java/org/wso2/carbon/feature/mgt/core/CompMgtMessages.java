/*
 * Copyright 2009-2010 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.feature.mgt.core;

import java.text.MessageFormat;
import java.util.ResourceBundle;


public class CompMgtMessages {
    private static final String BUNDLE_NAME = "org.wso2.carbon.feature.mgt.core.Resources";
    private static ResourceBundle bundle;

    static {
        // load message values from bundle file
        bundle = ResourceBundle.getBundle(BUNDLE_NAME);
    }

    public static final String INVALID_REPO_LOCATION = "invalid.repo.location";
    public static final String INVALID_REPO_NAME = "invalid.repo.name";
    public static final String INVALID_REPO = "invalid.repo";
    public static final String FAILD_TO_ADD_REPSITORY = "failed.add.repository";
    public static final String FAILD_TO_UPDATE_REPSITORY = "failed.update.repository";
    public static final String FAILD_TO_REMOVE_REPSITORY = "failed.remove.repository";
    public static final String FAILD_TO_ENABLE_REPSITORY = "failed.enable.repository";
    public static final String FAILD_TO_GET_REPSITORY_LIST = "failed.get.repositories";


    public static String bind(String key) {
        return bundle.getString(key);
    }

    public static String bind(String key, Object... arguments) {
        return MessageFormat.format(bundle.getString(key), arguments);
    }
}
