package org.wso2.carbon.utils;

import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Util methods for HTTP Client.
 */
public class HTTPClientUtils {

    public static final String DEFAULT_AND_LOCALHOST = "DefaultAndLocalhost";
    public static final String HOST_NAME_VERIFIER = "httpclient.hostnameVerifier";
    private HTTPClientUtils() {
        //disable external instantiation
    }

    /**
     * Get the httpclient builder with custom hostname verifier.
     *
     * @return HttpClientBuilder.
     */
    public static HttpClientBuilder getHTTPClientWithCustomHostNameVerifier() {

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().useSystemProperties();
        if (DEFAULT_AND_LOCALHOST.equals(System.getProperty(HOST_NAME_VERIFIER))) {
            X509HostnameVerifier hostnameVerifier = new CustomHostNameVerifier();
            httpClientBuilder.setHostnameVerifier(hostnameVerifier);
        }

        return httpClientBuilder;
    }
}
