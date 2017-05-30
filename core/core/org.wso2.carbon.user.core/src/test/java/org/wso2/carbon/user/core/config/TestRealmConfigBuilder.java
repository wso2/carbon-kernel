/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.user.core.config;

import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;

import java.io.InputStream;
import java.util.Map;

public class TestRealmConfigBuilder {

    public static final String JDBC_URL_PROPERTY_NAME = "url";

    public static RealmConfiguration buildRealmConfigWithJDBCConnectionUrl(InputStream inStream,
            String connectionUrl) throws UserStoreException {
        RealmConfigXMLProcessor builder = new RealmConfigXMLProcessor();
        RealmConfiguration realmConfig = builder.buildRealmConfiguration(inStream);
        Map<String, String> map = realmConfig.getRealmProperties();
        map.put(JDBC_URL_PROPERTY_NAME, connectionUrl);
        return realmConfig;
    }

}
