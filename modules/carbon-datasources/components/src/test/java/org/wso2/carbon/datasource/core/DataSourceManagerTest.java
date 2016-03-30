/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.datasource.core;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import java.net.MalformedURLException;
import java.util.List;

/**
 * Test class for DataSourceManager class.
 */
public class DataSourceManagerTest extends BaseTest {

    @BeforeSuite
    public void initialize() throws DataSourceException, MalformedURLException {
        super.init();
    }

    @Test(expectedExceptions = DataSourceException.class)
    public void getDataSourceReaderFailTest() throws DataSourceException {
        dataSourceManager.getDataSourceReader(null);
        Assert.fail("Exception is not thrown when an exception is expected.");
    }

    @Test
    public void getDataSourceRepositoryTest() throws DataSourceException {
        DataSourceRepository dsRepo = dataSourceManager.getDataSourceRepository();
        Assert.assertNotNull(dsRepo, "Expected a DataSourceRepository, but found none!!!");
        //Does loading the data prior to run the test.
    }

    @Test
    public void getDataSourceTypesTest() {
        List<String> types = dataSourceManager.getDataSourceTypes();
        Assert.assertEquals(types.size(), 1, "Expected only one data source type.");
    }

    @AfterSuite
    public void destroy() {
        clearEnv();
    }
}
