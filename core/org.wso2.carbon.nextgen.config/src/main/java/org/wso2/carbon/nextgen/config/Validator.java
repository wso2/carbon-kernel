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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validate the configuration according to the given set of rules. Rules will be specified in the configured file in
 * the following format.
 * <p>
 * {
 * "key_to_validate": [
 * {
 * "if": {
 * "dependent_key1": "value1"
 * "dependent_key2": "value2"
 * },
 * "regex": ".+",
 * "error_message": "key_to_validate should not be empty"
 * },
 * {
 * "if": {
 * "dependent_key1": "value3"
 * *      "dependent_key2": "value4"
 * },
 * "regex": ".*",
 * }
 * ]
 * }
 */
public class Validator {

    private static final Log log = LogFactory.getLog(Validator.class);

    private static final String IF = "if";
    private static final String REGEX = "regex";
    private static final String EMPTY_STRING = "";
    private static final String EMPTY_STRING_MESSAGE = "an empty string";

    private Validator() { }

    private static Map<String, Object> readConfiguration(String validationConfigFilePath) throws IOException {

        Gson gson = new Gson();
        try (FileInputStream fileInputStream = new FileInputStream(validationConfigFilePath)) {
            Reader validatorJson = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            return gson.fromJson(validatorJson, Map.class);
        }
    }

    public static void validate(Context context, String fileName)
            throws ConfigParserException {

        try {

            Map<String, Object> ruleConfiguration = readConfiguration(fileName);
            for (Map.Entry<String, Object> entry : ruleConfiguration.entrySet()) {
                List<Map<String, Object>> value = (List<Map<String, Object>>) entry.getValue();
                for (Object rule : value) {
                    doValidation(entry.getKey(), (Map<String, Object>) rule, context.getTemplateData());
                }
            }
        } catch (IOException e) {
            throw new ConfigParserException("Error while reading validator file", e);
        }
    }

    private static void doValidation(String keyToValidate, Map<String, Object> validationRule,
                                     Map<String, Object> configurationValues) throws ConfigParserException {

        if (validationRule.get(IF) instanceof Map) {
            Map<String, Object> valuesToMatch = (Map<String, Object>) validationRule.get(IF);
            for (Map.Entry<String, Object> entry : valuesToMatch.entrySet()) {
                if (configurationValues.containsKey(entry.getKey())) {
                    Object configValue = configurationValues.get(entry.getKey());
                    if (!configValue.equals(entry.getValue())) {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Condition doesn't match for %s, not enforcing rule. Expected %s " +
                                                    "for rule, was %s", entry.getKey(), configValue, entry.getValue()));
                        }
                        return;
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Config %s is not present, hence not enforcing rule. ",
                                                entry.getKey()));
                    }
                    return;
                }
            }
        }
        List<String> configValues = getConfigurationValues(configurationValues, keyToValidate);

        if (validationRule.get(REGEX) instanceof String) {

            for (String configValue : configValues) {

                String regex = (String) validationRule.get(REGEX);
                if (!configValue.matches(regex)) {
                    String errorMessage = (String) validationRule.get("error_message");
                    if (errorMessage == null) {
                        errorMessage = String.format("Validation failed for %s. Expected value to match \"%s\", but " +
                                "was " +
                                "%s", keyToValidate, regex, EMPTY_STRING.equals(configValue) ? EMPTY_STRING_MESSAGE :
                                configValue);
                    }
                    throw new ConfigParserException(errorMessage);
                }

            }

        }
    }

    private static List<String> getConfigurationValues(Map<String, Object> configurationValues, String keyToValidate) {

        List<String> list = new ArrayList<>();

        String[] splittedArray = StringUtils.split(keyToValidate, ":");
        if (splittedArray != null && splittedArray.length == 2) {
            Object configurations = configurationValues.get(splittedArray[0]);
            if (configurations instanceof List) {
                ((List) configurations).forEach(configuration -> {
                    if (configuration instanceof Map) {
                        if (((Map) configuration).get(splittedArray[1]) != null) {
                            list.add((String) ((Map) configuration).get(splittedArray[1]));
                        } else {
                            list.add(EMPTY_STRING);
                        }
                    }
                });
            }
        } else {
            if (configurationValues.containsKey(keyToValidate)) {
                list.add((String) configurationValues.get(keyToValidate));
            } else {
                list.add(EMPTY_STRING);
            }
        }
        return list;
    }
}

