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

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.nextgen.config.model.Context;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
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

    private static final Log log = LogFactory.getLog(ValueInferrer.class);

    static Context infer(Context context, String inferConfigFilePath) throws ConfigParserException {

        Map<String, Object> inferredContext = infer(context.getTemplateData(), inferConfigFilePath);
        context.getTemplateData().clear();
        context.getTemplateData().putAll(inferredContext);
        return context;
    }

    static Map<String, Object> infer(Map<String, Object> context, String inferConfigFilePath)
            throws ConfigParserException {

        Map<String, Object> enrichedContext = readConfiguration(inferConfigFilePath);
        enrichedContext = getInferredValues(context, enrichedContext);
        enrichedContext.putAll(context);
        return enrichedContext;
    }

    private static Map<String, Object> readConfiguration(String inferConfigFilePath) throws ConfigParserException {

        Gson gson = new Gson();

        try (FileInputStream fileInputStream = new FileInputStream(inferConfigFilePath)) {
            Reader input = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            return gson.fromJson(input, Map.class);
        } catch (IOException e) {
            throw new ConfigParserException("Error while reading inferring file", e);
        }
    }

    private static Map<String, Object> getInferredValues(Map<String, Object> configurationValues,
                                                         Map<String, Object> inferringData) {

        Map<String, Object> inferredValues = new LinkedHashMap<>();
        if (configurationValues != null) {
            configurationValues.forEach((key, value) -> {
                String matchedKey = getMatchedKey(key, inferringData.keySet());
                if (StringUtils.isNotEmpty(matchedKey)) {
                    Map<String, Object> inferringValues = (Map) inferringData.get(matchedKey);
                    if (inferringValues.containsKey(String.valueOf(value))) {
                        Map valuesInferredByKey = new LinkedHashMap((Map) inferringValues.get(String.valueOf(value)));
                        replaceReferences(matchedKey, key, valuesInferredByKey);
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
        new LinkedHashMap<>(valuesInferredByKey).forEach((key, value) -> {
            String resolvedKey = key;
            Object resolvedValue = value;
            for (Map.Entry<String, String> entry : resolvedValues.entrySet()) {
                String s1 = entry.getKey();
                String s2 = entry.getValue();
                resolvedKey = resolvedKey.replaceAll(Pattern.quote(s1), s2);
                if (value instanceof String) {
                    resolvedValue = ((String) resolvedValue).replaceAll(Pattern.quote(s1), s2);
                }
            }
            valuesInferredByKey.remove(key);
            valuesInferredByKey.put(resolvedKey, resolvedValue);

        });

    }

    private static void getRecursiveInferredValues(Map context, Map<Object, Object> valuesInferredByKey,
                                                   Map<String, Object> inferringData) {

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

    /**
     * Return Matched Key from infer
     *
     * @param key key to find from infered keys
     * @param inferredKeys list of infered keys
     * @return matched key
     *
     * deployment.toml
     *
     * [datasource.x]
     * type = "mysql"
     * [datasource.y]
     * type = "mysql"
     *
     * infer.json
     *
     *   "datasource.$1.type": {
     *     "mysql": {
     *       "datasource.$1.driver": "abcde"
     *     }
     * output
     * datasource.x.driver = "abcde"
     * datasource.y.driver = "abcde"
     *
     *
     */
    private static String getMatchedKey(String key, Set<String> inferredKeys) {

        if (inferredKeys.contains(key)) {
            return key;
        }
        for (String s : inferredKeys) {
            String matchedRegex = s.replaceAll("\\$[0-9]+", "\\\\w+");
            if (key.matches(matchedRegex)) {
                return s;
            }
        }
        return null;
    }

    private static Map getResolvedValues(String key, String matchedKey) {

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
