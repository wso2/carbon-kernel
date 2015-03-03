/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied. See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.integration.common.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.ContextUrls;
import org.wso2.carbon.integration.common.exception.CarbonToolsIntegrationTestException;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(CarbonIntegrationBaseTest.class);
    protected ContextUrls contextUrls = new ContextUrls();
    protected AutomationContext automationContext;
    protected TestUserMode userMode;

    protected void init() throws XPathExpressionException {
        userMode = TestUserMode.SUPER_TENANT_ADMIN;
        init(userMode);
    }

    protected void init(TestUserMode userMode) throws XPathExpressionException {
        automationContext = new AutomationContext(CarbonIntegrationConstants.PRODUCT_GROUP, userMode);
        contextUrls = automationContext.getContextUrls();
    }

    /**
     * Copy folders from a source to a destination
     *
     * @param source      - source file
     * @param destination - destination file
     * @throws CarbonToolsIntegrationTestException
     */
    public void copyFolder(File source, File destination)
            throws CarbonToolsIntegrationTestException {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                boolean folderCreated = destination.mkdir();
                log.info("[Status ]" + folderCreated + " Directory copied from " + source + "  to " + destination);
            }
            String files[] = source.list();
            if (files != null) {
                for (String file : files) {
                    File sourceFile = new File(source, file);
                    File destinationFile = new File(destination, file);
                    copyFolder(sourceFile, destinationFile);
                }
            }

        } else {
            //if file, then copy it
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new FileInputStream(source);
                out = new FileOutputStream(destination);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

            } catch (IOException ex) {
                log.error("Error while copying folder " + ex.getMessage());
                throw new CarbonToolsIntegrationTestException("Error while copying folder ", ex);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    log.warn("Unable to closing in and out streams");
                }
            }
            log.info("File copied from " + source + " to " + destination);
        }
    }
}
