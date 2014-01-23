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

package org.apache.axis2.jaxws.description.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Definition;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.MethodRetriever;
import org.apache.axis2.jaxws.description.ServiceDescriptionWSDL;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PostRI216MethodRetrieverImpl subclass implements the new SUN RI interpretation for
 * annotation processing. See MethodRetriever superclass...
 * 
 *  Please refer to the following links for more info:
 *  
 *  
 *     https://jax-ws.dev.java.net/issues/show_bug.cgi?id=577 
 *     http://forums.java.net/jive/thread.jspa?threadID=61630
 *     http://forums.java.net/jive/thread.jspa?threadID=55078 
 *
 * 
 */
public class PostRI216MethodRetrieverImpl extends MethodRetriever {

    //Logging setup
    private static final Log log = LogFactory.getLog(PostRI216MethodRetrieverImpl.class);
    
    private EndpointInterfaceDescriptionImpl eid = null;

    private DescriptionBuilderComposite dbc = null;
    public PostRI216MethodRetrieverImpl (DescriptionBuilderComposite dbc, 
        EndpointInterfaceDescriptionImpl eid)  {
        super();
        
        this.dbc = dbc;
        this.eid = eid;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.description.MethodRetriever#retrieveMethods()
     */
    public Iterator<MethodDescriptionComposite> retrieveMethods() {

        /*
         * Depending on whether this is an implicit SEI or an actual SEI, Gather up and build a
         * list of MDC's. If this is an actual SEI, then starting with this DBC, build a list of all
         * MDC's that are public methods in the chain of extended classes.
         * If this is an implicit SEI, then starting with this DBC,
         *  1. If a false exclude is found, then take only those that have false excludes
         *  2. Assuming no false excludes, take all public methods that don't have exclude == true
         *  3. For each super class, if 'WebService' present, take all MDC's according to rules 1&2
         *    But, if WebService not present, grab only MDC's that are annotated.
         */
        if (log.isTraceEnabled()) {
            log.trace("retrieveMethods: Enter");
        }

        ArrayList<MethodDescriptionComposite> retrieveList = new ArrayList<MethodDescriptionComposite>();

        if (dbc.isInterface()) {
            if (log.isDebugEnabled()) {
                log.debug("Removing overridden methods for interface: " + dbc.getClassName()
                    + " with super interface: " + dbc.getSuperClassName());
            }

            // make sure we retrieve all the methods, then remove the overridden
            // methods that exist in the base interface
            retrieveList = retrieveSEIMethodsChain(dbc, eid);
            retrieveList = removeOverriddenMethods(retrieveList, dbc, eid);

        } else {
            //this is an implied SEI...rules are more complicated

            retrieveList = retrieveImplicitSEIMethods(dbc);

            //Now, continue to build this list with relevent methods in the chain of
            //superclasses. If the logic for processing superclasses is the same as for
            //the original SEI, then we can combine this code with above code. But, its possible
            //the logic is different for superclasses...keeping separate for now.
            DescriptionBuilderComposite tempDBC = dbc;

            while (!DescriptionUtils.isEmpty(tempDBC.getSuperClassName())) {

                //verify that this superclass name is not
                //      java.lang.object, if so, then we're done processing
                if (DescriptionUtils.javifyClassName(tempDBC.getSuperClassName()).equals(
                    MDQConstants.OBJECT_CLASS_NAME))
                    break;

                DescriptionBuilderComposite superDBC = eid.getEndpointDescriptionImpl()
                .getServiceDescriptionImpl().getDBCMap().get(tempDBC.getSuperClassName());

                if (log.isTraceEnabled())
                    log.trace("superclass name for this DBC is:" + tempDBC.getSuperClassName());

                //Verify that we can find the SEI in the composite list
                if (superDBC == null) {
                    throw ExceptionFactory.makeWebServiceException(Messages
                        .getMessage("seiNotFoundErr"));
                }

                // Now, gather the list of Methods just like we do for
                // the lowest subclass
                retrieveList.addAll(retrieveImplicitSEIMethods(superDBC));
                tempDBC = superDBC;
            } //Done with implied SEI's superclasses
            retrieveList = removeOverriddenMethods(retrieveList, dbc, eid);
            //Let check to see if there where any operations with @Webmethod
            //If LeagcyWebmethod is NOT defined i.e default and if there 
            //are operations with @Webmethod annotation, then lets warn the
            //user that we may be exposing public operation that they did not
            //intend to expose, this can happen if user is migrating application
            //from old JAX-WS tooling version.
            //Let also inform user that if they can use LegacyWebmethod to expose 
            //Only those operations that have @webmethod(exclude=false) annotation on them.
            boolean isWebmethodDefined = DescriptionUtils.isWebmethodDefined(dbc);
            Iterator<MethodDescriptionComposite> iter = retrieveList.iterator();
            while(iter.hasNext()){
                MethodDescriptionComposite mdc = iter.next();
                //If user defined a legacyWemethod with no wsdl, has atleast one operation with @Wemethod annotation
                //and this is a public operation with no @Webmethod operation that is being exposed then
                //lets warn user of possible security exposure.
                Definition wsdlDef = ((ServiceDescriptionWSDL)eid.getEndpointDescription().getServiceDescription()).getWSDLDefinition(); 
                if(getLegacyWebMethod()==null && wsdlDef == null && isWebmethodDefined && mdc.getWebMethodAnnot()==null && !isConstructor(mdc)){
                    log.warn(Messages.getMessage("MethodRetrieverWarning1", mdc.getMethodName()));
                }
            }
        }//Done with implied SEI's

        return retrieveList.iterator();
    }

    /*
     * This is called when we know that this DBC is an implicit SEI and the user has set the
     * property for using Suns new behavior in JDK 1.6. We will retrieve all the public methods
     * that do not have @WebMethod(exclude == true). 
     */
    private ArrayList<MethodDescriptionComposite> retrieveImplicitSEIMethods(
        DescriptionBuilderComposite dbc) {

        ArrayList<MethodDescriptionComposite> retrieveList = new ArrayList<MethodDescriptionComposite>();

        Iterator<MethodDescriptionComposite> iter = null;
        List<MethodDescriptionComposite> mdcList = dbc.getMethodDescriptionsList();

        if (mdcList != null) {
            iter = dbc.getMethodDescriptionsList().iterator();           
            boolean isWebmethodDefined = DescriptionUtils.isWebmethodDefined(dbc);
            while (iter.hasNext()) {
                MethodDescriptionComposite mdc = iter.next();
                //flag to check if the method can be exposed as webservice.
                boolean isWebservice = !DescriptionUtils.isExcludeTrue(mdc) && !mdc.isStatic() && !mdc.isFinal();
                if(!isWebservice){
                    if(log.isDebugEnabled()){
                        log.debug(mdc.getMethodName() + " has static or final modifiers in method signature or has @Webmethod(exclude=true) set");
                        log.debug(mdc.getMethodName() + " cannot be exposed as a webservice");
                    }
                }
                if (isWebservice) {
                    mdc.setDeclaringClass(dbc.getClassName());
                    retrieveList.add(mdc);
                }
            }
            
        }

        return retrieveList;
    }
    
    private boolean isConstructor(MethodDescriptionComposite mdc){
        return mdc.getMethodName().equals("<init>");
    }

}
