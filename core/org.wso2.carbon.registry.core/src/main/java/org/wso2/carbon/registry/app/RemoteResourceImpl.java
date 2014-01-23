/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.app;

import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Implementation of a resource, to be used by the remote registry.
 */
public class RemoteResourceImpl extends ResourceImpl {

    private URL contentURL;
    private String authorizationString;

    /**
     * Method to set to content url.
     *
     * @param contentURL the content url.
     */
    public void setContentURL(URL contentURL) {
        this.contentURL = contentURL;
    }

    /**
     * Method to obtain the authorization string.
     *
     * @return the authorization string.
     */
    @SuppressWarnings("unused")
    public String getAuthorizationString() {
        return authorizationString;
    }

    /**
     * Method to set the authorization string.
     *
     * @param authorizationString the authorization string.
     */
    public void setAuthorizationString(String authorizationString) {
        this.authorizationString = authorizationString;
    }

    public InputStream getContentStream() throws RegistryException {

        if (!contentModified && contentURL != null && content == null) {
            return getContentStreamFromURL();
        }
        return super.getContentStream();
    }

    public Object getContent() throws RegistryException {

        if (content != null) {
            if (content instanceof String) {
                // Remote registry put method handles String content in a different manner - this is
                // to reflect that change - to be consistent with embedded registry.
                return RegistryUtils.encodeString((String) content);
            }
            return content;
        }

        if(!contentModified){
            try {

                InputStream is = getContentStreamFromURL();

                if (is == null) {
                    return null;
                }

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                int nextChar;
                while ((nextChar = is.read()) != -1) {
                    os.write(nextChar);
                }
                os.flush();
                content = os.toByteArray();
                return content;

            } catch (Exception e) {
                throw new RegistryException("Couldn't get content stream", e);
            }
        }
        return null;

    }

    // Method to get content stream from URL.
    private InputStream getContentStreamFromURL() throws RegistryException {

        if (contentURL != null) {
            try {
                URLConnection uc =
                        new URL(contentURL.toString().replace(" ", "+")).openConnection();
                if (uc instanceof HttpsURLConnection) { //HTTPS URL?
                    ((HttpsURLConnection) uc).setHostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                }
                if (authorizationString != null) {
                    uc.setRequestProperty("Authorization", authorizationString);
                }
                return uc.getInputStream();

            } catch (IOException e) {
                throw new RegistryException("Couldn't open stream to source URL " + contentURL, e);
            }
        }

        return null;
    }


}
