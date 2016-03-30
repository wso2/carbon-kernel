package org.wso2.carbon.datasource.api;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.datasource.core.BaseTest;
import org.wso2.carbon.datasource.core.api.DataSourceManagementService;
import org.wso2.carbon.datasource.core.beans.DataSourceMetadata;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.datasource.core.impl.DataSourceManagementServiceImpl;
import org.wso2.carbon.datasource.core.impl.DataSourceServiceImpl;

import java.net.MalformedURLException;
import java.util.List;

public class DataSourceManagementServiceTest extends BaseTest {

    private DataSourceManagementService dataSourceMgtService;

    @BeforeSuite
    public void initialize() throws DataSourceException, MalformedURLException {
        super.init();
        dataSourceMgtService = new DataSourceManagementServiceImpl();
    }

    @Test
    public void getDataSourceTest() throws DataSourceException {
        DataSourceMetadata dataSourceMetadata = dataSourceMgtService.getDataSource("WSO2_CARBON_DB_2");
        Assert.assertNotNull(dataSourceMetadata , "metadata for \"WSO2_CARBON_DB_2\" should not be null");
    }

    @Test(dependsOnMethods = "getDataSourceTest")
    public void getDataSourceListTest() throws DataSourceException {
        List<DataSourceMetadata> dataSourceMetadata = dataSourceMgtService.getDataSource();
        Assert.assertEquals(1, dataSourceMetadata.size(), "Only one WSO2_CARBON_DB_2 exist in the repository.");
    }

    @Test(dependsOnMethods = "getDataSourceListTest")
    public void addAndDeleteDataSourceTest() throws DataSourceException {
        DataSourceMetadata dataSourceMetadata = dataSourceMgtService.getDataSource("WSO2_CARBON_DB_2");
        Assert.assertNotNull(dataSourceMetadata, "dataSourceMetadata should not be null");
        dataSourceMgtService.deleteDataSource("WSO2_CARBON_DB_2");
        DataSourceMetadata dataSourceMetadata2 = dataSourceMgtService.getDataSource("WSO2_CARBON_DB_2");
        Assert.assertNull(dataSourceMetadata2, "After deleting WSO2_CARBON_DB_2 should not exist in the repository");

    }
}
