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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUploadService extends AbstractAdmin implements IFileUpload {

    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';

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
                String fileName = getFileName(fileItem.getFileName());
                verifyCanonicalDestination(extraFileLocation, dirs, fileName);
                File uploadedFile = new File(extraFileLocation, fileName);
                if ((fileName == null || fileName.length() == 0) && multiItems) {
                    continue;
                }
                try (FileOutputStream fileOutputStream = new FileOutputStream(uploadedFile)) {
                    fileItem.getDataHandler().writeTo(fileOutputStream);
                }
                if (fileResourceMap == null) {
                    fileResourceMap = new HashMap();
                    configurationContext.setProperty(ServerConstants.FILE_RESOURCE_MAP, fileResourceMap);
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

    private void verifyCanonicalDestination(String extraFileLocation, File dirs, String fileName) throws IOException {

        String canonicalDestinationDirPath = dirs.getCanonicalPath();
        File destinationFile = new File(extraFileLocation, fileName);
        String canonicalDestinationFile = destinationFile.getCanonicalPath();

        if (!canonicalDestinationFile.startsWith(canonicalDestinationDirPath + File.separator)) {
            throw new AxisFault(String.format("File path of %s is outside the allowed upload directory", fileName));
        }
    }

    private String getFileName(String fileName) {

        if (fileName == null) {
            return null;
        }
        return requireNonNullChars(fileName).substring(indexOfLastSeparator(fileName) + 1);
    }

    private int indexOfLastSeparator(final String fileName) {

        if (fileName == null) {
            return -1;
        }
        final int lastUnixPos = fileName.lastIndexOf(UNIX_SEPARATOR);
        final int lastWindowsPos = fileName.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    private String requireNonNullChars(final String path) {

        if (path.indexOf(0) >= 0) {
            throw new IllegalArgumentException(
                    "Null byte present in file/path name. There are no known legitimate use cases for such data, " +
                            "but several injection attacks may use it");
        }
        return path;
    }
}
