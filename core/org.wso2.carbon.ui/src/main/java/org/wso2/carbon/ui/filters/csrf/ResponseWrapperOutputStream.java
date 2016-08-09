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

package org.wso2.carbon.ui.filters.csrf;

import javax.servlet.ServletOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class ResponseWrapperOutputStream extends ServletOutputStream {
    private DataOutputStream stream = null;

    public ResponseWrapperOutputStream(OutputStream os) {
        stream = new DataOutputStream(os);
    }

    @Override
    public void print(String arg0) throws IOException {
        int len = arg0.length();

        for (int i = 0; i < len; i++) {
            char c = arg0.charAt(i);

            write(c);
        }
    }

    @Override
    public void print(boolean arg0) throws IOException {
        print(arg0);
    }

    @Override
    public void print(char arg0) throws IOException {
        print(arg0);
    }

    @Override
    public void print(int arg0) throws IOException {
        print(arg0);
    }

    @Override
    public void print(long arg0) throws IOException {
        print(arg0);
    }

    @Override
    public void print(float arg0) throws IOException {
        print(arg0);
    }

    @Override
    public void print(double arg0) throws IOException {
        print(arg0);
    }

    @Override
    public void println() throws IOException {
        print("\r\n");
    }

    @Override
    public void println(String arg0) throws IOException {
        print(arg0);
        println();
    }

    @Override
    public void println(boolean arg0) throws IOException {
        println(String.valueOf(arg0));
    }

    @Override
    public void println(char arg0) throws IOException {
        println(String.valueOf(arg0));
    }

    @Override
    public void println(int arg0) throws IOException {
        println(String.valueOf(arg0));
    }

    @Override
    public void println(long arg0) throws IOException {
        println(String.valueOf(arg0));
    }

    @Override
    public void println(float arg0) throws IOException {
        println(String.valueOf(arg0));
    }

    @Override
    public void println(double arg0) throws IOException {
        println(String.valueOf(arg0));
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        stream.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        stream.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        stream.write(b);
    }
}
