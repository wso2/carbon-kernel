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

package org.apache.axis2.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
    
    /**
     * Copies the input stream to the output stream
     *
     * @param in  the <code>InputStream</code>
     * @param out the <code>OutputStream</code>
     * @param close close input and output stream
     */
    public static void copy(InputStream in, OutputStream out, boolean close) throws IOException {
        byte[] buffer = new byte[4096];
        int count;
        try {
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
        } finally {
            if (close) {
                try { in.close(); } catch (IOException e) {}
                try { out.close(); } catch (IOException e) {}
            }
        }
    }
    
}
