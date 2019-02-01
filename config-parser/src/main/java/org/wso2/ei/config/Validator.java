package org.wso2.ei.config;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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

    private static final Logger LOG = LoggerFactory.getLogger(Validator.class);

    private static final String IF = "if";
    private static final String REGEX = "regex";
    private static final String EMPTY_STRING = "";
    private static final String DEFAULT_VALUE = "default_value";

    private static Map<String, Object> readConfiguration(String validationConfigFilePath) throws IOException {

        Gson gson = new Gson();
        Reader validatorJson = new InputStreamReader(new FileInputStream(validationConfigFilePath), Charsets.UTF_8);
        return gson.fromJson(validatorJson, Map.class);
    }

    public static void validate(Map<String, Object> configurationValues, String fileName)
            throws ValidationException, IOException {

        Map<String, Object> ruleConfiguration = readConfiguration(fileName);
        for (Map.Entry<String, Object> entry : ruleConfiguration.entrySet()) {
            List<Map<String, Object>> value = (List<Map<String, Object>>) entry.getValue();
            for (Object rule : value) {
                doValidation(entry.getKey(), (Map<String, Object>) rule, configurationValues);
            }
        }
    }

    private static void doValidation(String keyToValidate, Map<String, Object> validationRule,
                              Map<String, Object> configurationValues) throws ValidationException {

        if (validationRule.get(IF) instanceof Map) {
            Map<String, Object> valuesToMatch = (Map<String, Object>) validationRule.get(IF);
            for (Map.Entry<String, Object> entry : valuesToMatch.entrySet()) {
                if (configurationValues.containsKey(entry.getKey())) {
                    Object configValue = configurationValues.get(entry.getKey());
                    if (!configValue.equals(entry.getValue())) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(String.format("Condition doesn't match for %s, not enforcing rule. Expected %s " +
                                    "for rule, was %s", entry.getKey(), configValue, entry.getValue()));
                        }
                        return;
                    }
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("Config %s is not present, hence not enforcing rule. ",
                                entry.getKey()));
                    }
                    return;
                }
            }
        }
        Object configValue = configurationValues.get(keyToValidate);
        if (configValue == null && validationRule.containsKey(DEFAULT_VALUE)) {
            configurationValues.put(keyToValidate, validationRule.get(DEFAULT_VALUE));
        }

        if (validationRule.get(REGEX) instanceof String) {
            String confValueString = configValue == null ? EMPTY_STRING : configValue.toString();
            String regex = (String) validationRule.get(REGEX);
            if (!confValueString.matches(regex)) {
                String errorMessage = (String) validationRule.get("error_message");
                if (errorMessage == null) {
                    errorMessage = String.format("Validation failed for %s. Expected value to match \"%s\", but was " +
                            "%s", keyToValidate, regex, confValueString);
                }
                throw new ValidationException(errorMessage);
            }
        }
    }
}
