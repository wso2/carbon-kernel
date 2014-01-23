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

package org.apache.axis2.jaxws.message;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This is a value class that is an agnostic representation of a fault. The XMLFault can be added to
 * or queried from a Message/XMLPart.
 * <p/>
 * Even though XMLFault is SOAP 1.1/SOAP 1.2 agnostic, SOAP 1.2 terms will be used. For example,
 * "reason" means SOAP 1.2 Reason or SOAP 1.1 faultstring.
 *
 * @see XMLFaultUtils
 */
public class XMLFault {

    private static Log log = LogFactory.getLog(XMLFault.class);
    
    // Here is a sample comprehensive SOAP 1.2 fault which will help you understand the
    // structure of XMLFault.
    // <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope"
    //               xmlns:m="http://www.example.org/timeouts"
    //               xmlns:xml="http://www.w3.org/XML/1998/namespace">
    //   <env:Body>
    //      <env:Fault>
    //        <env:Code>
    //          <env:Value>env:Sender</env:Value>
    //          <env:Subcode>
    //            <env:Value>m:MessageTimeout</env:Value>
    //          </env:Subcode>
    //        </env:Code>
    //        <env:Reason>
    //          <env:Text xml:lang="en">Sender Timeout</env:Text>
    //          <env:Text xml:lang="de">Sender Timeout</env:Text>
    //        </env:Reason>
    //        <env:Node>http://my.example.org/Node</env:Node>
    //        <env:Role>http://my.example.org/Role</env:Role>
    //        <env:Detail>
    //          <m:MaxTime>P5M</m:MaxTime>
    //        </env:Detail>    
    //      </env:Fault>
    //   </env:Body>
    // </env:Envelope>

    // Here is the same information rendered as a SOAP 1.1 fault.  Notice
    // that this is a subset of information.
    // <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope"
    //               xmlns:m="http://www.example.org/timeouts"
    //               xmlns:xml="http://www.w3.org/XML/1998/namespace">
    //  <env:Body>
    //      <env:Fault>
    //        <faultcode>env.Client</faultcode>
    //        <faultstring>Sender Timeout</faultstring>
    //        <actor>http://my.example.org/Role</actor>
    //        <detail>
    //          <m:MaxTime>P5M</m:MaxTime>
    //        <detail>    
    //      </env:Fault>
    //   </env:Body>
    // </env:Envelope>

    // The required information (necessary for both SOAP 1.1 and SOAP 1.2)
    private XMLFaultCode code;
    private XMLFaultReason reason;
    private Block[] detailBlocks;

    // The optional information (can be set on XMLFault, but only rendered in SOAP 1.2 Faults)
    private String role;
    private String node;
    private QName[] subCodes =
            null;  // The subCodes are user-defined.  The values are not defined by a specification
    private XMLFaultReason[] secondaryReasons = null;

    /**
     * Construct Application XMLFault with minimum required information
     *
     * @param code         - XMLFaultCode or null if default XMLFaultCode
     * @param reason       - String reason
     * @param detailBlocks - Block[] or null if no detailBlocks
     */
    public XMLFault(XMLFaultCode code, XMLFaultReason reason, Block[] detailBlocks) {
        if (code == null) {
            code = XMLFaultCode.RECEIVER;
        }
        this.code = code;
        this.reason = reason;
        this.detailBlocks = detailBlocks;
        if (log.isDebugEnabled()) {
            log.debug("Created XMLFault:" + this.dump(""));
        }
    }


    /**
     * Construct System XMLFault with minimum required information
     *
     * @param code   - XMLFaultCode or null if default XMLFaultCode
     * @param reason - String reason
     */
    public XMLFault(XMLFaultCode code, XMLFaultReason reason) {
        this(code, reason, null);
    }


    /** @return Returns the code. */
    public XMLFaultCode getCode() {
        return code;
    }


    /** @return Returns the detailBlocks. */
    public Block[] getDetailBlocks() {
        return detailBlocks;
    }


    /** @return Returns the reason. */
    public XMLFaultReason getReason() {
        return reason;
    }


    /** @return Returns the node. */
    public String getNode() {
        return node;
    }


    /** @param node The node to set. */
    public void setNode(String node) {
        this.node = node;
    }


    /** @return Returns the role. */
    public String getRole() {
        return role;
    }


    /** @param role The role to set. */
    public void setRole(String role) {
        this.role = role;
    }


    /** @return Returns the secondaryReasons. */
    public XMLFaultReason[] getSecondaryReasons() {
        return secondaryReasons;
    }


    /** @param secondaryReasons The secondaryReasons to set. */
    public void setSecondaryReasons(XMLFaultReason[] secondaryReasons) {
        this.secondaryReasons = secondaryReasons;
    }


    /** @return Returns the subCodes. */
    public QName[] getSubCodes() {
        return subCodes;
    }


    /** @param subCodes The subCodes to set. */
    public void setSubCodes(QName[] subCodes) {
        this.subCodes = subCodes;
    }

    /**
     * dump contents, used for debugging
     * @return String containing contents of XMLFault
     */
    public String dump(String indent) {
        String text = "";
        final String NL = "\n";
        try {
            text += indent + "XMLFault " + this + NL;
            text += indent + " code=   " + code.toQName("") + NL;
            if (reason != null) {
                text += indent + " reason= " + reason.getText() + NL;
            } else {
                text += indent + " reason= null" + NL;
            }
            text += indent + " role   =" + role + NL;
            text += indent + " node   =" + node + NL;
            if (detailBlocks == null) {
                text += indent + " no detail blocks" + NL;
            } else {
                for (int i=0; i<detailBlocks.length; i++) {
                    text += indent + " detail= " + detailBlocks[i].getQName();
                }
            }
            
        } catch (Exception e) {
            text += "Could not dump the XMLFault due to exception " + e;
        }
        return text;
    }
}

