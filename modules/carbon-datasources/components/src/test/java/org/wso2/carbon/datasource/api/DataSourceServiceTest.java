package org.wso2.carbon.datasource.api;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.datasource.core.BaseTest;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.datasource.core.impl.DataSourceServiceImpl;

import java.net.MalformedURLException;

public class DataSourceServiceTest extends BaseTest {

    private DataSourceService dataSourceService;

    @BeforeSuite
    public void initialize() throws DataSourceException, MalformedURLException {
        super.init();
        dataSourceService = new DataSourceServiceImpl();
    }

    @Test
    public void getDataSourceTest() throws DataSourceException {
        Object dataSourceObject = dataSourceService.getDataSource("WSO2_CARBON_DB_2");
        Assert.assertNotNull(dataSourceObject, "test datasource \"WSO2_CARBON_DB_2\" should not be null");
    }
}
