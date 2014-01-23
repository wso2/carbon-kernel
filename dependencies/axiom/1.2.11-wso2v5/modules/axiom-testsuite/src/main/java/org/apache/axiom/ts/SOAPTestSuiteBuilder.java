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
package org.apache.axiom.ts;

import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.ts.soap.SOAPSpec;

public class SOAPTestSuiteBuilder extends AxiomTestSuiteBuilder {
    public SOAPTestSuiteBuilder(OMMetaFactory metaFactory) {
        super(metaFactory);
    }
    
    private void addTests(SOAPSpec spec) {
        addTest(new org.apache.axiom.ts.soap.body.TestAddFault1(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.body.TestAddFault2(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.body.TestGetFault(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.body.TestGetFaultWithParser(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.body.TestHasFault(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.body.TestHasFaultWithParser(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.envelope.TestAddHeaderToIncompleteEnvelope(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.envelope.TestBodyHeaderOrder(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.envelope.TestDiscardHeader(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.envelope.TestGetBody(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.envelope.TestGetBodyOnEmptyEnvelope(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.envelope.TestGetBodyOnEnvelopeWithHeaderOnly(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.envelope.TestGetBodyWithParser(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.envelope.TestGetHeader(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.envelope.TestGetHeaderWithParser(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.envelope.TestGetSOAPBodyFirstElementLocalNameAndNS(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.factory.TestCreateSOAPEnvelope(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.factory.TestGetDefaultFaultEnvelope(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.fault.TestGetCode(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.fault.TestGetCodeWithParser(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.fault.TestGetDetail(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.fault.TestGetDetailWithParser(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.fault.TestGetReason(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.fault.TestGetReasonWithParser(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.fault.TestGetRole(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.fault.TestGetRoleWithParser(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.fault.TestSetCode(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.fault.TestSetDetail(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.fault.TestSetReason(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.fault.TestSetRole(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.faultdetail.TestAddDetailEntry(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.faultdetail.TestGetAllDetailEntries(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.faultdetail.TestGetAllDetailEntriesWithParser(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.faultdetail.TestSerialization(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.faultdetail.TestWSCommons202(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.header.TestAddHeaderBlock(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.header.TestExamineAllHeaderBlocks(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.header.TestExamineHeaderBlocks(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.header.TestGetHeaderBlocksWithNSURI(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.headerblock.TestGetMustUnderstand(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.headerblock.TestGetRole(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.headerblock.TestSetMustUnderstand(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.headerblock.TestSetMustUnderstandString01(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.headerblock.TestSetMustUnderstandWithInvalidValue(metaFactory, spec));
        addTest(new org.apache.axiom.ts.soap.headerblock.TestSetRole(metaFactory, spec));
    }
    
    protected void addTests() {
        addTests(SOAPSpec.SOAP11);
        addTests(SOAPSpec.SOAP12);
        addTest(new org.apache.axiom.ts.soap11.envelope.TestAddElementAfterBody(metaFactory));
        addTest(new org.apache.axiom.ts.soap11.fault.TestGetNode(metaFactory));
        addTest(new org.apache.axiom.ts.soap11.fault.TestSetNode(metaFactory));
        addTest(new org.apache.axiom.ts.soap12.envelope.TestAddElementAfterBody(metaFactory));
        addTest(new org.apache.axiom.ts.soap12.fault.TestGetNode(metaFactory));
        addTest(new org.apache.axiom.ts.soap12.fault.TestGetNodeWithParser(metaFactory));
        addTest(new org.apache.axiom.ts.soap12.fault.TestMoreChildrenAddition(metaFactory));
        addTest(new org.apache.axiom.ts.soap12.fault.TestSetNode(metaFactory));
    }
}
