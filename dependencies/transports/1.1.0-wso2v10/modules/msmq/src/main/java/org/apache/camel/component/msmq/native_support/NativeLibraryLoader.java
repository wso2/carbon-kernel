/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.msmq.native_support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NativeLibraryLoader {

	public static void loadLibrary(String libname) throws IOException {
    	String actualLibName = System.mapLibraryName(libname);
    	File lib = extractResource(actualLibName);
        System.load(lib.getAbsolutePath());
    }
    
    static File extractResource(String resourcename) throws IOException {
    	InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcename);
        if(in == null)
            throw new IOException("Unable to find library "+resourcename+" on classpath");
    	File tmpDir = new File(System.getProperty("java.tmpdir","tmplib"));
    	if (!tmpDir.exists() ) {
            if( !tmpDir.mkdirs())
                throw new IOException("Unable to create JNI library working directory "+tmpDir);
        }
        File outfile = new File(tmpDir,resourcename);
        OutputStream out = new FileOutputStream(outfile);
        copy(in,out);
        out.close();
        in.close();
        return outfile;
    }
    
    static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] tmp = new byte[8192];
        int len = 0;
        while (true) {
            len = in.read(tmp);
            if (len <= 0) {
                break;
            }
            out.write(tmp, 0, len);
        }
    }

}
