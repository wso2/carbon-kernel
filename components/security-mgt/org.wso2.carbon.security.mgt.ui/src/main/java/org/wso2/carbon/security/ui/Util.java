/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security.ui;

import org.wso2.carbon.security.mgt.stub.keystore.xsd.CertData;
import org.wso2.carbon.security.mgt.stub.keystore.xsd.KeyStoreData;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;

import java.util.ArrayList;
import java.util.List;

public class Util {

    private Util(){}

    public static CertData[] doPaging(int pageNumber, CertData[] certDataSet) {

        int itemsPerPageInt = SecurityUIConstants.DEFAULT_ITEMS_PER_PAGE;
        int startIndex = pageNumber * itemsPerPageInt;
        int endIndex = (pageNumber + 1) * itemsPerPageInt;
        CertData[] returnedCertDataSet = new CertData[itemsPerPageInt];

        for (int i = startIndex, j = 0; i < endIndex && i < certDataSet.length; i++, j++) {
            returnedCertDataSet[j] = certDataSet[i];
        }

        return returnedCertDataSet;
    }

    public static KeyStoreData[] doPaging(int pageNumber, KeyStoreData[] keyStoreDataSet) {

        int itemsPerPageInt = SecurityUIConstants.KEYSTORE_DEFAULT_ITEMS_PER_PAGE;
        int startIndex = pageNumber * itemsPerPageInt;
        int endIndex = (pageNumber + 1) * itemsPerPageInt;
        KeyStoreData[] returnedDataSet = new KeyStoreData[itemsPerPageInt];

        for (int i = startIndex, j = 0; i < endIndex && i < keyStoreDataSet.length; i++, j++) {
            returnedDataSet[j] = keyStoreDataSet[i];
        }

        return returnedDataSet;
    }

    public static FlaggedName[] doFlaggedNamePaging(int pageNumber, FlaggedName[] flaggedName) {

        int itemsPerPageInt = SecurityUIConstants.DEFAULT_ITEMS_PER_PAGE;
        FlaggedName[] returnedFlaggedNameSet;

        int startIndex = pageNumber * itemsPerPageInt;
        int endIndex = (pageNumber + 1) * itemsPerPageInt;
        if (itemsPerPageInt < flaggedName.length - 1) {
            returnedFlaggedNameSet = new FlaggedName[itemsPerPageInt];
        } else {
            returnedFlaggedNameSet = new FlaggedName[flaggedName.length - 1];
        }
        for (int i = startIndex, j = 0; i < endIndex && i < flaggedName.length - 1; i++, j++) {
            returnedFlaggedNameSet[j] = flaggedName[i];
        }

        return returnedFlaggedNameSet;
    }

    public static KeyStoreData[] doFilter(String filter, KeyStoreData[] keyStoreDataSet) {
        String regPattern = filter.replace("*", ".*");
        List<KeyStoreData> list = new ArrayList<>();

        for (KeyStoreData keyStore : keyStoreDataSet) {
            if (keyStore != null && keyStore.getKeyStoreName().toLowerCase().matches(regPattern.toLowerCase())) {
                list.add(keyStore);
            }
        }

        return list.toArray(new KeyStoreData[list.size()]);
    }

    public static CertData[] doFilter(String filter, CertData[] certDataSet) {

        if (certDataSet == null || certDataSet.length == 0) {
            return new CertData[0];
        }

        String regPattern = filter.replace("*", ".*");
        List<CertData> list = new ArrayList<>();

        for (CertData cert : certDataSet) {
            if (cert != null
                    && cert.getAlias().toLowerCase()
                    .matches(regPattern.toLowerCase())) {
                list.add(cert);
            }
        }

        return list.toArray(new CertData[list.size()]);
    }
}
