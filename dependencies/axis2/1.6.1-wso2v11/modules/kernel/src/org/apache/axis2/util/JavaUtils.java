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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * JavaUtils
 */
public class JavaUtils {
    /**
     * These are java keywords as specified at the following URL (sorted alphabetically).
     * http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html#229308
     * Note that false, true, and null are not strictly keywords; they are literal values,
     * but for the purposes of this array, they can be treated as literals.
     * ****** PLEASE KEEP THIS LIST SORTED IN ASCENDING ORDER ******
     */
    static final String keywords[] =
            {
                    "abstract", "assert", "boolean", "break", "byte", "case",
                    "catch", "char", "class", "const", "continue",
                    "default", "do", "double", "else", "extends",
                    "false", "final", "finally", "float", "for",
                    "goto", "if", "implements", "import", "instanceof",
                    "int", "interface", "long", "native", "new",
                    "null", "package", "private", "protected", "public",
                    "return", "short", "static", "strictfp", "super",
                    "switch", "synchronized", "this", "throw", "throws",
                    "transient", "true", "try", "void", "volatile",
                    "while"
            };

    /**
     * Collator for comparing the strings
     */
    static final Collator englishCollator = Collator.getInstance(Locale.ENGLISH);

    /**
     * Use this character as suffix
     */
    static final char keywordPrefix = '_';

    /**
     * Is this an XML punctuation character?
     */
    private static boolean isPunctuation(char c) {
        return '-' == c
                || '.' == c
                || ':' == c
                || '\u00B7' == c
                || '\u0387' == c
                || '-' == c
                || '\u06DD' == c
                || '\u06DE' == c;
    } // isPunctuation

    /**
     * Checks if the input string is a valid java keyword.
     *
     * @return Returns boolean.
     */
    public static boolean isJavaKeyword(String keyword) {
        // None of the java keywords have uppercase characters
        if (hasUpperCase(keyword)) {
            return false;
        }
        return (Arrays.binarySearch(keywords, keyword, englishCollator) >= 0);
    }

    /**
     * Check if the word has any uppercase letters
     *
     * @param word
     * @return
     */
    public static boolean hasUpperCase(String word) {
        if (word == null) {
            return false;
        }
        int len = word.length();
        for (int i = 0; i < len; i++) {
            if (Character.isUpperCase(word.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Turns a java keyword string into a non-Java keyword string.  (Right now
     * this simply means appending an underscore.)
     */
    public static String makeNonJavaKeyword(String keyword) {
        return keywordPrefix + keyword;
    }

    public static String xmlNameToJava(String name) {
        // protect ourselves from garbage
        if (name == null || name.length() == 0) {
            return name;
        }

        char[] nameArray = name.toCharArray();
        int nameLen = name.length();
        StringBuffer result = new StringBuffer(nameLen);
        boolean wordStart = false;

        // The mapping indicates to convert first character.
        int i = 0;
        while (i < nameLen
                && (isPunctuation(nameArray[i])
                || !Character.isJavaIdentifierStart(nameArray[i]))) {
            i++;
        }
        if (i < nameLen) {
            // Decapitalization code used to be here, but we use the
            // Introspector function now after we filter out all bad chars.

            result.append(nameArray[i]);
            //wordStart = !Character.isLetter(nameArray[i]);
            wordStart = !Character.isLetter(nameArray[i]) && nameArray[i] != "_".charAt(0);
        } else {
            // The identifier cannot be mapped strictly according to
            // JSR 101
            if (Character.isJavaIdentifierPart(nameArray[0])) {
                result.append("_").append(nameArray[0]);
            } else {
                // The XML identifier does not contain any characters
                // we can map to Java.  Using the length of the string
                // will make it somewhat unique.
                result.append("_").append(nameArray.length);
            }
        }

        // The mapping indicates to skip over
        // all characters that are not letters or
        // digits.  The first letter/digit
        // following a skipped character is
        // upper-cased.
        for (++i; i < nameLen; ++i) {
            char c = nameArray[i];

            // if this is a bad char, skip it and remember to capitalize next
            // good character we encounter
            if (isPunctuation(c) || !Character.isJavaIdentifierPart(c)) {
                wordStart = true;
                continue;
            }
            if (wordStart && Character.isLowerCase(c)) {
                result.append(Character.toUpperCase(c));
            } else {
                result.append(c);
            }
            // If c is not a character, but is a legal Java
            // identifier character, capitalize the next character.
            // For example:  "22hi" becomes "22Hi"
            //wordStart = !Character.isLetter(c);
            wordStart = !Character.isLetter(c) && c != "_".charAt(0);
        }

        // covert back to a String
        String newName = result.toString();

        // check for Java keywords
        if (isJavaKeyword(newName)) {
            newName = makeNonJavaKeyword(newName);
        }

        return newName;
    } // xmlNameToJava

    /**
     * Capitalizes the first character of the name.
     *
     * @param name
     * @return Returns String.
     */
    public static String capitalizeFirstChar(String name) {

        if ((name == null) || name.length() == 0) {
            return name;
        }

        char start = name.charAt(0);

        if (Character.isLowerCase(start)) {
            start = Character.toUpperCase(start);

            return start + name.substring(1);
        }

        return name;
    }    // capitalizeFirstChar

    /**
     * converts an xml name to a java identifier
     *
     * @param name
     * @return java identifier
     */

    public static String xmlNameToJavaIdentifier(String name) {
        String javaName = xmlNameToJava(name);
        // convert the first letter to lowercase
        if ((javaName != null) && (javaName.length() > 0)) {
            javaName = javaName.substring(0, 1).toLowerCase() + javaName.substring(1);
        }

        return javaName;
    }

    /**
     * Tests the String 'value':
     * return 'false' if its 'false', '0', or 'no' - else 'true'
     * <p/>
     * Follow in 'C' tradition of boolean values:
     * false is specific (0), everything else is true;
     */
    public static boolean isTrue(String value) {
        return !isFalseExplicitly(value);
    }

    /**
     * Tests the String 'value':
     * return 'true' if its 'true', '1', or 'yes' - else 'false'
     */
    public static boolean isTrueExplicitly(String value) {
        return value != null &&
                (value.equalsIgnoreCase("true") ||
                        value.equals("1") ||
                        value.equalsIgnoreCase("yes"));
    }

    /**
     * Tests the Object 'value':
     * if its null, return default.
     * if its a Boolean, return booleanValue()
     * if its an Integer,  return 'false' if its '0' else 'true'
     * if its a String, return isTrueExplicitly((String)value).
     * All other types return 'true'
     */
    public static boolean isTrueExplicitly(Object value, boolean defaultVal) {
        if (value == null) {
            return defaultVal;
        }
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        if (value instanceof Integer) {
            return ((Integer) value).intValue() != 0;
        }
        if (value instanceof String) {
            return isTrueExplicitly((String) value);
        }
        return defaultVal;
    }

    public static boolean isTrueExplicitly(Object value) {
        return isTrueExplicitly(value, false);
    }

    /**
     * Tests the Object 'value':
     * if its null, return default.
     * if its a Boolean, return booleanValue()
     * if its an Integer,  return 'false' if its '0' else 'true'
     * if its a String, return 'false' if its 'false', 'no', or '0' - else 'true'
     * All other types return 'true'
     */
    public static boolean isTrue(Object value, boolean defaultVal) {
        return !isFalseExplicitly(value, !defaultVal);
    }

    public static boolean isTrue(Object value) {
        return isTrue(value, false);
    }

    /**
     * Tests the String 'value':
     * return 'true' if its 'false', '0', or 'no' - else 'false'
     * <p/>
     * Follow in 'C' tradition of boolean values:
     * false is specific (0), everything else is true;
     */
    public static boolean isFalse(String value) {
        return isFalseExplicitly(value);
    }

    /**
     * Tests the String 'value':
     * return 'true' if its null, 'false', '0', or 'no' - else 'false'
     */
    public static boolean isFalseExplicitly(String value) {
        return value == null ||
                value.equalsIgnoreCase("false") ||
                value.equals("0") ||
                value.equalsIgnoreCase("no");
    }

    /**
     * Tests the Object 'value':
     * if its null, return default.
     * if its a Boolean, return !booleanValue()
     * if its an Integer,  return 'true' if its '0' else 'false'
     * if its a String, return isFalseExplicitly((String)value).
     * All other types return 'false'
     */
    public static boolean isFalseExplicitly(Object value, boolean defaultVal) {
        if (value == null) {
            return defaultVal;
        }
        if (value instanceof Boolean) {
            return !((Boolean) value).booleanValue();
        }
        if (value instanceof Integer) {
            return ((Integer) value).intValue() == 0;
        }
        if (value instanceof String) {
            return isFalseExplicitly((String) value);
        }
        return false;
    }

    public static boolean isFalseExplicitly(Object value) {
        return isFalseExplicitly(value, true);
    }

    /**
     * Tests the Object 'value':
     * if its null, return default.
     * if its a Boolean, return booleanValue()
     * if its an Integer,  return 'false' if its '0' else 'true'
     * if its a String, return 'false' if its 'false', 'no', or '0' - else 'true'
     * All other types return 'true'
     */
    public static boolean isFalse(Object value, boolean defaultVal) {
        return isFalseExplicitly(value, defaultVal);
    }

    public static boolean isFalse(Object value) {
        return isFalse(value, true);
    }

    public static boolean isJavaId(String id) {
        if (id == null || id.length() == 0 || isJavaKeyword(id)) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(id.charAt(0))) {
            return false;
        }
        for (int i = 1; i < id.length(); i++) {
            if (!Character.isJavaIdentifierPart(id.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * An empty immutable <code>String</code> array.
     */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * <p>Splits the provided text into an array, separator specified.
     * This is an alternative to using StringTokenizer.</p>
     * <p/>
     * <p>The separator is not included in the returned String array.
     * Adjacent separators are treated as one separator.</p>
     * <p/>
     * <p>A <code>null</code> input String returns <code>null</code>.</p>
     * <p/>
     * <pre>
     * StringUtils.split(null, *)         = null
     * StringUtils.split("", *)           = []
     * StringUtils.split("a.b.c", '.')    = ["a", "b", "c"]
     * StringUtils.split("a..b.c", '.')   = ["a", "b", "c"]
     * StringUtils.split("a:b:c", '.')    = ["a:b:c"]
     * StringUtils.split("a\tb\nc", null) = ["a", "b", "c"]
     * StringUtils.split("a b c", ' ')    = ["a", "b", "c"]
     * </pre>
     *
     * @param str           the String to parse, may be null
     * @param separatorChar the character used as the delimiter,
     *                      <code>null</code> splits on whitespace
     * @return an array of parsed Strings, <code>null</code> if null String input
     */
    public static String[] split(String str, char separatorChar) {
        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return EMPTY_STRING_ARRAY;
        }
        List list = new ArrayList();
        int i = 0, start = 0;
        boolean match = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                }
                start = ++i;
                continue;
            }
            match = true;
            i++;
        }
        if (match) {
            list.add(str.substring(start, i));
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public static Class getWrapperClass(Class primitive) {
        if (primitive == int.class) {
            return java.lang.Integer.class;
        } else if (primitive == short.class) {
            return java.lang.Short.class;
        } else if (primitive == boolean.class) {
            return java.lang.Boolean.class;
        } else if (primitive == byte.class) {
            return java.lang.Byte.class;
        } else if (primitive == long.class) {
            return java.lang.Long.class;
        } else if (primitive == double.class) {
            return java.lang.Double.class;
        } else if (primitive == float.class) {
            return java.lang.Float.class;
        } else if (primitive == char.class) {
            return java.lang.Character.class;
        }

        return null;
    }

    public static Class getWrapperClass(String primitive) {
        if (primitive.equals("int")) {
            return java.lang.Integer.class;
        } else if (primitive.equals("short")) {
            return java.lang.Short.class;
        } else if (primitive.equals("boolean")) {
            return java.lang.Boolean.class;
        } else if (primitive.equals("byte")) {
            return java.lang.Byte.class;
        } else if (primitive.equals("long")) {
            return java.lang.Long.class;
        } else if (primitive.equals("double")) {
            return java.lang.Double.class;
        } else if (primitive.equals("float")) {
            return java.lang.Float.class;
        } else if (primitive.equals("char")) {
            return java.lang.Character.class;
        }

        return null;
    }

    /**
     * Scans the parameter string for the parameter search ignoring case when
     * comparing characters.
     *
     * @param string
     * @param search If test is empty -1 is always returned.
     * @return -1 if the string was not found or the index of the first matching
     *         character
     */
    public static int indexOfIgnoreCase(final String string,
                                        final String search) {
        int index = -1;
        final int stringLength = string.length();
        final int testLength = search.length();
        if (stringLength > 1 || testLength > 1) {
            final char firstCharOfTest = Character.toLowerCase(search.charAt(0));
            final int lastStringCharacterToCheck = stringLength - testLength + 1;

            for (int i = 0; i < lastStringCharacterToCheck; i++) {
                if (firstCharOfTest == Character.toLowerCase(string.charAt(i))) {
                    index = i;
                    for (int j = 1; j < testLength; j++) {
                        final char c = string.charAt(i + j);
                        final char otherChar = search.charAt(j);
                        if (Character.toLowerCase(c) != Character.toLowerCase(otherChar)) {
                            index = -1;
                            break;
                        }
                    }
                    if (-1 != index) {
                        break;
                    }
                }
            }
        }
        return index;
    }
    
    /**
     * replace: Like String.replace except that the old new items are strings.
     *
     * @param name string
     * @param oldT old text to replace
     * @param newT new text to use
     * @return replacement string
     */
    public static final String replace(String name,
                                       String oldT, String newT) {

        if (name == null) return "";

        // Create a string buffer that is twice initial length.
        // This is a good starting point.
        StringBuffer sb = new StringBuffer(name.length() * 2);

        int len = oldT.length();
        try {
            int start = 0;
            int i = name.indexOf(oldT, start);

            while (i >= 0) {
                sb.append(name.substring(start, i));
                sb.append(newT);
                start = i + len;
                i = name.indexOf(oldT, start);
            }
            if (start < name.length())
                sb.append(name.substring(start));
        } catch (NullPointerException e) {
            // No FFDC code needed
        }

        return new String(sb);
    }
    /**
     * Get a string containing the stack of the current location.
     * Note This utility is useful in debug scenarios to dump out 
     * the call stack.
     *
     * @return String
     */
    public static String callStackToString() {
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
     * Mimics the default Object.toString() produces a string of the form:
     * 
     *      obj.getClass().getName() + "@" + object's hashCode.
     * 
     * This method can be used to print the debug message when you want
     * just the short name or if using the toString will cause full expansion
     * of underlying data structures.
     * 
     * The returned value can also be used as an identity key.
     * 
     * @return obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
     */
    public static String getObjectIdentity(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
    }

}
