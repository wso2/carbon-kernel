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

package org.apache.axiom.util.activation;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.axiom.ext.activation.SizeAwareDataSource;

/**
 * Contains utility methods to work with {@link DataSource} objects.
 */
public class DataSourceUtils {
    /**
     * Determine the size of the data represented by a {@link DataSource} object.
     * The method will try to determine the size without reading the data source.
     * It will do so by looking for the {@link SizeAwareDataSource} interface. In addition, it
     * supports some other well known data source implementations for which it is possible to
     * get the size of the data without reading it.
     * <p>
     * As noted in the documentation of the {@link SizeAwareDataSource}, the returned value
     * may be an estimation that is not 100% accurate, and code using this method must be prepared
     * to receive more or less data from the data source.
     * 
     * @param ds the data source
     * @return (an estimation of) the size of the data or <code>-1</code> if the size is unknown
     */
    public static long getSize(DataSource ds) {
        if (ds instanceof SizeAwareDataSource) {
            return ((SizeAwareDataSource)ds).getSize();
        } else if (ds instanceof ByteArrayDataSource) {
            // Special optimization for JavaMail's ByteArrayDataSource (Axiom's ByteArrayDataSource
            // already implements SizeAwareDataSource and doesn't need further optimization):
            // we know that ByteArrayInputStream#available() directly returns the size of the
            // data source.
            try {
                return ((ByteArrayInputStream)ds.getInputStream()).available();
            } catch (IOException ex) {
                // We will never get here...
                return -1;
            }
        } else if (ds instanceof FileDataSource) {
            // Special optimization for FileDataSources: no need to open and read the file
            // to know its size!
            return ((FileDataSource)ds).getFile().length();
        } else {
            return -1;
        }
    }
}
