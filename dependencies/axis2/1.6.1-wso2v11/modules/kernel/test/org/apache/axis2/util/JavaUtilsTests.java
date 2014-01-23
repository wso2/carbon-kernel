/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.util;

import junit.framework.TestCase;

public class JavaUtilsTests extends TestCase {
    public void testXMLNameMapping() throws Exception {
        /* Begin TABLE 20-2 Illustrative Examples from JAXRPC Spec */
        assertEquals("mixedCaseName", JavaUtils.xmlNameToJavaIdentifier("mixedCaseName"));

        assertEquals("nameWithDashes", JavaUtils.xmlNameToJavaIdentifier("name-with-dashes"));

        assertEquals("name_with_underscore", JavaUtils.xmlNameToJavaIdentifier("name_with_underscore"));

        assertEquals("other_punctChars", JavaUtils.xmlNameToJavaIdentifier("other_punct.chars"));

        assertEquals("answer42", JavaUtils.xmlNameToJavaIdentifier("Answer42"));
        /* End TABLE 20-2 Illustrative Examples from JAXRPC Spec */

        assertEquals("nameWithDashes",
                JavaUtils.xmlNameToJavaIdentifier("name-with-dashes"));

        assertEquals("otherPunctChars",
                JavaUtils.xmlNameToJavaIdentifier("other.punct\u00B7chars"));

        assertEquals("answer42", JavaUtils.xmlNameToJavaIdentifier("Answer42"));

//        assertEquals("\u2160Foo", JavaUtils.xmlNameToJavaIdentifier("\u2160foo"));

        assertEquals("foo", JavaUtils.xmlNameToJavaIdentifier("2foo"));

        //assertEquals("_Foo_", JavaUtils.xmlNameToJavaIdentifier("_foo_"));
        assertEquals("_foo_", JavaUtils.xmlNameToJavaIdentifier("_foo_"));

        assertEquals("foobar", JavaUtils.xmlNameToJavaIdentifier("--foobar--"));

        assertEquals("foo22Bar", JavaUtils.xmlNameToJavaIdentifier("foo22bar"));

        assertEquals("foo\u2160Bar", JavaUtils.xmlNameToJavaIdentifier("foo\u2160bar"));

        assertEquals("fooBar", JavaUtils.xmlNameToJavaIdentifier("foo-bar"));

        assertEquals("fooBar", JavaUtils.xmlNameToJavaIdentifier("foo.bar"));

        assertEquals("fooBar", JavaUtils.xmlNameToJavaIdentifier("foo:bar"));

        //assertEquals("foo_Bar", JavaUtils.xmlNameToJavaIdentifier("foo_bar"));
        assertEquals("foo_bar", JavaUtils.xmlNameToJavaIdentifier("foo_bar"));

        assertEquals("fooBar", JavaUtils.xmlNameToJavaIdentifier("foo\u00B7bar"));

        assertEquals("fooBar", JavaUtils.xmlNameToJavaIdentifier("foo\u0387bar"));

        assertEquals("fooBar", JavaUtils.xmlNameToJavaIdentifier("foo\u06DDbar"));

        assertEquals("fooBar", JavaUtils.xmlNameToJavaIdentifier("foo\u06DEbar"));

        assertEquals("fooBar", JavaUtils.xmlNameToJavaIdentifier("FooBar"));

//        assertEquals("FOOBar", JavaUtils.xmlNameToJavaIdentifier("FOOBar"));

        assertEquals("a1BBB", JavaUtils.xmlNameToJavaIdentifier("A1-BBB"));

//        assertEquals("ABBB", JavaUtils.xmlNameToJavaIdentifier("A-BBB"));

//        assertEquals("ACCC", JavaUtils.xmlNameToJavaIdentifier("ACCC"));


        // the following cases are ambiguous in JSR-101
        assertEquals("fooBar", JavaUtils.xmlNameToJavaIdentifier("foo bar"));
        assertEquals("_1", JavaUtils.xmlNameToJavaIdentifier("-"));

        assertEquals("_abstract", JavaUtils.xmlNameToJavaIdentifier("abstract"));
    }
}
