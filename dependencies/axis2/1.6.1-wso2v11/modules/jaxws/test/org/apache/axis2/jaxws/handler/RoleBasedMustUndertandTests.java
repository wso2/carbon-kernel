/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.handler;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.RolePlayer;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainType;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;
import org.apache.axis2.jaxws.description.xml.handler.HandlerType;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.PortInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * Test Role (SOAP 1.2 terminology) or Actor (SOAP 1.1 terminology) mustUndertand processing.
 */
public class RoleBasedMustUndertandTests extends TestCase {
    private SOAPFactory soap11Factory;
    private SOAPFactory soap12Factory;
    private OMFactory omFactory;
    
    private String namespace = "http://RoleBasedMustUnderstandTest/good/namespace";
    private String soap11Namespace = "http://schemas.xmlsoap.org/soap/envelope/";
    private String header_ultimateReceiver = "header_ultimateReceiver";
    private String header_ultimateReceiver_processed = "header_ultmiateReceiver_processed";
    private String header_rolePlayed = "header_rolePlayed";
    private String header_roleNotPlayed = "header_roleNotPlayed";
    private String header_bindingAdded = "header_bindingAdded";
    private String header_SoapNext = "header_SoapNext";
    
    private String rolePlayed1 = "http://Role/Played/role1";
    private String roleNotPlayed = "http://Role/Not/Played/role1";
    private String roleBindingAdded = "http://Role/Added/By/Binding";
    private String roleNoHeaders = "http://Role/Played/No/Headers";
    private String roleHandlerAdded = "http://org/apache/axis2/jaxws/handler/AddedRole";
    private String roleHandlerAdded2 = "http://org/apache/axis2/jaxws/handler/AddedRole2";
    private String roleSoap11Next = "http://schemas.xmlsoap.org/soap/actor/next";

    public RoleBasedMustUndertandTests(String testName) {
        super(testName);
        soap11Factory = OMAbstractFactory.getSOAP11Factory();
        soap12Factory = OMAbstractFactory.getSOAP12Factory();
        omFactory = OMAbstractFactory.getOMFactory();
    }
    
    /**
     * Validate that handler information can be cached on the ServiceDescritpion on the server side. 
     */
    public void testCachingOnServer() {
        ServiceDescription serviceDesc = DescriptionFactory.createServiceDescription(RoleBasedMUServiceImpl.class);
        HandlerResolverImpl handlerResolver1 = new HandlerResolverImpl(serviceDesc);
        HandlerResolverImpl handlerResolver2 = new HandlerResolverImpl(serviceDesc);

        EndpointDescription epDesc = serviceDesc.getEndpointDescriptions()[0];
        PortInfo portInfo = epDesc.getPortInfo();
        
        List<String> roles1 = handlerResolver1.getRoles(portInfo);
        List<String> roles2 = handlerResolver2.getRoles(portInfo);
        assertNotNull(roles1);
        assertNotNull(roles2);
        assertTrue(roles1 == roles2);
    }
    
    
    /**
     * Validate that handler information can NOT be cached on the client.  That is because the client
     * can specify handler information per instance of a service delegate.  Those service
     * delegates could share a ServiceDescription, but since each service delegate could specify
     * unique handler information, we can't use common handler information stored on the
     * ServiceDescription 
     */
    public void testCachingOnClient() {
        ServiceDescription serviceDesc = DescriptionFactory.createServiceDescription(RoleBasedMUServiceImpl.class);
        HandlerResolverImpl handlerResolver1 = new HandlerResolverImpl(serviceDesc, "sd1");
        HandlerResolverImpl handlerResolver2 = new HandlerResolverImpl(serviceDesc, "sd2");

        EndpointDescription epDesc = serviceDesc.getEndpointDescriptions()[0];
        PortInfo portInfo = epDesc.getPortInfo();
        
        List<String> roles1 = handlerResolver1.getRoles(portInfo);
        List<String> roles2 = handlerResolver2.getRoles(portInfo);
        assertNotNull(roles1);
        assertNotNull(roles2);
        assertTrue(roles1 != roles2);
        
    }
    
    /**
     * The JAXWS Spec, section 10, says roles can be set on the SOAP binding.  Verify that a 
     * mustUnderstand header which is not processed for a role added by the SOAPBinding causes
     * a Not Understood fault 
     */
    public void testRoleAddedToSoapBinding() {
        RolePlayer rolePlayer = new UltimateDestinationRoles();
        
        MessageContext mscCtx11 = createMessageContext(soap11Factory);
        // Note that since this role is UltimateReceiver, all unprocessed headers will be set
        // on the message context
        setUnprocessedHeaders(mscCtx11, rolePlayer);
        
        // Indicate we understand (e.g. via handlers) the headers for Ultimate Receiver and the
        // role we are acting in.  We don't indicate we understand the header that the SOAPBinding
        // added, so that should cause an exception
        List<QName> understoodHeaders = new ArrayList<QName>();
        understoodHeaders.add(new QName(namespace, header_ultimateReceiver));
        understoodHeaders.add(new QName(namespace, header_rolePlayed));
        
        // Create a list of additional roles that acted in (e.g. one played by a JAXWS
        // handler)
        List<String> headerRolesPlayed = bindingAddedRole();

        try {
            HandlerUtils.checkMustUnderstand(mscCtx11, understoodHeaders, headerRolesPlayed);
            fail("Should have gotten NotUnderstood fault for header for role added by binding: " + header_bindingAdded);
        } catch (AxisFault af) {
            // Expected path; should get exception for ultimate receiver not processed.
            String checkFault = af.toString();
            if (!checkFault.contains("Must Understand check failed")
                || !checkFault.contains(header_bindingAdded)) {
                fail("Did not get expected NotUnderstood AxisFault.  Unexpected fault " + af);
            }
        } catch (Exception e) {
            fail("Caught unexpected exception  + e");
        }
    }
    
    /**
     * Test where there are no previous non-understood headers on the MessageContext and there's an 
     * additional Role added by a JAXWS handler
     */
    public void testOnlySoapBindingRole() {
        MessageContext mscCtx11 = createMessageContext(soap11Factory);
        // Note that we are not putting any non-understood headers on the message context
        // Create a list of additional roles (e.g. one played by a JAXWS
        // handler
        List<String> headerRolesPlayed = bindingAddedRole();

        try {
            HandlerUtils.checkMustUnderstand(mscCtx11, null, headerRolesPlayed);
            fail("Should have gotten NotUnderstood fault for header for role added by binding: " + header_bindingAdded);
        } catch (AxisFault af) {
            // Expected path; should get exception for ultimate receiver not processed.
            String checkFault = af.toString();
            if (!checkFault.contains("Must Understand check failed")
                || !checkFault.contains(header_bindingAdded)) {
                fail("Did not get expected NotUnderstood AxisFault.  Unexpected fault " + af);
            }
        } catch (Exception e) {
            fail("Caught unexpected exception  + e");
        }
    }
    
    /**
     * Test that adding a role for headers that does not exist does not cause any fault
     */
    public void testNoHeadersForSoapBindingRole() {
        MessageContext mscCtx11 = createMessageContext(soap11Factory);

        List<String> headerRolesPlayed = noHeadersRole();

        try {
            HandlerUtils.checkMustUnderstand(mscCtx11, null, headerRolesPlayed);
        } catch (AxisFault af) {
            fail("Unexpected AxisFault "+ af);
        } catch (Exception e) {
            fail("Caught unexpected exception  + e");
        }
        
    }
    /**
     * A mustUnderstand header for a role not being acted in should NOT cause a notUnderstood
     * fault. 
     */
    public void testRoleNotActingIn() {
        RolePlayer rolePlayer = new UltimateDestinationRoles();
        
        MessageContext mscCtx11 = createMessageContext(soap11Factory);
        // Note that since this role is UltimateReceiver, all unprocessed headers will be set
        // on the message context
        setUnprocessedHeaders(mscCtx11, rolePlayer);
        
        // Indicate we understand (e.g. via handlers) the headers for Ultimate Receiver and the
        // role we are acting in.  This leaves the mustUnderstand header for the role we are not 
        // acting in, which should not cause an exception.
        List<QName> understoodHeaders = new ArrayList<QName>();
        understoodHeaders.add(new QName(namespace, header_ultimateReceiver));
        understoodHeaders.add(new QName(namespace, header_rolePlayed));
        try {
            HandlerUtils.checkMustUnderstand(mscCtx11, understoodHeaders, null);
        } catch (AxisFault e) {
            fail("Should not have caught an AxisFault " + e);
        }
    }
    
    /**
     * Test mustUnderstand processing for Ultimate Receiver with an understood header list
     * passed to the mustUnderstand checking
     */
    public void testUltimateReceiverRoles() {
        
        RolePlayer rolePlayer = new UltimateDestinationRoles();
        MessageContext mscCtx11 = createMessageContext(soap11Factory);
        setUnprocessedHeaders(mscCtx11, rolePlayer);
        // Indicate we understand (e.g. via handlers) all the headers.
        List<QName> understoodHeaders = new ArrayList<QName>();
        understoodHeaders.add(new QName(namespace, header_ultimateReceiver));
        understoodHeaders.add(new QName(namespace, header_rolePlayed));
        try {
            HandlerUtils.checkMustUnderstand(mscCtx11, understoodHeaders, null);
        } catch (AxisFault e) {
            fail("Should not have caught an AxisFault " + e);
        }
    }

    /**
     * Verify that an Ultimate Receiver with no roles only yields the header with no role specified
     * as unprocessed and that passing a list of understood headers including just that one causes
     * the mustUnderstand checks to pass.
     */
    public void testUltimateReceiver() {
        RolePlayer rolePlayer = new UltimateDestinationNoRoles();
        MessageContext msgCtx = createMessageContext(soap11Factory);
        // Indicate the must understand header without an explicit role will be understood.
        // The other headers, with explicit roles, should not cause an error.
        setUnprocessedHeaders(msgCtx, rolePlayer);
        List<QName> understoodHeaders = new ArrayList<QName>();
        understoodHeaders.add(new QName(namespace, header_ultimateReceiver));
        try {
            HandlerUtils.checkMustUnderstand(msgCtx, understoodHeaders, null);
        } catch (AxisFault e) {
            fail("Should not have caught and AxisFault " + e);
        }
    }
    /**
     * Test mustUnderstand processing for Ultimate Receiver without additional understood headers
     * being passed to the checker.  This should cause a notUnderstood fault
     */
    public void testUltimateReceiverNoRoles() {
        RolePlayer rolePlayer = new UltimateDestinationNoRoles();

        MessageContext messageContext11 = createMessageContext(soap11Factory);
        setUnprocessedHeaders(messageContext11, rolePlayer);
        try {
            HandlerUtils.checkMustUnderstand(messageContext11, null, null);
            fail("Should have gotten MustUnderstand Header Not Understood fault");
        } catch (AxisFault af) {
            // Expected path; should get exception for ultimate receiver not processed.
            String checkFault = af.toString();
            if (!checkFault.contains("Must Understand check failed")
                || !checkFault.contains(header_ultimateReceiver)) {
                fail("Did not get expected NotUnderstood AxisFault.  Unexpected fault " + af);
            }
        } catch (Exception e) {
            fail("Caught unexpected exception  + e");
        }
        
        MessageContext messageContext12 = createMessageContext(soap12Factory);
        setUnprocessedHeaders(messageContext12, rolePlayer);
        try {
            HandlerUtils.checkMustUnderstand(messageContext12, null, null);
            fail("Should have gotten MustUnderstand Header Not Understood fault");
        } catch (AxisFault af) {
            // Expected path; should get exception for ultimate receiver not processed.
            if (!af.toString().contains("Must Understand check failed")) {
                fail("Did not get expected NotUnderstood AxisFault.  Unexpected fault " + af);
            }
        } catch (Exception e) {
            fail("Caught unexpected exception  + e");
        }
    }
    
    /**
     * Test a RolePlayer that is not an ultimate receiver and does not act in any roles
     * corresponding to headers.  There should be no faults cause by the mustUnderstand headers
     */
    public void testNotUltimateReceiverNoRoles() {
        RolePlayer rolePlayer = new NotUltimateDestinationNoRoles();
        
        MessageContext msgCtx11 = createMessageContext(soap11Factory);
        setUnprocessedHeaders(msgCtx11, rolePlayer);
        try {
            HandlerUtils.checkMustUnderstand(msgCtx11, null, null);
        } catch (AxisFault e) {
            fail("Should not have caught and AxisFault");
        }
        
        MessageContext msgCtx12 = createMessageContext(soap12Factory);
        setUnprocessedHeaders(msgCtx12, rolePlayer);
        try {
            HandlerUtils.checkMustUnderstand(msgCtx12, null, null);
        } catch (AxisFault e) {
            fail("Should not have caught and AxisFault");
        }
    }
    
    /**
     * Test that the HandlerResolverImpl returns the correct roles defined in the handler config
     * file <soap-role> elements.
     */
    public void testHandlerResolverGetRoles() {
        ServiceDescription serviceDesc = DescriptionFactory.createServiceDescription(RoleBasedMUServiceImpl.class);
        HandlerResolverImpl handlerResolver = new HandlerResolverImpl(serviceDesc);
        
        EndpointDescription epDesc = serviceDesc.getEndpointDescriptions()[0];
        
        // Make sure the role information is specified in the handler config file
        HandlerChainsType epHandlerChains = epDesc.getHandlerChain();
        assertNotNull(epHandlerChains);
        List<HandlerChainType> epHandlerChain = epHandlerChains.getHandlerChain();
        assertEquals(1, epHandlerChain.size());
        List<HandlerType> epHandler = epHandlerChain.get(0).getHandler();
        assertEquals(1, epHandler.size());
        HandlerType handlerType = epHandler.get(0);
        List<org.apache.axis2.jaxws.description.xml.handler.String> soapRoles = 
            handlerType.getSoapRole();
        assertNotNull(soapRoles);
        assertEquals(1, soapRoles.size());
        String addedRole = soapRoles.get(0).getValue();
        assertEquals(roleHandlerAdded, addedRole);
        
        // Now verify the role information in the handler config file correctly affects the
        // roles played returned from the Resolver
        PortInfo portInfo = epDesc.getPortInfo();

        List<String> handlerRoles = handlerResolver.getRoles(portInfo);
        assertNotNull(handlerRoles);
        assertEquals(1, handlerRoles.size());
        assertEquals(roleHandlerAdded, handlerRoles.get(0));
        
        List<Handler> handlerChain = handlerResolver.getHandlerChain(portInfo);
        assertNotNull(handlerChain);
        assertEquals(1, handlerChain.size());
    }
    
    /**
     * Test that multiple <soap-role> elements in the handler config file work correctly. 
     */
    public void testHandlerResolverGerRoles2() {
        ServiceDescription serviceDesc = DescriptionFactory.createServiceDescription(RoleBasedMUServiceImpl2.class);
        HandlerResolverImpl handlerResolver = new HandlerResolverImpl(serviceDesc);
        EndpointDescription epDesc = serviceDesc.getEndpointDescriptions()[0];
        
        // Make sure the role information is specified in the handler config file
        HandlerChainsType epHandlerChains = epDesc.getHandlerChain();
        assertNotNull(epHandlerChains);
        List<HandlerChainType> epHandlerChain = epHandlerChains.getHandlerChain();
        assertEquals(1, epHandlerChain.size());
        List<HandlerType> epHandler = epHandlerChain.get(0).getHandler();
        assertEquals(1, epHandler.size());
        HandlerType handlerType = epHandler.get(0);
        List<org.apache.axis2.jaxws.description.xml.handler.String> soapRoles = 
            handlerType.getSoapRole();
        assertNotNull(soapRoles);
        assertEquals(2, soapRoles.size());
        ArrayList<String> checkRoles = new ArrayList<String>();
        checkRoles.add(soapRoles.get(0).getValue());
        checkRoles.add(soapRoles.get(1).getValue());
        assertTrue(checkRoles.contains(roleHandlerAdded));
        assertTrue(checkRoles.contains(roleHandlerAdded2));

        // Now verify the role information in the handler config file correctly affects the
        // roles played returned from the Resolver
        PortInfo portInfo = epDesc.getPortInfo();

        List<String> handlerRoles = handlerResolver.getRoles(portInfo);
        assertNotNull(handlerRoles);
        assertEquals(2, handlerRoles.size());
        ArrayList<String> checkResolverRoles = new ArrayList<String>();
        checkResolverRoles.add((String) handlerRoles.get(0));
        checkResolverRoles.add((String) handlerRoles.get(1));
        assertTrue(checkResolverRoles.contains(roleHandlerAdded));
        assertTrue(checkResolverRoles.contains(roleHandlerAdded2));
        
        List<Handler> handlerChain = handlerResolver.getHandlerChain(portInfo);
        assertNotNull(handlerChain);
        assertEquals(1, handlerChain.size());

    
    }
    
    /**
     * Test that a RolePlayer with no roles doesn't cause any problems in the mustUnderstand
     * checking.
     */
    public void testHandlerResolverEmptyRolesPlayed() {
        
        RolePlayer rolePlayer = new UltimateDestinationRoles();
        MessageContext msgCtx11 = createMessageContext(soap11Factory);
        setUnprocessedHeaders(msgCtx11, rolePlayer);
        // Indicate we understand (e.g. via handlers) all the headers.
        List<QName> understoodHeaders = new ArrayList<QName>();
        understoodHeaders.add(new QName(namespace, header_ultimateReceiver));
        understoodHeaders.add(new QName(namespace, header_rolePlayed));

        List<String> handlerRolePlayer = new ArrayList<String>();
        try {
            HandlerUtils.checkMustUnderstand(msgCtx11, understoodHeaders, handlerRolePlayer);
        } catch (AxisFault e) {
            fail("Should not have caught and AxisFault");
        }
    }
    
    // =============================================================================================
    // Utility methods
    // =============================================================================================

    private MessageContext createMessageContext(SOAPFactory soapFactory) {
        MessageContext messageContext = null;
        AxisService as1 = new AxisService("Service1");
        ConfigurationContext cc = null;
        try {
            cc = ConfigurationContextFactory.createEmptyConfigurationContext();
            AxisConfiguration ac = cc.getAxisConfiguration();
            ac.addService(as1);
            messageContext = cc.createMessageContext();
            messageContext.setAxisService(as1);

            SOAPEnvelope se = soapFactory.createSOAPEnvelope();

            SOAPHeader sh = soapFactory.createSOAPHeader(se);

            SOAPHeaderBlock shb1 = sh.addHeaderBlock(header_ultimateReceiver, 
                                                     omFactory.createOMNamespace(namespace, header_ultimateReceiver));
            // Since no role was set on the shb1, default is ultimate receiver
            shb1.setMustUnderstand(true);
            
            SOAPHeaderBlock shb2 = sh.addHeaderBlock(header_rolePlayed, 
                                                     omFactory.createOMNamespace(namespace, header_rolePlayed));
            shb2.setRole(rolePlayed1);
            shb2.setMustUnderstand(true);
            
            SOAPHeaderBlock shb3 = sh.addHeaderBlock(header_roleNotPlayed, 
                                                     omFactory.createOMNamespace(namespace, header_roleNotPlayed));
            shb3.setRole(roleNotPlayed);
            shb3.setMustUnderstand(true);
            
            SOAPHeaderBlock shb4 = sh.addHeaderBlock(header_bindingAdded,
                                                     omFactory.createOMNamespace(namespace, header_bindingAdded));
            shb4.setRole(roleBindingAdded);
            shb4.setMustUnderstand(true);
            
            // This header is destined for the ulmiate receiver, but it is already processed
            // so it shouldn't cause mustUnderstand fault
            SOAPHeaderBlock shb5 = sh.addHeaderBlock(header_ultimateReceiver_processed,
                                                     omFactory.createOMNamespace(namespace, header_ultimateReceiver_processed));
            // Since no role was set on the shb1, default is ultimate receiver
            shb5.setMustUnderstand(true);
            shb5.setProcessed();
            
            // Header targeted for SOAP11 role of Next, not set to MustUnderstand
            SOAPHeaderBlock shb6 = sh.addHeaderBlock(header_SoapNext,
                                                     omFactory.createOMNamespace(soap11Namespace, header_SoapNext));
            shb6.setRole(roleSoap11Next);

            messageContext.setEnvelope(se);
        } catch (AxisFault e) {
            fail("Caught unexpected exception creating message context" + e);
        }
        return messageContext;
    }
    
    private void setUnprocessedHeaders(MessageContext messageContext, RolePlayer rolePlayer) {
        SOAPEnvelope envelope = messageContext.getEnvelope();
        Iterator headerBlocks = envelope.getHeader().getHeadersToProcess(rolePlayer);
        List<QName> unprocessedHeaders = new ArrayList<QName>();
        while (headerBlocks.hasNext()) {
            SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) headerBlocks.next();
            QName headerName = headerBlock.getQName();
            // if this header block has been processed or mustUnderstand isn't
            // turned on then its cool
            if (headerBlock.isProcessed() || !headerBlock.getMustUnderstand()) {
                continue;
            } else {
                unprocessedHeaders.add(headerName);
            }
        }
        if(unprocessedHeaders !=null && unprocessedHeaders.size()>0){
            messageContext.setProperty(Constants.UNPROCESSED_HEADER_QNAMES, unprocessedHeaders);           
        }       
    }
    
    private List<String> bindingAddedRole() {
        List<String> roles = new ArrayList<String>();
        roles.add(roleBindingAdded);
        return roles;
    }
    
    private List<String> noHeadersRole() {
        List<String> roles = new ArrayList<String>();
        roles.add(roleNoHeaders);
        return roles;
    }
    
    
    // =============================================================================================
    // Inner Test Classes
    // =============================================================================================
    
    class UltimateDestinationNoRoles implements RolePlayer {
        public List getRoles() {
            return null;
        }
        public boolean isUltimateDestination() {
            return true;
        }
    }

    class NotUltimateDestinationNoRoles implements RolePlayer {
        public List getRoles() {
            return null;
        }
        public boolean isUltimateDestination() {
            return false;
        }
    }
    
    class UltimateDestinationRoles implements RolePlayer {
        List<String> roles = new ArrayList<String>();
        public UltimateDestinationRoles() {
            roles.add(rolePlayed1);
        }
        public List getRoles() {
            return roles;
        }
        public boolean isUltimateDestination() {
            return true;
        }
    }
    
    @WebService
    @HandlerChain(file="RoleBasedMustUnderstandTests.xml")
    class RoleBasedMUServiceImpl {
        
    }

    @WebService
    @HandlerChain(file="RoleBasedMustUnderstandTests2.xml")
    class RoleBasedMUServiceImpl2 {
        
    }
}
