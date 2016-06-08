/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.kernel.securevault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Base64;

/**
 * Helper to handle encoding and decoding of data streams.
 */
public final class EncodingHelper {

    private static final Logger logger = LoggerFactory.getLogger(EncodingHelper.class);

    private EncodingHelper() {
    }

    /**
     * Encodes the provided ByteArrayOutputStream using the specified encoding type.
     *
     * @param baos         The ByteArrayOutputStream to encode
     * @param encodingType The encoding to use
     * @return The encoded ByteArrayOutputStream as a String
     */
    public static byte[] encode(ByteArrayOutputStream baos, EncodingType encodingType) {
        switch (encodingType) {
            case BASE64:
                if (logger.isDebugEnabled()) {
                    logger.debug("base64 encoding on output ");
                }
                return Base64.getEncoder().encode(baos.toByteArray());
            case BIGINTEGER16:
                if (logger.isDebugEnabled()) {
                    logger.debug("BigInteger 16 encoding on output ");
                }
                return new BigInteger(baos.toByteArray()).toByteArray();
            default:
                throw new IllegalArgumentException("Unsupported encoding type");
        }
    }

    /**
     * Decodes the provided InputStream using the specified encoding type.
     *
     * @param inputStream  The InputStream to decode
     * @param encodingType The encoding to use
     * @return The decoded InputStream
     * @throws java.io.IOException      If an error occurs decoding the input stream
     * @throws IllegalArgumentException if the specified encodingType is not supported
     */
    public static InputStream decode(byte[] inputStream, EncodingType encodingType)
            throws IOException {

        InputStream decodedInputStream = null;
        switch (encodingType) {
            case BASE64:
                if (logger.isDebugEnabled()) {
                    logger.debug("base64 decoding on input  ");
                }
                decodedInputStream = new ByteArrayInputStream(
                        Base64.getDecoder().decode(new String(inputStream, "UTF-8")));
                break;
            default:
                throw new IllegalArgumentException("Unsupported encoding type");
        }

        return decodedInputStream;
    }
}
