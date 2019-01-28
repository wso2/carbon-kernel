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
import net.consensys.cava.toml.Toml;
import net.consensys.cava.toml.TomlParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Parses the toml file and builds object model.
 */
class TomlParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TomlParser.class);

    private TomlParser() {}

    static Map<String, Object> parse(String filePath) {
        return parseToml(filePath);
    }

    private static Map<String, Object> parseToml(String filePath) {
        Map<String, Object> context = new HashMap<>();
        try {
            String source = Resources.toString(Resources.getResource(filePath), Charsets.UTF_8);
            TomlParseResult result;

            result = Toml.parse(source);
            result.errors().forEach(error -> LOGGER.error(error.toString()));

            Set<String> dottedKeySet = result.dottedKeySet();
            for (String dottedKey: dottedKeySet) {
                context.put(dottedKey, result.get(dottedKey));
            }
        } catch (IOException e) {
            LOGGER.error("Error parsing file {}", filePath, e);
        }

        return context;
    }
}
