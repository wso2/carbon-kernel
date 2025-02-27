package org.wso2.carbon.http.client;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.HttpEntity;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public abstract class HttpClientImpl implements HttpClient, CloseableHttpClientFactory {

    private static HttpClientConnectionManager connectionManager;

    protected HttpClientImpl(HttpClientConnectionManager connectionManager) {
        HttpClientImpl.connectionManager = connectionManager;
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
}
