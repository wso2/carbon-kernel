/*
 * Copyright (c) 2010, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.core.keystore.util;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.internal.KeyStoreManagerDataHolder;
import org.wso2.carbon.core.keystore.KeyStoreManagementException;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.WSO2Constants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.Map;

/**
 * This class includes Key Store management utility functions.
 */
public class KeyStoreMgtUtil {

    private static final Log log = LogFactory.getLog(KeyStoreMgtUtil.class);

    private KeyStoreMgtUtil(){}

    /**
     * Dumping the generated pub. cert to a file
     *
     * @param configurationContext
     * @param cert                 content of the certificate
     * @param fileName             file name
     * @return file system location of the pub. cert
     */
    public static String dumpCert(ConfigurationContext configurationContext, byte[] cert,
                                  String fileName) {
        if (!verifyCertExistence(fileName, configurationContext)) {
            String workDir = (String) configurationContext.getProperty(ServerConstants.WORK_DIR);
            File pubCert = new File(workDir + File.separator + "pub_certs");

            if (fileName == null) {
                fileName = String.valueOf(System.currentTimeMillis() + new SecureRandom().nextDouble()) + ".cert";
            }
            if (!pubCert.exists()) {
                pubCert.mkdirs();
            }

            String filePath = workDir + File.separator + "pub_certs" + File.separator + fileName;
            OutputStream outStream = null;
            try {
                outStream = new FileOutputStream(filePath);
                outStream.write(cert);
            } catch (Exception e) {
                String msg = "Error when writing the public certificate to a file";
                log.error(msg);
                throw new SecurityException("msg", e);
            } finally {
                KeyStoreIOStreamUtils.flushOutputStream(outStream);
                KeyStoreIOStreamUtils.closeOutputStream(outStream);
            }

            Map fileResourcesMap = (Map) configurationContext.getProperty(WSO2Constants.FILE_RESOURCE_MAP);
            if (fileResourcesMap == null) {
                fileResourcesMap = new Hashtable();
                configurationContext.setProperty(WSO2Constants.FILE_RESOURCE_MAP, fileResourcesMap);
            }

            fileResourcesMap.put(fileName, filePath);
        }
        return WSO2Constants.ContextPaths.DOWNLOAD_PATH + "?id=" + fileName;
    }

    /**
     * Check whether the certificate is available in the file system
     *
     * @param fileName             file name
     * @param configurationContext configuration context of the current message
     */
    private static boolean verifyCertExistence(String fileName, ConfigurationContext configurationContext) {
        String workDir = (String) configurationContext.getProperty(ServerConstants.WORK_DIR);
        String filePath = workDir + File.separator + "pub_certs" + File.separator + fileName;
        File pubCert = new File(workDir + File.separator + "pub_certs" + File.separator + fileName);

        //if cert is still available then exit
        if (pubCert.exists()) {
            Map fileResourcesMap = (Map) configurationContext.getProperty(WSO2Constants.FILE_RESOURCE_MAP);
            if (fileResourcesMap == null) {
                fileResourcesMap = new Hashtable();
                configurationContext.setProperty(WSO2Constants.FILE_RESOURCE_MAP, fileResourcesMap);
            }
            if (fileResourcesMap.get(fileName) == null) {
                fileResourcesMap.put(fileName, filePath);
            }
            return true;
        }
        return false;
    }

    /**
     * Get the tenant UUID for the given tenant ID.
     * @param tenantId Tenant ID
     * @return Tenant UUID
     * @throws KeyStoreManagementException If an error occurs while getting the tenant UUID.
     * @throws UserStoreException If an error occurs while getting the tenant UUID.
     */
    public static String getTenantUUID(int tenantId) throws KeyStoreManagementException, UserStoreException {

        // Super tenant does not have a tenant UUID. Therefore, set a hard coded value.
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            // Set a hard length of 36 characters for super tenant ID.
            // This is to avoid the database column length constraint violation.
            return String.format("%1$-36d", tenantId);
        }

        if (tenantId != MultitenantConstants.INVALID_TENANT_ID) {
            TenantManager tenantManager = KeyStoreManagerDataHolder.getRealmService().getTenantManager();
            Tenant tenant = tenantManager.getTenant(tenantId);
            return tenant.getTenantUniqueID();
        }

        throw new KeyStoreManagementException("Invalid tenant id: " + tenantId);
    }
}
