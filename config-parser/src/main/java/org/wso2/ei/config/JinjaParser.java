package org.wso2.ei.config;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class JinjaParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JinjaParser.class);

    private JinjaParser() {}

    static String execute(Map<String, Object> dottedKeyMap, String templateFilePath) {
        Jinjava jinjava = new Jinjava();
        String renderedTemplate = "";
        Map<String, Object> context = getHierarchicalDottedKeyMap(dottedKeyMap);
        try {
            String template = Resources.toString(Resources.getResource(templateFilePath), Charsets.UTF_8);
            renderedTemplate = jinjava.render(template, context);

        } catch (IOException e) {
            LOGGER.error("Error while parsing Jinja template", e);
        }

        return renderedTemplate;
    }

    private static Map<String, Object> getHierarchicalDottedKeyMap(Map<String, Object> dottedKeyMap) {
        Map<String, Object> newContext = new HashMap<>();
        for (Map.Entry<String, Object> entry: dottedKeyMap.entrySet()) {
            processFlatDottedKey(entry, newContext);
        }
        return newContext;
    }

    private static void processFlatDottedKey(Map.Entry<String, Object> entry, Map<String, Object> newContext) {

        String dottedKey = entry.getKey();
        String[] dottedKeyArray = dottedKey.split("\\.");
        Map<String, Object> parentMap = newContext;
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
        parentMap.put(finalSubKey, entry.getValue());
    }
}
