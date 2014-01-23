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
package org.apache.axiom.attachments;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;

import org.apache.axiom.om.AbstractTestCase;

public class PdfAttachmentStreamingTest extends AbstractTestCase {

	String contentType = "multipart/related;type=\"text/xml\";boundary=\"----=_Part_0_3437046.1188904239130\";start=__WLS__1188904239161__SOAP__";
	String inputFile = "mtom/msg-soap-wls81.txt";
	
	public PdfAttachmentStreamingTest(String name) {
		super(name);
	}
	
	public void testStreamingAttachments() throws Exception {
		InputStream inStream = getTestResource(inputFile);
		// creating attachments using that stream
		Attachments attachments = new Attachments(inStream, contentType);

		// getting attachments as streams
		IncomingAttachmentStreams attachStreams = attachments.getIncomingAttachmentStreams();
		

		// getting first attachments after the soap part
		IncomingAttachmentInputStream firstAttach = attachStreams.getNextStream();
		
		// coping contents of the attachment to byte array
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(firstAttach, output);

		// reading the message again, getting second attachment using datahandlers
		inStream = getTestResource(inputFile);
		attachments = new Attachments(inStream, contentType);
		DataHandler h = attachments.getDataHandler((String)attachments.getAllContentIDs()[1]);

		ByteArrayOutputStream input = new ByteArrayOutputStream();
		copy(h.getInputStream(), input);

		assertEquals(input.toString("UTF-8"), output.toString("UTF-8"));
	}
	


	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[4096];
		while(true) {
			int len = in.read(buf);
			if (len != -1) {
				out.write(buf, 0, len);
			} else {
				break;
			}
		}
	}
}
