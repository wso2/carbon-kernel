/*
 *
 * Copyright (c) 2023, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.user.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data holder to maintain datasource (JDBC) references of each domain.
 */
public class DatasourceDataHolder {

    private static final DatasourceDataHolder Instance = new DatasourceDataHolder();
    private static Log log = LogFactory.getLog(DatasourceDataHolder.class);
    //key = <tenantId,domain of the userstore)>
    private static ConcurrentHashMap<AbstractMap.SimpleEntry<String, String>, DataSource> dataSourceHolder
            = new ConcurrentHashMap<>();

    public static DatasourceDataHolder getInstance() {
        return Instance;
    }

    public void addDataSourceForDomain(AbstractMap.SimpleEntry<String, String> key, DataSource dataSource) {
        dataSourceHolder.put(key, dataSource);
    }

    public void removeDomainDataSources(AbstractMap.SimpleEntry<String, String> domain) {
        dataSourceHolder.remove(domain);
    }

    public DataSource getDataStoreForDomain(AbstractMap.SimpleEntry<String, String> domain) {
        return dataSourceHolder.get(domain);
    }

    public static void removeDatasourcesOfTenant(int tenantId) {
        Iterator<AbstractMap.SimpleEntry<String, String>> iterator = dataSourceHolder.keySet().iterator();
        while (iterator.hasNext()) {
            AbstractMap.SimpleEntry<String, String> entry = iterator.next();
            if (entry.getKey().equals(String.valueOf(tenantId))) {
                // Closing the connections
                if (dataSourceHolder.get(entry) instanceof org.apache.tomcat.jdbc.pool.DataSourceProxy) {
                    ((org.apache.tomcat.jdbc.pool.DataSourceProxy) dataSourceHolder.get(entry)).close(true);
                }
                dataSourceHolder.remove(entry);
            }
        }
    }
}
