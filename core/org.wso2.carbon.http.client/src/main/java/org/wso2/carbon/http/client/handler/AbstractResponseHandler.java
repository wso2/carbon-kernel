package org.wso2.carbon.http.client.handler;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.wso2.carbon.http.client.HttpClientConstants;
import org.wso2.carbon.http.client.exception.HttpClientException;

public abstract class AbstractResponseHandler<T> implements HttpClientResponseHandler<T> {

    public T handleResponse(ClassicHttpResponse response) throws HttpClientException {

        // TODO: handle response status codes
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new HttpClientException(HttpClientConstants.Error.RESPONSE_ENTITY_EMPTY.getCode(),
                    HttpClientConstants.Error.RESPONSE_ENTITY_EMPTY.getMessage());
        }
        return this.handleEntity(entity);
    }

    protected abstract T handleEntity(HttpEntity httpEntity) throws HttpClientException;
}
