/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// framework setting property loader

package org.wso2.carbon.integration.framework.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FrameworkSettings {
    private static final Log log = LogFactory.getLog(FrameworkSettings.class);

    public static String CARBON_HOME;
    public static String HOST_NAME;
    public static String HTTP_PORT;
    public static String HTTPS_PORT;
    public static String NHTTP_PORT;
    public static String NHTTPS_PORT;
    public static String DERBY_PORT;
    public static String MYSQL_PORT;
    public static String MYSQL_USER_NAME;
    public static String MYSQL_PASSWORD;
    public static String CONTEXT_ROOT;
    public static String USER_NAME;
    public static String PASSWORD;
    public static String TRUSTSTORE_PATH;
    public static String TRUSTSTORE_PASSWORD;
    public static boolean BACKENDSERVER_RUNNING;
    public static String TEST_FRAMEWORK_HOME;
    public static String NIO_TRANSPORT_HTTPS;
    public static String TENANT_NAME;
    public static String SERVICE_URL;
    public static String STRATOS;
    public static String BACKENDSERVER_HOST_NAME;
    public static String BACKENDSERVER_HTTPS_PORT;
    public static String BACKENDSERVER_HTTP_PORT;
    public static String P2_REPO;
    public static String SUPERTENANT_USERNAME;
    public static String SUPERTENANT_PASSWORD;
    public static String ESB_TEST_SERVER;


    public static void init() {
        InputStream inputStream = null;
        try {
            String relativePath = getFrameworkPath();
            Properties prop = new Properties();
            inputStream = FrameworkSettings.class.getResourceAsStream("/framework.properties");
            if (inputStream != null) {
                prop.load(inputStream);
            }
            // fReader.close();
            CARBON_HOME = (prop.getProperty("carbon.home", System.getProperty("carbon.home")));
            HOST_NAME = (prop.getProperty("host.name", "localhost"));

            //GS product runs on 8443/8080 | port should be set as a system property
            String httpsPort = System.getProperty("https.port");
            String httpPort = System.getProperty("http.port");
            if (httpsPort == null || httpsPort.equals("")) {
                httpsPort = "9443";
            }
            if (httpPort == null || httpPort.equals("")) {
                httpPort = "9763";
            }

            HTTPS_PORT = (prop.getProperty("https.port", httpsPort));
            HTTP_PORT = (prop.getProperty("http.port", httpPort));
            NHTTP_PORT = (prop.getProperty("nhttp.port", "8280"));
            NHTTPS_PORT = (prop.getProperty("nhttps.port", "8284"));
            ESB_TEST_SERVER = (prop.getProperty("esb.test.server", "9002"));
            NIO_TRANSPORT_HTTPS = (prop.getProperty("nio.transport.port", "8243"));
            DERBY_PORT = prop.getProperty("derby.port", "1527");
            MYSQL_PORT = prop.getProperty("mysql.port", "3306");
            MYSQL_USER_NAME = prop.getProperty("mysql.username", "root");
            MYSQL_PASSWORD = prop.getProperty("mysql.password", "");
            P2_REPO = prop.getProperty("p2.repo", "http://builder.wso2.org/~carbon/releases/carbon/3.0.1/RC3/p2-repo/");

            CONTEXT_ROOT = (prop.getProperty("context.root", null));
            STRATOS = (prop.getProperty("stratos", "false"));
            USER_NAME = (prop.getProperty("server.username", "admin"));
            PASSWORD = (prop.getProperty("server.password", "admin"));
            SUPERTENANT_USERNAME = (prop.getProperty("supertenant.username", "admin"));
            SUPERTENANT_PASSWORD = (prop.getProperty("supertenant.password", "admin"));

            BACKENDSERVER_RUNNING = Boolean.parseBoolean(prop.getProperty("backendserver.running", "true"));
            BACKENDSERVER_HTTP_PORT = (prop.getProperty("backendserver_http.port", "9000"));
            BACKENDSERVER_HTTPS_PORT = (prop.getProperty("backendserver_https.port", "9443"));
            BACKENDSERVER_HOST_NAME = (prop.getProperty("backendserver_host.name", "localhost"));

            TRUSTSTORE_PATH = System.getProperty("carbon.home") + File.separator + "repository" +
                              File.separator + "resources" + File.separator + "security" + File.separator + "wso2carbon.jks";
            TRUSTSTORE_PASSWORD = (prop.getProperty("truststore.password", "wso2carbon"));
            TEST_FRAMEWORK_HOME = relativePath;
            if (STRATOS.equalsIgnoreCase("false")) {

                if (CONTEXT_ROOT == null) {
                    SERVICE_URL = "https://" + HOST_NAME + ":" + HTTPS_PORT + "/services/";
                } else {
                    SERVICE_URL = "https://" + HOST_NAME + ":" + HTTPS_PORT + "/" + CONTEXT_ROOT + "/services/";
                }
            }
            if (STRATOS.equalsIgnoreCase("true")) {
                TENANT_NAME = (prop.getProperty("tenant.name"));
                CARBON_HOME = relativePath + File.separator + "lib" + File.separator + "stratos-artifacts";
                if (CONTEXT_ROOT == null) {
                    SERVICE_URL = "https://" + HOST_NAME + "/services/";
                } else {
                    SERVICE_URL = "https://" + HOST_NAME + "/" + CONTEXT_ROOT + "/services/";
                }
                TRUSTSTORE_PATH = (prop.getProperty("truststore.path", FrameworkSettings.CARBON_HOME + File.separator + "wso2carbon.jks"));
            }

            String clientTrustStorePath = FrameworkSettings.TRUSTSTORE_PATH;
            System.setProperty("javax.net.ssl.trustStore", clientTrustStorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", FrameworkSettings.TRUSTSTORE_PASSWORD);
            System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Cannot close InputStream to framework.properties file", e);
                }
            }
        }
    }

    public static String getFrameworkPath() {
        String relativePath = null;
        int pathIndex;
        try {
            File filePath = new File("./");
            relativePath = filePath.getCanonicalPath();
            if (relativePath.contains("component-test-framework")) {
                pathIndex = relativePath.indexOf("component-test-framework");
                relativePath = relativePath.substring(0, pathIndex);
                relativePath = relativePath + "component-test-framework";
            } else {
                relativePath = filePath.getCanonicalPath();
            }
        } catch (Exception e) {
            log.error("Exception occurred while calculating relative path: " + e.getMessage(), e);
        }
        return relativePath;
    }


}