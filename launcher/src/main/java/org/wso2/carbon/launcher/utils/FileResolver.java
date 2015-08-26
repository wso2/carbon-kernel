/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.launcher.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * TODO: class level comment
 */
public class FileResolver {

    /**
     * Request: file:org.eclipse.osgi_3.9.1.v20130814-1242.jar
     * Response:
     * file:/user/wso2carbon-kernel-5.0.0/repository/components/plugins/org.eclipse.osgi_3.9.1.v20130814-1242.jar
     *
     * @param path       path to resolve
     * @param parentPath parent path
     * @return resolved URL path
     */
    public static URL resolve(String path, String parentPath) {
        if (path.contains(":") && !path.startsWith("file:")) {
            throw new RuntimeException("URLs other than file URLs are not supported.");
        }

        String relativeFilePath = path;
        if (path.startsWith("file:")) {
            relativeFilePath = path.substring(5);
        }

        File file = new File(relativeFilePath);
        if (file.isAbsolute()) {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            file = new File(parentPath, relativeFilePath);
            if (file.isAbsolute()) {
                try {
                    return file.toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return null;
    }
}
