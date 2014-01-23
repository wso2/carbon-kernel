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
package components;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.ws.security.components.crypto.X509NameTokenizer;

/**
 *
 */
public class TestX509NameTokenizer extends TestCase {

    public TestX509NameTokenizer(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(TestX509NameTokenizer.class);
    }

    public void 
    testEmptyX509NameTokenizer() {
        checkEmpty(new X509NameTokenizer(""));
        checkEmpty(new X509NameTokenizer("  "));
        checkEmpty(new X509NameTokenizer(" \t \n  \r\n"));
    }
    
    public void 
    testWellFormedX509NameTokenizer() {
        checkTokenizer(
            new X509NameTokenizer("foo"), 
            new String[] { "foo" }
        );
        checkTokenizer(
            new X509NameTokenizer(" foo   "), 
            new String[] { "foo" }
        );
        checkTokenizer(
            new X509NameTokenizer(" foo,   "), 
            new String[] { "foo", "" }
        );
        checkTokenizer(
            new X509NameTokenizer(" foo\\,   "), 
            new String[] { "foo\\,"}
        );
        checkTokenizer(
            new X509NameTokenizer(" foo\\,   bar  "), 
            new String[] { "foo\\,   bar"}
        );
        checkTokenizer(
            new X509NameTokenizer(" \"foo,\"   "), 
            new String[] { "\"foo,\""}
        );
        checkTokenizer(
            new X509NameTokenizer("foo, bar"), 
            new String[] { "foo", "bar"}
        );
        checkTokenizer(
            new X509NameTokenizer("\"foo bar\", gnu gnat"), 
            new String[] { "\"foo bar\"", "gnu gnat"}
        );
        checkTokenizer(
            new X509NameTokenizer("foo\\ "), 
            new String[] { "foo\\ "}
        );
        checkTokenizer(
            new X509NameTokenizer("foo\\\\ "), 
            new String[] { "foo\\\\"}
        );
    }
    
    private void
    checkEmpty(
        final X509NameTokenizer tokenizer
    ) {
        checkTokenizer(
            tokenizer, new String[0]
        );
    }
    
    private void
    checkTokenizer(
        final X509NameTokenizer tokenizer,
        final String[] expected
    ) {
        for (int i = 0;  i < expected.length;  ++i) {
            assertTrue(tokenizer.hasMoreTokens());
            assertEquals(tokenizer.nextToken(), expected[i]);
        }
        assertTrue(!tokenizer.hasMoreTokens());
        assertEquals(tokenizer.nextToken(), "");
    }
}
