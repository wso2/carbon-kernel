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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Once persisted the versions of a resource will be written as a byte stream and it is often not so
 * useful to be accessed in such a format. This class is able to read through the stream and output
 * a list of versions.
 * <p/>
 * This class works hand in hand with the version input stream, where the version input stream
 * persists data to the database, and the version retriever fetches persisted versions from the
 * database.
 *
 * @see VersionInputStream
 */
public class VersionRetriever {

    private InputStream inputStream;
    private List<Long> versionList;
    private int maxRetrievedVersion;
    private static final int SIZE_OF_BYTE = Byte.SIZE;
    private static final int SIZE_OF_LONG = Long.SIZE;

    private static Log log = LogFactory.getLog(VersionRetriever.class);

    /**
     * Creates a version retriever from the given input stream.
     *
     * @param inputStream a stream of versions as bytes.
     */
    public VersionRetriever(InputStream inputStream) {
        this.inputStream = inputStream;
        versionList = new ArrayList<Long>();
        maxRetrievedVersion = -1;
    }

    /**
     * Method to fetch the version at the given index.
     *
     * @param versionIndex the version index.
     *
     * @return the version at the given index.
     * @throws RegistryException if the operation failed.
     */
    public long getVersion(int versionIndex) throws RegistryException {
        while (versionIndex > maxRetrievedVersion) {
            long nextVersion = getNextVersion();
            if (nextVersion == 0) {
                // the stream is over
                return -1;
            }
            versionList.add(nextVersion);
            maxRetrievedVersion++;
        }
        return versionList.get(versionIndex);
    }

    // Utility method to obtain next version.
    private long getNextVersion() throws RegistryException {
        // Logic reads the input stream 8 times and build the
        // 64 bit long value
        int shiftLimit = SIZE_OF_LONG / SIZE_OF_BYTE;
        long versionValue = 0;
        try {
            for (int i = 0; i < shiftLimit; i++) {
                int nextByte = inputStream.read();
                if (i != 0) {
                    // left shift one byte
                    versionValue <<= SIZE_OF_BYTE;
                }
                // do 'or' with the current byte.
                versionValue |= nextByte;
            }
        }
        catch (IOException e) {
            String msg = "Failed to read the stream to get the version value. " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return versionValue;
    }
}
