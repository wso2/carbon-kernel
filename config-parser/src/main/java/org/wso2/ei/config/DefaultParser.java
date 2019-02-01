package org.wso2.ei.config;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

/**
 * Default value pasing .
 */
public class DefaultParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultParser.class);

    private DefaultParser() {

    }

    static Map<String, Object> addDefaultValues(Map<String, Object> enrichedContext, String defaultValueFilePath) {

        try {
            Map<String, Object> defaultValueMap = readConfiguration(defaultValueFilePath);
            defaultValueMap.forEach((key, value) -> {
                if (!enrichedContext.containsKey(key)) {
                    enrichedContext.put(key, value);
                } else {
                    Map retrievedEnrichedContext = (Map) enrichedContext.get(key);

                }
            });
        } catch (IOException e) {
            LOGGER.error("Error while inferring values with file {}", defaultValueFilePath, e);

        }
        return enrichedContext;
    }

    private static Map<String, Object> readConfiguration(String defaultValueFilePath) throws IOException {

        Gson gson = new Gson();
        Reader input = new InputStreamReader(new FileInputStream(defaultValueFilePath), Charsets.UTF_8);
        return gson.fromJson(input, Map.class);
    }
}
