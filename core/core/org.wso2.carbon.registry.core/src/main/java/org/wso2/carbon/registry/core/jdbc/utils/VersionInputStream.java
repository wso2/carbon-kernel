/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.core.jdbc.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This is an implementation of an input stream, wrapping the version list content written to the
 * database. This stream is capable of serializing a list of versions into a stream of bytes. The
 * byte stream will then be stored on the database, and will be much more optimal than storing them
 * one by one.
 * <p/>
 * The version retriever can read through such a stream of bytes and obtain the list of versions.
 *
 * @see VersionRetriever
 */
public class VersionInputStream extends InputStream {

    private int available = 0;
    private int read = 0;
    private List<Long> versionList;
    private static final int SIZE_OF_BYTE = Byte.SIZE;
    private static final int SIZE_OF_LONG = Long.SIZE;
    private static final int SIZE_OF_MULTIPLIER = SIZE_OF_LONG / SIZE_OF_BYTE;

    /**
     * Creates a version input stream from the given list of versions.
     *
     * @param versionList list of versions.
     */
    public VersionInputStream(List<Long> versionList) {
        this.versionList = versionList;
        available = versionList.size() * SIZE_OF_MULTIPLIER;
        read = 0;
    }

    /**
     * Reads the stream one by one.
     *
     * @return the value to be read.
     * @throws IOException if an error occurs.
     */
    public int read() throws IOException {
        int nextIndex = read / SIZE_OF_MULTIPLIER;
        int shiftOffset = SIZE_OF_MULTIPLIER - 1 - read % SIZE_OF_MULTIPLIER;

        if (nextIndex >= versionList.size()) {
            return -1;
        }
        long nextVersion = versionList.get(nextIndex);
        int valueToRead = (int) ((nextVersion >> (shiftOffset * SIZE_OF_BYTE)) & 0xff);
        read++;
        available--;
        return valueToRead;
    }

    /**
     * Method to check whether anymore bytes are left to read.
     *
     * @return the number of versions to be read.
     * @throws IOException if an error occurs.
     */
    public int available() throws IOException {
        return available;
    }
}
