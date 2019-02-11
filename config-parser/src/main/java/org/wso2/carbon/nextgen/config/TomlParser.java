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

import net.consensys.cava.toml.Toml;
import net.consensys.cava.toml.TomlArray;
import net.consensys.cava.toml.TomlParseResult;
import net.consensys.cava.toml.TomlTable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parses the TOML file and builds object model.
 */
class TomlParser {

    private static final Log LOGGER = LogFactory.getLog(TomlParser.class);

    private TomlParser() {}

    static Map<String, Object> parse(String filePath) {
        return parseToml(filePath);
    }

    private static Map<String, Object> parseToml(String filePath) {
        Map<String, Object> context = new LinkedHashMap<>();
        try {
            TomlParseResult result;

            result = Toml.parse(Paths.get(filePath));
            result.errors().forEach(error -> LOGGER.error(error.toString()));

            Set<String> dottedKeySet = result.dottedKeySet();
            for (String dottedKey: dottedKeySet) {
                context.put(dottedKey, getValue(result.get(dottedKey)));
            }
        } catch (IOException e) {
            LOGGER.error("Error parsing file " + filePath, e);
        }

        return context;
    }

    /**
     * Return the relevant object representation.
     * @param value value returned by the TOML parser.
     * @return This can be  a simple value, an array ({@link ArrayList}, or a key value map {@link Map}
     */
    private static Object getValue(Object value) {
        Object returnValue;
        if (value instanceof TomlArray) {
            returnValue = processTomlArray((TomlArray) value);
        } else if (value instanceof TomlTable) {
            returnValue = processTomlMap((TomlTable) value);
        } else {
            returnValue = value;
        }
        return returnValue;
    }

    /**
     * Process the TomlTable and output the relevant Map representation.
     * @param tomlTable {@link TomlTable}
     * @return Map representation of the {@link TomlTable}
     */
    private static Object processTomlMap(TomlTable tomlTable) {
        Map<String, Object> finalMap = new LinkedHashMap<>();
        Set<String> dottedKeySet = tomlTable.dottedKeySet();
        for (String key: dottedKeySet) {
            Object value = tomlTable.get(key);
            if (value instanceof TomlArray) {
                finalMap.put(key, processTomlArray((TomlArray) value));
            }
            finalMap.put(key, tomlTable.get(key));
        }

        return finalMap;
    }

    /**
     * Process the {@link TomlArray} and output the relevant {@link List} representation.
     * @param value {@link TomlArray}
     * @return List representation of the {@link TomlArray}
     */
    private static Object processTomlArray(TomlArray value) {
        ArrayList<Object> finalList = new ArrayList<>();
        List<Object> tomlList = value.toList();
        for (Object obj : tomlList) {
            if (obj instanceof TomlArray) {
                finalList.add(processTomlArray((TomlArray) obj));
            } else if (obj instanceof TomlTable) {
                finalList.add(processTomlMap((TomlTable) obj));
            } else {
                finalList.add(obj);
            }
        }
        return finalList;
    }
}
