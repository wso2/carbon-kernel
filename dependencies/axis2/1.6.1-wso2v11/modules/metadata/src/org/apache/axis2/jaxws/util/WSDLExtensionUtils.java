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
package org.apache.axis2.jaxws.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.WSDLElement;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.xml.namespace.QName;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.common.config.WSDLValidatorElement;
import org.apache.axis2.jaxws.common.config.WSDLValidatorElement.State;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionWSDL;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * This utility class stores methods that can be used to fetch extension elements
 * from wsdl and will be used from RespectBindingConfigurator on Client and Server.
 */
public class WSDLExtensionUtils {
    private static final Log log = LogFactory.getLog(WSDLExtensionUtils.class);
    /**
     * This method will search for all wsdl extensibility elements marked as required=true in wsdl:bindings
     * As per the wsdl 2.2 specification section 2.5 here is how a wsdl:binding is defined:
     * <wsdl:definitions .... >
     *      <wsdl:binding name="nmtoken" type="qname"> *
     *       <-- extensibility element (1) --> *
     *       <wsdl:operation name="nmtoken"> *
     *          <-- extensibility element (2) --> *
     *          <wsdl:input name="nmtoken"? > ?
     *              <-- extensibility element (3) --> 
     *          </wsdl:input>
     *          <wsdl:output name="nmtoken"? > ?
     *              <-- extensibility element (4) --> *
     *          </wsdl:output>
     *          <wsdl:fault name="nmtoken"> *
     *              <-- extensibility element (5) --> *
     *          </wsdl:fault>
     *       </wsdl:operation>
     *   </wsdl:binding>
     * </wsdl:definitions>
     * we will look for wsdl extensions in binding root, wsdl:operation, wsdl:input, wsdl:output and wsdl:fault.
     * If the extensibility element is defines outside of these sections it will not be picked up by this method.
     * 
     * @param wsdlBinding - WSDLBinding Object read from WSDL Definition.
     * @param set - Set that will be filled with list of required=true extension elements.
     * @return
     */
    public static void search(WSDLElement element, Set<WSDLValidatorElement> set, List<QName> unusedExtensions) {
        if(log.isDebugEnabled()){
            log.debug("Start Searching for WSDLExtensions");
        }
        if(element == null){
            return;
        }
        //This search method uses a simple BFS technique to search for Extension elements in WSDLBindings.
        //I will Queue all available WSDLElements starting in wsdl:binding and traverse them looking for 
        //extensions. Queue will be empty when I have processed everything.
        //NOTE:Binding, Operation, OperationInput, OperationOutput and OperationFault are all WSDLElements.
        LinkedList<WSDLElement> queue = new LinkedList<WSDLElement>();
        queue.offer(element);
        
        while(!queue.isEmpty()){
            WSDLElement wsdlElement = queue.remove();
            //WSDLElement in Queue could be wsdl Binding, BindingOperations, Input, Output or Fault
            //Find Extensibility Elements in wsdlElement.
            processWSDLElement(wsdlElement, set, unusedExtensions);
            //check if we are dealing with wsdlBinding;
            //store all BindingOpeations from wsdlBindings
            if(wsdlElement instanceof Binding){
                //lets get all operations and add to queue
                //TODO: WSDLDef API's don't use generics, hence we use Iterator below and type cast.
                List operations = ((Binding)wsdlElement).getBindingOperations();
                Iterator iter = operations.iterator();
                while(iter.hasNext()){
                    BindingOperation op =(BindingOperation) iter.next();
                    queue.offer(op);
                }
            }
            //check if we are dealing with Bindingoperations
            //Store all input, output and faults.
            if(wsdlElement instanceof BindingOperation){
                BindingInput bi = ((BindingOperation)wsdlElement).getBindingInput();
                queue.offer(bi);
                BindingOutput bo = ((BindingOperation)wsdlElement).getBindingOutput();
                queue.offer(bo);
                Map map = ((BindingOperation)wsdlElement).getBindingFaults();
                Collection c = map.values();
                Iterator iter = c.iterator();
                while(iter.hasNext()){
                    Object o = iter.next();
                    if(o instanceof BindingFault){
                        BindingFault bf = (BindingFault)o;
                        queue.offer(bf);
                    }
                }
            }          
        }
        if(log.isDebugEnabled()){
            log.debug("End Searching for WSDLExtensions");
        }
    }
    
    private static void processWSDLElement(WSDLElement wsdlElement, Set<WSDLValidatorElement> set, List<QName> unusedExtensions){
        if(log.isDebugEnabled()){
            log.debug("Start processWSDLElement");
        }
        List list = wsdlElement.getExtensibilityElements();
        if (list == null || list.size() == 0) {
            return;
        }
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            ExtensibilityElement e = (ExtensibilityElement) iter.next();
            //TODO in JAX-WS 2.1 Respect Binding implementation we are ignoring
            //SOAPBinding, review the reason behind this.
            if (e instanceof SOAPBinding || e instanceof SOAP12Binding)
                continue;

            if (e instanceof UnknownExtensibilityElement) {              
                UnknownExtensibilityElement ue = (UnknownExtensibilityElement) e;
                String reqd = ue.getElement().getAttribute("required");
                //check if extension element is required.
                //one can set extension as required two different ways in wsdl
                //lets check both ways here
                boolean wsdl_required = e.getRequired() != null && e.getRequired();
                boolean wsdl_attribute = reqd!=null && reqd.equalsIgnoreCase("true");
                
                if (wsdl_attribute || wsdl_required) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found a required element: " + e.getElementType());
                    }
                    WSDLValidatorElement element = new WSDLValidatorElement();
                    element.setExtensionElement(e);
                    element.setState(State.NOT_RECOGNIZED);
                    set.add(element);
                }
                else {
                    if (log.isDebugEnabled()) {
                        log.debug("Found a NOT required element: " + e.getElementType());
                    }
                    unusedExtensions.add(e.getElementType());
                }
            }
        }
        if(log.isDebugEnabled()){
            log.debug("Exit processWSDLElement");
        }
    }
    
    public static void processExtensions(EndpointDescription endpointDescription){
        if(endpointDescription == null){
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("RespectBindingConfiguratorErr1"));
        }

        EndpointDescriptionWSDL edw = (EndpointDescriptionWSDL) endpointDescription;
        if(endpointDescription == null){
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("RespectBindingConfiguratorErr2"));
        }
        javax.wsdl.Binding wsdlBinding = edw.getWSDLBinding();
        Set<WSDLValidatorElement> set = endpointDescription.getRequiredBindings();
        if(set.size()>0){
            //we have already looked for wsdl extension once, no need to 
            //find them again. WSDL is shared for all serviceDesc artifacts.
            return;
        }
        List<QName> unusedExtensions = new ArrayList<QName>();
        
        WSDLExtensionUtils.search(wsdlBinding, set, unusedExtensions);
        if (log.isDebugEnabled()) {
            log.debug("The following extensibility elements were found, but were not required.");
            for (int n = 0; n < unusedExtensions.size(); ++n)
                log.debug("[" + (n + 1) + "] - " + unusedExtensions.get(n));
        }           
        
    }
}
