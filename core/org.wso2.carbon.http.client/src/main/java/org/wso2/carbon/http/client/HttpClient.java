package org.wso2.carbon.http.client;

public interface HttpClient {
    <T> T get(String url);

//    <T> T post(String url, String body);

//    void close();
//
//    void closeConnectionManager();
}
