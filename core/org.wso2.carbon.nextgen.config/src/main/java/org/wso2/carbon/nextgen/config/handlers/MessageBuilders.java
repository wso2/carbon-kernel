/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

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
