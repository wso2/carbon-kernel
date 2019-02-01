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
package org.wso2.carbon.registry.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

/**
 * Implementation of a servlet to handle requests having /registry/resource/. This will provide the
 * raw resource so that it could be accessed as via a valid URL.
 */
public class ResourceServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(ResourceServlet.class);

    /**
     * Logic that will be executed for a get request.
     *
     * @param request  the HTTP Servlet request.
     * @param response the HTTP Servlet response.
     *
     * @throws IOException if an error occurred.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String uri = request.getRequestURI();
            int idx = uri.indexOf("resource");
            String path = uri.substring(idx + 8);
            if (path == null) {
                String msg = "Could not get the resource content. Path is not specified.";
                log.error(msg);
                response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
                return;
            }

            Resource resource;
            try {
                UserRegistry registry = Utils.getRegistry(request);
                try {
                    path = new URI(path).normalize().toString();
                } catch (URISyntaxException e) {
                    log.error("Unable to normalize requested resource path: " + path, e);
                }
                String decodedPath = URLDecoder.decode(path, RegistryConstants.DEFAULT_CHARSET_ENCODING);

                CurrentSession.setUserRealm(registry.getUserRealm());
                CurrentSession.setUser(registry.getUserName());
                try {
                    if (!AuthorizationUtils.authorize(RegistryUtils.getAbsolutePath(
                            registry.getRegistryContext(), decodedPath),
                            ActionConstants.GET)) {
                        response.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
                        response.setHeader("WWW-Authenticate", "Basic realm=\"WSO2-Registry\"");
                        return;
                    }
                    resource = registry.get(decodedPath);
                } finally {
                    CurrentSession.removeUserRealm();
                    CurrentSession.removeUser();
                }
            } catch (AuthorizationFailedException e) {
                log.error(e.getMessage());
                response.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
                response.setHeader("WWW-Authenticate", "Basic realm=\"WSO2-Registry\"");
                return;
            } catch (RegistryException e) {
                String msg = "Error retrieving the resource " + path + ". " + e.getMessage();
                log.error(msg, e);
                throw e;
            }

            if (resource instanceof Collection) {
                String msg = "Could not get the resource content. Path " + path +
                        " refers to a collection.";
                log.error(msg);
                response.setStatus(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
                return;
            }

            // date based conditional get
            long ifModifiedSinceValue = request.getDateHeader("If-Modified-Since");
            long lastModifiedValue = resource.getLastModified().getTime();
            if (ifModifiedSinceValue > 0) {
                // convert the time values from milliseconds to seconds
                ifModifiedSinceValue /= 1000;
                lastModifiedValue /= 1000;

                /* condition to check we have latest updates in terms of dates */
                if (ifModifiedSinceValue >= lastModifiedValue) {
                    /* no need to response with data */
                    response.setStatus(HttpURLConnection.HTTP_NOT_MODIFIED);
                    return;
                }
            }
            response.setDateHeader("Last-Modified", lastModifiedValue);

            // eTag based conditional get
            String ifNonMatchValue = request.getHeader("if-none-match");
            String currentETag = Utils.calculateEntityTag(resource);
            if (ifNonMatchValue != null) {
                if (ifNonMatchValue.equals(currentETag)) {
                    /* the version is not modified */
                    response.setStatus(HttpURLConnection.HTTP_NOT_MODIFIED);
                    return;
                }
            }
            response.setHeader("ETag", currentETag);
            if (resource.getMediaType() != null && resource.getMediaType().length() > 0) {
                response.setContentType(resource.getMediaType());
            } else {
                response.setHeader(
                        "Content-Disposition",
                        "attachment; filename=" + RegistryUtils.getResourceName(path));
                response.setContentType("application/download");
            }

            InputStream contentStream = null;
            if (resource.getContent() != null) {
                contentStream = resource.getContentStream();
            }
            if (contentStream != null) {

                try {
                    ServletOutputStream servletOutputStream = response.getOutputStream();
                    byte[] contentChunk = new byte[RegistryConstants.DEFAULT_BUFFER_SIZE];
                    int byteCount;
                    while ((byteCount = contentStream.read(contentChunk)) != -1) {
                        servletOutputStream.write(contentChunk, 0, byteCount);
                    }

                } finally {
                    contentStream.close();
                }

            } else {
                Object content = resource.getContent();
                if (content != null) {

                    if (content instanceof byte[]) {
                        ServletOutputStream servletOutputStream = response.getOutputStream();
                        servletOutputStream.write((byte[]) content);
                        response.flushBuffer();
                        servletOutputStream.flush();
                    } else {
                        PrintWriter writer = response.getWriter();
                        writer.write(content.toString());
                        writer.flush();
                    }
                }
            }

            resource.discard();

        } catch (RegistryException e) {
            String msg = "Failed to get resource content. " + e.getMessage();
            log.error(msg, e);
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

}
