/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.ei.config;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Infer configuration values depending on the configurations provided by the user. This step minimises the
 * configurations user has to do.
 */
class ValueInferrer {

    private ValueInferrer() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(ValueInferrer.class);

    static Map<String, Object> infer(Map<String, Object> context, String inferConfigFilePath) {
        Map<String, Object> enrichedContext = Collections.emptyMap();
        try {
            enrichedContext = readConfiguration(inferConfigFilePath);
            return getInferredValues(context, enrichedContext);
        } catch (IOException e) {
            LOGGER.error("Error while inferring values with file {}", inferConfigFilePath, e);
        }
        return enrichedContext;
    }

    private static Map<String, Object> readConfiguration(String inferConfigFilePath) throws IOException {

        Gson gson = new Gson();
        String configJson = Resources.toString(Resources.getResource(inferConfigFilePath), Charsets.UTF_8);
        return gson.fromJson(configJson, Map.class);
    }

    private static Map<String, Object> getInferredValues(Map<String, Object> configurationValues, Map inferringData) {

        Map<String, Object> inferredValues = new HashMap<>();
        if (configurationValues != null) {
            configurationValues.forEach((s, o) -> {
                if (inferringData.containsKey(s)) {
                    Map inferringValues = (Map) inferringData.get(s);
                    if (inferringValues.containsKey(String.valueOf(o))) {
                        Map valuesInferredByKey = (Map) inferringValues.get(String.valueOf(o));
                        inferredValues.putAll(valuesInferredByKey);
                    }
                }
            });
        }
        return inferredValues;
    }
}
