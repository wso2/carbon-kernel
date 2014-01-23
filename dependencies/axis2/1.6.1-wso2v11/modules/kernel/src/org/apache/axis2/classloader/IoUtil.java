/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.axis2.classloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.jar.JarFile;

/**
 * @version $Rev: 704201 $ $Date: 2008-10-14 00:22:25 +0530 (Tue, 14 Oct 2008) $
 */
public final class IoUtil {
    private IoUtil() {
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        try {
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (int count = inputStream.read(buffer); count >= 0; count = inputStream.read(buffer)) {
                out.write(buffer, 0, count);
            }
            byte[] bytes = out.toByteArray();
            return bytes;
        } finally {
            close(inputStream);
        }
    }

    public static void flush(OutputStream thing) {
        if (thing != null) {
            try {
                thing.flush();
            } catch(Exception ignored) {
            }
        }
    }

    public static void flush(Writer thing) {
        if (thing != null) {
            try {
                thing.flush();
            } catch(Exception ignored) {
            }
        }
    }

    public static void close(JarFile thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch(Exception ignored) {
            }
        }
    }

    public static void close(InputStream thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch(Exception ignored) {
            }
        }
    }

    public static void close(OutputStream thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch(Exception ignored) {
            }
        }
    }

    public static void close(Reader thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch(Exception ignored) {
            }
        }
    }

    public static void close(Writer thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch(Exception ignored) {
            }
        }
    }

    public static final class EmptyInputStream extends InputStream {
        public int read() {
            return -1;
        }

        public int read(byte b[])  {
            return -1;
        }

        public int read(byte b[], int off, int len) {
            return -1;
        }

        public long skip(long n) {
            return 0;
        }

        public int available() {
            return 0;
        }

        public void close() {
        }

        public synchronized void mark(int readlimit) {
        }

        public synchronized void reset() {
        }

        public boolean markSupported() {
            return false;
        }
    }
}
