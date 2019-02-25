/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.tomcat.ext.valves;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Tomcat valve for adding character encoding if the character encoding is not available in the request.
 */
public class RequestEncodingValve extends ValveBase {

    private static Log log = LogFactory.getLog(RequestEncodingValve.class);
    private String encoding = "UTF-8";

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {

        if (null == request.getCharacterEncoding()) {
            if (log.isDebugEnabled()) {
                log.debug("The request has no character encoding. Hence, setting the encoding to " + encoding);
            }
            request.setCharacterEncoding(encoding);
        }

        // Invoking other valves.
        if (getNext() != null) {
            getNext().invoke(request, response);
        }

    }

    public void setEncoding(String encoding) {

        // First get the encoding type from the valve, Next from the system properties.
        // If nothing configured default will be UTF-8.
        if (encoding != null) {
            this.encoding = encoding;
        } else if (System.getProperty("file.encoding") != null) {
            this.encoding = System.getProperty("file.encoding");
        }
    }

    public String getEncoding() {

        return this.encoding;
    }
}
