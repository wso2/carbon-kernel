/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.securevault;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.securevault.SecurityConstants;
import org.wso2.securevault.secret.SecretCallbackHandler;
import org.wso2.securevault.secret.SecretCallbackHandlerFactory;
import org.wso2.securevault.secret.SecretManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 */
public class SecretManagerInitializer {

    private SecretManager secretManager = SecretManager.getInstance();
    private static final Log log = LogFactory.getLog(SecretManagerInitializer.class);
    public static final String CARBON_HOME = "carbon.home";    
    private String SECRET_CONF = "secret-conf.properties";
    private static String CONF_DIR = "conf";
    private static String REPOSITORY_DIR = "repository";
    private static final String SECURITY_DIR = "security";
    private static String GLOBAL_PREFIX = "carbon.";

    public SecretCallbackHandlerServiceImpl init() {

        Properties properties = new Properties();

        if (secretManager.isInitialized()) {
            if (log.isDebugEnabled()) {
                log.debug("SecretManager already has been initialized.");
            }
        } else {
            properties = loadProperties();
            secretManager.init(properties);
        }

        SecretCallbackHandlerServiceImpl serviceImpl = null;

        if (!secretManager.isInitialized()) {

            SecretCallbackHandler passwordProvider =
                    SecretCallbackHandlerFactory.createSecretCallbackHandler(properties,
                            GLOBAL_PREFIX + SecurityConstants.PASSWORD_PROVIDER_SIMPLE);

            if (passwordProvider != null) {
                serviceImpl = new SecretCallbackHandlerServiceImpl();
                serviceImpl.setSecretCallbackHandler(passwordProvider);

            }
        }

        if (serviceImpl == null) {
            serviceImpl = new SecretCallbackHandlerServiceImpl();
            serviceImpl.setSecretCallbackHandler(
                    new SecretManagerSecretCallbackHandler(secretManager));
        }

        return serviceImpl;
    }


    private Properties loadProperties() {
        Properties properties = new Properties();
        String carbonHome = System.getProperty(CARBON_HOME);
        String filePath = carbonHome + File.separator + REPOSITORY_DIR + File.separator +
                          CONF_DIR + File.separator + SECURITY_DIR+ File.separator + SECRET_CONF;

        File dataSourceFile = new File(filePath);
        if (!dataSourceFile.exists()) {
            return properties;
        }

        InputStream in = null;
        try {
            in = new FileInputStream(dataSourceFile);
            properties.load(in);
        } catch (IOException e) {
            String msg = "Error loading properties from a file at :" + filePath;
            log.warn(msg, e);
            return properties;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {

                }
            }
        }
        return properties;
    }
}
