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

package org.apache.axis2.fastinfoset;

import com.sun.xml.fastinfoset.stax.StAXDocumentParser;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

public class FastInfosetBuilder implements Builder {

	private static  Log logger = LogFactory.getLog(FastInfosetBuilder.class);
	
	/**
	 * Returns a OMElement handler to the document element of the Fast Infoset message.
	 * 
	 * @param inputStream InputStream to the message
	 * @param contentType Content type of the message
	 * @param messageContext MessageContext to be used
	 * 
	 * @return OMElement handler to the document element
	 * 
	 * @see org.apache.axis2.builder.Builder#processDocument(InputStream, String, MessageContext)
	 */
	public OMElement processDocument(InputStream inputStream, String contentType, 
			MessageContext messageContext) throws AxisFault {
		if (logger.isDebugEnabled()) {
			logger.debug("Processing a Document with the content type: " + contentType);
		}
		//Create a instance of the StAX Parser which can handle the fast infoset stream 
		XMLStreamReader streamReader = new StAXDocumentParser(inputStream);
		StAXBuilder builder = new StAXSOAPModelBuilder(streamReader);

		return builder.getDocumentElement();
	}
}
