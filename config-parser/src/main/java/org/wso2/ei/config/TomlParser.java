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

class TomlParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TomlParser.class);

    static Map<String, Object> execute(String filePath) {
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
            for (String dottedKey : dottedKeySet) {
                String[] dottedKeyArray = dottedKey.split("\\.");
                handleDottedKey(context, result, dottedKey, dottedKeyArray);
            }

        } catch (IOException e) {
            LOGGER.error("Error parsing file {}", filePath, e);
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
