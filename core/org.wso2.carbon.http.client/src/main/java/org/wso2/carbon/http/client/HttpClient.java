package org.wso2.carbon.http.client;

import org.wso2.carbon.http.client.exception.HttpClientException;

public interface HttpClient {
    <T> T get(String url) throws HttpClientException;

//    <T> T post(String url, String body);

}
