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

package org.apache.axis2.schema.populate.simple;

public class SimpleTypeQNamePopulateTest extends AbstractSimplePopulater{
    private String xmlString[] = {
            "<qNameParam xmlns=\"http://soapinterop.org/xsd\">university</qNameParam>",
            "<qNameParam xmlns=\"http://soapinterop.org/xsd\" xmlns:ns1=\"http://ws.apache.org/axis2\">ns1:axis2</qNameParam>"
            //"<qNameParam>http://mail.google.com/mail/?auth=DQAAAHEAAAC041</qNameParam>"
    };
    // force others to implement this method
    public void testPopulate() throws Exception {
        process(xmlString[0],"org.soapinterop.xsd.QNameParam");
        process(xmlString[1],"org.soapinterop.xsd.QNameParam");
       // process(xmlString[2],"org.soapinterop.qNameParam");
    }
}
