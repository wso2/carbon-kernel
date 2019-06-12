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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.nextgen.config.handlers.Builders;
import org.wso2.carbon.nextgen.config.model.Context;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default value parsing .
 */
class DefaultParser {

    private static final Log log = LogFactory.getLog(DefaultParser.class);

    private DefaultParser() {

    }

    static Context addDefaultValues(Context enrichedContext, String defaultValueFilePath)
            throws ConfigParserException {

        try {
            Map<String, Object> defaultValueMap = readConfiguration(defaultValueFilePath);
            for (Map.Entry<String, Object> entry : defaultValueMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (!enrichedContext.getTemplateData().containsKey(key)) {
                    enrichedContext.getTemplateData().put(key, value);
                } else {
                    Object retrievedEnrichedContext = enrichedContext.getTemplateData().get(key);
                    Builders builders = readHandles(key);
                    enrichedContext.getTemplateData().put(key, builders.handle(retrievedEnrichedContext, value));
                }
            }
        } catch (IOException e) {
            throw new ConfigParserException("Error while default values with file" + defaultValueFilePath, e);
        } catch (IllegalAccessException e) {
            throw new ConfigParserException("Error while accessing Handler", e);
        } catch (InstantiationException | ClassNotFoundException e) {
            throw new ConfigParserException("Error while initializing Handler", e);
        }
        return enrichedContext;
    }

    private static LinkedHashMap readConfiguration(String defaultValueFilePath) throws IOException {

        Gson gson = new Gson();
        try (FileInputStream fileInputStream = new FileInputStream(defaultValueFilePath)) {
            Reader input = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            return gson.fromJson(input, LinkedHashMap.class);

        }
    }

    private static Builders readHandles(String key) throws ClassNotFoundException,
            IllegalAccessException, InstantiationException {

        Gson gson = new Gson();
        Reader input = new InputStreamReader(DefaultParser.class.getClassLoader()
                        .getResourceAsStream("handle.json"), StandardCharsets.UTF_8);
        Map<String, String> handlers = gson.fromJson(input, Map.class);
        String className = handlers.get(key);
        if (className != null) {
            return (Builders) Class.forName(className).newInstance();
        }
        return new Builders();
    }
}
