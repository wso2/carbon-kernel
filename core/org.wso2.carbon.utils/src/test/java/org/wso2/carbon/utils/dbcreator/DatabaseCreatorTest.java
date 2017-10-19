/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.utils.dbcreator;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.BaseTest;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.nio.file.Paths;

/**
 * Test cases to verify DatabaseCreator functionality.
 */
@Test(dependsOnGroups = "org.wso2.carbon.context")
public class DatabaseCreatorTest extends BaseTest {

    private File dataSourceDirectory = Paths.get(testSampleDirectory.getPath(), "testDataSourceDirectory").toFile();
    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CHECK_SQL = "select * from UM_SYSTEM_USER";

    @BeforeTest(alwaysRun = true)
    public void setupDataSourceDirectory() throws Exception {
        if (dataSourceDirectory.exists()) {
            FileUtils.deleteDirectory(dataSourceDirectory);
        }
        dataSourceDirectory.mkdirs();
    }

    @Test(groups = "org.wso2.carbon.utils.dbcreator", description = "Test case to create and verify carbon database")
    public void testCreateRegistryDatabase() throws Exception {
        File file = new File("../../distribution/kernel/carbon-home");
        if (!file.exists()) {
            file = new File("distribution/kernel/carbon-home");
        }
        System.setProperty(ServerConstants.CARBON_HOME, file.getAbsolutePath());
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(DB_DRIVER);
        String dbUrl = "jdbc:h2:" + dataSourceDirectory.getAbsolutePath() + "/DATA_SOURCE_TEST";
        ds.setUrl(dbUrl);
        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();
        Assert.assertTrue(creator.isDatabaseStructureCreated(DB_CHECK_SQL));
        System.clearProperty(ServerConstants.CARBON_HOME);
    }
}
