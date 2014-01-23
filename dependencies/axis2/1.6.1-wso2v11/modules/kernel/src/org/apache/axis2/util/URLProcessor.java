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

import java.net.MalformedURLException;
import java.util.StringTokenizer;

public class URLProcessor {
    public static final String DEFAULT_PACKAGE = "org.apache.axis2";

    /**
     * Method makePackageName.
     *
     * @param namespace
     * @return Returns String.
     */
    public static String makePackageName(String namespace) {

        String hostname = null;
        String path = "";

        // get the target namespace of the document
        try {
            java.net.URL u = new java.net.URL(namespace);

            hostname = u.getHost();
            path = u.getPath();
        } catch (MalformedURLException e) {
            if (namespace.indexOf(":") > -1) {
                hostname = namespace.substring(namespace.indexOf(":") + 1);

                // remove the leading /
                while (hostname.startsWith("/")) {
                    hostname = hostname.substring(1);
                }

                if (hostname.indexOf("/") > -1) {
                    hostname = hostname.substring(0, hostname.indexOf("/"));
                }
            } else {
                hostname = namespace.replace('/','.');
            }
        }

        // if we didn't file a hostname, bail
        if (hostname == null || hostname.length() == 0) {
            return null;
        }

        // convert illegal java identifier
        hostname = hostname.replace('-', '_');
        path = path.replace('-', '_');

        path = path.replace(':', '_');

        // chomp off last forward slash in path, if necessary
        if ((path.length() > 0) && (path.charAt(path.length() - 1) == '/')) {
            path = path.substring(0, path.length() - 1);
        }

        // tokenize the hostname and reverse it
        StringTokenizer st = new StringTokenizer(hostname, ".:");
        String[] words = new String[st.countTokens()];

        for (int i = 0; i < words.length; ++i) {
            words[i] = st.nextToken();
        }

        StringBuffer sb = new StringBuffer(namespace.length());

        for (int i = words.length - 1; i >= 0; --i) {
            addWordToPackageBuffer(sb, words[i], (i == words.length - 1));
        }

        // tokenize the path
        StringTokenizer st2 = new StringTokenizer(path, "/");

        while (st2.hasMoreTokens()) {
            addWordToPackageBuffer(sb, st2.nextToken(), false);
        }

        return sb.toString().toLowerCase();
    }


    /**
     * Massages <tt>word</tt> into a form suitable for use in a Java package name.
     * Appends it to the target string buffer with a <tt>.</tt> delimiter if
     * <tt>word</tt> is not the first word in the package name.
     *
     * @param sb        the buffer to append to
     * @param word      the word to append
     * @param firstWord a flag indicating whether this is the first word
     */
    private static void addWordToPackageBuffer(StringBuffer sb, String word,
                                               boolean firstWord) {

        if (JavaUtils.isJavaKeyword(word)) {
            word = JavaUtils.makeNonJavaKeyword(word);
        }

        // separate with dot after the first word
        if (!firstWord) {
            sb.append('.');
        }

        // prefix digits with underscores
        if (Character.isDigit(word.charAt(0))) {
            sb.append('_');
        }

        // replace periods with underscores
        if (word.indexOf('.') != -1) {
            char[] buf = word.toCharArray();

            for (int i = 0; i < word.length(); i++) {
                if (buf[i] == '.') {
                    buf[i] = '_';
                }
            }

            word = new String(buf);
        }

        sb.append(word);
    }
}
