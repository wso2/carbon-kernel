/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.kernel.utils;

/**
 * Simple tokenizer class. Used to parse data.
 * This class is taken from org.eclipse.osgi.framework.internal.core
 *
 * @since 5.0.0
 */
public class Tokenizer {
    private char value[];
    private int max;
    private int cursor;
    /**
     * Construct the tokenizer from the given string
     *
     * @param value String
     */
    public Tokenizer(String value) {
        this.value = value.toCharArray();
        max = this.value.length;
        cursor = 0;
    }

    private void skipWhiteSpace() {
        char[] val = value;
        int cur = cursor;

        for (; cur < max; cur++) {
            char c = val[cur];
            if ((c == ' ') || (c == '\t') || (c == '\n') || (c == '\r')) {
                continue;
            }
            break;
        }
        cursor = cur;
    }

    /**
     * Returns the next token delimited by any character in the provided terminals String.
     *
     * @param terminals String
     * @return String
     */
    public String getToken(String terminals) {
        skipWhiteSpace();
        char[] val = value;
        int cur = cursor;

        int begin = cur;
        for (; cur < max; cur++) {
            char c = val[cur];
            if ((terminals.indexOf(c) != -1)) {
                break;
            }
        }
        cursor = cur;
        int count = cur - begin;
        if (count > 0) {
            skipWhiteSpace();
            while (count > 0 && (val[begin + count - 1] == ' ' || val[begin + count - 1] == '\t')) {
                count--;
            }
            return new String(val, begin, count);
        }
        return null;
    }

    /**
     * Returns the next token delimited by any character in the provided terminals String. If the text content being
     * considered is within quotes, then return the entire string within quotes. If the text content within contains
     * escape characters, preserve it if it is provided as the second parameter.
     *
     * @param terminals String
     * @param preserveEscapes String
     * @return String
     */
    public String getString(String terminals, String preserveEscapes) {
        skipWhiteSpace();
        char[] val = value;
        int cur = cursor;

        if (cur < max) {
            if (val[cur] == '\"') { /* if a quoted string */
                StringBuilder sb = new StringBuilder();
                cur++; /* skip quote */
                char c = '\0';
                int begin = cur;
                for (; cur < max; cur++) {
                    c = val[cur];
                    // this is an escaped char
                    if (c == '\\') {
                        cur++; // skip the escape char
                        if (cur == max) {
                            break;
                        }
                        c = val[cur]; // include the escaped char
                        if (preserveEscapes != null && preserveEscapes.indexOf(c) != -1) {
                            sb.append('\\'); // must preserve escapes for c
                        }
                    } else if (c == '\"') {
                        break;
                    }
                    sb.append(c);
                }
                int count = cur - begin;
                if (c == '\"') {
                    cur++;
                }
                cursor = cur;
                if (count > 0) {
                    skipWhiteSpace();
                    return sb.toString();
                }
            } else { /* not a quoted string; same as token */
                return getToken(terminals);
            }
        }
        return null;
    }

    /**
     * Returns the next token delimited by any character in the provided terminals String. If the text content being
     * considered is within quotes, then return the entire string within quotes. If the text content within contains
     * escape characters, those are ignored.
     *
     * @param terminals String
     * @return String
     */
    public String getString(String terminals) {
        return getString(terminals, null);
    }

    /**
     * Returns the next character to be processed by the tokenizer.
     *
     * @return char
     */
    public char getChar() {
        int cur = cursor;
        if (cur < max) {
            cursor = cur + 1;
            return (value[cur]);
        }
        return ('\0'); /* end of value */
    }
}
