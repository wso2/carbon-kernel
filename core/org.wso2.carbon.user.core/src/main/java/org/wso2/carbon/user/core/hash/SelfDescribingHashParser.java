/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core.hash;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads and writes the self-describing form of a stored password hash.
 *
 * The stored value is a JSON object carrying the identifier of the algorithm that produced the
 * hash alongside the hash itself, as {@code {"algorithm":"<algorithm>","hash":"<payload>"}}. The
 * payload is treated as opaque: it is stored and returned unchanged, and its internal structure
 * remains the concern of the provider that produced it.
 */
public final class SelfDescribingHashParser {

    public static final String ALGORITHM = "algorithm";
    public static final String HASH = "hash";
    public static final String PROPERTIES = "properties";

    private static final char OBJECT_START = '{';

    // Gson escapes characters such as '=' by default, which would mangle the base64 padding that
    // ends most provider payloads. The stored value is never rendered as HTML, so escaping is off.
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private SelfDescribingHashParser() {

    }

    /**
     * Builds a self-describing stored hash from an algorithm identifier and a provider payload.
     *
     * @param algorithm The algorithm identifier of the {@link HashProvider} which produced the payload.
     * @param payload   The provider-produced payload, used unchanged.
     * @return The stored hash as a self-describing JSON object.
     */
    public static String format(String algorithm, String payload) {

        return format(algorithm, payload, null);
    }

    /**
     * Builds a self-describing stored hash, recording the parameters the hash was produced with.
     * <p>
     * Parameter values are written as strings, which is the form
     * {@link HashProviderFactory#getHashProvider(Map)} accepts: a provider handed a value of any
     * other type silently keeps its own default, which would verify the credential against
     * parameters other than the ones that produced it. The member is omitted entirely when the
     * provider reports no parameters.
     * </p>
     *
     * @param algorithm  The algorithm identifier of the {@link HashProvider} which produced the payload.
     * @param payload    The provider-produced payload, used unchanged.
     * @param properties The provider parameters in effect when the payload was produced.
     * @return The stored hash as a self-describing JSON object.
     */
    public static String format(String algorithm, String payload, Map<String, Object> properties) {

        JsonObject storedValue = new JsonObject();
        storedValue.addProperty(ALGORITHM, algorithm);
        storedValue.addProperty(HASH, payload);

        if (properties != null && !properties.isEmpty()) {
            JsonObject storedProperties = new JsonObject();
            for (Map.Entry<String, Object> property : properties.entrySet()) {
                if (property.getValue() != null) {
                    storedProperties.addProperty(property.getKey(), String.valueOf(property.getValue()));
                }
            }
            if (storedProperties.size() > 0) {
                storedValue.add(PROPERTIES, storedProperties);
            }
        }

        return GSON.toJson(storedValue);
    }

    /**
     * Parses a stored password hash.
     * <p>
     * The value is first parsed as JSON. A JSON object is read as a self-describing value, and one
     * that does not carry both members is reported as malformed. A value that is not a JSON object
     * was written before this format was introduced and is reported as legacy, unless it opens an
     * object without being a valid one: such a value is a damaged record, so it is reported as
     * malformed and fails verification rather than being mistaken for a credential in the older form.
     * </p>
     *
     * @param storedHash The stored password hash.
     * @return The parse result, identifying a legacy, self-describing, or malformed value.
     */
    public static ParseResult parse(String storedHash) {

        if (storedHash == null) {
            return ParseResult.malformed();
        }

        JsonObject storedValue = parseJsonObject(storedHash);
        if (storedValue == null) {
            // A bare value such as 12345 may parse as a JSON primitive. Only a JSON object is the
            // self-describing format; anything else is legacy, except a value that opens an object,
            // which is a damaged self-describing record.
            if (!storedHash.isEmpty() && storedHash.charAt(0) == OBJECT_START) {
                return ParseResult.malformed();
            }
            return ParseResult.legacy(storedHash);
        }

        String algorithm = readMember(storedValue, ALGORITHM);
        String payload = readMember(storedValue, HASH);
        if (StringUtils.isBlank(algorithm) || payload == null) {
            return ParseResult.malformed();
        }

        return ParseResult.prefixed(algorithm, payload, readProperties(storedValue));
    }

    /**
     * Parses a stored value as a JSON object.
     *
     * @param storedHash The stored password hash.
     * @return The JSON object, or null when the value is not a valid JSON object.
     */
    private static JsonObject parseJsonObject(String storedHash) {

        // Parsed strictly, because JsonParser is lenient and would accept an object with unquoted
        // member names, passing a value that is not valid JSON on to a provider.
        try (JsonReader reader = new JsonReader(new StringReader(storedHash))) {
            reader.setLenient(false);
            JsonElement parsed = GSON.getAdapter(JsonElement.class).read(reader);
            if (reader.peek() != JsonToken.END_DOCUMENT) {
                return null;
            }
            return parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    /**
     * Reads the recorded provider parameters, if the stored value carries any.
     *
     * @param storedValue The parsed stored value.
     * @return The parameters, as strings, or an empty map when none were recorded.
     */
    private static Map<String, Object> readProperties(JsonObject storedValue) {

        JsonElement properties = storedValue.get(PROPERTIES);
        if (properties == null || !properties.isJsonObject()) {
            return Collections.emptyMap();
        }

        Map<String, Object> parameters = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> property : properties.getAsJsonObject().entrySet()) {
            JsonElement value = property.getValue();
            if (value.isJsonPrimitive()) {
                // getAsString also renders a bare number, so a hand-edited record still reads back
                // in the form a provider accepts.
                parameters.put(property.getKey(), value.getAsString());
            }
        }
        return parameters;
    }

    /**
     * Reads a member of the stored value as a string.
     *
     * @param storedValue The parsed stored value.
     * @param member      The member to read.
     * @return The member as a string, or null when it is absent or not a string.
     */
    private static String readMember(JsonObject storedValue, String member) {

        JsonElement element = storedValue.get(member);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            return null;
        }
        return element.getAsString();
    }

    /**
     * Represents the result of parsing a stored password hash.
     */
    public static final class ParseResult {

        private final boolean legacy;
        private final boolean valid;
        private final String algorithm;
        private final String payload;
        private final Map<String, Object> properties;

        private ParseResult(boolean legacy, boolean valid, String algorithm, String payload,
                            Map<String, Object> properties) {

            this.legacy = legacy;
            this.valid = valid;
            this.algorithm = algorithm;
            this.payload = payload;
            this.properties = properties;
        }

        private static ParseResult legacy(String storedHash) {

            return new ParseResult(true, true, null, storedHash, Collections.emptyMap());
        }

        private static ParseResult prefixed(String algorithm, String payload, Map<String, Object> properties) {

            return new ParseResult(false, true, algorithm, payload, properties);
        }

        private static ParseResult malformed() {

            return new ParseResult(false, false, null, null, Collections.emptyMap());
        }

        public boolean isLegacy() {

            return legacy;
        }

        public boolean isValid() {

            return valid;
        }

        public String getAlgorithm() {

            return algorithm;
        }

        public String getPayload() {

            return payload;
        }

        /**
         * The provider parameters recorded with the hash, as strings.
         *
         * @return The recorded parameters, empty when the stored value carries none.
         */
        public Map<String, Object> getProperties() {

            return properties;
        }
    }
}
