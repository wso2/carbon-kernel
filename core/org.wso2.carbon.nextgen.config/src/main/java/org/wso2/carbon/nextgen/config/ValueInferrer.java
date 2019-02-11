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

package org.wso2.carbon.nextgen.config;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Infer configuration values depending on the configurations provided by the user. This step minimises the
 * configurations user has to do.
 */
class ValueInferrer {

    private ValueInferrer() {

    }

    private static final Log LOGGER = LogFactory.getLog(ValueInferrer.class);

    static Map<String, Object> infer(Map<String, Object> context, String inferConfigFilePath) {

        Map<String, Object> enrichedContext = Collections.emptyMap();
        try {
            enrichedContext = readConfiguration(inferConfigFilePath);
            enrichedContext = getInferredValues(context, enrichedContext);
            enrichedContext.putAll(context);
            return enrichedContext;
        } catch (IOException e) {
            LOGGER.error("Error while inferring values with file " + inferConfigFilePath, e);
        }
        return enrichedContext;
    }

    private static Map<String, Object> readConfiguration(String inferConfigFilePath) throws IOException {

        Gson gson = new Gson();
        Reader input = new InputStreamReader(new FileInputStream(inferConfigFilePath), Charsets.UTF_8);
        return gson.fromJson(input, Map.class);
    }

    private static Map<String, Object> getInferredValues(Map<String, Object> configurationValues, Map inferringData) {

        Map<String, Object> inferredValues = new LinkedHashMap<>();
        if (configurationValues != null) {
            configurationValues.forEach((s, o) -> {
                String matchedKey = getMatchedKey(s, inferringData.keySet());
                if (StringUtils.isNotEmpty(matchedKey)) {
                    Map inferringValues = (Map) inferringData.get(matchedKey);
                    if (inferringValues.containsKey(String.valueOf(o))) {
                        Map valuesInferredByKey = new LinkedHashMap((Map) inferringValues.get(String.valueOf(o)));
                        replaceReferences(matchedKey, s, valuesInferredByKey);
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

    private static void replaceReferences(String matchedKey, String s, Map<String, Object> valuesInferredByKey) {

        Map<String, String> resolvedValues = getResolvedValues(s, matchedKey);
        new LinkedHashMap<String, Object>(valuesInferredByKey).forEach((key, value) -> {
            String resolvedKey = key;
            for (Map.Entry<String, String> entry : resolvedValues.entrySet()) {
                String s1 = entry.getKey();
                String s2 = entry.getValue();
                resolvedKey = resolvedKey.replaceAll(Pattern.quote(s1), s2);
            }
            valuesInferredByKey.remove(key);
            valuesInferredByKey.put(resolvedKey, value);

        });

    }

    private static void getRecursiveInferredValues(Map context, Map<Object, Object> valuesInferredByKey,
                                                   Map inferringData) {

        valuesInferredByKey.forEach((key, value) -> {
            String matchedKey = getMatchedKey((String) key, inferringData.keySet());

            if (StringUtils.isNotEmpty(matchedKey)) {
                Map dataMap = (Map) inferringData.get(matchedKey);
                if (dataMap.containsKey(value)) {
                    Map inferredValues = (Map) dataMap.get(value);
                    context.putAll(inferredValues);
                    getRecursiveInferredValues(context, inferredValues, inferringData);
                }
            }
        });
    }

    private static String getMatchedKey(String key, Set<String> inferredKeys) {

        if (inferredKeys.contains(key)) {
            return key;
        }
        for (String s : inferredKeys) {
            String matchedRegex = s.replaceAll("\\$[0-9]+", "\\\\\\w+");
            if (key.matches(matchedRegex)) {
                return s;
            }
        }
        return null;
    }

    private static Map<String, String> getResolvedValues(String key, String matchedKey) {

        Map map = new LinkedHashMap();
        int l = 0;
        for (String s : matchedKey.split("\\.")) {
            if (s.matches("\\$[0-9]+")) {
                map.put(s, key.split("\\.")[l]);
            }
            l++;
        }
        return map;
    }
}
