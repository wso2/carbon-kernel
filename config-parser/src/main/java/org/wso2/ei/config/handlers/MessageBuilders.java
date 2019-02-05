package org.wso2.ei.config.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for Messagebuilders.
 */
public class MessageBuilders extends Builders {

    @Override
    public Object handle(Object deploymentValues, Object defaultValues) {

        Map<String, Object> mergedlist = new HashMap<>();
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
