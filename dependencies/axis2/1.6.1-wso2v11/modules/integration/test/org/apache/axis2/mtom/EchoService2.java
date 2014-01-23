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

package org.apache.axis2.mtom;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.attachments.IncomingAttachmentInputStream;
import org.apache.axiom.attachments.IncomingAttachmentStreams;
import org.apache.axiom.attachments.utils.IOUtils;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.context.MessageContext;

public class EchoService2 {


    public OMElement mtomSample(OMElement element) throws Exception {

        Attachments attachments = null;
        attachments = (Attachments)MessageContext.getCurrentMessageContext()
                .getProperty(MTOMConstants.ATTACHMENTS);
        // Get image data
        IncomingAttachmentStreams streams = attachments.getIncomingAttachmentStreams();
        IncomingAttachmentInputStream stream = streams.getNextStream();

        byte[] data = IOUtils.getStreamAsByteArray(stream);

        //setting response
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace("urn://fakenamespace", "ns");
        OMElement response, elem;

        response = fac.createOMElement("response", ns);

        elem = fac.createOMElement("data", ns);
        elem.setText(Base64.encode(data));
        response.addChild(elem);

        return response;
    }
}
