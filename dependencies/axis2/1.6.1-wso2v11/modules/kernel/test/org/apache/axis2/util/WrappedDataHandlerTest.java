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

package org.apache.axis2.util;

import java.net.URL;
import javax.activation.DataHandler;
import junit.framework.TestCase;

/**
 * Test the WrappedDataHandler class.
 */
public class WrappedDataHandlerTest extends TestCase {
   
   /**
    * Verify that the Wrapped DataHandler maintains the correct content-type value
    * for an XML document attachment.
    */
   public void testWrappedDataHandler() throws Exception {
      URL xmlAttachment = new URL("file:./test-resources/soapmessage.xml");

      DataHandler dh = new DataHandler(xmlAttachment);
      assertTrue(dh.getContentType().equals("application/xml"));

      WrappedDataHandler wrappedDH = new WrappedDataHandler(dh, "text/xml");
      assertTrue(wrappedDH.getContentType() != null);
      assertTrue(wrappedDH.getContentType().equals("text/xml"));
   }
}