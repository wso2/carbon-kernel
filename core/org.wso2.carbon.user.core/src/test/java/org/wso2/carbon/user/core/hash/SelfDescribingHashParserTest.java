/*
 * Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0.
 */

package org.wso2.carbon.user.core.hash;

import org.testng.annotations.Test;
import org.wso2.carbon.user.core.BaseTestCase;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unit tests for SelfDescribingHashParser.
 * <p>
 * These are the one place the stored layout is asserted literally. Everywhere else a stored value
 * is built through {@link SelfDescribingHashParser#format(String, String)}, so that a change to
 * the layout is felt here and nowhere else.
 * </p>
 */
public class SelfDescribingHashParserTest extends BaseTestCase {

    @Test
    public void testParsePBKDF2Hash() {

        SelfDescribingHashParser.ParseResult result =
                SelfDescribingHashParser.parse("{\"algorithm\":\"PBKDF2\",\"hash\":\"encoded-payload\"}");

        assertTrue(result.isValid());
        assertFalse(result.isLegacy());
        assertEquals("PBKDF2", result.getAlgorithm());
        assertEquals("encoded-payload", result.getPayload());
    }

    @Test
    public void testParseBcryptHash() {

        // A BCrypt payload carries dollar signs and slashes, which the JSON layout leaves alone.
        SelfDescribingHashParser.ParseResult result = SelfDescribingHashParser.parse(
                "{\"algorithm\":\"BCRYPT\",\"hash\":\"$2a$10$N9qo8uLOickgx2ZMRZoMye\"}");

        assertTrue(result.isValid());
        assertEquals("BCRYPT", result.getAlgorithm());
        assertEquals("$2a$10$N9qo8uLOickgx2ZMRZoMye", result.getPayload());
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
    public void testParseBase64LegacyHash() {

        // A real legacy value: base64 with slashes and padding, which is not JSON.
        String storedHash = "d8/vswX7Q/vN9YMHtSCI1bnKDgik333qVmQQm6XdcOk=";
        SelfDescribingHashParser.ParseResult result = SelfDescribingHashParser.parse(storedHash);

        assertTrue(result.isValid());
        assertTrue(result.isLegacy());
        assertEquals(storedHash, result.getPayload());
    }

    @Test
    public void testParseValuesThatAreJsonButNotObjects() {

        // Some of these parse as JSON primitives or arrays. Only a JSON object is the
        // self-describing format, so each of them is still a legacy value.
        for (String storedHash : new String[]{"12345", "true", "null", "\"quoted\"", "[1,2]", "plainpassword"}) {
            SelfDescribingHashParser.ParseResult result = SelfDescribingHashParser.parse(storedHash);

            assertTrue(storedHash, result.isValid());
            assertTrue(storedHash, result.isLegacy());
            assertEquals(storedHash, storedHash, result.getPayload());
        }
    }

    @Test
    public void testParseNullHash() {

        SelfDescribingHashParser.ParseResult result = SelfDescribingHashParser.parse(null);

        assertFalse(result.isValid());
        assertFalse(result.isLegacy());
    }

    @Test
    public void testParseEmptyHash() {

        // An empty column is not a damaged self-describing value; it is read in the older form and
        // will simply fail to match any credential.
        SelfDescribingHashParser.ParseResult result = SelfDescribingHashParser.parse("");

        assertTrue(result.isValid());
        assertTrue(result.isLegacy());
        assertEquals("", result.getPayload());
    }

    @Test
    public void testParseTruncatedObject() {

        SelfDescribingHashParser.ParseResult result =
                SelfDescribingHashParser.parse("{\"algorithm\":\"PBKDF2\",\"hash\":");

        assertFalse(result.isValid());
        assertFalse(result.isLegacy());
    }

    @Test
    public void testParseObjectWithUnquotedMemberNames() {

        // Not valid JSON, so it is a damaged record rather than a value to hand to a provider.
        SelfDescribingHashParser.ParseResult result =
                SelfDescribingHashParser.parse("{algorithm:PBKDF2,hash:encoded-payload}");

        assertFalse(result.isValid());
        assertFalse(result.isLegacy());
    }

    @Test
    public void testParseObjectWithTrailingContent() {

        SelfDescribingHashParser.ParseResult result =
                SelfDescribingHashParser.parse("{\"algorithm\":\"PBKDF2\",\"hash\":\"encoded-payload\"} extra");

        assertFalse(result.isValid());
        assertFalse(result.isLegacy());
    }

    @Test
    public void testParseObjectWithoutTheHashMember() {

        SelfDescribingHashParser.ParseResult result =
                SelfDescribingHashParser.parse("{\"algorithm\":\"PBKDF2\"}");

        assertFalse(result.isValid());
        assertFalse(result.isLegacy());
    }

    @Test
    public void testParseObjectWithoutTheAlgorithmMember() {

        SelfDescribingHashParser.ParseResult result =
                SelfDescribingHashParser.parse("{\"hash\":\"encoded-payload\"}");

        assertFalse(result.isValid());
        assertFalse(result.isLegacy());
    }

    @Test
    public void testParseObjectWithABlankAlgorithm() {

        // An identifier that names no provider cannot route verification anywhere, so a record
        // carrying a blank one is damaged rather than usable.
        SelfDescribingHashParser.ParseResult result =
                SelfDescribingHashParser.parse("{\"algorithm\":\"  \",\"hash\":\"encoded-payload\"}");

        assertFalse(result.isValid());
        assertFalse(result.isLegacy());
    }

    @Test
    public void testParseEmptyPayload() {

        SelfDescribingHashParser.ParseResult result =
                SelfDescribingHashParser.parse("{\"algorithm\":\"PBKDF2\",\"hash\":\"\"}");

        assertTrue(result.isValid());
        assertEquals("PBKDF2", result.getAlgorithm());
        assertEquals("", result.getPayload());
    }

    @Test
    public void testFormatAndParseRoundTrip() {

        String stored = SelfDescribingHashParser.format("PBKDF2", "encoded-payload");

        assertEquals("{\"algorithm\":\"PBKDF2\",\"hash\":\"encoded-payload\"}", stored);

        SelfDescribingHashParser.ParseResult result = SelfDescribingHashParser.parse(stored);
        assertTrue(result.isValid());
        assertFalse(result.isLegacy());
        assertEquals("PBKDF2", result.getAlgorithm());
        assertEquals("encoded-payload", result.getPayload());
    }

    @Test
    public void testFormatRecordsParametersAsStrings() {

        // A provider reports its parameters as the types it holds them in, but only accepts them
        // back as strings. Writing them in any other form would hand a provider a value it ignores,
        // leaving it on its own defaults and verifying the credential against the wrong parameters.
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("iteration.count", 600000);
        parameters.put("prf", "HmacSHA256");

        String stored = SelfDescribingHashParser.format("PBKDF2", "encoded-payload", parameters);

        assertEquals("{\"algorithm\":\"PBKDF2\",\"hash\":\"encoded-payload\","
                + "\"properties\":{\"iteration.count\":\"600000\",\"prf\":\"HmacSHA256\"}}", stored);
    }

    @Test
    public void testParseReadsRecordedParameters() {

        SelfDescribingHashParser.ParseResult result = SelfDescribingHashParser.parse(
                "{\"algorithm\":\"PBKDF2\",\"hash\":\"encoded-payload\","
                        + "\"properties\":{\"iteration.count\":\"600000\"}}");

        assertTrue(result.isValid());
        assertEquals("600000", result.getProperties().get("iteration.count"));
    }

    @Test
    public void testParseReadsABareNumberAsAString() {

        // A hand-edited record may carry a number rather than a string. It still has to read back
        // in the form a provider accepts, rather than being silently dropped.
        SelfDescribingHashParser.ParseResult result = SelfDescribingHashParser.parse(
                "{\"algorithm\":\"PBKDF2\",\"hash\":\"encoded-payload\","
                        + "\"properties\":{\"iteration.count\":600000}}");

        assertEquals("600000", result.getProperties().get("iteration.count"));
    }

    @Test
    public void testFormatOmitsEmptyParameters() {

        assertEquals("{\"algorithm\":\"BCRYPT\",\"hash\":\"payload\"}",
                SelfDescribingHashParser.format("BCRYPT", "payload", Collections.emptyMap()));
        assertEquals("{\"algorithm\":\"BCRYPT\",\"hash\":\"payload\"}",
                SelfDescribingHashParser.format("BCRYPT", "payload", null));
    }

    @Test
    public void testParseReportsNoParametersWhenTheMemberIsAbsent() {

        SelfDescribingHashParser.ParseResult result =
                SelfDescribingHashParser.parse("{\"algorithm\":\"BCRYPT\",\"hash\":\"payload\"}");

        assertTrue(result.isValid());
        assertTrue(result.getProperties().isEmpty());
    }

    @Test
    public void testParametersRoundTrip() {

        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("rounds", 12);

        String stored = SelfDescribingHashParser.format("TESTALG", "payload", parameters);
        SelfDescribingHashParser.ParseResult result = SelfDescribingHashParser.parse(stored);

        assertEquals("TESTALG", result.getAlgorithm());
        assertEquals("payload", result.getPayload());
        assertEquals("12", result.getProperties().get("rounds"));
    }

    @Test
    public void testFormatLeavesBase64PaddingIntact() {

        // Gson escapes '=' by default, which would corrupt the padding that ends a base64 payload
        // and inflate every stored value. The parser must keep it verbatim.
        String payload = "sJ9p0xKcXUbYq3wVfTnM2hLgR8eZ5aDvC1kN7iPuO4s=";

        String stored = SelfDescribingHashParser.format("PBKDF2", payload);

        assertTrue(stored.contains(payload));
        assertFalse(stored.contains("\\u003d"));
        assertEquals(payload, SelfDescribingHashParser.parse(stored).getPayload());
    }
}
