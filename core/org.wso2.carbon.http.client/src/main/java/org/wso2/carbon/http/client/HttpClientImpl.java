package org.wso2.carbon.http.client;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.wso2.carbon.utils.CustomHostNameVerifier;

import javax.net.ssl.HostnameVerifier;
import java.io.IOException;
import java.io.InputStream;

import static org.wso2.carbon.CarbonConstants.ALLOW_ALL;
import static org.wso2.carbon.CarbonConstants.DEFAULT_AND_LOCALHOST;
import static org.wso2.carbon.CarbonConstants.HOST_NAME_VERIFIER;

/**
 *
 */
public abstract class HttpClientImpl implements HttpClient, CloseableHttpClientFactory {

    protected HttpClientConnectionManager connectionManager;

    protected HttpClientImpl() {
    }

    protected HttpClientImpl(HttpClientConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    protected void setConnectionManager(HttpClientConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    private CloseableHttpClient getClient() {
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setConnectionManagerShared(true)
                .build();
    }

    /**
     * @param url
     * @return
     */
    @Override
    public InputStream get(String url) {

        HttpGet getUrl = new HttpGet(url);

        try (CloseableHttpClient httpClient = getClient()) {
            return httpClient.execute(getUrl, response -> {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return entity.getContent();
                } else {
                    return null;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void closeConnectionManager() {
        try {
            connectionManager.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a httpclient with custom hostname verifier.
     *
     * @return CloseableHttpClient.
     */
    public CloseableHttpClient createClientWithCustomVerifier() {

        HttpClientBuilder httpClientBuilder = HttpClients.custom().useSystemProperties();
//        if (DEFAULT_AND_LOCALHOST.equals(System.getProperty(HOST_NAME_VERIFIER))) {
//            httpClientBuilder.setHostnameVerifier(new CustomHostNameVerifier());
//        } else if (ALLOW_ALL.equals(System.getProperty(HOST_NAME_VERIFIER))) {
//            httpClientBuilder.setHostnameVerifier(NoopHostnameVerifier.INSTANCE);
//        }

        HostnameVerifier hostnameVerifier;
        if (DEFAULT_AND_LOCALHOST.equals(System.getProperty(HOST_NAME_VERIFIER))) {
            hostnameVerifier = new CustomHostNameVerifier();
        } else if (ALLOW_ALL.equals(System.getProperty(HOST_NAME_VERIFIER))) {
            hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        } else {
            hostnameVerifier = null;
        }

        TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                                .setHostnameVerifier(hostnameVerifier).build();


        httpClientBuilder.setConnectionManager(connectionManager)
                .setConnectionManagerShared(true);

        return httpClientBuilder.build();
    }
}
