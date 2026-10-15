/*
 * Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core.hash;

/**
 * Parses the algorithm identifier prefix from a stored password hash.
 *
 * The expected format is {@code {algorithm}<provider-produced-payload>}.
 * The provider payload is returned unchanged; its internal format is not parsed.
 */
public final class SelfDescribingHashParser {

    private static final char PREFIX_START = '{';
    private static final char PREFIX_END = '}';

    private SelfDescribingHashParser() {

    }

    /**
     * Builds a self-describing stored hash from an algorithm identifier and a provider payload.
     *
     * @param algorithm The algorithm identifier of the {@link HashProvider} which produced the payload.
     * @param payload   The provider-produced payload, used unchanged.
     * @return The stored hash in {@code {algorithm}<payload>} format.
     */
    public static String format(String algorithm, String payload) {

        return PREFIX_START + algorithm + PREFIX_END + payload;
    }

    /**
     * Parses a stored password hash.
     *
     * @param storedHash The stored password hash.
     * @return The parse result, identifying a legacy, prefixed, or malformed value.
     */
    public static ParseResult parse(String storedHash) {

        if (storedHash == null) {
            return ParseResult.malformed();
        }

        if (!storedHash.startsWith(String.valueOf(PREFIX_START))) {
            return ParseResult.legacy(storedHash);
        }

        int prefixEnd = storedHash.indexOf(PREFIX_END);
        if (prefixEnd <= 1) {
            return ParseResult.malformed();
        }

        String algorithm = storedHash.substring(1, prefixEnd);
        String payload = storedHash.substring(prefixEnd + 1);

        if (algorithm.indexOf(PREFIX_START) >= 0 || algorithm.indexOf(PREFIX_END) >= 0) {
            return ParseResult.malformed();
        }

        return ParseResult.prefixed(algorithm, payload);
    }

    /**
     * Represents the result of parsing a stored password hash.
     */
    public static final class ParseResult {

        private final boolean legacy;
        private final boolean valid;
        private final String algorithm;
        private final String payload;

        private ParseResult(boolean legacy, boolean valid, String algorithm, String payload) {

            this.legacy = legacy;
            this.valid = valid;
            this.algorithm = algorithm;
            this.payload = payload;
        }

        private static ParseResult legacy(String storedHash) {

            return new ParseResult(true, true, null, storedHash);
        }

        private static ParseResult prefixed(String algorithm, String payload) {

            return new ParseResult(false, true, algorithm, payload);
        }

        private static ParseResult malformed() {

            return new ParseResult(false, false, null, null);
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
    }
}
