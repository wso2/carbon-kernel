/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */
package org.wso2.carbon.tomcat.ext.valves;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * Tomcat valve for restricting resources based on the resource path.
 */
public class RestrictResourcePathValve extends ValveBase {

    private static Log log = LogFactory.getLog(RestrictResourcePathValve.class);
    private String restrictedResourcePaths;
    private List<String> restrictedResourcePathList;
    // Whitelisted file upload resource paths
    private List<String> whitelistedResources = Arrays.asList("attachment-mgt", "bpel", "bpmn", "carbonapp",
            "contractFirst", "dbs", "humantask", "jaggeryapp", "mediationlib", "module", "resource", "wsdl");

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        if (restrictedResourcePaths != null && !restrictedResourcePaths.trim().isEmpty()){
            restrictedResourcePathList = Arrays.asList(restrictedResourcePaths.split("\\s*,\\s*"));
        }

        // Check if the request URI is whitelisted
        boolean isWhitelisted = false;
        for (String resource : whitelistedResources) {
            if (requestURI.contains("/fileupload/"+resource)) {
                isWhitelisted = true;
                break;
            }
        }

        // Check if the request URI matches any of the restricted resource paths.
        if (!isWhitelisted) {
            for (String restrictedResource : restrictedResourcePathList) {
                if (requestURI.contains(restrictedResource)) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested resource is not found");
                    log.error("Requested resource is not found");
                    return;
                }
            }
        }

        if (getNext() != null) {
            getNext().invoke(request, response);
        }
    }

    public void setRestrictedResourcePaths(String restrictedResourcePaths) {
        this.restrictedResourcePaths = restrictedResourcePaths;
    }
}
