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
package org.wso2.carbon.core.util;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

public class KeyStoreUtil {

    /**
     * KeyStore name will be here.
     * 
     * @param store - keyStore
     * @return
     */
    public static String getPrivateKeyAlias(KeyStore store) throws Exception {
        String alias = null;
        Enumeration<String> enums = store.aliases();
        while(enums.hasMoreElements()){
            String name = enums.nextElement();
            if(store.isKeyEntry(name)){
                alias = name;
                break;
            }
        }
        return alias;
    }

    public static String getKeyStoreFileName(String fullName) {
        ServerConfigurationService config =
                CarbonCoreDataHolder.getInstance().getServerConfigurationService();
        String fileName = config
                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE);
        String name = null;
        int index = fileName.lastIndexOf('/');
        if (index != -1) {
            name = fileName.substring(index + 1);
        } else {
            index = fileName.lastIndexOf(File.separatorChar);
            if (index != -1) {
                name = fileName.substring(fileName.lastIndexOf(File.separatorChar));
            } else {
                name = fileName;
            }
        }
        return name;
    }

    public static boolean isPrimaryStore(String id) {
        ServerConfigurationService config =
                CarbonCoreDataHolder.getInstance().getServerConfigurationService();
        String fileName = config
                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE);
        int index = fileName.lastIndexOf('/');
        if (index != -1) {
            String name = fileName.substring(index + 1);
            if (name.equals(id)) {
                return true;
            }
        } else {
            index = fileName.lastIndexOf(File.separatorChar);
            String name = null;
            if (index != -1) {
                name = fileName.substring(fileName.lastIndexOf(File.separatorChar));
            } else {
                name = fileName;
            }

            if (name.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static Certificate getCertificate(String alias, KeyStore store) throws AxisFault {
        try {
            Enumeration enumeration = store.aliases();
            while (enumeration.hasMoreElements()) {
                String itemAlias = (String) enumeration.nextElement();
                if (itemAlias.equals(alias)) {
                    return store.getCertificate(alias);
                }
            }
            return null;
        } catch (Exception e) {
            String msg = "Could not read certificates from keystore file. ";
            throw new AxisFault(msg + e.getMessage());
        }
    }

}
