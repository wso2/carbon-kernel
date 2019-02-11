/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.utils.xml;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.BaseTest;

/**
 * Test class for StringUtil related methods usage.
 */
public class StringUtilsTest extends BaseTest {
    @Test
    public void testStartsWithIgnoreWhitespaces() throws Exception {
        String text1 = "CarbonUtilsTestString";
        String text2 = "AnotherCarbonUtilsTestString";

        Assert.assertTrue(StringUtils.startsWithIgnoreWhitespaces("CarbonU", text1));
        Assert.assertFalse(StringUtils.startsWithIgnoreWhitespaces("CarbonU", text2));
    }

    /**
     * StringUtils.split(null, *)         = null
     * StringUtils.split("", *)           = []
     * StringUtils.split("a.b.c", '.')    = ["a", "b", "c"]
     * StringUtils.split("a..b.c", '.')   = ["a", "b", "c"]
     * StringUtils.split("a:b:c", '.')    = ["a:b:c"]
     * StringUtils.split("a b c", ' ')    = ["a", "b", "c"]
     */
    @Test
    public void testSplit() throws Exception {
        Assert.assertNull(StringUtils.split(null, '*'), null);
        Assert.assertEquals(StringUtils.split("", '*'), new String[0]);
        Assert.assertEquals(StringUtils.split("a.b.c", '.'), new String[]{"a", "b", "c"});
        Assert.assertEquals(StringUtils.split("a..b.c", '.'), new String[]{"a", "b", "c"});
        Assert.assertEquals(StringUtils.split("a:b:c", '.'), new String[]{"a:b:c"});
        Assert.assertEquals(StringUtils.split("a b c", ' '), new String[]{"a", "b", "c"});
    }

    /**
     * StringUtils.strip(null)     = null
     * StringUtils.strip("")       = ""
     * StringUtils.strip("   ")    = ""
     * StringUtils.strip("abc")    = "abc"
     * StringUtils.strip("  abc")  = "abc"
     * StringUtils.strip("abc  ")  = "abc"
     * StringUtils.strip(" abc ")  = "abc"
     * StringUtils.strip(" ab c ") = "ab c"
     * StringUtils.strip(null, *)          = null
     * StringUtils.strip("", *)            = ""
     * StringUtils.strip("abc", null)      = "abc"
     * StringUtils.strip("  abc", null)    = "abc"
     * StringUtils.strip("abc  ", null)    = "abc"
     * StringUtils.strip(" abc ", null)    = "abc"
     * StringUtils.strip("  abcyx", "xyz") = "  abc"
     */
    @Test
    public void testStrip() throws Exception {
        Assert.assertNull(StringUtils.strip(null), null);
        Assert.assertEquals(StringUtils.strip(""), "");
        Assert.assertEquals(StringUtils.strip("   "), "");
        Assert.assertEquals(StringUtils.strip("abc"), "abc");
        Assert.assertEquals(StringUtils.strip("abc   "), "abc");
        Assert.assertEquals(StringUtils.strip("   abc"), "abc");
        Assert.assertEquals(StringUtils.strip("    abc    "), "abc");
        Assert.assertEquals(StringUtils.strip("    ab c  "), "ab c");

        Assert.assertNull(StringUtils.strip(null, "*"), null);
        Assert.assertEquals(StringUtils.strip("", "*"), "");
        Assert.assertEquals(StringUtils.strip("abc", null), "abc");
        Assert.assertEquals(StringUtils.strip("abc   ", null), "abc");
        Assert.assertEquals(StringUtils.strip("   abc", null), "abc");
        Assert.assertEquals(StringUtils.strip("    abc    ", null), "abc");
        Assert.assertEquals(StringUtils.strip("    ab c  ", null), "ab c");
        Assert.assertEquals(StringUtils.strip("   abcyz", "xyz"), "   abc");
        Assert.assertEquals(StringUtils.strip("xyzabc   ", "xyz"), "abc   ");

        Assert.assertNull(StringUtils.stripStart(null, "*"), null);
        Assert.assertEquals(StringUtils.stripStart("", "*"), "");
        Assert.assertEquals(StringUtils.stripStart("abc", null), "abc");
        Assert.assertEquals(StringUtils.stripStart("abc   ", null), "abc   ");
        Assert.assertEquals(StringUtils.stripStart("   abc", null), "abc");
        Assert.assertEquals(StringUtils.stripStart("    abc   ", null), "abc   ");
        Assert.assertEquals(StringUtils.stripStart("    ab c  ", null), "ab c  ");
        Assert.assertEquals(StringUtils.stripStart("xyzabc  ", "xyz"), "abc  ");

        Assert.assertNull(StringUtils.stripEnd(null, "*"), null);
        Assert.assertEquals(StringUtils.stripEnd("", "*"), "");
        Assert.assertEquals(StringUtils.stripEnd("abc", null), "abc");
        Assert.assertEquals(StringUtils.stripEnd("abc   ", null), "abc");
        Assert.assertEquals(StringUtils.stripEnd("   abc", null), "   abc");
        Assert.assertEquals(StringUtils.stripEnd("   abc    ", null), "   abc");
        Assert.assertEquals(StringUtils.stripEnd("   ab c  ", null), "   ab c");
        Assert.assertEquals(StringUtils.stripEnd("   abcyz", "xyz"), "   abc");
    }

    /**
     *  makeQNameToMatchLocalName("Foo.bar")  = fooBar
     * 	makeQNameToMatchLocalName("Foo.Bar")  = fooBar
     * 	makeQNameToMatchLocalName("FooBar")   = fooBar
     * 	makeQNameToMatchLocalName("Foobar")   = foobar
     * 	makeQNameToMatchLocalName("fooBar")   = fooBar
     * 	makeQNameToMatchLocalName("foobar")   = foobar
     * 	makeQNameToMatchLocalName("Foo:bar")  = fooBar
     * 	makeQNameToMatchLocalName("Foo-Bar")  = fooBar
     * 	makeQNameToMatchLocalName("Foo-bar")  = fooBar
     * 	makeQNameToMatchLocalName("Foo_bar")  = foo_bar
     * 	makeQNameToMatchLocalName("Foo_Bar")  = foo_Bar
     * 	makeQNameToMatchLocalName("foo:bar")  = fooBar
     *
     */
    @Test
    public void testMakeQNameToMatchLocalName() throws Exception {
        Assert.assertEquals(StringUtils.makeQNameToMatchLocalName("Foo.bar"), "fooBar");
        Assert.assertEquals(StringUtils.makeQNameToMatchLocalName("Foo.Bar"), "fooBar");
        Assert.assertEquals(StringUtils.makeQNameToMatchLocalName("FooBar"), "fooBar");
        Assert.assertEquals(StringUtils.makeQNameToMatchLocalName("Foobar"), "foobar");
        Assert.assertEquals(StringUtils.makeQNameToMatchLocalName("fooBar"), "fooBar");
        Assert.assertEquals(StringUtils.makeQNameToMatchLocalName("Foo:Bar"), "fooBar");
        Assert.assertEquals(StringUtils.makeQNameToMatchLocalName("Foo-Bar"), "fooBar");
        Assert.assertEquals(StringUtils.makeQNameToMatchLocalName("Foo-bar"), "fooBar");
        Assert.assertEquals(StringUtils.makeQNameToMatchLocalName("Foo_bar"), "foo_bar");
        Assert.assertEquals(StringUtils.makeQNameToMatchLocalName("Foo_Bar"), "foo_Bar");
        Assert.assertEquals(StringUtils.makeQNameToMatchLocalName("foo:bar"), "fooBar");
    }

    @Test
    public void testDeleteCharAndChangeNextCharToUpperCase() throws Exception {
        Assert.assertEquals(StringUtils.deleteCharAndChangeNextCharToUpperCase("Foo.bar", "."), "FooBar");
        Assert.assertEquals(StringUtils.deleteCharAndChangeNextCharToUpperCase("Foo.Bar", "."), "FooBar");
        Assert.assertEquals(StringUtils.deleteCharAndChangeNextCharToUpperCase("FooBar", "."), "FooBar");
        Assert.assertEquals(StringUtils.deleteCharAndChangeNextCharToUpperCase("Foobar", "."), "Foobar");
        Assert.assertEquals(StringUtils.deleteCharAndChangeNextCharToUpperCase("fooBar", "."), "fooBar");
        Assert.assertEquals(StringUtils.deleteCharAndChangeNextCharToUpperCase("Foo:Bar", ":"), "FooBar");
        Assert.assertEquals(StringUtils.deleteCharAndChangeNextCharToUpperCase("Foo-Bar", "-"), "FooBar");
        Assert.assertEquals(StringUtils.deleteCharAndChangeNextCharToUpperCase("Foo-bar", "-"), "FooBar");
        Assert.assertEquals(StringUtils.deleteCharAndChangeNextCharToUpperCase("Foo_bar", "_"), "FooBar");
        Assert.assertEquals(StringUtils.deleteCharAndChangeNextCharToUpperCase("Foo_Bar", "_"), "FooBar");
        Assert.assertEquals(StringUtils.deleteCharAndChangeNextCharToUpperCase("foo:bar", ":"), "fooBar");
    }

    @Test
    public void testUnescapeNumericChar() throws Exception {
        Assert.assertEquals(StringUtils.unescapeNumericChar("&#x3088;&#x3046;&#x3053;&#x305d;"), "ようこそ");
    }
}
