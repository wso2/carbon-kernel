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

package org.apache.axis2.schema.unqualified;

import org.apache.axis2.schema.AbstractTestCase;
import org.apache.axis2.schema.unqualified.companyflow.CompanyResponse;
import org.apache.axis2.schema.unqualified.companyservice.GetCompanyDetailsResponse;
import org.apache.axis2.schema.unqualified.companytype.Address_type;
import org.apache.axis2.schema.unqualified.companytype.String_Type;

public class UnqualifiedTest extends AbstractTestCase {
    public void testTestElement() throws Exception {
        TestElement testElement = new TestElement();
        testElement.setElement1("element1");
        testElement.setElement2("element2");
        testSerializeDeserialize(testElement, false);
    }
    
    /**
     * Regression test for AXIS2-4374.
     * 
     * @throws Exception
     */
    public void testGetCompanyDetailsResponse() throws Exception {
        String_Type companyId = new String_Type();
        companyId.setString("112233");
        
        String_Type userId = new String_Type();
        userId.setString("user");
        
        String_Type address1 = new String_Type();
        address1.setString("Fenchurch street");
        
        Address_type address = new Address_type();
        address.setAddress1(address1);
        
        CompanyResponse company = new CompanyResponse();
        company.setCompanyId(companyId);
        company.setUserId(userId);
        company.setAddress(address);
        
        GetCompanyDetailsResponse response = new GetCompanyDetailsResponse();
        response.setOut(company);
        
        testSerializeDeserialize(response, false);
    }
}
