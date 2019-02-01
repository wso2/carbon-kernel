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
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Infer configuration values depending on the configurations provided by the user. This step minimises the
 * configurations user has to do.
 */
class ValueInferrer {

    private ValueInferrer() {

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ValueInferrer.class);

    static Map<String, Object> infer(Map<String, Object> context, String inferConfigFilePath) {

        Map<String, Object> enrichedContext = Collections.emptyMap();
        try {
            enrichedContext = readConfiguration(inferConfigFilePath);
            enrichedContext = getInferredValues(context, enrichedContext);
            enrichedContext.putAll(context);
            return enrichedContext;
        } catch (IOException e) {
            LOGGER.error("Error while inferring values with file {}", inferConfigFilePath, e);
        }
        return enrichedContext;
    }

    private static Map<String, Object> readConfiguration(String inferConfigFilePath) throws IOException {

        Gson gson = new Gson();
        Reader input = new InputStreamReader(new FileInputStream(inferConfigFilePath), Charsets.UTF_8);
        return gson.fromJson(input, Map.class);
    }

    private static Map<String, Object> getInferredValues(Map<String, Object> configurationValues, Map inferringData) {

        Map<String, Object> inferredValues = new HashMap<>();
        if (configurationValues != null) {
            configurationValues.forEach((s, o) -> {
                if (inferringData.containsKey(s)) {
                    Map inferringValues = (Map) inferringData.get(s);
                    if (inferringValues.containsKey(String.valueOf(o))) {
                        Map valuesInferredByKey = (Map) inferringValues.get(String.valueOf(o));
                        getRecursiveInferredValues(inferredValues, valuesInferredByKey, inferringData);
                        inferredValues.putAll(valuesInferredByKey);
                    }
                }
            });
        }

        for (Map.Entry<String, Object> entry : inferredValues.entrySet()) {
            if (entry.getValue() instanceof Double) {
                Double value = (Double) entry.getValue();
                if (value.compareTo(Math.rint(value)) == 0) {
                    entry.setValue(value.intValue());
                }
            }
        }

        return inferredValues;
    }

    private static void getRecursiveInferredValues(Map context, Map<Object, Object> valuesInferredByKey,
                                                   Map inferringData) {
        valuesInferredByKey.forEach((key, value) -> {
            if (inferringData.containsKey(key)) {
                Map dataMap = (Map) inferringData.get(key);
                if (dataMap.containsKey(value)) {
                    Map inferredValues = (Map) dataMap.get(value);
                    context.putAll(inferredValues);
                    getRecursiveInferredValues(context, inferredValues, inferringData);
                }
            }
        });
    }
}
