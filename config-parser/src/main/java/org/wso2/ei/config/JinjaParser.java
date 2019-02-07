package org.wso2.ei.config;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser based on Jinja templating engine. When the configuration and the relevant Jinja template is provided this
 * parser outputs the relevant configuration file.
 */
class JinjaParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JinjaParser.class);

    private JinjaParser() {

    }

    static Map<String, String> parse(Map<String, Object> dottedKeyMap, Map<String, File> templateFiles) {

        Map<String, String> outputs = new LinkedHashMap<>();
        for (Map.Entry<String, File> templateFile : templateFiles.entrySet()) {
            JinjavaConfig configurator = JinjavaConfig.newBuilder().withLstripBlocks(true).withTrimBlocks(true).build();
            Jinjava jinjava = new Jinjava(configurator);
            String renderedTemplate = "";
            Map<String, Object> context = getHierarchicalDottedKeyMap(dottedKeyMap);
            try {
                String template = Files.asCharSource(templateFile.getValue(), Charsets.UTF_8).read();
                renderedTemplate = jinjava.render(template, context);

            } catch (IOException e) {
                LOGGER.error("Error while parsing Jinja template", e);
            }
            outputs.put(templateFile.getKey(), renderedTemplate);
        }
        return outputs;

    }

    static Map<String, Object> getHierarchicalDottedKeyMap(Map<String, Object> dottedKeyMap) {

        Map<String, Object> newContext = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : dottedKeyMap.entrySet()) {
            changeToHierarchicalMap(entry, newContext);
        }
        return newContext;
    }

    private static void changeToHierarchicalMap(Map.Entry<String, Object> entry, Map<String, Object> context) {

        String flatKey = entry.getKey();
        String[] dottedKeyArray = flatKey.split("\\.");

        Map<String, Object> parentMap = context;
        for (int i = 0; i < dottedKeyArray.length - 1; i++) {
            String keyElement = dottedKeyArray[i];
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
        parentMap.put(dottedKeyArray[dottedKeyArray.length - 1], value);

    }

    private static List<Object> processArray(List<Object> list) {

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
}
