/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.core.servlet;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.ResourceImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

@Deprecated
public class FileUploadUtil {

    public static ResourceImpl processUpload(HttpServletRequest req)
            throws IOException, ServletException {
        RequestContext reqContext = new ServletRequestContext(req);
        boolean isMultipart = ServletFileUpload.isMultipartContent(reqContext);
        if (isMultipart) {
            ResourceImpl resource = new ResourceImpl();
            try {
                //Create a factory for disk-based file items
                FileItemFactory factory = new DiskFileItemFactory();
                //Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);
                List items = upload.parseRequest(req);
                // Process the uploaded items
                Iterator iter = items.iterator();

                String parentPath = "";
                String resourceName = "";

                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();

                    if (item.isFormField()) {
                        if (item.getFieldName().equalsIgnoreCase("path")) {
                            parentPath = item.getString();
                        } else if (item.getFieldName().equalsIgnoreCase("filename")) {
                            resourceName = item.getString();
                        } else if (item.getFieldName().equalsIgnoreCase("description")) {
                            resource.setDescription(item.getString());
                        } else if (item.getFieldName().equalsIgnoreCase("mediaType")) {
                            resource.setMediaType(item.getString());
                        }
                    } else {
                        InputStream in = item.getInputStream();

                        //ByteArrayOutputStream out = new ByteArrayOutputStream();
                        //byte[] buffer = new byte[1024];
                        //int c;
                        //while ((c = in.read(buffer)) != -1) {
                        //    out.write(buffer, 0, c);
                        //}
                        //out.flush();

                        resource.setContentStream(in);
                    }

                    resource.setPath(calcualtePath(parentPath, resourceName));
                }
            } catch (Exception e) {
                req.setAttribute("status", "failure");
                req.setAttribute("cause", e.getMessage());
            }
            return resource;
        }
        return null;
    }

    private static String calcualtePath(String parentPath, String resourceName) {
        String resourcePath;
        if (!parentPath.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            parentPath = RegistryConstants.PATH_SEPARATOR + parentPath;
        }
        if (parentPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            resourcePath = parentPath + resourceName;
        } else {
            resourcePath = parentPath + RegistryConstants.PATH_SEPARATOR + resourceName;
        }
        return resourcePath;
    }
}
