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

package org.apache.axis2.jaxws.description;

import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.impl.DescriptionUtils;
import org.apache.axis2.jaxws.description.impl.EndpointInterfaceDescriptionImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A MethodRetriever is an abstract class which is meant to be sub-classed for each type of 
 * method retrieval behavior. The catalyst for this was the introduction of new spec.
 * interpretation by SUN RI. 
 * 
 *  Please refer to the following links:
 *  
 *  
 *     https://jax-ws.dev.java.net/issues/show_bug.cgi?id=577 
 *     http://forums.java.net/jive/thread.jspa?threadID=61630
 *     http://forums.java.net/jive/thread.jspa?threadID=55078 
 *
 *  This base is being used to allow for a cleaner componentization of the old/new and potential
 *  future behavior changes.
 *  
 *  The sub-class is required to implement only the abstract 'retrieveMethods' 
 * 
 */

public abstract class MethodRetriever {

    //Logging setup
    private static final Log log = LogFactory.getLog(MethodRetriever.class);
    private String legacyWebMethod = null;
    public String getLegacyWebMethod() {
        return legacyWebMethod;
    }

    public void setLegacyWebMethod(String legacyWebMethod) {
        this.legacyWebMethod = legacyWebMethod;
    }

    protected MethodRetriever() {}

    /*
     * Returns a non-null (possibly empty) list of MethodDescriptionComposites
     */
    public abstract Iterator<MethodDescriptionComposite> retrieveMethods();


    /**
     * A recursive method which peruses and retrieves methods in the super class hierarchy
     * @param tmpDBC
     * @param eid
     * @return
     */
    protected ArrayList<MethodDescriptionComposite> retrieveSEIMethodsChain(
        DescriptionBuilderComposite tmpDBC, EndpointInterfaceDescriptionImpl eid) {

        DescriptionBuilderComposite dbc = tmpDBC;
        ArrayList<MethodDescriptionComposite> retrieveList = new ArrayList<MethodDescriptionComposite>();

        retrieveList = retrieveSEIMethods(dbc);

        //Since this is an interface, anything that is in the extends clause will actually appear
        // in the interfaces list instead.
        Iterator<String> iter = null;
        List<String> interfacesList = dbc.getInterfacesList();
        if (interfacesList != null) {
            iter = dbc.getInterfacesList().iterator();

            while (iter.hasNext()) {

                String interfaceName = iter.next();
                DescriptionBuilderComposite superInterface = 
                    eid.getEndpointDescriptionImpl().getServiceDescriptionImpl().getDBCMap().get(interfaceName);

                retrieveList.addAll(retrieveSEIMethodsChain(superInterface, eid));
            }
        }

        return retrieveList;
    }

    /**
     * This method will loop through each method that was previously determined as being relevant to
     * the current composite. It will then drive the call to determine if this represents a method
     * that has been overridden. If it represents an overriding method declaration it will remove
     * the inherited methods from the list leaving only the most basic method declaration.
     *
     * @param methodList - <code>ArrayList</code> list of relevant methods
     * @param dbc        - <code>DescriptionBuilderComposite</code> current composite
     * @return - <code>ArrayList</code>
     */
    protected ArrayList<MethodDescriptionComposite> removeOverriddenMethods(
        ArrayList<MethodDescriptionComposite> methodList, DescriptionBuilderComposite dbc, EndpointInterfaceDescriptionImpl eid) {
        Map<String, Integer> hierarchyMap = dbc.isInterface() ? getInterfaceHierarchy(dbc, eid)
            : getClassHierarchy(dbc, eid);
        ArrayList<MethodDescriptionComposite> returnMethods = new ArrayList<MethodDescriptionComposite>();
        for (int i = 0; i < methodList.size(); i++) {
            if (notFound(returnMethods, methodList.get(i))) {
                returnMethods.add(getBaseMethod(methodList.get(i), i, methodList, hierarchyMap));
            }

        }
        return returnMethods;
    }

    /**
     * This method drives the establishment of the hierarchy of interfaces for an SEI.
     */
    private Map<String, Integer> getInterfaceHierarchy(DescriptionBuilderComposite dbc, EndpointInterfaceDescriptionImpl eid) {
        if (log.isDebugEnabled()) {
            log.debug("Getting interface hierarchy for: " + dbc.getClassName());
        }
        Map<String, Integer> hierarchyMap = new HashMap<String, Integer>();
        hierarchyMap.put(dbc.getClassName(), 0);
        return getInterfaceHierarchy(dbc.getInterfacesList(), hierarchyMap, 1, eid);
    }

    protected ArrayList<MethodDescriptionComposite> retrieveSEIMethods(DescriptionBuilderComposite dbc) {

        //Rules for retrieving Methods on an SEI (or a superclass of an SEI) are simple
        //Just retrieve all methods regardless of WebMethod annotations
        ArrayList<MethodDescriptionComposite> retrieveList = new ArrayList<MethodDescriptionComposite>();

        Iterator<MethodDescriptionComposite> iter = null;
        List<MethodDescriptionComposite> mdcList = dbc.getMethodDescriptionsList();

        if (mdcList != null) {
            iter = dbc.getMethodDescriptionsList().iterator();
            while (iter.hasNext()) {
                MethodDescriptionComposite mdc = iter.next();
                mdc.setDeclaringClass(dbc.getClassName());
                retrieveList.add(mdc);
            }
        }

        return retrieveList;
    }

    /**
     * This method will establish a <code>HashMap</code> that represents a class name of a composite
     * and an integer value for the entry. The integer represents the classes level in the Java
     * hierarchy. 0 represents the most basic class with n representing the highest level class.
     *
     * @param dbc - <code>DescriptionBuilderComposite</code>
     * @return - <code>HashMap</code>
     */
    private HashMap<String, Integer> getClassHierarchy(DescriptionBuilderComposite dbc, EndpointInterfaceDescriptionImpl eid) {
        HashMap<String, DescriptionBuilderComposite> dbcMap = eid.getEndpointDescriptionImpl()
        .getServiceDescriptionImpl().getDBCMap();
        HashMap<String, Integer> hierarchyMap = new HashMap<String, Integer>();
        if (log.isDebugEnabled()) {
            log.debug("Putting class at base level: " + dbc.getClassName());
        }
        hierarchyMap.put(dbc.getClassName(), Integer.valueOf(0));
        DescriptionBuilderComposite superDBC = dbcMap.get((dbc.getSuperClassName()));
        int i = 1;
        while (superDBC != null && !superDBC.getClassName().equals("java.lang.Object")) {
            hierarchyMap.put(superDBC.getClassName(), Integer.valueOf(i));
            if (log.isDebugEnabled()) {
                log.debug("Putting class: " + superDBC.getClassName() + " at hierarchy rank: " + i);
            }
            i++;
            superDBC = dbcMap.get(superDBC.getSuperClassName());
        }
        return hierarchyMap;
    }

    /**
     * This method will loop through each method we have already identified as a base method and
     * compare the current method.
     *
     * @param mdcList - <code>ArrayList</code> identified base methods
     * @param mdc     - <code>MethodDescriptionComposite</code> current method
     * @return - boolean
     */
    private boolean notFound(ArrayList<MethodDescriptionComposite> mdcList,
        MethodDescriptionComposite mdc) {
        for (MethodDescriptionComposite method : mdcList) {
            if (mdc.compare(method)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Recursive method that builds the hierarchy of interfaces. This begins with an
     * SEI and walks all of its super interfaces.
     */
    private Map<String, Integer> getInterfaceHierarchy(List<String> interfaces,
        Map<String, Integer> hierarchyMap, int level, EndpointInterfaceDescriptionImpl eid) {
        HashMap<String, DescriptionBuilderComposite> dbcMap = eid.getEndpointDescriptionImpl()
        .getServiceDescriptionImpl().getDBCMap();

        // walk through all of the interfaces
        if (interfaces != null && !interfaces.isEmpty()) {
            for (String interfaze : interfaces) {
                DescriptionBuilderComposite interDBC = dbcMap.get(interfaze);
                if (interDBC != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Inserting super interface " + interDBC.getClassName()
                            + " at level " + level);
                    }
                    hierarchyMap.put(interDBC.getClassName(), level);
                    return getInterfaceHierarchy(interDBC.getInterfacesList(), hierarchyMap,
                        level++, eid);
                }
            }
        }
        return hierarchyMap;
    }

    /**
     * This method is responsible for determining the most basic level of a method declaration in
     * the <code>DescriptionBuilderComposite</code> hierarchy.
     *
     * @param mdc          - <code>MethodDescriptionComposite</code> current method
     * @param index        - <code>int</code> current location in method list
     * @param methodList   - <code>List</code> list of methods available on this composite
     * @param hierarchyMap - <code>HashMap</code> map that represents the hierarchy of the current
     *                     <code>DescriptionBuilderComposite</code>
     * @return - <code>MethodDescriptionComposite</code> most basic method declaration
     */
    private static MethodDescriptionComposite getBaseMethod(MethodDescriptionComposite mdc,
        int index, ArrayList<MethodDescriptionComposite> methodList,
        Map<String, Integer> hierarchyMap) {
        int baseLevel = hierarchyMap.get(mdc.getDeclaringClass());
        if (log.isDebugEnabled()) {
            log.debug("Base method: " + mdc.getMethodName() + " initial level: " + baseLevel);
        }
        for (; index < methodList.size(); index++) {
            MethodDescriptionComposite compareMDC = methodList.get(index);
            // If the two methods are the same method that means we have found an inherited
            // overridden case
            if (mdc.equals(compareMDC)) {
                if (log.isDebugEnabled()) {
                    log.debug("Found equivalent methods: " + mdc.getMethodName());
                }
                // get the declaration level of the method we are comparing to
                int compareLevel = hierarchyMap.get(compareMDC.getDeclaringClass());
                // if the method was declared by a class in a lower level of the hierarchy it
                // becomes the method that we will compare other methods to
                if (compareLevel < baseLevel) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found method lower in hierarchy chain: "
                            + compareMDC.getMethodName() + " of class: "
                            + compareMDC.getMethodName());
                    }
                    mdc = compareMDC;
                    baseLevel = compareLevel;
                }
            }
        }
        return mdc;
    }

}