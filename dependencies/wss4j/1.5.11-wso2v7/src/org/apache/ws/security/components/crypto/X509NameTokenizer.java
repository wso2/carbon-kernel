/**
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
package org.apache.ws.security.components.crypto;

import java.util.ArrayList;

import org.apache.xml.security.utils.RFC2253Parser;

/**
 * class for breaking up an X500 Name into it's component tokens, ala
 * java.util.StringTokenizer. We need this class as some of the
 * lightweight Java environment don't support classes like
 * StringTokenizer.
 */
public class X509NameTokenizer {

    private final java.util.List tokens = new ArrayList();
    private int index = 0;

    public X509NameTokenizer(String dn) {
       final String _DN = RFC2253Parser.normalize(dn);
       int i = 0;
       int l = 0;
       int k;
       for (int j = 0; (k = _DN.indexOf(",", j)) >= 0; j = k + 1) {
          l += countQuotes(_DN, j, k);
          if ((k > 0) && (_DN.charAt(k - 1) != '\\') && (l % 2) == 0) {
             tokens.add(_DN.substring(i, k).trim());
             i = k + 1;
             l = 0;
          }
       }
       if (_DN.trim().length() != 0) {
           tokens.add(trim(_DN.substring(i)));
       }
    }

    public boolean hasMoreTokens() {
        return (index < tokens.size());
    }

    public String nextToken() {
        if (hasMoreTokens()) {
            return (String) tokens.get(index++);
        } else {
            return "";
        }
    }


    /**
     * Returns the number of Quotation from i to j
     *
     * @param s
     * @param i
     * @param j
     * @return number of quotes
     */
    private static int countQuotes(String s, int i, int j) {
       int k = 0;
       for (int l = i; l < j; l++) {
          if (s.charAt(l) == '"') {
             k++;
          }
       }
       return k;
    }

    /**
     * Method trim
     *
     * @param str
     * @return the string
     */
    private static String trim(String str) {
       String trimed = str.trim();
       int i = str.indexOf(trimed) + trimed.length();
       if ((str.length() > i) 
           && trimed.endsWith("\\")
           && !trimed.endsWith("\\\\")
           && (str.charAt(i) == ' ')) {
         trimed = trimed + " ";
       }
       return trimed;
    }

}
