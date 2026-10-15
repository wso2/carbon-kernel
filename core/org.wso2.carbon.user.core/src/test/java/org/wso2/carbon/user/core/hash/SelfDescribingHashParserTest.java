/*
 * Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0.
 */

package org.wso2.carbon.user.core.hash;

import org.testng.annotations.Test;
import org.wso2.carbon.user.core.BaseTestCase;

/**
 * Unit tests for SelfDescribingHashParser.
 */
public class SelfDescribingHashParserTest extends BaseTestCase {

    @Test
    public void testParsePBKDF2Hash() {

        SelfDescribingHashParser.ParseResult result =
                SelfDescribingHashParser.parse("{PBKDF2}encoded-payload");

        assertTrue(result.isValid());
        assertFalse(result.isLegacy());
        assertEquals("PBKDF2", result.getAlgorithm());
        assertEquals("encoded-payload", result.getPayload());
    }

    @Test
    public void testParseBcryptHash() {

        SelfDescribingHashParser.ParseResult result =
                SelfDescribingHashParser.parse("{BCRYPT}bcrypt-payload");

        assertTrue(result.isValid());
        assertEquals("BCRYPT", result.getAlgorithm());
        assertEquals("bcrypt-payload", result.getPayload());
    }

    @Test
    public void testParseLegacyHash() {

        SelfDescribingHashParser.ParseResult result =
                SelfDescribingHashParser.parse("legacy-hash-value");

        assertTrue(result.isValid());
        assertTrue(result.isLegacy());
        assertNull(result.getAlgorithm());
        assertEquals("legacy-hash-value", result.getPayload());
    }

    @Test
    public void testParseNullHash() {

        SelfDescribingHashParser.ParseResult result = SelfDescribingHashParser.parse(null);

        assertFalse(result.isValid());
        assertFalse(result.isLegacy());
    }

    @Test
    public void testParseMalformedPrefix() {

        SelfDescribingHashParser.ParseResult result = SelfDescribingHashParser.parse("{PBKDF2");

        assertFalse(result.isValid());
        assertFalse(result.isLegacy());
    }

    @Test
    public void testParseEmptyPayload() {

        SelfDescribingHashParser.ParseResult result = SelfDescribingHashParser.parse("{PBKDF2}");

        assertTrue(result.isValid());
        assertEquals("PBKDF2", result.getAlgorithm());
        assertEquals("", result.getPayload());
    }

    @Test
    public void testFormatAndParseRoundTrip() {

        String stored = SelfDescribingHashParser.format("PBKDF2", "encoded-payload");

        assertEquals("{PBKDF2}encoded-payload", stored);

        SelfDescribingHashParser.ParseResult result = SelfDescribingHashParser.parse(stored);
        assertTrue(result.isValid());
        assertFalse(result.isLegacy());
        assertEquals("PBKDF2", result.getAlgorithm());
        assertEquals("encoded-payload", result.getPayload());
    }
}
