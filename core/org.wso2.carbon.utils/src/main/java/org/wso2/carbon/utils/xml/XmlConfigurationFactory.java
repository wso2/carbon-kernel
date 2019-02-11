/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.utils.xml;

import org.wso2.carbon.utils.ServerException;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class XmlConfigurationFactory {
    private static Map configurations = new HashMap();

    public static void init(String configKey, String configFile) throws ServerException {
        init(configKey, configFile, null);
    }

    public static void init(String configKey,
                            String configFile,
                            String serverNamespace) throws ServerException {
        if (serverNamespace != null) {
            configurations.put(configKey, new XmlConfiguration(configFile, serverNamespace));
        } else {
            configurations.put(configKey, new XmlConfiguration(configFile));
        }
    }

    public static XmlConfiguration getXmlConfiguration(String configKey) {
        XmlConfiguration config = null;
        Object obj = configurations.get(configKey);
        if (obj != null) {
            config = (XmlConfiguration) obj;
        }
        return config;
    }
}
