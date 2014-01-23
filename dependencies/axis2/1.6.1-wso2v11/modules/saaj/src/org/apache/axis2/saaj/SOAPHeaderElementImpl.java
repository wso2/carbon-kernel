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

package org.apache.axis2.saaj;

import org.apache.axiom.om.impl.dom.ElementImpl;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

public class SOAPHeaderElementImpl extends SOAPElementImpl implements SOAPHeaderElement {

    private SOAPHeaderBlock headerElem;

    /** @param element  */
    public SOAPHeaderElementImpl(SOAPHeaderBlock element) {
        super((ElementImpl)element);
        this.headerElem = element;
    }

    /**
     * Sets the actor associated with this <CODE> SOAPHeaderElement</CODE> object to the specified
     * actor. The default value of an actor is: <CODE> SOAPConstants.URI_SOAP_ACTOR_NEXT</CODE>
     *
     * @param actorURI a <CODE>String</CODE> giving the URI of the actor to set
     * @throws IllegalArgumentException
     *          if there is a problem in setting the actor.
     * @see #getActor() getActor()
     */
    public void setActor(String actorURI) {
        this.headerElem.setRole(actorURI);
    }

    /**
     * Returns the uri of the actor associated with this <CODE> SOAPHeaderElement</CODE> object.
     *
     * @return a <CODE>String</CODE> giving the URI of the actor
     * @see #setActor(String) setActor(java.lang.String)
     */
    public String getActor() {
        return this.headerElem.getRole();
    }

    /**
     * Sets the mustUnderstand attribute for this <CODE> SOAPHeaderElement</CODE> object to be on or
     * off.
     * <p/>
     * <P>If the mustUnderstand attribute is on, the actor who receives the
     * <CODE>SOAPHeaderElement</CODE> must process it correctly. This ensures, for example, that if
     * the <CODE> SOAPHeaderElement</CODE> object modifies the message, that the message is being
     * modified correctly.</P>
     *
     * @param mustUnderstand <CODE>true</CODE> to set the mustUnderstand attribute on;
     *                       <CODE>false</CODE> to turn if off
     * @throws IllegalArgumentException
     *          if there is a problem in setting the actor.
     * @see #getMustUnderstand() getMustUnderstand()
     */
    public void setMustUnderstand(boolean mustUnderstand) {
        this.headerElem.setMustUnderstand(mustUnderstand);
    }

    /**
     * Returns whether the mustUnderstand attribute for this <CODE>SOAPHeaderElement</CODE> object
     * is turned on.
     *
     * @return <CODE>true</CODE> if the mustUnderstand attribute of this
     *         <CODE>SOAPHeaderElement</CODE> object is turned on; <CODE>false</CODE> otherwise
     */
    public boolean getMustUnderstand() {
        return this.headerElem.getMustUnderstand();
    }

    /**
     * Sets the Role associated with this SOAPHeaderElement object to the specified Role.
     *
     * @param uri - the URI of the Role
     * @throws SOAPException - if there is an error in setting the role java.lang.UnsupportedOperationException
     *                       - if this message does not support the SOAP 1.2 concept of Fault Role.
     */
    public void setRole(String uri) throws SOAPException {
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            throw new UnsupportedOperationException();
        } else {
            this.headerElem.setRole(uri);
        }
    }

    public String getRole() {
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            throw new UnsupportedOperationException();
        } else {
            return this.headerElem.getRole();
        }
    }

    /**
     * Sets the relay attribute for this SOAPHeaderElement to be either true or false. The SOAP
     * relay attribute is set to true to indicate that the SOAP header block must be relayed by any
     * node that is targeted by the header block but not actually process it. This attribute is
     * ignored on header blocks whose mustUnderstand attribute is set to true or that are targeted
     * at the ultimate reciever (which is the default). The default value of this attribute is
     * false.
     *
     * @param relay - the new value of the relay attribute
     * @throws SOAPException - if there is a problem in setting the relay attribute.
     *                       java.lang.UnsupportedOperationException - if this message does not
     *                       support the SOAP 1.2 concept of Relay attribute.
     */
    public void setRelay(boolean flag) throws SOAPException {
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            throw new UnsupportedOperationException();
        } else {
            this.headerElem.setRelay(flag);
        }
    }

    public boolean getRelay() {
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            throw new UnsupportedOperationException();
        } else {
            return this.headerElem.getRelay();
        }
    }

    public void setParentElement(SOAPElement parent) throws SOAPException {
        if (!(parent instanceof SOAPHeader)) {
            throw new IllegalArgumentException("Parent is not a SOAPHeader");
        }
        super.setParentElement(parent);
    }
}
