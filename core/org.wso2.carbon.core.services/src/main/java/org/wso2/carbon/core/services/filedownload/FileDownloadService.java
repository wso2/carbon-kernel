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
package org.wso2.carbon.core.services.filedownload;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.common.IFileDownload;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.File;
import java.util.Map;

public class FileDownloadService extends AbstractAdmin implements IFileDownload {

    public DataHandler downloadFile(String id) {
        ConfigurationContext configurationContext = getConfigContext();
        Map fileResourceMap =
                (Map) configurationContext.getProperty(ServerConstants.FILE_RESOURCE_MAP);
        String filePath = (String) fileResourceMap.get(id);
        if (filePath != null) {
            File file = new File(filePath);
            DataSource dataSource = new FileDataSource(file);
            return new DataHandler(dataSource);
        }
        return null;
    }
}
