/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.core.services.fileupload;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.common.IFileUpload;
import org.wso2.carbon.core.common.UploadedFileItem;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUploadService extends AbstractAdmin implements IFileUpload {
    public String[] uploadFiles(UploadedFileItem[] uploadedFileItems) throws Exception {

        try {
            ConfigurationContext configurationContext = getConfigContext();
            Map fileResourceMap =
                    (Map) configurationContext.getProperty(
                            ServerConstants.FILE_RESOURCE_MAP);
            boolean multiItems = false;
            if (uploadedFileItems.length > 1) {
                multiItems = true;
            }
            List<String> uuidList = new ArrayList<String>(uploadedFileItems.length);
            for (UploadedFileItem fileItem : uploadedFileItems) {
                String uuid = String.valueOf(System.currentTimeMillis() + Math.random());
                String extraFileLocation =
                        configurationContext.getProperty(ServerConstants.WORK_DIR) +
                                File.separator + "extra" +
                                File.separator + uuid + File.separator;

                File dirs = new File(extraFileLocation);
                if (!dirs.exists()) {
                    dirs.mkdirs();
                }
                File uploadedFile = new File(extraFileLocation,
                                             getFileName(fileItem.getFileName()));
                String fileName = fileItem.getFileName();
                if ((fileName == null || fileName.length() == 0) && multiItems) {
                    continue;
                }
                try (FileOutputStream fileOutputStream = new FileOutputStream(uploadedFile)) {
                    fileItem.getDataHandler().writeTo(fileOutputStream);
                }
                if (fileResourceMap == null) {
                    fileResourceMap = new HashMap();
                    configurationContext.setProperty(ServerConstants.FILE_RESOURCE_MAP,
                                                     fileResourceMap);
                }
                fileResourceMap.put(uuid, uploadedFile.getAbsolutePath());
                uuidList.add(uuid);
            }

            String[] uuidArray = new String[uuidList.size()];
            uuidArray = uuidList.toArray(uuidArray);
            return uuidArray;
        } catch (Exception e) {
            throw new AxisFault("Error occurred while uploading service artifacts", e);
        }
    }

    private String getFileName(String fileName) {
        String fileNameOnly;
        if (fileName.indexOf("\\") < 0) {
            fileNameOnly = fileName.substring(fileName.lastIndexOf('/') + 1,
                                              fileName.length());
        } else {
            fileNameOnly = fileName.substring(fileName.lastIndexOf("\\") + 1,
                                              fileName.length());
        }
        return fileNameOnly;
    }
}
