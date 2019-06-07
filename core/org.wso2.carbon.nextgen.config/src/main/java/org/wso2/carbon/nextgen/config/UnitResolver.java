/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.nextgen.config;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.nextgen.config.model.Context;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves units provided in values to component expected values.
 */
class UnitResolver {

    private static final String DAY_SUFFIX = "d";
    private static final String HOUR_SUFFIX = "h";
    private static final String MINUTE_SUFFIX = "m";
    private static final String SECOND_SUFFIX = "s";
    private static final String MILLI_SECOND_SUFFIX = "ms";
    private static final String TIME = "time";
    private static Map<String, String> timeMapping;
    private static final Pattern REGEX = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(\\w*)");

    private UnitResolver() {
    }

    static void updateUnits(Context context, String unitConfigFilePath)
            throws ConfigParserException {
        readConfiguration(unitConfigFilePath);
        resolveTimeConfiguration(context.getTemplateData());
        timeMapping = null;
    }

    private static void readConfiguration(String configFile) throws ConfigParserException {
        Gson gson = new Gson();
        Map<Object, Object> unitConfigs = null;
        try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
            Reader input = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            unitConfigs = gson.fromJson(input, Map.class);
            if (unitConfigs.containsKey(TIME)) {
                Object timeConfigMapObj = unitConfigs.get("time");
                if (timeConfigMapObj instanceof Map) {
                    timeMapping = (Map<String, String>) timeConfigMapObj;
                }
            }
        } catch (IOException e) {
            throw new ConfigParserException("Error while reading unit config file", e);
        }
    }


    private static void resolveTimeConfiguration(Map<String, Object> context) throws ConfigParserException {

        if (timeMapping != null) {
            for (Map.Entry<String, String> entry : timeMapping.entrySet()) {
                if (context.containsKey(entry.getKey())) {
                    context.put(entry.getKey(), getResolvedValue(entry.getKey(), context.get(entry.getKey()),
                            entry.getValue()));
                }
            }
        }
    }

    private static String getResolvedValue(String key, Object value, String expectedUnit) throws ConfigParserException {

        if (value instanceof Integer || value instanceof Double || value instanceof Long) {
            return String.valueOf(value);
        }

        if (value == null) {
            throw new ConfigParserException(
                    String.format("Invalid value for configuration key %s. Value is null", key));
        }

        if (!(value instanceof String)) {
            throw new ConfigParserException(String.format("Invalid value for configuration key %s. Value type is %s",
                    key, value.getClass().getName()));
        }

        String stringValue = (String) value;
        Matcher matcher = REGEX.matcher(stringValue);
        if (matcher.find()) {
            Double numericValue = Double.valueOf(matcher.group(1));
            String unit = matcher.group(2);

            if (StringUtils.isEmpty(unit)) {
                return (String) value;
            }
            TimeUnit sourceUnit = getTimeUnit(unit);
            TimeUnit destinationUnit = getTimeUnit(expectedUnit);
            long convertedValue = destinationUnit.convert(numericValue.longValue(), sourceUnit);
            if (convertedValue == 0 && numericValue.longValue() != 0) {
                throw new ConfigParserException(String.format("Converted value result in 0 for non zero source value." +
                        " key: %s, value given = %s, converted value = 0 %s",
                        key, stringValue, destinationUnit.toString()));
            }
            return String.valueOf(convertedValue);
        } else {
            throw new ConfigParserException(String.format("Invalid configuration value %s = %s", key, value));
        }
    }

    private static TimeUnit getTimeUnit(String unit) throws ConfigParserException {
        switch (unit) {
            case DAY_SUFFIX:
                return TimeUnit.DAYS;
            case HOUR_SUFFIX:
                return TimeUnit.HOURS;
            case MINUTE_SUFFIX:
                return TimeUnit.MINUTES;
            case SECOND_SUFFIX:
                return TimeUnit.SECONDS;
            case MILLI_SECOND_SUFFIX:
                return TimeUnit.MILLISECONDS;
            default:
                throw new ConfigParserException("Invalid unit : " + unit);
        }
    }
}
