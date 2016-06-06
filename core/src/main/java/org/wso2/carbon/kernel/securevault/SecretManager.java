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

package org.wso2.carbon.kernel.securevault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by nipuni on 6/6/16.     //todo
 */
public class SecretManager {

    private static final Logger logger = LoggerFactory.getLogger(SecretManager.class);
    //True , if secret manage has been started up properly
    private boolean initialized = false;


    /**
     * Initializes the Secret Manager by providing configuration properties
     *
     * @param serverConfigurationFile Path to configuration file.
     */
    public void init(Path serverConfigurationFile) {
        //we only initialize secretManager once.
        if (initialized) {
            if (logger.isDebugEnabled()) {
                logger.debug("Secret Manager already has been initialized.");
            }
            return;
        }

        if (!Files.exists(serverConfigurationFile)) {
            if (logger.isDebugEnabled()) {
                logger.debug("KeyStore configuration cannot be loaded");
            }
            return;
        }

        InputStream fileInputStream = null;

        Yaml secretManagerConfiguration = new Yaml();
        try {
            fileInputStream = new FileInputStream(serverConfigurationFile.toString());
            Map<String, Object> configuration = (Map<String, Object>) secretManagerConfiguration.load(fileInputStream);
            if (configuration == null || configuration.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Configuration properties can not be loaded form : " +
                            serverConfigurationFile.toString());
                }
                return; //todo throw exception
            }

//        globalSecretProvider = MiscellaneousUtil.getProperty(configuration, PROP_SECRET_PROVIDER,null);
//        if(globalSecretProvider==null || "".equals(globalSecretProvider)){
//            if(logger.isDebugEnabled()){
//                logger.debug("No global secret provider is configured.");
//            }
//        }
            Map<String, Object> keyStorConfig = (Map<String, Object>) configuration.get("keystore");
            Map<String, Object> secretRepositories = (Map<String, Object>) configuration.get("secretRepositories");
            //todo support multiple repos
            if (secretRepositories.isEmpty()) {
                logger.debug("No secret repositories have been configured");
                return;
            }

//        //Create a KeyStore Information  for private key entry KeyStore
//        IdentityKeyStoreInformation identityInformation =
//                KeyStoreInformationFactory.createIdentityKeyStoreInformation(properties);
//
//        // Create a KeyStore Information for trusted certificate KeyStore
//        TrustKeyStoreInformation trustInformation =
//                KeyStoreInformationFactory.createTrustKeyStoreInformation(properties);
//
//        String identityKeyPass = null;
//        String identityStorePass = null;
//        String trustStorePass = null;
//        if(identityInformation != null){
//            identityKeyPass = identityInformation
//                    .getKeyPasswordProvider().getResolvedSecret();
//            identityStorePass = identityInformation
//                    .getKeyStorePasswordProvider().getResolvedSecret();
//        }
//
//        if(trustInformation != null){
//            trustStorePass = trustInformation
//                    .getKeyStorePasswordProvider().getResolvedSecret();
//        }
//
//
//        if (!validatePasswords(identityStorePass, identityKeyPass, trustStorePass)) {
//            if (logger.isDebugEnabled()) {
//                logger.info("Either Identity or Trust keystore password is mandatory" +
//                        " in order to initialized secret manager.");
//            }
//            return;
//        }
//
//        IdentityKeyStoreWrapper identityKeyStoreWrapper = new IdentityKeyStoreWrapper();
//        identityKeyStoreWrapper.init(identityInformation, identityKeyPass);
//
//        TrustKeyStoreWrapper trustKeyStoreWrapper = new TrustKeyStoreWrapper();
//        if(trustInformation != null){
//            trustKeyStoreWrapper.init(trustInformation);
//        }
//
            for (Map.Entry<String, Object> entry : secretRepositories.entrySet()) {
                SecretRepository secretRepository = new FileBaseSecretRepository(); //todo should be able to extend
                LinkedHashMap<String, String> values = (LinkedHashMap<String, String>) entry.getValue();
                secretRepository.setLocation(values.get("location"));
                secretRepository.setProvider(values.get("provider"));
                secretRepository.init(keyStorConfig);
            }


        } catch (FileNotFoundException e) {
            logger.error("No file found"); //todo
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Error while closing inputstream for : " +
                        serverConfigurationFile.toString());
            }
        }

        initialized = true;
    }
}
