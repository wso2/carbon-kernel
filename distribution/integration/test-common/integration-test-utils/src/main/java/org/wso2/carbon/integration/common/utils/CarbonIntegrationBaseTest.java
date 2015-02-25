/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.beans.ContextUrls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class CarbonIntegrationBaseTest {

    protected ContextUrls contextUrls = new ContextUrls();
    protected AutomationContext automationContext;
    protected TestUserMode userMode;
    private static final Log log = LogFactory.getLog(CarbonIntegrationBaseTest.class);

    protected void init() throws Exception {
        userMode = TestUserMode.SUPER_TENANT_ADMIN;
        init(userMode);
    }

    protected void init(TestUserMode userMode) throws Exception {
        automationContext = new AutomationContext("CARBON", userMode);
        contextUrls = automationContext.getContextUrls();

    }


    public void copyFolder(File src, File dest)
            throws IOException {
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
                log.info("Directory copied from " + src + "  to " + dest);
            }
            String files[] = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                copyFolder(srcFile, destFile);
            }

        } else {
            //if file, then copy it
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
            log.info("File copied from " + src + " to " + dest);
        }
    }
}
