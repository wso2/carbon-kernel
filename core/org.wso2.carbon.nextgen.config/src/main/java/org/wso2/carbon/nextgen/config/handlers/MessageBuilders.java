package org.wso2.carbon.nextgen.config.handlers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for Messagebuilders.
 */
public class MessageBuilders extends Builders {

    @Override
    public Object handle(Object deploymentValues, Object defaultValues) {

        Map<String, Object> mergedlist = new LinkedHashMap<>();
        ((List<Map>) defaultValues).forEach(map -> {
            String contentType = (String) map.get("content_type");
            mergedlist.put(contentType, map);
        });
        ((List<Map>) deploymentValues).forEach(map -> {
            String contentType = (String) map.get("content_type");
            if (mergedlist.containsKey(contentType)) {
                mergedlist.replace(contentType, map);
            } else {
                mergedlist.put(contentType, map);
            }
        });
        return  mergedlist.values();
    }
}
