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

package org.wso2.carbon.security.ui.jsp;

import org.apache.axis2.builder.DiskFileDataSource;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.security.ui.ServiceHolder;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.util.List;

public class SecurityUIUtil {

    private static String url = null;

    private SecurityUIUtil() {
    }

    public static String getUrl() throws Exception {

        if (url == null) {
            ServiceHolder serviceHodler = ServiceHolder.getInstance();
            RegistryService regService = serviceHodler.getRegistryService();
            Registry systemRegistry = regService.getConfigSystemRegistry();
            Resource resource = systemRegistry.get("/carbon/connection/props");
            String servicePath = resource.getProperty("service-path");
            String contextRoot = resource.getProperty("context-root");

            String host = resource.getProperty("host-name");
            contextRoot = StringUtils.equals("/", contextRoot) ? "" : contextRoot;

            host = (host == null) ? "localhost" : host;
            String port = System.getProperty("carbon.https.port");
            StringBuilder urlValue = new StringBuilder();
            url = (urlValue.append("https://").append(host).append(":").append(port).append("/").append(contextRoot).append(servicePath).append("/")).toString();
        }

        return url;
    }

    public static List parseRequest(ServletRequestContext requestContext)
            throws FileUploadException {
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        return upload.parseRequest(requestContext);
    }

    public static String getTextParameter(DiskFileItem diskFileItem, String characterEncoding)
            throws Exception {
        String encoding = diskFileItem.getCharSet();
        if (encoding == null) {
            encoding = characterEncoding;
        }
        String textValue;
        if (encoding == null) {
            textValue = new String(diskFileItem.get());
        } else {
            textValue = new String(diskFileItem.get(), encoding);
        }
        return textValue;
    }

    public static DataHandler getFileParameter(DiskFileItem diskFileItem) throws Exception {
        DataSource dataSource = new DiskFileDataSource(diskFileItem);
        DataHandler dataHandler = new DataHandler(dataSource);
        return dataHandler;
    }

}
