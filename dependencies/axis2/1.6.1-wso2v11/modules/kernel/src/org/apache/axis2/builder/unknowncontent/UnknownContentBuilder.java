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

package org.apache.axis2.builder.unknowncontent;

import java.io.InputStream;

import javax.activation.DataHandler;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;

public class UnknownContentBuilder implements Builder {

	public OMElement processDocument(InputStream inputStream,
			String contentType, MessageContext messageContext) throws AxisFault {

		// using this to figure out whether the received is a MIME message
		if (messageContext.isDoingSwA()) {
			String rootPartContentID = messageContext.attachments
					.getSOAPPartContentID();
			DataHandler rootPartDataHandler = messageContext.attachments
					.getDataHandler(rootPartContentID);
			UnknownContentOMDataSource unknownContentDataSource = new UnknownContentOMDataSource(
					rootPartDataHandler);
			OMFactory factory = OMAbstractFactory.getOMFactory();
			messageContext.setDoingSwA(false);
			messageContext.setDoingMTOM(false);
			return factory.createOMElement(unknownContentDataSource, UnknownContentOMDataSource.unknownContentQName);
		}
		InputStreamDataSource inStreamDataSource = new InputStreamDataSource(
				inputStream);
		inStreamDataSource.setType(contentType);
		DataHandler dataHandler = new DataHandler(inStreamDataSource);
		UnknownContentOMDataSource unknownContentDataSource = new UnknownContentOMDataSource(
				dataHandler);
		OMFactory factory = OMAbstractFactory.getOMFactory();
		return factory.createOMElement(unknownContentDataSource, UnknownContentOMDataSource.unknownContentQName);
	}
}
