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
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * Wraps the cipher and expose encrypt/decrypt operations.
 *
 * @since 5.2.0
 */
public class BaseCipher {

    private static final Logger logger = LoggerFactory.getLogger(BaseCipher.class);
    private static final String DEFAULT_ALGORITHM = "RSA";
    /* Underlying cipher instance*/
    private Cipher cipher;

    public BaseCipher(CipherInformation cipherInformation, Key key) {
        init(cipherInformation, key);
    }

    /**
     * Decodes the provided InputStream using the Base64 encoding type.
     *
     * @param inputStream The InputStream to decode
     * @return The decoded InputStream
     * @throws java.io.IOException      If an error occurs decoding the input stream
     * @throws IllegalArgumentException if the specified encodingType is not supported
     */
    public static InputStream decode(InputStream inputStream)
            throws IOException {

        byte[] decodedValue = Base64.getDecoder().decode(asBytes(inputStream));

        return  new ByteArrayInputStream(decodedValue);
    }

    private static byte[] asBytes(InputStream in) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new SecureVaultException("Error during converting a inputstream " +
                    "into a byte array ", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
            try {
                out.close();
            } catch (IOException ignored) {
            }
        }
        return out.toByteArray();
    }

    private void init(CipherInformation cipherInformation, Key key) {  //todo how to find the public key?

        String algorithm = DEFAULT_ALGORITHM;
        try {
            cipher = Cipher.getInstance(algorithm);
            if (cipherInformation.getCipherOperationMode() == CipherOperationMode.ENCRYPT) {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            } else if (cipherInformation.getCipherOperationMode() == CipherOperationMode.DECRYPT) {
                cipher.init(Cipher.DECRYPT_MODE, key);
            } else {
                logger.error("Invalid operation mode");
                throw new SecureVaultException("Invalid operation mode ");
            }

        } catch (NoSuchAlgorithmException e) {
            logger.error("There is no algorithm support for " +
                    "'" + algorithm + "' in the operation mode '" +
                    cipherInformation.getCipherOperationMode() + "'" + e, logger);
        } catch (NoSuchPaddingException e) {
            logger.error("There is no padding scheme  for " +
                    "'" + algorithm + "' in the operation mode '" +
                    cipherInformation.getCipherOperationMode() + "'" + e, logger);
        } catch (InvalidKeyException e) {
            logger.error("Invalid key ", e, logger);
        }
    }

    private byte[] doCipherOperation(byte[] inputStream) {
        byte[] cipherText = null;
        try {
            cipherText = cipher.doFinal(inputStream);
        } catch (IllegalBlockSizeException e) {
            logger.error("...", e);     //todo
        } catch (BadPaddingException e) {
            logger.error(".xx..", e);     //todo
        }
        return cipherText;
    }

    public byte[] encrypt(byte[] plainText) {
        return doCipherOperation(plainText);
    }

    public byte[] decrypt(byte[] inputStream) {
        InputStream sourceStream = new ByteArrayInputStream(inputStream);
        try {
            sourceStream = decode(
                    sourceStream);
        } catch (IOException e) {
            throw new SecureVaultException("IOError when decoding the input " +
                    "stream for cipher ", e);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CipherOutputStream out = new CipherOutputStream(baos, cipher);

        byte[] buffer = new byte[64];
        int length;
        try {
            while ((length = sourceStream.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new SecureVaultException("IOError when reading the input" +
                    " stream for cipher ", e);
        } finally {
            try {
                sourceStream.close();
                out.flush();
                out.close();
            } catch (IOException ignored) {
                // ignore exception
            }
        }

        return baos.toByteArray();
    }
}
