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

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.datasource.core.common.DataSourceException;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Test case for DataSourceManager class.
 */
public class DataSourceManagerTest {

    private DataSourceManager dsManager;

    @BeforeSuite
    public void initialize() throws DataSourceException, MalformedURLException {
        setEnv();
        dsManager = DataSourceManager.getInstance();
        dsManager.initSystemDataSources();

    }

    private void setEnv() {
        //Set carbon home
        Path carbonHomePath = Paths.get("component", "target", "carbonHome");
        System.setProperty("carbon.home", carbonHomePath.toFile().getAbsolutePath());

        Path configFilePath = Paths.get("component", "src", "test", "resources", "conf", "datasources", "master-datasources.xml");
        Path configPathCopyLocation = Paths.get("component/target/carbonHome/conf/datasources/master-datasources.xml");
        Utils.copy(configFilePath.toFile().getAbsolutePath(), configPathCopyLocation.toFile().getAbsolutePath());

//        OutputStream outputStream = null;
//        try (InputStream inputStream = new FileInputStream(configFileLocation)) {
//
//            String yamlFileString;
//            try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
//                yamlFileString = scanner.useDelimiter("\\A").next();
//                yamlFileString = org.wso2.carbon.kernel.utils.Utils.substituteVariables(yamlFileString);
//            }
//            File confPath = new File("component/target/carbonHome/conf/datasources");
//            confPath.mkdirs();
//            outputStream = new FileOutputStream(confPath + "/master-datasources.xml");
//            outputStream.write(yamlFileString.getBytes());
//        } catch (IOException e) {
//            String errorMessage = "Failed populate CarbonConfiguration from " + configFileLocation;
//            throw new RuntimeException(errorMessage);
//        } finally {
//            if(outputStream != null) {
//                try {
//                    outputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

    }


    private void loadDataSource() {
        //Does loading the data prior to run the test.
    }

    @Test
    public void getAllDataSourcesTest() {

    }


    @Test
    public void getAllDataSourceTest() {

    }


    @AfterSuite
    public void destroy() {
        clearEnv();
    }

    private void clearEnv() {
        System.clearProperty("carbon.home");
    }
}
