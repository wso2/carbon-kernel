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
import org.wso2.carbon.nextgen.config.model.Context;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Map user provided config values to different keys.
 */
class KeyMapper {

    private KeyMapper() {
    }

    static Context mapWithConfig(Context inputContext, String mappingFile) throws ConfigParserException {
        try (Reader validatorJson = new InputStreamReader(new FileInputStream(mappingFile),
                                                          Charset.defaultCharset())) {
            Gson gson = new Gson();
            Map<String, Object> keyMappings = gson.fromJson(validatorJson, Map.class);
            return map(inputContext, keyMappings);
        } catch (IOException e) {
            throw new ConfigParserException("Error while parsing JSON file " + mappingFile, e);
        }
    }

    static Context map(Context context, Map<String, Object> keyMappings) throws ConfigParserException {
        Map<String, Object> mappedConfigs = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : context.getTemplateData().entrySet()) {
            Object mappedKeys = keyMappings.getOrDefault(entry.getKey(), entry.getKey());
            if (mappedKeys instanceof String) {
                mappedConfigs.put((String) mappedKeys, entry.getValue());
            } else if (mappedKeys instanceof List) {
                ((List<String>) mappedKeys).forEach(mappedKey -> {
                    mappedConfigs.put(mappedKey, entry.getValue());
                });
            }
        }

        processArrayKeys(keyMappings, mappedConfigs);
        context.getTemplateData().clear();
        context.getTemplateData().putAll(mappedConfigs);
        return context;
    }

    /**
     * Process array keys that are denoted in the config file suffixed with a ":".
     *
     * ex:
     * Toml configuration
     * [a]
     * b_c = "xyz"
     * Key Mapper
     * {
     *  "a:b_c":"a:b.c"
     * }
     *
     *
     */
    private static void processArrayKeys(Map<String, Object> keyMappings, Map<String, Object> mappedConfigs)
            throws ConfigParserException {
        for (Map.Entry<String, Object> entry : keyMappings.entrySet()) {
            String key = entry.getKey();
            String[] splitKey = key.split(":");
            if (splitKey.length == 2) {
                Object object = mappedConfigs.get(splitKey[0]);
                processArrayKey(entry, splitKey, object);

            } else if (splitKey.length > 2) {
                throw new ConfigParserException("Unknown key mapping key with multiple array elements: "
                                                + entry.getKey());
            }
        }
    }

    private static void processArrayKey(Map.Entry<String, Object> entry, String[] splitKey, Object object)
            throws ConfigParserException {
        if (object instanceof List) {
            List<Object> list = (List) object;
            for (Object o : list) {
                if (o instanceof Map) {
                    processMap(entry, splitKey[1], (Map) o);
                }
            }
        }
    }

    private static void processMap(Map.Entry<String, Object> entry, String key, Map<String, Object> map)
            throws ConfigParserException {
        Object removedValue = map.remove(key);
        if (removedValue != null) {
            Object value = entry.getValue();
            if (value instanceof String) {
                String[] splitValue = ((String) value).split(":");
                if (splitValue.length != 2) {
                    throw new ConfigParserException("Unknown key mapping value with multiple array " +
                                                    "elements: " + entry.getValue());
                }
                map.put(splitValue[1], removedValue);
            } else if (value instanceof List) {
                /* usecase
                deployment.toml
                [a]
                b = "value"
                keymapping.json
                {"a.b":["x.y.z","c.d.e"]}
                result
                x.y.z = "value"
                c.d.e = "value"
                 */
                for (String mappedValue : (List<String>) value) {
                    String[] splitValue = mappedValue.split(":");
                    if (splitValue.length != 2) {
                        throw new ConfigParserException("Unknown key mapping value with multiple array " +
                                                        "elements: " + entry.getValue());
                    }
                    map.put(splitValue[1], removedValue);
                }
            }
        }
    }
}
