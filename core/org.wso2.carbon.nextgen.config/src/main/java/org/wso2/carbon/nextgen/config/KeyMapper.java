package org.wso2.carbon.nextgen.config;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map user provided config values to different keys.
 */
class KeyMapper {

    private static final Log LOGGER = LogFactory.getLog(KeyMapper.class);

    private KeyMapper() {
    }

    static Map<String, Object> mapWithConfig(Map<String, Object> inputContext, String mappingFile)
            throws ConfigParserException {
        try (Reader validatorJson = new InputStreamReader(new FileInputStream(mappingFile),
                                                          Charset.defaultCharset())) {
            Gson gson = new Gson();
            Map<String, String> keyMappings = gson.fromJson(validatorJson, Map.class);
            return map(inputContext, keyMappings);
        } catch (IOException e) {
            throw new ConfigParserException("Error while parsing JSON file " + mappingFile, e);
        }
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
