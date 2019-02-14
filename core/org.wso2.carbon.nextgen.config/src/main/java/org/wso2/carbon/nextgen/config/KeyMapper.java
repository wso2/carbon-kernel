package org.wso2.carbon.nextgen.config;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Map user provided config values to different keys.
 */
class KeyMapper {

    private KeyMapper() {
    }

    static Map<String, Object> mapWithConfig(Map<String, Object> inputContext,
                                             String mappingFile) throws ConfigParserException {
        try (Reader validatorJson = new InputStreamReader(new FileInputStream(mappingFile),
                                                          Charset.defaultCharset())) {
            Gson gson = new Gson();
            Map<String, String> keyMappings = gson.fromJson(validatorJson, Map.class);
            return map(inputContext, keyMappings);
        } catch (IOException e) {
            throw new ConfigParserException("Error while parsing JSON file " + mappingFile, e);
        }
    }

    static Map<String, Object> map(Map<String, Object> context,
                                   Map<String, String> keyMappings) throws ConfigParserException {
        Map<String, Object> mappedConfigs = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String mappedKey = keyMappings.getOrDefault(entry.getKey(), entry.getKey());
            mappedConfigs.put(mappedKey, entry.getValue());
        }

        processArrayKeys(keyMappings, mappedConfigs);
        return mappedConfigs;
    }

    /**
     * Process array keys that are denoted in the config file suffixed with a ":".
     */
    private static void processArrayKeys(Map<String, String> keyMappings, Map<String, Object> mappedConfigs)
            throws ConfigParserException {
        for (Map.Entry<String, String> entry : keyMappings.entrySet()) {
            String key = entry.getKey();
            String[] splitKey = key.split(":");
            if (splitKey.length == 2) {
                Object object = mappedConfigs.get(splitKey[0]);
                processArrayKey(entry, splitKey, object);

            } else if (splitKey.length > 2) {
                throw new ConfigParserException("Unknown key mapping key with multiple array elements: "
                                                + entry.getKey());
            }
        }
    }

    private static void processArrayKey(Map.Entry<String, String> entry, String[] splitKey, Object object)
            throws ConfigParserException {
        if (object instanceof List) {
            List<Object> list = (List) object;
            for (Object o : list) {
                if (o instanceof Map) {
                    processMap(entry, splitKey[1], (Map) o);
                }
            }
        }
    }

    private static void processMap(Map.Entry<String, String> entry, String key, Map<String, Object> map)
            throws ConfigParserException {
        Object removedValue = map.remove(key);
        if (Objects.nonNull(removedValue)) {
            String[] splitValue = entry.getValue().split(":");
            if (splitValue.length != 2) {
                throw new ConfigParserException("Unknown key mapping value with multiple array "
                                                + "elements: " + entry.getValue());
            }
            map.put(splitValue[1], removedValue);
        }
    }
}
