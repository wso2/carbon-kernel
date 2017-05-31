/*
 * Copyright 2009-2010 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.user.core.jdbc;

import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.util.JDBCRealmUtil;

import java.io.IOException;
import java.io.InputStream;

/**
Test case for getSQL function
 */
public class JDBCConfigurationTest extends BaseTestCase {

    private RealmConfiguration realmConfig = null;

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testStuff() throws Exception {

        doSQLQueryStuff();

    }

    public void doSQLQueryStuff() throws IOException, UserStoreException {

        InputStream inStream = this.getClass().getClassLoader().getResource(JDBCRealmTest.JDBC_TEST_USERMGT_XML).openStream();

        RealmConfigXMLProcessor realmConfigProcessor = new RealmConfigXMLProcessor();
        realmConfig = realmConfigProcessor.buildRealmConfiguration(inStream);

        assertEquals(realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_ROLE), null);
        assertEquals(realmConfig.getUserStoreProperty(JDBCRealmConstants.SELECT_USER), JDBCRealmConstants.SELECT_USER_SQL);
        realmConfig.setUserStoreProperties(JDBCRealmUtil.getSQL(realmConfig.getUserStoreProperties()));
        assertEquals(realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_ROLE), JDBCRealmConstants.ADD_ROLE_SQL);
        assertEquals(realmConfig.getUserStoreProperty(JDBCRealmConstants.SELECT_USER), JDBCRealmConstants.SELECT_USER_SQL);

    }
}
