package org.wso2.ei.config;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.ei.config.handlers.Builders;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default value pasing .
 */
public class DefaultParser {

    private static final Log LOGGER = LogFactory.getLog(DefaultParser.class);

    private DefaultParser() {

    }

    static Map<String, Object> addDefaultValues(Map<String, Object> enrichedContext, String defaultValueFilePath) {

        try {
            Map<String, Object> defaultValueMap = readConfiguration(defaultValueFilePath);
            for (Map.Entry<String, Object> entry : defaultValueMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (!enrichedContext.containsKey(key)) {
                    enrichedContext.put(key, value);
                } else {
                    Object retrievedEnrichedContext = enrichedContext.get(key);
                    Builders messageBuilder = readHandles(key);
                    enrichedContext.put(key, messageBuilder.handle(retrievedEnrichedContext, value));

                }
            }
        } catch (IOException e) {
            LOGGER.error("Error while default values with file" + defaultValueFilePath, e);

        } catch (IllegalAccessException e) {
            LOGGER.error("Error while accessing Builder", e);
        } catch (InstantiationException | ClassNotFoundException e) {
            LOGGER.error("Error while creating Builder", e);
        }
        return enrichedContext;
    }

    private static Map<String, Object> readConfiguration(String defaultValueFilePath) throws IOException {

        Gson gson = new Gson();
        Reader input = new InputStreamReader(new FileInputStream(defaultValueFilePath), Charsets.UTF_8);
        return gson.fromJson(input, LinkedHashMap.class);
    }

    private static Builders readHandles(String key) throws ClassNotFoundException,
            IllegalAccessException, InstantiationException {

        Gson gson = new Gson();
        Reader input = new InputStreamReader(DefaultParser.class.getClassLoader().getResourceAsStream("handle.json"),
                Charsets.UTF_8);
        Map<String, String> handlers = gson.fromJson(input, Map.class);
        String className = handlers.get(key);
        if (className != null) {
            return (Builders) Class.forName(className).newInstance();
        }
        return new Builders();
    }
}
