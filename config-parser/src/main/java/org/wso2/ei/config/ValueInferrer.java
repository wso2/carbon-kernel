package org.wso2.ei.config;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ValueInferrer {

    private static final String INFER_CONFIG_FILE_PATH = "infer.json";
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueInferrer.class);


    private Map inferringData = new HashMap();

    public static void main(String[] args) throws IOException {

        ValueInferrer inferrer = new ValueInferrer();
        inferrer.readConfiguration();
        Map<String, Object> configValues = new HashMap<>();
        configValues.put("user_store.type", "jdbc");
        configValues.put("database.type", "mysql");
        Map<String, Object> inferredValues = inferrer.getInferredValues(configValues);
        inferredValues.forEach((s, o) -> LOGGER.info(s + " = " + o.toString()));
    }

    public void readConfiguration() throws IOException {

        Gson gson = new Gson();
        String configJson = Resources.toString(Resources.getResource(INFER_CONFIG_FILE_PATH), Charsets.UTF_8);

        inferringData = gson.fromJson(configJson, Map.class);

    }

    public Map<String, Object> getInferredValues(Map<String, Object> configurationValues) {

        Map<String, Object> inferredValues = new HashMap<>();
        if (configurationValues != null) {
            configurationValues.forEach((s, o) -> {
                if (inferringData.containsKey(s)) {
                    Map inferringValues = (Map) inferringData.get(s);
                    if (inferringValues.containsKey(String.valueOf(o))) {
                        Map valuesInferredByKey = (Map) inferringValues.get(String.valueOf(o));
                        inferredValues.putAll(valuesInferredByKey);
                    }
                }
            });
        }
        return inferredValues;
    }
}
