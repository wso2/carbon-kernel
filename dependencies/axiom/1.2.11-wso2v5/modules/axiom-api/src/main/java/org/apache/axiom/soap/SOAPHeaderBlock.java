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

package org.apache.axiom.soap;

import org.apache.axiom.om.OMSourcedElement;

/**
 * <P>An object representing the contents in the SOAP header part of the SOAP envelope. The
 * immediate children of a <CODE> SOAPHeader</CODE> object can be represented only as <CODE>
 * SOAPHeaderBlock</CODE> objects.</P> <P>B <CODE>SOAPHeaderBlock</CODE> object can have other
 * <CODE>OMElement</CODE> objects as its children.</P>
 */
public interface SOAPHeaderBlock extends OMSourcedElement {
    
    /**
     * A SOAPHeaderBlock may be represented as an unexpanded OMSourcedElement.
     * In such cases, the underlying OMDataSource may have a property that contains
     * the value of the ROLE/ACTOR, RELAY or MUST_UNDERSTAND setting.
     */
    public String ROLE_PROPERTY = "org.apache.axiom.soap.SOAPHeader.ROLE";
    public String RELAY_PROPERTY = "org.apache.axiom.soap.SOAPHeader.RELAY";
    public String MUST_UNDERSTAND_PROPERTY = "org.apache.axiom.soap.SOAPHeader.MUST_UNDERSTAND";
    
    /**
     * Sets the actor associated with this <CODE> SOAPHeaderBlock</CODE> object to the specified
     * actor.
     *
     * @param roleURI a <CODE>String</CODE> giving the URI of the actor to set
     * @throws IllegalArgumentException
     *          if there is a problem in setting the actor.
     * @see #getRole() getRole()
     */
    void setRole(String roleURI);

    /**
     * Returns the uri of the actor associated with this <CODE> SOAPHeaderBlock</CODE> object.
     *
     * @return a <CODE>String</CODE> giving the URI of the actor
     * @see #setRole(String) setRole(java.lang.String)
     */
    String getRole();

    /**
     * Sets the mustUnderstand attribute for this <CODE> SOAPHeaderBlock</CODE> object to be on or
     * off. <P>If the mustUnderstand attribute is on, the actor who receives the
     * <CODE>SOAPHeaderBlock</CODE> must process it correctly. This ensures, for example, that if
     * the <CODE> SOAPHeaderBlock</CODE> object modifies the message, that the message is being
     * modified correctly.</P>
     *
     * @param mustUnderstand <CODE>true</CODE> to set the mustUnderstand attribute on;
     *                       <CODE>false</CODE> to turn if off
     * @throws IllegalArgumentException
     *          if there is a problem in setting the actor.
     * @see #getMustUnderstand() getMustUnderstand()
     */
    void setMustUnderstand(boolean mustUnderstand);

    void setMustUnderstand(String mustUnderstand) throws SOAPProcessingException;

    /**
     * Returns whether the mustUnderstand attribute for this <CODE>SOAPHeaderBlock</CODE> object is
     * turned on.
     *
     * @return <CODE>true</CODE> if the mustUnderstand attribute of this
     *         <CODE>SOAPHeaderBlock</CODE> object is turned on; <CODE>false</CODE> otherwise
     */
    boolean getMustUnderstand() throws SOAPProcessingException;


    boolean isProcessed();

    /**
     * We need to know whether all the mustUnderstand headers have been processed by the node. This
     * will done by a specific validation handler at the end of the execution chain. For this all
     * the handlers who process a particular header block must explicitly say that he processesd the
     * header by calling setProcessed()
     */
    void setProcessed();


    /**
     * Sets the relay attribute for this SOAPHeaderBlock to be either true or false. The SOAP relay
     * attribute is set to true to indicate that the SOAP header block must be relayed by any node
     * that is targeted by the header block but not actually process it.
     *
     * @param relay a <CODE>boolean</CODE> giving the value to be set
     */
    void setRelay(boolean relay);

    /**
     * Returns the relay  status associated with this <CODE> SOAPHeaderBlock</CODE> object.
     *
     * @return a <CODE>boolean</CODE> giving the relay status
     */
    boolean getRelay();

    /**
     * What SOAP version is this HeaderBlock?
     *
     * @return a SOAPVersion, one of the two singletons.
     */
    SOAPVersion getVersion();
}
