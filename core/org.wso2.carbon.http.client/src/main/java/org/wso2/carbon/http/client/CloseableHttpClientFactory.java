package org.wso2.carbon.http.client;

import org.wso2.carbon.http.client.exception.HttpClientException;

public interface CloseableHttpClientFactory {

    void closeConnectionManager() throws HttpClientException;

}
