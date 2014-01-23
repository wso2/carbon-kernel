/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
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

package org.apache.axis2.json.gson;

import com.google.gson.stream.JsonReader;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.json.gson.factory.JsonConstant;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class JsonBuilderTest {

    @Test
    public void testProcessDocument() throws Exception {
        MessageContext messageContext = new MessageContext();
        String contentType = "application/json-impl";
        String jsonString = "{\"methodName\":{\"param\":\"value\"}}";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonString.getBytes("UTF-8"));
        messageContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, "UTF-8");

        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.processDocument(inputStream, contentType, messageContext);

        Object isJson = messageContext.getProperty(JsonConstant.IS_JSON_STREAM);
        Assert.assertNotNull(isJson);
        isJson = Boolean.valueOf(isJson.toString());
        Assert.assertEquals(true, isJson);
        Object streamReader = messageContext.getProperty(JsonConstant.GSON_XML_STREAM_READER);
        Assert.assertNotNull(streamReader);
        GsonXMLStreamReader gsonXMLStreamReader = (GsonXMLStreamReader) streamReader;
        JsonReader jsonReader = gsonXMLStreamReader.getJsonReader();
        Assert.assertNotNull(jsonReader);
        try {
            String actualString = readJsonReader(jsonReader);
            Assert.assertEquals("value", actualString);
        } catch (IOException e) {
            Assert.assertFalse(true);
        }

        inputStream.close();

    }

    private String readJsonReader(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        jsonReader.nextName();
        jsonReader.beginObject();
        jsonReader.nextName();
        String name = jsonReader.nextString();
        jsonReader.endObject();
        jsonReader.endObject();
        return name;
    }
}
