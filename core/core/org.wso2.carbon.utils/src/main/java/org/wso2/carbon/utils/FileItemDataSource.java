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
package org.wso2.carbon.utils;

import org.apache.commons.fileupload.FileItem;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileItemDataSource implements DataSource {

    private FileItem fileItem;

    public FileItemDataSource(FileItem fileItem) {
        this.fileItem = fileItem;
    }

    public String getContentType() {
        return this.fileItem.getContentType();
    }

    public InputStream getInputStream() throws IOException {
        return this.fileItem.getInputStream();
    }

    public String getName() {
        return this.fileItem.getName();
    }

    public OutputStream getOutputStream() throws IOException {
        return this.fileItem.getOutputStream();
    }

    public void delete() {
        this.fileItem.delete();
    }
}