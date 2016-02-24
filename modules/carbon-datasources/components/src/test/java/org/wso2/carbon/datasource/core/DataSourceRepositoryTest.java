//package org.wso2.carbon.datasource.core;
//
//import org.testng.Assert;
//import org.testng.annotations.AfterSuite;
//import org.testng.annotations.BeforeSuite;
//import org.testng.annotations.Test;
//import org.wso2.carbon.datasource.core.beans.CarbonDataSource;
//import org.wso2.carbon.datasource.core.common.DataSourceException;
//
//import java.net.MalformedURLException;
//
///**
// * Test class for DataSourceRepository class.
// */
//public class DataSourceRepositoryTest extends BaseTest {
//
//    private DataSourceManager dsManager;
//    private DataSourceRepository dsRepository;
//
//    @BeforeSuite
//    public void initialize() throws DataSourceException, MalformedURLException {
//        setEnv();
//        dsManager = DataSourceManager.getInstance();
//        dsManager.initDataSources();
//        dsRepository = dsManager.getDataSourceRepository();
//    }
//
//    @Test
//    public void getAllDataSourcesTest() {
//        int size = dsRepository.getAllDataSources().size();
//        Assert.assertEquals(size, 1, "Only one data source is configured.");
//    }
//
//
//    @Test
//    public void getAllDataSourceTest() {
//        CarbonDataSource carbonDataSource = dsRepository.getDataSource("WSO2_CARBON_DB");
//        Assert.assertNotNull(carbonDataSource, "WSO2_CARBON_DB not found in the repository.");
//    }
//
//    @Test(dependsOnMethods = "getAllDataSourceTest")
//    public void deleteDataSourceTest() {
//        try {
//            dsRepository.deleteDataSource("WSO2_CARBON_DB");
//            CarbonDataSource carbonDataSource = dsRepository.getDataSource("WSO2_CARBON_DB");
//            Assert.assertNull(carbonDataSource, "WSO2_CARBON_DB is deleted, but found in the repository.");
//        } catch (DataSourceException e) {
//            Assert.fail("Error occurred while deleting the data source");
//        }
//    }
//
//    @AfterSuite
//    public void destroy() {
//        clearEnv();
//    }
//
//}
