/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core;

import org.apache.commons.lang.ArrayUtils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;

/**
 * This class wraps a character array to be used to handle sensitive data like passwords.
 */
public class Credential {
    private char[] chars;
    private byte[] bytes;
    private boolean isNew;

    public Credential() {
        this(new char[0]);
    }

    public Credential(char[] chars) {
        this.chars = chars;
        isNew = true;
    }

    public char[] getChars() {
        if (chars == null) {
            this.chars = new char[0];
        }

        return chars;
    }

    public byte[] getBytes() {

        clearBytes(bytes);

        CharBuffer charBuffer = CharBuffer.wrap(getChars());
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        bytes = Arrays.copyOfRange(byteBuffer.array(),
                                          byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {

        clearBytes(bytes);

        CharBuffer charBuffer = CharBuffer.wrap(getChars());
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = Charset.forName(charsetName).encode(charBuffer);
        } catch (UnsupportedCharsetException e) {
            throw new UnsupportedEncodingException("Unsupported charset " + charsetName);
        }

        bytes = Arrays.copyOfRange(byteBuffer.array(),
                                          byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    public boolean isNew() {
        return isNew;
    }

    public boolean isEmpty() {
        return chars == null || chars.length < 1;
    }

    public void setChars(char[] chars) {
        clearChars(this.chars);
        this.chars = chars;
    }

    public void addChars(char[] chars) {
        char[] previous = getChars();
        setChars(ArrayUtils.addAll(previous, chars));
        clearChars(previous);
    }

    public void clear() {
        clearChars(this.chars);
        clearBytes(this.bytes);
    }

    private void clearChars(char[] chars) {
        if (chars != null) {
            Arrays.fill(chars, '\u0000');
        }
    }

    private void clearBytes(byte[] bytes) {
        if (bytes != null) {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    public static Credential getCredential(Object credential) throws UserStoreException {
        if (credential != null) {
            if (credential instanceof Credential) {
                Credential credentialObj = (Credential) credential;
                credentialObj.isNew = false;
                return credentialObj;
            } else if (credential instanceof char[]) {
                char[] credentialChars = (char[]) credential;
                return new Credential(Arrays.copyOf(credentialChars, credentialChars.length));
            } else if (credential instanceof String) {
                return new Credential(((String) credential).trim().toCharArray());
            } else {
                throw new UserStoreException("Unsupported Credential Type. Can handle only string type or character " +
                                             "array type credentials");
            }
        }

        return new Credential();
    }

}
