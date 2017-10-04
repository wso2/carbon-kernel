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
package org.wso2.carbon.utils.logging;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.BaseTest;

/**
 * Test cases for TenantAwarePatternLayout
 */
public class TenantAwarePatternLayoutTest extends BaseTest {
    @Test
    public void testCreateTenantAwarePatternLayout() throws Exception {
        String pattern = "TID: [%T] [%S] [%U] [%A] [%D] [%I] [%H] [%P] [%d] %P%5p {%c} - %x %m%n";
        TenantAwarePatternLayout tenantAwarePatternLayout = new TenantAwarePatternLayout(pattern);
        Assert.assertEquals(tenantAwarePatternLayout.getConversionPattern(), pattern);
    }

    @Test
    public void testCreateTenantAwarePatternParser() throws Exception {
        String pattern = "TID: [%T] [%S] [%U] [%A] [%D] [%I] [%H] [%P] [%d] %P%5p {%c} - %x %m%n";
        TenantAwarePatternLayout tenantAwarePatternLayout = new TenantAwarePatternLayout();
        tenantAwarePatternLayout.createPatternParser(pattern);
    }
}
