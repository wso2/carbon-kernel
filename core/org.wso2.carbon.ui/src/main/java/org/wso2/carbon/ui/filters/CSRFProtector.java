/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.ui.filters;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class generates a CSRF token for a valid session and protects the request against that token.
 * This also implements the logic to inject a javascript for the response, to enforce that the generated token is
 * added as a hidden parameter in every form.
 */
public class CSRFProtector {
    private static final Log log = LogFactory.getLog(CSRFProtector.class);
    private static final String JS_TEMPLATE = "/web/admin/js/csrf.js";

    private CSRFFilterConfig config;
    private Pattern skipUrlPattern;
    private StringBuilder jsTemplate;

    public CSRFProtector(CSRFFilterConfig config) {
        this.config = config;

        initSkipUrlPattern(config.getSkipUrlPattern());

        loadJSTemplate();
    }

    public boolean skipUrl(String uri) {

        if (skipUrlPattern != null) {
            return skipUrlPattern.matcher(uri).matches();
        }

        return false;
    }

    /**
     * Validates POST requests for CSRF token
     *
     * @param request   HTTPServerRequest instance
     * @param responseWrapper   HTTPServerletResponseWrapper instance
     * @throws CSRFException exception is thrown when there is a probable attack
     */
    public void applyProtection(HttpServletRequest request, CSRFResponseWrapper responseWrapper) throws CSRFException {

        HttpSession session = request.getSession(false);

        if (session != null && CSRFConstants.METHOD_POST.equalsIgnoreCase(request.getMethod())) {

            String CSRFRequestToken = request.getParameter(CSRFConstants.CSRF_TOKEN);
            String CSRFSessionToken = getCSRFTokenFromSession(session);

            if (CSRFSessionToken == null) {
                setCSRFTokenForSession(session);
            } else if (!CSRFSessionToken.equals(CSRFRequestToken)) {
                throw new CSRFException("A potential CSRF attack from " + request.getRequestURI());
            }
        }

    }

    /**
     * Generates and injects CSRF Token in the response as a hidden parameter
     *
     * @param request HTTPServerRequest instance
     * @param responseWrapper HTTPServerletResponseWrapper instance
     * @throws IOException
     */
    public void enforceProtection(HttpServletRequest request, CSRFResponseWrapper responseWrapper) throws IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            String CSRFSessionToken = getCSRFTokenFromSession(session);
            if (CSRFSessionToken == null) {
                setCSRFTokenForSession(session);
            }

            CSRFSessionToken = getCSRFTokenFromSession(session);
            String content = new String(responseWrapper.getContent());
            content = CSRFConstants.HTML_HEAD_PATTERN.matcher(content)
                                                     .replaceAll("<script type=\"text/javascript\">\n" + Matcher
                                                             .quoteReplacement(getInjectingJS(CSRFSessionToken)) +
                                                                 "\n</script>\n</head>");
            responseWrapper.setContent(content);
            responseWrapper.write();
        }
    }

    public CSRFFilterConfig getConfig() {
        return config;
    }

    private void initSkipUrlPattern(String regex) {
        if (StringUtils.isNotBlank(regex)) {
            skipUrlPattern = Pattern.compile(regex);
        }
    }

    private void loadJSTemplate() {
        jsTemplate = new StringBuilder();

        try (InputStream inputStream = getClass().getResourceAsStream(JS_TEMPLATE)) {
            int i = 0;
            while ((i = inputStream.read()) > 0) {
                jsTemplate.append((char) i);
            }
        } catch (IOException e) {
            log.error("Failed to load CSRF javascript template", e);
        }
    }

    private String getCSRFTokenFromSession(HttpSession session) {
        return (String) session.getAttribute(CSRFConstants.CSRF_TOKEN);
    }

    private void setCSRFTokenForSession(HttpSession session) {
        try {
            String token = generateCSRFToken();
            session.setAttribute(CSRFConstants.CSRF_TOKEN, token);
        } catch (NoSuchAlgorithmException e) {
            log.error("CSRF Token generation failed.", e);
        }
    }

    private String getInjectingJS(String token) {
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(CSRFConstants.JSTemplateToken.CSRF_TOKEN_NAME, CSRFConstants.CSRF_TOKEN);
        valuesMap.put(CSRFConstants.JSTemplateToken.CSRF_TOKEN_VALUE, token);

        StrSubstitutor substitutor = new StrSubstitutor(valuesMap);
        return substitutor.replace(jsTemplate.toString());
    }

    private static String generateCSRFToken() throws NoSuchAlgorithmException {
        byte random[] = new byte[16];

        // Render the result as a String of hexadecimal digits
        StringBuilder buffer = new StringBuilder();

        SecureRandom.getInstance(CSRFConstants.CSRF_TOKEN_PRNG).nextBytes(random);

        for (int j = 0; j < random.length; j++) {
            byte b1 = (byte) ((random[j] & 0xf0) >> 4);
            byte b2 = (byte) (random[j] & 0x0f);
            if (b1 < 10) {
                buffer.append((char) ('0' + b1));
            } else {
                buffer.append((char) ('A' + (b1 - 10)));
            }
            if (b2 < 10) {
                buffer.append((char) ('0' + b2));
            } else {
                buffer.append((char) ('A' + (b2 - 10)));
            }
        }

        return buffer.toString();
    }
}
