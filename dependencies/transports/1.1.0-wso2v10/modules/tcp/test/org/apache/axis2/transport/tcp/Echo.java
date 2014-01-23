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

package org.apache.axis2.transport.tcp;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Echo {

    private static final Log log = LogFactory.getLog(Echo.class);
    public static final String SERVICE_NAME = "EchoXMLService";
    public static final String ECHO_OM_ELEMENT_OP_NAME = "echoOMElement";
    
    public Echo() {
    }

    public void echoVoid() {
        log.info("echo Service Called");
    }

    public void echoOMElementNoResponse(OMElement omEle) {
        log.info("echoOMElementNoResponse service called.");
    }

    public OMElement echoOMElement(OMElement omEle) {
        omEle.buildWithAttachments();
        omEle.setLocalName(omEle.getLocalName() + "Response");
        if (omEle.getFirstElement().getText().trim().startsWith("fault")) {
            throw new RuntimeException("fault string found in echoOMElement");
        }
        return omEle;
    }

    public OMElement echoOM(OMElement omEle) {
        return omEle;
    }

    public String echoString(String in) {
        return in;
    }

    public int echoInt(int in) {
        return in;
    }

    public OMElement echoMTOMtoBase64(OMElement omEle) {
        OMText omText = (OMText)(omEle.getFirstElement()).getFirstOMChild();
        omText.setOptimize(false);
        return omEle;
    }
}