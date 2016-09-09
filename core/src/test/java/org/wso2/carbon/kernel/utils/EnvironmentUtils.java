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

package org.wso2.carbon.kernel.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * EnvironmentUtils.
 *
 * @since 5.2.0
 */
public class EnvironmentUtils {
    private static Logger logger = LoggerFactory.getLogger(EnvironmentUtils.class);

    private static final String PROCESS_ENVIRONMENT = "java.lang.ProcessEnvironment";
    private static final String THE_ENVIRONMENT_FILED = "theEnvironment";
    private static final String THE_CASE_INSENSITIVE_ENVIRONMENT = "theCaseInsensitiveEnvironment";
    private static final String COLLECTIONS_UNMODIFIABLE_MAP = "java.util.Collections$UnmodifiableMap";
    private static final String FIELD_M = "m";

    public static void setEnv(String key, String value) {
        Map<String, String> newenv = new HashMap<>();
        newenv.put(key, value);
        setEnv(newenv);
    }

    public static void setEnv(Map<String, String> newVariables) {
        Map<String, String> newenv = new HashMap<>();
        newenv.putAll(System.getenv());
        newenv.putAll(newVariables);
        try {
            Class<?> processEnvironmentClass = Class.forName(PROCESS_ENVIRONMENT);

            Field theEnvironmentField = processEnvironmentClass.getDeclaredField(THE_ENVIRONMENT_FILED);
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);

            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
                    .getDeclaredField(THE_CASE_INSENSITIVE_ENVIRONMENT);
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            try {
                Class[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for (Class cl : classes) {
                    if (COLLECTIONS_UNMODIFIABLE_MAP.equals(cl.getName())) {
                        Field field = cl.getDeclaredField(FIELD_M);
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(newenv);
                    }
                }
            } catch (IllegalAccessException | NoSuchFieldException ex) {
                logger.error("Unable to set environment variable via unmodifiable map", ex);
            }
        } catch (ClassNotFoundException | IllegalAccessException e) {
            logger.error("Unable to set environment variable", e);
        }
    }
}
