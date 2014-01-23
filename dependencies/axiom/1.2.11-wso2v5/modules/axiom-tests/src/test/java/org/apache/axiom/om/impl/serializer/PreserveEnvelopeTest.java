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

package org.apache.axiom.om.impl.serializer;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.custommonkey.xmlunit.XMLTestCase;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;

public class PreserveEnvelopeTest extends XMLTestCase {


    private String originalXML =
            "<Assertion xmlns=\"urn:oasis:names:tc:SAML:1.0:assertion\" xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\" xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\" AssertionID=\"ef98507718ea606c6ae1e89d2c0865ee\" IssueInstant=\"2006-01-03T15:09:35.164Z\" Issuer=\"C=US,O=National Center for Supercomputing Applications,CN=Hemapani Srinath Perera\" MajorVersion=\"1\" MinorVersion=\"1\"><Conditions NotBefore=\"2006-01-03T15:09:35.164Z\" NotOnOrAfter=\"2006-01-03T15:39:35.164Z\"><AudienceRestrictionCondition><Audience>http://www.extreme.indiana.edu/lead/TestCMD_Simple_Tue_Jan_03_10_09_27_EST_2006_142825</Audience></AudienceRestrictionCondition></Conditions><AuthorizationDecisionStatement Decision=\"Permit\" Resource=\"http://www.extreme.indiana.edu/lead/TestCMD_Simple_Tue_Jan_03_10_09_27_EST_2006_142825\"><Subject><NameIdentifier>/C=US/O=Indiana University/OU=Computer Science/CN=Hemapani Srinath Perera</NameIdentifier><SubjectConfirmation><ConfirmationMethod>urn:oasis:names:tc:SAML:1.0:cm:bearer</ConfirmationMethod></SubjectConfirmation></Subject><Action Namespace=\"http://www.extreme.indiana.edu/lead\">Run</Action></AuthorizationDecisionStatement><ds:Signature xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">\n" +
                    "<ds:SignedInfo>\n" +
                    "<ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"></ds:CanonicalizationMethod>\n" +
                    "<ds:SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"></ds:SignatureMethod>\n" +
                    "<ds:Reference URI=\"#ef98507718ea606c6ae1e89d2c0865ee\">\n" +
                    "<ds:Transforms>\n" +
                    "<ds:Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"></ds:Transform>\n" +
                    "<ds:Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"><ec:InclusiveNamespaces xmlns:ec=\"http://www.w3.org/2001/10/xml-exc-c14n#\" PrefixList=\"code ds kind rw saml samlp typens #default\"></ec:InclusiveNamespaces></ds:Transform>\n" +
                    "</ds:Transforms>\n" +
                    "<ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"></ds:DigestMethod>\n" +
                    "<ds:DigestValue>0bqebD+zkADcbXleyxpFUySp7cY=</ds:DigestValue>\n" +
                    "</ds:Reference>\n" +
                    "</ds:SignedInfo>\n" +
                    "<ds:SignatureValue>\n" +
                    "HVZDR4InypCICJxbCSrZJsikcpSACZRTt5Ly2gfLBUpdVeq6koHAKo8WGrGHbZVzNFAmxj3i52jQ\n" +
                    "aYeO0J6LIA==\n" +
                    "</ds:SignatureValue>\n" +
                    "<ds:KeyInfo>\n" +
                    "<ds:X509Data>\n" +
                    "<ds:X509Certificate>\n" +
                    "MIIClDCCAXygAwIBAwICBEkwDQYJKoZIhvcNAQEEBQAwaTELMAkGA1UEBhMCVVMxODA2BgNVBAoT\n" +
                    "L05hdGlvbmFsIENlbnRlciBmb3IgU3VwZXJjb21wdXRpbmcgQXBwbGljYXRpb25zMSAwHgYDVQQD\n" +
                    "ExdIZW1hcGFuaSBTcmluYXRoIFBlcmVyYTAeFw0wNjAxMDMxNDM2MTFaFw0wNjAxMDQwMjQxMTFa\n" +
                    "MHkxCzAJBgNVBAYTAlVTMTgwNgYDVQQKEy9OYXRpb25hbCBDZW50ZXIgZm9yIFN1cGVyY29tcHV0\n" +
                    "aW5nIEFwcGxpY2F0aW9uczEgMB4GA1UEAxMXSGVtYXBhbmkgU3JpbmF0aCBQZXJlcmExDjAMBgNV\n" +
                    "BAMTBXByb3h5MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALyBo53oJTrVjs/RCFLJaMKektAqeNCi\n" +
                    "IAhEx+pE0BxL23WV8mhxUNDJevO7lkRuwuDWchbobq8S1WNRYrbOqRkCAwEAATANBgkqhkiG9w0B\n" +
                    "AQQFAAOCAQEAEZRsxz1izBa8SNw7x1nCttjTDnRM102CTad/uq3vihKmOauBo8lFqklT2xoFv+Nx\n" +
                    "WX1FXT5AAqedITtUn99zk3Sr5c/SgdO59NxSaWgqxTOd7koZUmz8Sta9IK7d1fDFhDie2W2EtUMk\n" +
                    "QX0Rr7KHReF6a0knQ+kGlhDuA2YN27CpNg+gBTq0+p1yaVA79qf2BVDYBklPG2N5tWn2GekHoUMs\n" +
                    "7nlU9WlUI0tRUKuGGdQ+2WLTW0w2UuqsJuHvDaquZRnTOjhdiRw5/Mg62LqkSwo99Dc3JiJusqoY\n" +
                    "16Unq8wp5gVJbj36UoVvqnVOyBltseIaU5bLS5LIrv11hXA6fg==\n" +
                    "</ds:X509Certificate>\n" +
                    "<ds:X509Certificate>\n" +
                    "MIIEVzCCAz+gAwIBAgICBEkwDQYJKoZIhvcNAQEEBQAwaTELMAkGA1UEBhMCVVMxODA2BgNVBAoT\n" +
                    "L05hdGlvbmFsIENlbnRlciBmb3IgU3VwZXJjb21wdXRpbmcgQXBwbGljYXRpb25zMSAwHgYDVQQD\n" +
                    "ExdDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTAeFw0wNTEwMjUxOTMxMzRaFw0wNzEwMjUxOTMxMzRa\n" +
                    "MGkxCzAJBgNVBAYTAlVTMTgwNgYDVQQKEy9OYXRpb25hbCBDZW50ZXIgZm9yIFN1cGVyY29tcHV0\n" +
                    "aW5nIEFwcGxpY2F0aW9uczEgMB4GA1UEAxMXSGVtYXBhbmkgU3JpbmF0aCBQZXJlcmEwggEiMA0G\n" +
                    "CSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC/pkVn4ZUEM5BjfocwiiQVF4ivFpJJ8u1m8vq0WNlc\n" +
                    "bw4rjMOet1YArC46fZ1Gg652A1dF92+8hHNeBa6chll5HRZkx5FLN7XEGY4EcHhXA6lHmjklpf8N\n" +
                    "Iy6/91ElOT1pVoLRpOeJ1TFT/nQxMBMJbMyhycrUV49M8oL3O/CYAm1YVQjCtdieK/ibFXzgP2iX\n" +
                    "R8oOrAX/Ogp+FilUkdrZlhhhzV/NdGdQrPQxxpWbXsLOgiyEU4W1BWSHHI1E1cs0KUoYvaboAYaq\n" +
                    "E+0WZlhqDxQy3SKOZVPYk9fJu9+qAb0gaDIgtN4FrZFYvTrlNMcrmyaC2ozr43nxwVMlq9dnAgMB\n" +
                    "AAGjggEHMIIBAzAJBgNVHRMEAjAAMAsGA1UdDwQEAwIE8DAdBgNVHQ4EFgQU90FgTLg4k1yN3yJu\n" +
                    "d4TZIbxUGigwgZMGA1UdIwSBizCBiIAU8r6NqmFJES25W3IkKhjSwoXGmIGhbaRrMGkxCzAJBgNV\n" +
                    "BAYTAlVTMTgwNgYDVQQKEy9OYXRpb25hbCBDZW50ZXIgZm9yIFN1cGVyY29tcHV0aW5nIEFwcGxp\n" +
                    "Y2F0aW9uczEgMB4GA1UEAxMXQ2VydGlmaWNhdGlvbiBBdXRob3JpdHmCAQAwNAYDVR0fBC0wKzAp\n" +
                    "oCegJYYjaHR0cDovL2NhLm5jc2EudWl1Yy5lZHUvNGE2Y2Q4YjEucjAwDQYJKoZIhvcNAQEEBQAD\n" +
                    "ggEBAHnFMFeZTHjuq/juH3qw1jDrr4IuKnljuLHWSi8GuMt1mxOs/vR7rMg0OAs16gMJP3X2uSK5\n" +
                    "0miXqsbZ4AQuBJqo5Pm8UaSzeJlBle02SUvt2Fxhze9odLgoUXHp/1hKm9blHbp5cZqlbsmckk8y\n" +
                    "4xOPgNYh1G3oxdv2OcadmJBMlYwcBK+TkO8GbemqXqdFt6itGkkhLGQdspw9c1r38bXd9lhLbVR8\n" +
                    "7yif8ffqIgouVe/wj3NIjSbxgjff88Hz6CCuOWiafvfpgmrU906yOZqe6jBDBTKF5xmqyCNKKFAJ\n" +
                    "UbmPCX2vMKCpWrLU+MotR2HSbljslSUhfKCjHvFb/AA=\n" +
                    "</ds:X509Certificate>\n" +
                    "</ds:X509Data>\n" +
                    "</ds:KeyInfo></ds:Signature></Assertion>";


    public void testOMNS() {
        try {
            StAXOMBuilder builder =
                    new StAXOMBuilder(new ByteArrayInputStream(originalXML.getBytes()));
            OMElement documentElement = builder.getDocumentElement();
            //assertXMLEqual(originalXML, documentElement.toString());
            documentElement.build();

            String outstr = documentElement.toString();
            assertTrue(outstr.indexOf("xmlns:saml=") > 0);
            assertTrue(outstr.indexOf("<Assertion") == 0);

        } catch (XMLStreamException e) {
            e.printStackTrace();

        }
    }


}





