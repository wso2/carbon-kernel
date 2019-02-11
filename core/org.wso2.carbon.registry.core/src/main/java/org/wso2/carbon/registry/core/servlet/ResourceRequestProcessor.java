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

import org.apache.axiom.om.util.Base64;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.servlet.utils.Utils;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.Date;

@Deprecated
public final class ResourceRequestProcessor {

    private ResourceRequestProcessor() {
    }

    public static void processResourceGET(
            HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {

        Resource resource;
        try {
            // if the client has sent "Authorization" header, log in as the new user.
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.length() > 0) {
                String[] aParts = auth.trim().split(" ");
                if (aParts.length == 2) {
                    String credentials = aParts[1];
                    String decodedCredentials = new String(Base64.decode(credentials));
                    String[] cParts = decodedCredentials.trim().split(":");
                    if (cParts.length == 2) {
                        String userName = cParts[0];
                        String password = cParts[1];
                        Utils.logInUser(request, userName, password);
                    }
                }
            }

            resource = ResourceRequestProcessor.getResource(request, path);

        } catch (AuthorizationFailedException ae) {

            response.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);

            // if the user is not logged in, give him a challange
            if (!Utils.isLoggedIn(request)) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"WSO2-Registry\"");
            }

            try {
                response.getWriter().flush();
            } catch (IOException e) {
            }

            return;

        } catch (RegistryException e) {
            request.getSession().setAttribute(RegistryConstants.ERROR_MESSAGE, e.getMessage());
            request.getRequestDispatcher(RegistryConstants.ERROR_JSP);
            return;
        }

        if (resource == null) {
            response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
            request.getSession().setAttribute(RegistryConstants.ERROR_MESSAGE, "404 Not Found");
            request.getRequestDispatcher(RegistryConstants.ERROR_JSP);
            return;
        }

        // handle if-modified-since header
        long modifiedSince = request.getDateHeader("If-Modified-Since");
        if (modifiedSince > 0 && !resource.getLastModified().after(new Date(modifiedSince))) {
            response.setStatus(HttpURLConnection.HTTP_NOT_MODIFIED);
            response.getWriter().flush();
            return;
        }

        if (resource instanceof Collection) {
            response.sendRedirect("/wso2registry/web" + path);
        } else {
            sendResourceContent(request, response, path);
        }
    }

    private static void sendResourceContent(HttpServletRequest request,
                                            HttpServletResponse response,
                                            String path) {

        Resource resource;
        try {
            resource = ResourceRequestProcessor.getResource(request, path);
            try {

                response.setDateHeader("Last-Modified", resource.getLastModified().getTime());

                if (resource.getMediaType() != null && resource.getMediaType().length() > 0) {
                    response.setContentType(resource.getMediaType());
                } else {
                    response.setContentType("application/download");
                }

                InputStream contentStream = resource.getContentStream();
                if (contentStream != null) {

                    try {
                        ServletOutputStream servletOutputStream = response.getOutputStream();
                        byte[] contentChunk = new byte[RegistryConstants.DEFAULT_BUFFER_SIZE];
                        int byteCount;
                        while ((byteCount = contentStream.read(contentChunk)) != -1) {
                            servletOutputStream.write(contentChunk, 0, byteCount);
                        }

                        response.flushBuffer();
                        servletOutputStream.flush();

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

            } catch (IOException e) {
                setErrorMessage(request, e.getMessage());
            }
        } catch (RegistryException e) {
            setErrorMessage(request, e.getMessage());
        }
    }

    private static void setErrorMessage(HttpServletRequest request, String message) {
        request.getSession().setAttribute(RegistryConstants.ERROR_MESSAGE, message);
    }

    public static Resource getResource(HttpServletRequest request, String path)
            throws RegistryException {

        UserRegistry userRegistry = getUserRegistry(request);
        return userRegistry.get(path);
    }

    public static UserRegistry getUserRegistry(HttpServletRequest request)
            throws RegistryException {

        UserRegistry userRegistry =
                (UserRegistry) request.getSession()
                        .getAttribute(RegistryConstants.ROOT_REGISTRY_INSTANCE);

        if (userRegistry == null) {

            // user is not logged in. create a anonymous userRegistry for the user.

            ServletContext context = request.getSession().getServletContext();

            EmbeddedRegistryService embeddedRegistryService =
                    (EmbeddedRegistryService) context.getAttribute(RegistryConstants.REGISTRY);

            userRegistry = embeddedRegistryService.getRegistry();
            request.getSession()
                    .setAttribute(RegistryConstants.ROOT_REGISTRY_INSTANCE, userRegistry);
        }

        return userRegistry;
    }
}
