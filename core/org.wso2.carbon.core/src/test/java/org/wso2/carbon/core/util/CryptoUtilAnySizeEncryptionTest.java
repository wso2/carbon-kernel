/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.core.util;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.crypto.api.CryptoService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for the any-size encryption support in {@link CryptoUtil}:
 * {@link CryptoUtil#encryptAndBase64EncodeAnySize(byte[])},
 * {@link CryptoUtil#base64DecodeAndDecryptAnySize(String)} and
 * {@link CryptoUtil#isChunkedCipherText(String)}.
 * <p>
 * The underlying {@link CryptoService} is stubbed with an identity transform (ciphertext == plaintext) so the
 * tests exercise the chunking, marker-routing and reassembly logic - not the cipher itself - without a
 * keystore. The base64 encode/decode that {@link CryptoUtil} applies around each block still runs for real,
 * which is what makes the {@code ';'} chunk delimiter safe for arbitrary binary input.
 */
public class CryptoUtilAnySizeEncryptionTest {

    private static final String CHUNK_MARKER = "chunk:v1:";
    private static final String LEGACY_CHUNK_MARKER = "rsachunk:v1:";
    private static final String CHUNK_DELIMITER = ";";
    // Must match CryptoUtil.MAX_PLAINTEXT_CHUNK_SIZE.
    private static final int BLOCK_SIZE = 126;

    private CryptoUtil cryptoUtil;

    @BeforeClass
    public void setUp() throws Exception {

        // Identity crypto: encrypt/decrypt return their input bytes, so a round trip reduces to the
        // chunk-split/base64/join and decode/reassemble logic under test.
        CryptoService cryptoService = mock(CryptoService.class);
        when(cryptoService.encrypt(any(byte[].class), any(), any(), anyBoolean()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cryptoService.decrypt(any(byte[].class), any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CarbonCoreDataHolder.getInstance().setCryptoService(cryptoService);
        cryptoUtil = CryptoUtil.getDefaultCryptoUtil(null, null);
    }

    @AfterClass
    public void tearDown() {

        // Avoid leaking the stubbed crypto service into other tests in the suite.
        CarbonCoreDataHolder.getInstance().setCryptoService(null);
    }

    @DataProvider(name = "plaintextSizes")
    public Object[][] plaintextSizes() {

        // Sizes around the block boundary (126) plus a realistic multi-KB secret (e.g. a service-account key).
        return new Object[][]{
                {1}, {2}, {BLOCK_SIZE - 1}, {BLOCK_SIZE}, {BLOCK_SIZE + 1},
                {2 * BLOCK_SIZE - 1}, {2 * BLOCK_SIZE}, {2 * BLOCK_SIZE + 1}, {1000}, {4096},
        };
    }

    @Test(dataProvider = "plaintextSizes")
    public void testRoundTripAcrossSizes(int size) throws Exception {

        byte[] original = deterministicBytes(size);
        String encrypted = cryptoUtil.encryptAndBase64EncodeAnySize(original);
        byte[] decrypted = cryptoUtil.base64DecodeAndDecryptAnySize(encrypted);
        assertEquals(decrypted, original, "Round trip must return the original bytes for size " + size);
    }

    @Test
    public void testArbitraryBinaryBytesRoundTrip() throws Exception {

        // Spans every byte value (0..255), including ';' (0x3B). The delimiter must stay safe because each
        // block is base64-encoded before joining.
        byte[] original = new byte[600];
        for (int i = 0; i < original.length; i++) {
            original[i] = (byte) (i % 256);
        }
        byte[] decrypted = cryptoUtil.base64DecodeAndDecryptAnySize(
                cryptoUtil.encryptAndBase64EncodeAnySize(original));
        assertEquals(decrypted, original);
    }

    @Test
    public void testDataAtBlockBoundaryProducesSingleChunk() throws Exception {

        String encrypted = cryptoUtil.encryptAndBase64EncodeAnySize(deterministicBytes(BLOCK_SIZE));
        assertTrue(encrypted.startsWith(CHUNK_MARKER), "Expected the chunk marker");
        String body = encrypted.substring(CHUNK_MARKER.length());
        assertFalse(body.contains(CHUNK_DELIMITER), "Exactly one block must produce a single chunk");
    }

    @Test
    public void testDataAboveBlockBoundaryProducesMultipleChunks() throws Exception {

        // 300 bytes => ceil(300 / 126) = 3 chunks.
        String encrypted = cryptoUtil.encryptAndBase64EncodeAnySize(deterministicBytes(300));
        assertTrue(encrypted.startsWith(CHUNK_MARKER), "Expected the chunk marker");
        String body = encrypted.substring(CHUNK_MARKER.length());
        assertEquals(body.split(CHUNK_DELIMITER, -1).length, 3, "Expected 3 chunks for 300 bytes");
    }

    @Test
    public void testEmptyArrayIsEncryptedSingleShot() throws Exception {

        String encrypted = cryptoUtil.encryptAndBase64EncodeAnySize(new byte[0]);
        assertFalse(cryptoUtil.isChunkedCipherText(encrypted), "Empty input must not be chunked");
        assertEquals(cryptoUtil.base64DecodeAndDecryptAnySize(encrypted), new byte[0]);
    }

    @Test
    public void testLegacyRsaChunkMarkerIsDecrypted() throws Exception {

        byte[] original = deterministicBytes(300);
        // A value written by an earlier version carried the legacy "rsachunk:v1:" marker. Simulate one and
        // verify it still decrypts (backward compatibility - the legacy marker is read but never written).
        String current = cryptoUtil.encryptAndBase64EncodeAnySize(original);
        String legacy = LEGACY_CHUNK_MARKER + current.substring(CHUNK_MARKER.length());
        assertTrue(cryptoUtil.isChunkedCipherText(legacy));
        assertEquals(cryptoUtil.base64DecodeAndDecryptAnySize(legacy), original);
    }

    @Test
    public void testNullPlaintextIsRejected() {

        try {
            cryptoUtil.encryptAndBase64EncodeAnySize(null);
            fail("Expected a CryptoException for null plaintext");
        } catch (CryptoException e) {
            assertEquals(e.getMessage(), "Plaintext to encrypt can't be null.");
        }
    }

    @Test
    public void testNullCipherTextIsRejected() {

        try {
            cryptoUtil.base64DecodeAndDecryptAnySize(null);
            fail("Expected a CryptoException for null ciphertext");
        } catch (CryptoException e) {
            assertEquals(e.getMessage(), "Ciphertext can't be null.");
        }
    }

    @Test
    public void testMarkerOnlyValueIsRejectedAsEmptyChunk() {

        try {
            cryptoUtil.base64DecodeAndDecryptAnySize(CHUNK_MARKER);
            fail("Expected a CryptoException for a marker-only value");
        } catch (CryptoException e) {
            assertTrue(e.getMessage().contains("empty chunk"), "Unexpected message: " + e.getMessage());
        }
    }

    @Test
    public void testEmbeddedEmptyChunkIsRejected() throws Exception {

        // Introduce an empty chunk (";;") into an otherwise valid multi-chunk value.
        String valid = cryptoUtil.encryptAndBase64EncodeAnySize(deterministicBytes(300));
        String malformed = CHUNK_MARKER
                + valid.substring(CHUNK_MARKER.length()).replaceFirst(CHUNK_DELIMITER, ";;");
        try {
            cryptoUtil.base64DecodeAndDecryptAnySize(malformed);
            fail("Expected a CryptoException for an embedded empty chunk");
        } catch (CryptoException e) {
            assertTrue(e.getMessage().contains("empty chunk"), "Unexpected message: " + e.getMessage());
        }
    }

    @Test
    public void testIsChunkedCipherText() {

        assertFalse(cryptoUtil.isChunkedCipherText(null));
        assertFalse(cryptoUtil.isChunkedCipherText(""));
        assertFalse(cryptoUtil.isChunkedCipherText("SGVsbG8="));
        assertTrue(cryptoUtil.isChunkedCipherText(CHUNK_MARKER + "SGVsbG8="));
        assertTrue(cryptoUtil.isChunkedCipherText(LEGACY_CHUNK_MARKER + "SGVsbG8="));
    }

    private static byte[] deterministicBytes(int size) {

        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = (byte) (i % 256);
        }
        return bytes;
    }
}
