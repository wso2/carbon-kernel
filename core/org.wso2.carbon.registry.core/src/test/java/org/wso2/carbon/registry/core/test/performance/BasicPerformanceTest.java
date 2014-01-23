/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.core.test.performance;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

public class BasicPerformanceTest extends BaseTestCase {

//    public static final int ITERATIONS = 20;
//    public static final int NUM_USERS = 50;
//    public static final int ITERATIONS = 30;
//    public static final int NUM_USERS = 10;


    public void setUp() {
        //String connURL = "jdbc:log4jdbc:derby:target/REG1_DB";
        //String connURL = "jdbc:derby:target/REG1_DB";
        String connURL = "jdbc:mysql://localhost:3306/registry1";
        BasicDataSource ds = new BasicDataSource();
        //ds.setUrl(connURL + ";");
        //ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl(connURL);
        ds.setUsername("root");
        ds.setPassword("password");



//        ds.setMaxWait(1000*60*2);

        ds.setMaxActive(150);
        ds.setMaxIdle(1000*60*2);
        ds.setMinIdle(5);
        //ds.setDriverClassName("net.sf.log4jdbc.DriverSpy");


        //DerbyDatabaseCreator creator = new DerbyDatabaseCreator(ds);
        DatabaseCreator creator = new DatabaseCreator(ds);
        try {
             creator.createRegistryDatabase();
        } catch (Exception e) {
             fail("Failed to create database. Caused by: " + e.getMessage());
        }
		//String fileName = "target/db/registry";
		//File file = new File(fileName);

		//if (! file.exists()) {
	        //creator.createDefaultDatabaseTables();
		//}

//        UserRealm realm = new DefaultRealm();
//        DefaultRealmConfig config = (DefaultRealmConfig) realm.getBootstrapRealmConfiguration();
//        config.setConnectionURL(connURL);
//        realm.init(config);
//        UserRealm registryRealm = new UserRealm(realm);
//
//        InputStream configStream =
//            Thread.currentThread().getContextClassLoader().getResourceAsStream("registry.xml");
//        RegistryContext regContext = new RegistryContext(configStream, registryRealm);
//        embeddedRegistryService = new EmbeddedRegistryService(regContext);
//        adminRegistry = embeddedRegistryService.getUserRegistry(
//            RegistryConstants.ADMIN_USER, RegistryConstants.ADMIN_PASSWORD);
        System.out.println("~~~~~setup method done~~~~~");
    }

    public void testResource() throws Exception {

//        int numUsers = NUM_USERS;
//        Worker[] workers = new Worker[numUsers];
//        for (int i = 0; i < numUsers; i++) {
//            Worker worker = new Worker3("T" + i, ITERATIONS, adminRegistry);
//            workers[i] = worker;
//        }
//
//
//        log.info("Starting workers.");
//        for (int i = 0; i < numUsers; i++) {
//            workers[i].start();
//        }
//
//        log.info("Waiting for worker to complete.");
//        for (int i = 0; i < numUsers; i++) {
//            workers[i].join();
//        }
//

    }
}
