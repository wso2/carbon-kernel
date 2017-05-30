/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.carbon.utils.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;

/**
 * A utility that allows importing a certificate from one keystore to another
 */
public class KeyImporter {

    private static Log log = LogFactory.getLog(KeyImporter.class);

    /**
     * sourcekeystore sourceStorepass keyalias targetstore targetStorePass
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {

        if(log.isDebugEnabled()){
            log.debug("Importing certificate ...");
        }

        if (args.length != 5) {
            throw new Exception("Incorrect number of parameters");
        }

        String sourceStorePath = args[0];
        String sourceStorePass = args[1];
        String keyAlias = args[2];
        String targetStorePath = args[3];
        String targetStorePass = args[4];

        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(targetStorePath).getAbsolutePath());
             FileInputStream fis = new FileInputStream(new File(sourceStorePath).getAbsolutePath());) {

            KeyStore sourceStore = KeyStore.getInstance("JKS");
            sourceStore.load(fis, sourceStorePass.toCharArray());

            Certificate cert = sourceStore.getCertificateChain(keyAlias)[0];
            KeyStore targetStore = KeyStore.getInstance("JKS");

            File targetStoreFile = new File(targetStorePath);
            if (targetStoreFile.exists()) {
                targetStore.load(new FileInputStream(targetStoreFile.getAbsolutePath()), targetStorePass.toCharArray());
            } else {
                targetStore.load(null, null);
            }
            targetStore.setCertificateEntry(keyAlias, cert);
            targetStore.store(fileOutputStream, targetStorePass.toCharArray());

            fileOutputStream.flush();
            if (log.isDebugEnabled()) {
                log.debug("Importing certificate ... DONE !");
            }
        } catch (Exception e) {
            log.error("Importing of key failed");
            throw e;

        }
    }
}
