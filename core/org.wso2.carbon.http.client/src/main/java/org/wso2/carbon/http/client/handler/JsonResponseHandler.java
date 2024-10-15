/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.http.client.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.wso2.carbon.http.client.HttpClientConstants;
import org.wso2.carbon.http.client.exception.HttpClientException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class JsonResponseHandler extends AbstractResponseHandler<JsonObject> {

     protected JsonObject handleEntity(HttpEntity entity) throws HttpClientException {

        try (final InputStream inputStream = entity.getContent()) {
            if (inputStream == null) {
                return null;
            }
            JsonElement jsonElement =
                    JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            return jsonElement.getAsJsonObject();
        } catch (IOException e) {
            throw new HttpClientException(HttpClientConstants.Error.RESPONSE_PARSE_ERROR.getCode(),
                    HttpClientConstants.Error.RESPONSE_PARSE_ERROR.getMessage(), e);
        }
    }
}
