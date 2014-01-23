/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.apache.axis2.java.security;

import javax.net.ssl.X509TrustManager;

/**
 * Normally when we connect over HTTPS, if the server sends us a certificate that is not well
 * known,  we have to specify a keystore using system properties:
 * <p/>
 * System.setProperty("javax.net.ssl.trustStore","path to keystore" );
 * System.setProperty("javax.net.ssl.trustStorePassword","apache");
 * <p/>
 * Using this X509TrustManager we can allow the client to disregard the certificate and trust the
 * server. One of the reason this may be done is because clients are sometimes deployed on systems
 * where the developers haveno access to the file system and therefore cannot configure the
 * keystores.
 * <p/>
 * This TrustManager can be used in the client stub as follows:
 * <p/>
 * <pre>
 * <code>
 * SSLContext sslCtx = SSLContext.getInstance("http");
 * sslCtx.init(null, new TrustManager[] {new TrustAllTrustManager()}, null);
 * stub._getServiceClient().getOptions().setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER,
 *          new Protocol("https",(ProtocolSocketFactory)new SSLProtocolSocketFactory(sslCtx),443));
 * </code>
 * </pre>
 * @see SSLProtocolSocketFactory
 */
public class TrustAllTrustManager implements X509TrustManager {
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
    }

    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
    }
}
