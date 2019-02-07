package org.wso2.ei.config;

import net.consensys.cava.toml.Toml;
import net.consensys.cava.toml.TomlParseResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Map user provided config values to different keys.
 */
class KeyMapper {

    private static final Log LOGGER = LogFactory.getLog(KeyMapper.class);

    static Map<String, Object> mapWithTomlConfig(Map<String, Object> inputContext, String tomlMappingFile) {
        try {
            Map<String, String> keyMappings = parseTomlMappingFile(tomlMappingFile);
            return map(inputContext, keyMappings);
        } catch (IOException e) {
            LOGGER.error("Error while parsing toml file " + tomlMappingFile, e);
        }

        return Collections.emptyMap();
    }

    private static Map<String, String> parseTomlMappingFile(String tomlMappingFile) throws IOException {
        String source;

        TomlParseResult result;
        result = Toml.parse(Paths.get(tomlMappingFile));
        result.errors().forEach(error -> LOGGER.error(error.toString()));
        Set<String> dottedKeySet = result.dottedKeySet();
        Map<String, String> keyMappings = new LinkedHashMap<>();
        for (String key : dottedKeySet) {
            keyMappings.put(key, (String) result.get(key));
        }
        return keyMappings;
    }

    static Map<String, Object> map(Map<String, Object> context, Map<String, String> keyMappings) {
        Map<String, Object> mappedConfigs = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String mappedKey = keyMappings.getOrDefault(entry.getKey(), entry.getKey());
            mappedConfigs.put(mappedKey, entry.getValue());
        }
        return mappedConfigs;
    }
}
