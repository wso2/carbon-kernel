package org.wso2.carbon.http.client.request;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpPostRequest {

    private HttpPostRequest() {
    }

    public static HttpPost createUrlEncodedRequest(String url, Map<String, String> paramsMap) {

        HttpPost httpPost = new HttpPost(url);
        List<BasicNameValuePair> params = new ArrayList<>();
        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        return httpPost;
    }

    public static HttpPost createJsonRequest(String url, String jsonPayload) {

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

        return httpPost;
    }

}
