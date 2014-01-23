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

package org.apache.axis2.jaxws.sample.faultsservice;

import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.saaj.SOAPEnvelopeImpl;
import org.test.polymorphicfaults.BaseFault;
import org.test.polymorphicfaults.ComplexFault;
import org.test.polymorphicfaults.DerivedFault1;
import org.test.polymorphicfaults.DerivedFault2;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

/**
 * This class provides server side implementation for the 
 * FaultsServicePortType.
 */

@WebService (targetNamespace="http://org/test/polymorphicfaults",
             serviceName="FaultsService",
             portName="FaultsPort",
           //wsdlLocation="WEB-INF/wsdl/FaultsService.wsdl",
             endpointInterface="org.apache.axis2.jaxws.sample.faultsservice.FaultsServicePortType")

public class FaultsServiceSoapBindingImpl implements FaultsServicePortType {

    private WebServiceContext ctx = null;
    private boolean init = false;
    
    /**
     * Throws wrapper exceptions for fault beans
     */
    public float getQuote(String tickerSymbol) throws 
        BaseFault_Exception, DerivedFault1_Exception, 
        DerivedFault2_Exception, InvalidTickerFault_Exception, SimpleFault {
        TestLogger.logger.debug("\nIn getQuote(): " + tickerSymbol + "\n");
        if (tickerSymbol.equals("SMPL")) {
            throw new SimpleFault("Server throws SimpleFault", 100);
        } else if (tickerSymbol.equals("LEGC")) {
            throw new InvalidTickerFault_Exception("Server throws InvalidTickerFault_Exception", tickerSymbol, 123);
        } else if (tickerSymbol.equals("DF1")) {
            DerivedFault1 df = new DerivedFault1();
            df.setA(100);
            df.setB(tickerSymbol);
            throw new DerivedFault1_Exception("Server throws DerivedFault1_Exception", df);
        }  else if (tickerSymbol.equals("DF2")) {
            DerivedFault2 df = new DerivedFault2();
            df.setA(200);
            df.setB(tickerSymbol);
            df.setC(80.0F);
            throw new DerivedFault2_Exception("Server throws DerivedFault2_Exception", df);
        } else if (tickerSymbol.equals("BASE")) {
            BaseFault bf = new BaseFault();
            bf.setA(400);
            throw new BaseFault_Exception("Server throws BaseFault_Exception", bf);
        } else if (tickerSymbol.equals("INJECTION")) {
           if (ctx != null && init) {
               // Only return this value if the context is injected and 
               // the initialization method is invoked
               return 1234567; 
           }
        }
        return 100;
    }


    /**
     * Throws wrapper exceptions for derived fault beans
     */
    public int throwFault(int a, String b, float c) throws 
        BaseFault_Exception, ComplexFault_Exception {
        if(b.equals("Complex")) {
            ComplexFault cf = new ComplexFault();
            cf.setA(a); 
            cf.setB(b); 
            cf.setC(c); 
            cf.setD(5); 
            throw new ComplexFault_Exception("Server throws ComplexFault_Exception", cf);
        } else if (b.equals("BaseFault")) {
            BaseFault bf = new BaseFault();
            bf.setA(a);  
            throw new BaseFault_Exception("Server throws BaseFault_Exception", bf);
        } else if (b.equals("DerivedFault1")) {
            DerivedFault1 df = new DerivedFault1();
            df.setA(a); 
            df.setB(b); 
            throw new BaseFault_Exception("Server throws BaseFault_Exception", df);
        } else if (b.equals("DerivedFault2")) {
            DerivedFault2 df = new DerivedFault2();
            df.setA(a); 
            df.setB(b); 
            df.setC(c); 
            throw new BaseFault_Exception("Server throws BaseFault_Exception", df);
        } else if (b.equals("SOAPFaultException")) {
            try {
                SOAPFault soapFault = createSOAPFault();
                soapFault.setFaultString("hello world");
                soapFault.setFaultActor("actor");
                throw new SOAPFaultException(soapFault);
            } catch (SOAPException se) {}
        } else if (b.equals("SOAPFaultException2")) {
            try {
                SOAPFault soapFault = createSOAPFault();
                soapFault.setFaultString("hello world2");
                soapFault.setFaultActor("actor2");
                Detail detail = soapFault.addDetail();
                DetailEntry de = detail.addDetailEntry(new QName("urn://sample", "detailEntry"));
                de.setValue("Texas");
                throw new SOAPFaultException(soapFault);
            } catch (SOAPException se) {}
        } else if (b.equals("NPE")) {
            throw new NullPointerException();
        } else if (b.equals("NPE2")) {
            // Throw NPE with a message
            throw new NullPointerException("Null Pointer Exception occurred");
        } else if (b.equals("WSE")) {
            WebServiceException wsf = new WebServiceException("This is a WebServiceException");
            throw wsf;
        }
        return 0;
    }

    /**
     * Returns a fault bean or throws a wrapper exception
     */
    public void returnFault(int a, String b, float c, 
                            Holder<DerivedFault1> fault) throws EqualFault {

        DerivedFault2 newFault = new DerivedFault2();
        newFault.setA(fault.value.getA());            
        newFault.setB(fault.value.getB());            
        newFault.setC(c);

        if(fault.value.getB().equals("fault")) {
            fault.value = newFault;
            return;
        } else if (fault.value.getB().equals("exception")) {
            throw new EqualFault("Server throws EqualFault", newFault);            
        }
        DerivedFault1 df = new DerivedFault1();
        df.setA(a + 1); 
        df.setB("Server: " + b); 
        throw new EqualFault("Server throws EqualFault", df);
    }
    
    SOAPFault createSOAPFault() throws SOAPException {
        SOAPFault soapFault = null;
    
        // REVIEW: The following does not work due to Axis2 SAAJ problems.
        //
        // SOAPFactory soapFactory = SOAPFactory.newInstance();
        // SOAPFault soapFault = soapFactory.createFault();
        
        // Alternate Approach
        org.apache.axiom.soap.SOAPFactory asf = DOOMAbstractFactory.getSOAP11Factory();
        org.apache.axiom.soap.impl.dom.SOAPEnvelopeImpl axiomEnv = (org.apache.axiom.soap.impl.dom.SOAPEnvelopeImpl) asf.createSOAPEnvelope();
        javax.xml.soap.SOAPEnvelope env = new SOAPEnvelopeImpl(axiomEnv);
        SOAPBody body = env.addBody();
        soapFault = body.addFault();
        return soapFault;
    }
    
    @PostConstruct
    public void initialize(){
        //Called after resource injection and before a method is called.
        TestLogger.logger.debug("Calling PostConstruct to Initialize");
        this.init = true;
    }
    
    @PreDestroy
    public void distructor(){
        //Called before the scope of request or session or application ends.

        TestLogger.logger.debug("Calling PreDestroy ");
        
    }
    @Resource
    private void setCtx(WebServiceContext ctx) {
        // The setter is private.  This should not matter because the engine
        // should still make it accessible.
        this.ctx = ctx;
    }
}
