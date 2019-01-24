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
import com.hubspot.jinjava.Jinjava;
import net.consensys.cava.toml.Toml;
import net.consensys.cava.toml.TomlParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class JinjaParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JinjaParser.class);

    private static final String UX_FILE_PATH = "deployment.toml";
    private static final String TEMPLATE_FILE_PATH = "user-mgt.xml";

    public static void main(String[] args) {
        try {
            execute();
        } catch (IOException e) {
            LOGGER.error("Error creating rendered template file for {}", TEMPLATE_FILE_PATH, e);
        }
    }

    private static void execute() throws IOException {
        Jinjava jinjava = new Jinjava();
        Map<String, Object> context = parseToml();

        String template = Resources.toString(Resources.getResource(TEMPLATE_FILE_PATH), Charsets.UTF_8);
        String renderedTemplate = jinjava.render(template, context);
        LOGGER.info("Output :\n{}", renderedTemplate);
    }

    private static Map<String, Object> parseToml() {
        Map<String, Object> context = new HashMap<>();
        try {
            String source = Resources.toString(Resources.getResource(UX_FILE_PATH), Charsets.UTF_8);
            TomlParseResult result;

            result = Toml.parse(source);
            result.errors().forEach(error -> LOGGER.error(error.toString()));

            Set<String> dottedKeySet = result.dottedKeySet();
            for (String dottedKey : dottedKeySet) {
                String[] dottedKeyArray = dottedKey.split("\\.");
                handleDottedKey(context, result, dottedKey, dottedKeyArray);
            }

        } catch (IOException e) {
            LOGGER.error("Error parsing file {}", UX_FILE_PATH, e);
        }

        return context;
    }

    private static void handleDottedKey(Map<String, Object> context, TomlParseResult result, String dottedKey,
                                        String[] dottedKeyArray) {
        Map<String, Object> parentMap = context;
        for (int i = 0; i < dottedKeyArray.length - 1; i++) {
            Map map;
            Object value = parentMap.get(dottedKeyArray[i]);
            if (value instanceof Map) {
                map = (Map) value;
            } else {
                map = new HashMap<>();
                parentMap.put(dottedKeyArray[i], map);

            }
            parentMap = map;
        }
        String finalSubKey = dottedKeyArray[dottedKeyArray.length - 1];
        parentMap.put(finalSubKey, result.get(dottedKey));
    }


}
