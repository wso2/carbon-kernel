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

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.nextgen.config.model.Context;
import org.wso2.carbon.nextgen.config.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser based on Jinja templating engine. When the configuration and the relevant Jinja template is provided this
 * parser outputs the relevant configuration file.
 */
class JinjaParser {

    private static final Log log = LogFactory.getLog(JinjaParser.class);


    private JinjaParser() {
    }

    static Map<String, String> parse(Context context, Map<String, File> templateFiles)
            throws ConfigParserException {

        Map<String, String> outputs = new LinkedHashMap<>();
        JinjavaConfig configurator = JinjavaConfig.newBuilder().withLstripBlocks(true).withTrimBlocks(true).build();
        Jinjava jinjava = new Jinjava(configurator);
        Map<String, Object> dottedKeyMap = getHierarchicalDottedKeyMap(context.getTemplateData());
        templateFiles.entrySet().parallelStream().forEach((templateFile) -> {
            String renderedTemplate = "";
            try {
                String template = FileUtils.readFile(templateFile.getValue());
                renderedTemplate = jinjava.render(template, dottedKeyMap);

            } catch (ConfigParserException e) {
                log.error("Error while parsing Jinja template", e);
            }
            outputs.put(templateFile.getKey(), renderedTemplate);
        });
        return outputs;

    }

    static Map<String, Object> getHierarchicalDottedKeyMap(Map<String, Object> dottedKeyMap)
            throws ConfigParserException {

        Map<String, Object> newContext = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : dottedKeyMap.entrySet()) {
            changeToHierarchicalMap(entry, newContext);
        }
        return newContext;
    }

    private static void changeToHierarchicalMap(Map.Entry<String, Object> entry, Map<String, Object> context)
            throws ConfigParserException {
        List<String> dottedKeyList = getDottedKeyArray(entry.getKey());

        Map<String, Object> parentMap = context;
        for (int i = 0; i < dottedKeyList.size() - 1; i++) {
            String keyElement = dottedKeyList.get(i);
            Object object = parentMap.get(keyElement);
            Map<String, Object> map;
            if (object instanceof Map) {
                map = (Map) object;
            } else {
                map = new LinkedHashMap<>();
                parentMap.put(keyElement, map);
            }
            parentMap = map;
        }

        Object value = entry.getValue();
        if (value instanceof List) {
            value = processArray((List) value);
        }
        parentMap.put(dottedKeyList.get(dottedKeyList.size() - 1), value);

    }

    static List<String> getDottedKeyArray(String dottedFlatKey) throws ConfigParserException {

        List<String> list = new ArrayList<>();
        int lastMatchedIndex = 0;
        while (lastMatchedIndex != -1) {
            int startIndex = 0;
            if (lastMatchedIndex == 0) {
                startIndex = dottedFlatKey.indexOf('\'', lastMatchedIndex);
            } else {
                startIndex = dottedFlatKey.indexOf('\'', lastMatchedIndex + 1);
            }
            if (startIndex == -1) {
                int beginIndx = lastMatchedIndex == 0 ? 0 : lastMatchedIndex + 1;
                splitAndAddToList(dottedFlatKey, list, beginIndx, dottedFlatKey.length());
                lastMatchedIndex = startIndex;
            } else {
                int endIndex = dottedFlatKey.indexOf('\'', startIndex + 1);
                if (endIndex == -1) {
                    throw new ConfigParserException("Couldn't find matching ending \"'\" for sub key in flat key "
                            + dottedFlatKey);
                }
                if (lastMatchedIndex == 0) {
                    splitAndAddToList(dottedFlatKey, list, lastMatchedIndex, startIndex);
                } else {
                    splitAndAddToList(dottedFlatKey, list, lastMatchedIndex + 1, startIndex);
                }
                list.add(dottedFlatKey.substring(startIndex + 1, endIndex));
                lastMatchedIndex = endIndex;
            }
        }
        return list;
    }

    private static void splitAndAddToList(String flatKey, List<String> list, int startIndex, int endIndex) {
        String dottedSubKey = flatKey.substring(startIndex, endIndex);
        List<String> subKeyList = splitWithoutEmptyStrings(dottedSubKey);
        list.addAll(subKeyList);
    }

    private static List<Object> processArray(List<Object> list) throws ConfigParserException {

        List<Object> newList = new ArrayList<>(list.size());

        for (Object obj : list) {
            Object processedObject = obj;
            if (obj instanceof Map) {
                processedObject = getHierarchicalDottedKeyMap((Map) obj);
            } else if (obj instanceof List) {
                processedObject = processArray((List) obj);
            }
            newList.add(processedObject);
        }
        return newList;
    }

    static List<String> splitWithoutEmptyStrings(String input) {
        List<String> list = new ArrayList<>();
        int lastDelimiterIndex = -1;
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (currentChar == '.') {
                if ((i - lastDelimiterIndex) != 1) {
                    String substring = input.substring(lastDelimiterIndex + 1, i);
                    list.add(substring);
                }
                lastDelimiterIndex = i;
            }
        }
        if (lastDelimiterIndex != (input.length() - 1)) {
            list.add(input.substring(lastDelimiterIndex + 1));
        }

        return list;
    }
}
