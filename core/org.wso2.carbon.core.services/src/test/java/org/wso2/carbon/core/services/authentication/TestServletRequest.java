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

package org.wso2.carbon.core.services.authentication;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * Servlet request class for testing.
 */
public class TestServletRequest implements HttpServletRequest {

    private String authorizationHeader;

    public TestServletRequest(String header) {
        authorizationHeader = header;
    }

    public String getAuthType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Cookie[] getCookies() {
        return new Cookie[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getDateHeader(String s) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getHeader(String s) {

        if (s.equals("Authorization")) {
            return authorizationHeader;
        }

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getHeaders(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getHeaderNames() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getIntHeader(String s) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getMethod() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getPathInfo() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getPathTranslated() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getContextPath() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getQueryString() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRemoteUser() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isUserInRole(String s) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Principal getUserPrincipal() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRequestedSessionId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRequestURI() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public StringBuffer getRequestURL() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getServletPath() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public HttpSession getSession(boolean b) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public HttpSession getSession() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isRequestedSessionIdValid() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isRequestedSessionIdFromCookie() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isRequestedSessionIdFromURL() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isRequestedSessionIdFromUrl() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
    
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void login(String s, String s1) throws ServletException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void logout() throws ServletException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection<Part> getParts() throws IOException, ServletException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Part getPart(String s) throws IOException, ServletException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getAttribute(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getAttributeNames() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getCharacterEncoding() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getContentLength() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getContentType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ServletInputStream getInputStream() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getParameter(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getParameterNames() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getParameterValues(String s) {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getParameterMap() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getProtocol() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getScheme() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getServerName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getServerPort() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public BufferedReader getReader() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRemoteAddr() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRemoteHost() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAttribute(String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeAttribute(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Locale getLocale() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getLocales() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isSecure() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRealPath(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getRemotePort() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getLocalName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getLocalAddr() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getLocalPort() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ServletContext getServletContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public AsyncContext startAsync() throws IllegalStateException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAsyncStarted() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAsyncSupported() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public AsyncContext getAsyncContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DispatcherType getDispatcherType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
