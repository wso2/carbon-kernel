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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Configuration parser class. Entry point to the config parsing logic.
 */
public class ConfigParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigParser.class);

    private static final String UX_FILE_PATH = "deployment.toml";
    private static final String TEMPLATE_FILE_PATH = "user-mgt.xml";
    private static final String INFER_CONFIG_FILE_PATH = "infer.json";

    public static void main(String[] args) {
        parse();
    }

    public static void parse() {
        Map<String, Object> context = TomlParser.parse(UX_FILE_PATH);
        Map<String, Object> enrichedContext = ValueInferrer.infer(context, INFER_CONFIG_FILE_PATH);
        String output = JinjaParser.parse(enrichedContext, TEMPLATE_FILE_PATH);
        LOGGER.info("Output :\n{}", output);
    }
}
