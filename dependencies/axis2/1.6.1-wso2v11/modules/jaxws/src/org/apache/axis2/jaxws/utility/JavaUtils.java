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

package org.apache.axis2.jaxws.utility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/** Common Java Utilites */
public class JavaUtils extends org.apache.axis2.util.JavaUtils {

    private static Log log = LogFactory.getLog(JavaUtils.class);
    
    /** Private Constructor...All methods of this class are static */
    private JavaUtils() {
    }

       
    /**
     * @param namespace
     * @return List of String containing 1 or 2 packages
     */
    public static List getPackagesFromNamespace(String namespace) {
        List list = new ArrayList();
        // Get a package using JAXB Rules
        String jaxbPkg = getPackageFromNamespace(namespace, true);
        list.add(jaxbPkg);
        if (jaxbPkg.contains("_")) {
            if (log.isDebugEnabled()) {
                log.debug("Calling getPackageFromNamespace with wsimport rule:" + namespace);
            }
            String altPkg = getPackageFromNamespace(namespace, false);  // Using wsimport rule
            list.add(altPkg);
        }
        return list;    
    }

    /**
     * Namespace 2 Package algorithm as defined by the JAXB Specification
     *
     * @param Namespace
     * @return String representing Namespace
     * @see getPackagesFromNamespace
     */
    public static String getPackageFromNamespace(String namespace) {
        return getPackageFromNamespace(namespace, true);
    }
      
    /**
     * @param Namespace
     * @param apend underscore to keyword
     * @return String representing Namespace
     */
    public static String getPackageFromNamespace(String namespace, 
            boolean appendUnderscoreToKeyword) {
        // The following steps correspond to steps described in the JAXB Specification

        if (log.isDebugEnabled()) {
            log.debug("namespace (" +namespace +")");
        }
        // Step 1: Scan off the host name
        String hostname = null;
        String path = null;
        try {
            URL url = new URL(namespace);
            hostname = url.getHost();
            path = url.getPath();
        }
        catch (MalformedURLException e) {
            // No FFDC code needed
            // If a MalformedURLException occurs, then
            // just simply get one string and put it in the hostname.
            // In such cases the path will remain empty.
            // This code is necessary so that we can process namespaces
            // like "urn:acme" or simply "sampleNamespace"
            if (namespace.indexOf(":") > -1) {
                // Brain-dead code to skip over the protocol
                hostname = namespace.substring(namespace.indexOf(":") + 1);
            } else {
                hostname = namespace;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("hostname (" +hostname +")");
            log.debug("path (" +path +")");
        }
        

        // Step 3: Tokenize the host name using ":" and "/"
        StringTokenizer st = new StringTokenizer(hostname, ":/");

        ArrayList<String> wordList = new ArrayList<String>();

        //Read Hostname first.
        while (st != null && st.hasMoreTokens()) {
            wordList.add(st.nextToken());
        }
        //Read rest Of the path now
        if (path != null) {
            StringTokenizer pathst = new StringTokenizer(path, "/");
            while (pathst != null && pathst.hasMoreTokens()) {
                wordList.add(pathst.nextToken());
            }
        }
        String[] words = wordList.toArray(new String[0]);

        // Now do step 2: Strip off the trailing "." (i.e. strip off .html)
        if (words != null && words.length > 1) {
            String lastWord = words[words.length - 1];
            int index = lastWord.lastIndexOf('.');
            if (index > 0) {
                words[words.length - 1] = lastWord.substring(0, index);
            }
        }

        // Step 4: Unescape each escape sequence
        // TODO I don't know what to do here.

        // Step 5: If protocol is urn, replace - with . in the first word
        if (namespace.startsWith("urn:")) {
            words[0] = replace(words[0], "-", ".");
        }

        // Step 6: Tokenize the first word with "." and reverse the order. (the www is also removed).
        // TODO This is not exactly equivalent to the JAXB Rule.
        ArrayList<String> list = new ArrayList<String>();
        if (words.length > 0) {
            StringTokenizer st2 = new StringTokenizer(words[0], ".");
        
            while (st2.hasMoreTokens()) {
                // Add the strings so they are in reverse order
                list.add(0, st2.nextToken());
            }
        }
        
        // Remove www
        if (list.size() > 0) {
            String last = list.get(list.size() - 1);
            if (last.equals("www")) {
                list.remove(list.size() - 1);
            }
        }
        // Now each of words is represented by list
        for (int i = 1; i < words.length; i++) {
            list.add(words[i]);
        }

        // Step 7: lowercase each word
        for (int i = 0; i < list.size(); i++) {
            String word = list.remove(i);
            word = word.toLowerCase();
            list.add(i, word);
        }

        // Step 8: make into and an appropriate java word
        for (int i = 0; i < list.size(); i++) {
            String word = list.get(i);

            // 8a: Convert special characters to underscore
            // Convert non-java words to underscore.
            // TODO: Need to do this for all chars..not just hyphens
            word = replace(word, "-", "_");

            // 8b: Append _ to java keywords
            if (JavaUtils.isJavaKeyword(word)) {
                if (appendUnderscoreToKeyword) {
                    word = word + "_";  // This is defined by the JAXB Spec
                } else {
                    word = "_" +word;  // Apparently wsimport can generate this style
                }
            }
            // 8c: prepend _ if first character cannot be the first character of a java identifier
            if (!Character.isJavaIdentifierStart(word.charAt(0))) {
                word = "_" + word;
            }

            list.set(i, word);
        }

        // Step 9: Concatenate and return
        String name = "";
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                name = list.get(0);
            } else {
                name = name + "." + list.get(i);
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("package name (" +name +")");
        }
        return name;
    }


    /**
     * Get a string containing the stack of the current location
     *
     * @return String
     */
    public static String stackToString() {
        return stackToString(new RuntimeException());
    }

    /**
     * Get a string containing the stack of the specified exception
     *
     * @param e
     * @return
     */
    public static String stackToString(Throwable e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.BufferedWriter bw = new java.io.BufferedWriter(sw);
        java.io.PrintWriter pw = new java.io.PrintWriter(bw);
        e.printStackTrace(pw);
        pw.close();
        String text = sw.getBuffer().toString();
        // Jump past the throwable
        text = text.substring(text.indexOf("at"));
        text = replace(text, "at ", "DEBUG_FRAME = ");
        return text;
    }
    
    /**
     * Get checked exception
     * @param throwable Throwable
     * @param method Method
     * @return Class of the checked exception or null
     */
    public static Class getCheckedException(Throwable throwable, Method method) {
        if (method == null) {
            return null;
        }
        Class[] exceptions = method.getExceptionTypes();
        if (exceptions != null) {
            for (int i=0; i< exceptions.length; i++ ) {
                if (exceptions[i].isAssignableFrom(throwable.getClass())) {
                    return exceptions[i];
                }
            }
        }
        return null;
    }
        
    /**
     * Convert a String to a URI, handling special characters in the String such as
     * spaces.
     * 
     * @param pathString The String to be converted to a URI
     * @return a URI or null if the String can't be converted.
     */
    public static URI createURI(String pathString) {
        URI pathURI = null;
        if (pathString == null || "".equals(pathString)) {
            if (log.isDebugEnabled()) {
                log.debug("Path string argument is invalid [" + pathString + "]; returning null");
            }
            return null;
        }

        try {
            pathURI = new URI(pathString);
        }
        catch (URISyntaxException ex1) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to create URI from [" + pathString + 
                          "], trying alternative approach");
            }
            /*
             * The URI creation requires special characters, such as spaces, be escaped or
             * converted.  The 5 argument constuctor will do that for us.
             */
            try {
                pathURI = new URI(null, null, pathString, null);
            } catch (URISyntaxException ex2) {
                if (log.isTraceEnabled()) {
                    log.trace("Unable to create URI using alternative approach; returning null.  Exception caught during inital attempt: "
                              + JavaUtils.stackToString(ex1));
                    log.trace("Exception caught during alternet attemt "
                              + JavaUtils.stackToString(ex2));
                }
                log.error(ex2.toString(), ex2);
            }
        }
        return pathURI;
    }
}
