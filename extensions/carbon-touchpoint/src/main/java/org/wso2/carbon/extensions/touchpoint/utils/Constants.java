/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.extensions.touchpoint.utils;

/**
 * Carbon P2 Touchpoint Constants class.
 *
 * @since 5.2.0
 */
public final class Constants {

    /**
     * Remove default constructor and make it not available to initialize.
     */
    private Constants() {
    }

    public static final String PLUGIN_ID = "org.wso2.carbon.p2.touchpoint";
    public static final String PROFILE = "profile";
    public static final String PROFILE_END_CHAR = "(";
    public static final String PARM_COPY_TARGET = "target";
    public static final String PARM_COPY_SOURCE = "source";
    public static final String PARM_COPY_OVERWRITE = "overwrite";
    public static final String RUNTIME_KEY = "\\{runtime\\}";

}
