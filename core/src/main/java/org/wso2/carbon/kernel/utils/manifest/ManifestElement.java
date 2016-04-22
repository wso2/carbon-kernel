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
package org.wso2.carbon.kernel.utils.manifest;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.utils.Tokenizer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * This class represents a single manifest element.  A manifest element must consist of a single
 * {@link String} value.  The {@link String} value may be split up into component values each
 * separated by a semi-colon (';').  A manifest element may optionally have a set of
 * attribute and directive values associated with it. The general syntax of a manifest element is as follows:
 * <pre>
 * ManifestElement ::= component (';' component)* (';' parameter)*
 * component ::= ([^;,:="\#x0D#x0A#x00])+ | quoted-string
 * quoted-string::= '"' ( [^"\#x0D#x0A#x00] | '\"'| '\\')* '"'
 * parameter ::= directive | attribute
 * directive ::= token ':=' argument
 * attribute ::= token '=' argument
 * argument ::= extended  | quoted-string
 * token ::= ( alphanum | '_' | '-' )+
 * extended ::= ( alphanum | '_' | '-' | '.' )+
 * </pre>
 * <p>
 * For example, the following is an example of a manifest element to the <tt>Export-Package</tt> header:
 * <pre>
 * org.osgi.framework; specification-version="1.2"; another-attr="examplevalue"
 * </pre>
 * <p>
 * This manifest element has a value of <tt>org.osgi.framework</tt> and it has two attributes,
 * <tt>specification-version</tt> and <tt>another-attr</tt>.
 * <p>
 * The following manifest element is an example of a manifest element that has multiple
 * components to its value:
 * <pre>
 * code1.jar;code2.jar;code3.jar;attr1=value1;attr2=value2;attr3=value3
 * </pre>
 * <p>
 * This manifest element has a value of <tt>code1.jar;code2.jar;code3.jar</tt>.
 * This is an example of a multiple component value.  This value has three
 * components: <tt>code1.jar</tt>, <tt>code2.jar</tt>, and <tt>code3.jar</tt>.
 * <p>
 * If components contain delimiter characters (e.g ';', ',' ':' "=") then it must be
 * a quoted string.  For example, the following is an example of a manifest element
 * that has multiple components containing delimiter characters:
 * <pre>
 * "component ; 1"; "component , 2"; "component : 3"; attr1=value1; attr2=value2; attr3=value3
 * </pre>
 * <p>
 * This manifest element has a value of <tt>"component ; 1"; "component , 2"; "component : 3"</tt>.
 * This value has three components: <tt>"component ; 1"</tt>, <tt>"component , 2"</tt>, <tt>"component : 3"</tt>.
 * <p>
 * This class is not intended to be subclassed by clients.
 * <p>
 * This class is taken from org.eclipse.osgi.util
 *
 * @since 5.0.0
 */
public class ManifestElement {

    private static final Logger logger = LoggerFactory.getLogger(ManifestElement.class);

    private static final String MANIFEST_INVALID_HEADER_EXCEPTION = "Invalid header found.";

    private final String manifestHeaderName;

    /**
     * The value of the manifest element.
     */
    private final String mainValue;

    /**
     * The table of attributes for the manifest element.
     */
    private Hashtable<String, Object> attributes;

    /**
     * The table of directives for the manifest element.
     */
    private Hashtable<String, Object> directives;

    /**
     * Containing OSGi bundle.
     */
    private final Bundle bundle;

    /**
     * Constructs an empty manifest element with no value or attributes.
     */
    private ManifestElement(String manifestHeaderName, String value, Bundle bundle) {
        this.manifestHeaderName = manifestHeaderName;
        this.mainValue = value;
        this.bundle = bundle;
    }

    /**
     * Returns the name of the manifest header of this manifest element.
     * <p>
     * e.g. Provide-Capability
     * e.g. Bundle-Version
     *
     * @return the name of the manifest header
     */
    public String getManifestHeaderName() {
        return manifestHeaderName;
    }

    /**
     * Returns the value of the manifest element.  The value returned is the
     * complete value up to the first attribute or directive.  For example, the
     * following manifest element:
     * <pre>
     * test1.jar;test2.jar;test3.jar;selection-filter="(os.name=Windows XP)"
     * </pre>
     * <p>
     * This manifest element has a value of <tt>test1.jar;test2.jar;test3.jar</tt>
     *
     * @return the value of the manifest element.
     */
    public String getValue() {
        return mainValue;
    }

    /**
     * Returns the value for the specified attribute or <code>null</code> if it does
     * not exist.  If the attribute has multiple values specified then the last value
     * specified is returned. For example the following manifest element:
     * <pre>
     * elementvalue; myattr="value1"; myattr="value2"
     * </pre>
     * <p>
     * specifies two values for the attribute key <tt>myattr</tt>.  In this case <tt>value2</tt>
     * will be returned because it is the last value specified for the attribute
     * <tt>myattr</tt>.
     *
     * @param key the attribute key to return the value for
     * @return the attribute value or <code>null</code>
     */
    public String getAttribute(String key) {
        return getTableValue(attributes, key);
    }

    /**
     * Returns an array of values for the specified attribute or
     * <code>null</code> if the attribute does not exist.
     *
     * @param key the attribute key to return the values for
     * @return the array of attribute values or <code>null</code>
     * @see #getAttribute(String)
     */
    public String[] getAttributes(String key) {
        return getTableValues(attributes, key);
    }

    /**
     * Returns an enumeration of attribute keys for this manifest element or
     * <code>null</code> if none exist.
     *
     * @return the enumeration of attribute keys or null if none exist.
     */
    public Enumeration<String> getKeys() {
        return getTableKeys(attributes);
    }

    /**
     * Returns the bundle in which this manifest elements resides.
     *
     * @return the OSGi bundle
     */
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Add an attribute to this manifest element.
     *
     * @param key   the key of the attribute
     * @param value the value of the attribute
     */
    private void addAttribute(String key, String value) {
        attributes = addTableValue(attributes, key, value);
    }

    /**
     * Returns an array of string values for the specified directives or
     * <code>null</code> if it does not exist.
     *
     * @param key the directive key to return the values for
     * @return the array of directive values or <code>null</code>
     */
    public String[] getDirectives(String key) {
        return getTableValues(directives, key);
    }

    /**
     * Return an enumeration of directive keys for this manifest element or
     * <code>null</code> if there are none.
     *
     * @return the enumeration of directive keys or <code>null</code>
     */
    public Enumeration<String> getDirectiveKeys() {
        return getTableKeys(directives);
    }

    /**
     * Add a directive to this manifest element.
     *
     * @param key   the key of the attribute
     * @param value the value of the attribute
     */
    private void addDirective(String key, String value) {
        directives = addTableValue(directives, key, value);
    }

    /**
     * Return the last value associated with the given key in the specified table.
     *
     * @param table Hashtable&lt;String, Object&gt;
     * @param key   String
     * @return String
     */
    private String getTableValue(Hashtable<String, Object> table, String key) {
        if (table == null) {
            return null;
        }
        Object result = table.get(key);
        if (result == null) {
            return null;
        }
        if (result instanceof String) {
            return (String) result;
        }
        @SuppressWarnings("unchecked")
        List<String> valueList = (List<String>) result;
        //return the last value
        return valueList.get(valueList.size() - 1);
    }

    /**
     * Return the values associated with the given key in the specified table.
     *
     * @param table Hashtable&lt;String, Object&gt;
     * @param key   String
     * @return String[]
     */
    private String[] getTableValues(Hashtable<String, Object> table, String key) {
        if (table == null) {
            return new String[]{};
        }
        Object result = table.get(key);
        if (result == null) {
            return new String[]{};
        }
        if (result instanceof String) {
            return new String[]{(String) result};
        }
        @SuppressWarnings("unchecked")
        List<String> valueList = (List<String>) result;
        return valueList.toArray(new String[valueList.size()]);
    }

    /**
     * Return an enumeration of table keys for the specified table.
     *
     * @param table Hashtable&lt;String, Object&gt;
     * @return Enumeration&lt;String&gt;
     */
    private Enumeration<String> getTableKeys(Hashtable<String, Object> table) {
        if (table == null) {
            return null;
        }
        return table.keys();
    }

    /**
     * Add the given key/value association to the specified table. If an entry already exists
     * for this key, then create an array list from the current value (if necessary) and
     * append the new value to the end of the list.
     *
     * @param table Hashtable&lt;String, Object&gt;
     * @param key   String
     * @param value String
     * @return Hashtable&lt;String, Object&gt;
     */
    @SuppressWarnings("unchecked")
    private Hashtable<String, Object> addTableValue(Hashtable<String, Object> table, String key, String value) {
        if (table == null) {
            table = new Hashtable<>(7);
        }
        Object curValue = table.get(key);
        if (curValue != null) {
            List<String> newList;
            // create a list to contain multiple values
            if (curValue instanceof List) {
                newList = (List<String>) curValue;
            } else {
                newList = new ArrayList<>(5);
                newList.add((String) curValue);
            }
            newList.add(value);
            table.put(key, newList);
        } else {
            table.put(key, value);
        }
        return table;
    }

    /**
     * Parses a manifest header value into an array of ManifestElements.  Each
     * ManifestElement returned will have a non-null value returned by getValue().
     *
     * @param header the header name to parse.  This is only specified to provide error messages
     *               when the header value is invalid.
     * @param value  the header value to parse.
     * @return the array of ManifestElements that are represented by the header value; null will be
     * returned if the value specified is null or if the value does not parse into
     * one or more ManifestElements.
     * @throws ManifestElementParserException if the header value is invalid
     */
    public static List<ManifestElement> parseHeader(String header, String value, Bundle bundle)
            throws ManifestElementParserException {
        if (value == null) {
            return new ArrayList<>();
        }
        List<ManifestElement> headerElements = new ArrayList<>(10);
        Tokenizer tokenizer = new Tokenizer(value);
        while (true) {
            String next = tokenizer.getString(";,");
            if (next == null) {
                throw new ManifestElementParserException(MANIFEST_INVALID_HEADER_EXCEPTION + " Header : " +
                        header + ", Value: " + value);
            }
            StringBuilder headerValue = new StringBuilder(next);

            logger.debug("parseHeader: " + next);
            boolean directive = false;
            char c = tokenizer.getChar();
            // Header values may be a list of ';' separated values.  Just append them all into one value until the
            // first '=' or ','
            while (c == ';') {
                next = tokenizer.getString(";,=:");
                if (next == null) {
                    throw new ManifestElementParserException(MANIFEST_INVALID_HEADER_EXCEPTION + " Header: " +
                            header + ", Value: " + value);
                }
                c = tokenizer.getChar();
                while (c == ':') { // may not really be a :=
                    c = tokenizer.getChar();
                    if (c != '=') {
                        String restOfNext = tokenizer.getToken(";,=:");
                        if (restOfNext == null) {
                            throw new ManifestElementParserException(MANIFEST_INVALID_HEADER_EXCEPTION + " Header: " +
                                    header + ", Value: " +
                                    value);
                        }
                        next = next.concat(":" + c + restOfNext);
                        c = tokenizer.getChar();
                    } else {
                        directive = true;
                    }
                }
                if (c == ';' || c == ',' || c == '\0') /* more */ {
                    headerValue.append(";").append(next);
                    logger.debug(";" + next);
                }
            }
            // found the header value create a manifestElement for it.
            ManifestElement manifestElement = new ManifestElement(header, headerValue.toString(), bundle);

            // now add any attributes/directives for the manifestElement.
            while (c == '=' || c == ':') {
                while (c == ':') { // may not really be a :=
                    c = tokenizer.getChar();
                    if (c != '=') {
                        String restOfNext = tokenizer.getToken("=:");
                        if (restOfNext == null) {
                            throw new ManifestElementParserException(MANIFEST_INVALID_HEADER_EXCEPTION + " Header: " +
                                    header + ", Value: " +
                                    value);
                        }
                        next = next.concat(":" + c + restOfNext);
                        c = tokenizer.getChar();
                    } else {
                        directive = true;
                    }
                }
                // determine if the attribute is the form attr:List<type>
                String preserveEscapes = null;
                String tempNextWithoutFirstLetter = next.substring(1);
                if (!directive && tempNextWithoutFirstLetter.contains("List")) {
                    Tokenizer listTokenizer = new Tokenizer(next);
                    String attrKey = listTokenizer.getToken(":");
                    if (attrKey != null && listTokenizer.getChar() == ':' && "List"
                            .equals(listTokenizer.getToken("<"))) {
                        // we assume we must preserve escapes for , and "
                        preserveEscapes = "\\,";
                    }
                }

                String val = tokenizer.getString(";,", preserveEscapes);
                if (val == null) {
                    throw new ManifestElementParserException(MANIFEST_INVALID_HEADER_EXCEPTION + " Header: " +
                            header + ", Value: " + value);
                }

                logger.debug(";" + next + "=" + val);
                try {
                    if (directive) {
                        manifestElement.addDirective(next, val);
                    } else {
                        manifestElement.addAttribute(next, val);
                    }
                    directive = false;
                } catch (Exception e) {
                    throw new ManifestElementParserException(MANIFEST_INVALID_HEADER_EXCEPTION + " Header: " +
                            header + ", Value: " + value);
                }
                c = tokenizer.getChar();
                if (c == ';') /* more */ {
                    next = tokenizer.getToken("=:");
                    if (next == null) {
                        throw new ManifestElementParserException(MANIFEST_INVALID_HEADER_EXCEPTION + " Header: " +
                                header + ", Value: " +
                                value);
                    }
                    c = tokenizer.getChar();
                }
            }
            headerElements.add(manifestElement);
            if (c == ',') { /* another manifest element */
                continue;
            }
            if (c == '\0') { /* end of value */
                break;
            }
            throw new ManifestElementParserException(MANIFEST_INVALID_HEADER_EXCEPTION + " Header: " +
                    header + ", Value: " + value);
        }
        int size = headerElements.size();
        if (size == 0) {
            return new ArrayList<>();
        }

        return headerElements;
    }

    /**
     * Returns the string representation of the manifest element.
     *
     * @return String
     */
    public String toString() {
        Enumeration<String> attrKeys = getKeys();
        Enumeration<String> directiveKeys = getDirectiveKeys();
        if (attrKeys == null && directiveKeys == null) {
            return mainValue;
        }
        StringBuffer result = new StringBuffer(mainValue);
        if (attrKeys != null) {
            while (attrKeys.hasMoreElements()) {
                String key = attrKeys.nextElement();
                addValues(false, key, getAttributes(key), result);
            }
        }
        if (directiveKeys != null) {
            while (directiveKeys.hasMoreElements()) {
                String key = directiveKeys.nextElement();
                addValues(true, key, getDirectives(key), result);
            }
        }
        return result.toString();
    }

    private void addValues(boolean directive, String key, String[] values, StringBuffer result) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            result.append(';').append(key);
            if (directive) {
                result.append(':');
            }
            result.append("=\"").append(value).append('\"');
        }
    }
}
