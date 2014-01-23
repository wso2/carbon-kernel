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

package org.apache.axis2.jaxws.marshaller.impl.alt;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.runtime.description.marshal.FaultBeanDesc;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;
import org.apache.axis2.jaxws.utility.PropertyDescriptorPlus;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.WebServiceException;
import java.beans.IntrospectionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The JAX-WS Specification (chapter 3.7) indicates that JAX-WS
 * supports exceptions that do not match the normal pattern (the normal
 * pattern is defined in chapter 2.5.  
 * 
 * These non-matching exceptions are the result of running WSGen
 * on a pre-existing webservice.  I am going to use the term, legacy exception,
 * to describe these non-matching exceptions.
 * 
 * The JAX-WS marshaller (server) must marshal a legacy exception thrown from
 * the web service impl.  The marshalling/mapping algorithm is defined in chapter 3.7.
 * 
 * On the client, the JAX-WS engine will need to demarshal exceptions.  However
 * the specification assumes that the client is always created via wsimport; therefore
 * the assumption is that all exceptions on the client are compliant (never legacy exceptions).
 * I have included some code here in case we have to deal with legacy exceptions on the client...this is non-spec.
 * 
 *
 */

class LegacyExceptionUtil {

    private static Log log = LogFactory.getLog(LegacyExceptionUtil.class);

    private static Set<String> ignore = new HashSet<String>();

    static {
        // Per Chap 3.7 rule 3, ignore these properties on the exception
        ignore.add("localizedMessage");
        ignore.add("stackTrace");
        ignore.add("class");
        ignore.add("cause");
    }

    /** Static class.  Constructor is intentionally private */
    private LegacyExceptionUtil() {
    }


    /**
     * Create a FaultBean populated with the data from the Exception t The algorithm used to
     * populate the FaultBean is described in JAX-WS 3.7
     *
     * @param t
     * @param fd
     * @param marshalDesc
     * @return faultBean
     */
    static Object createFaultBean(Throwable t, FaultDescription fd,
                                  MarshalServiceRuntimeDescription marshalDesc)
            throws WebServiceException {

        Object faultBean = null;
        try {
            // Get the fault bean name from the fault description.
            // REVIEW The default name should be:
            //      Package = <SEI package> or <SEI package>.jaxws
            //      Name = <exception name> + Bean
            FaultBeanDesc faultBeanDesc = marshalDesc.getFaultBeanDesc(fd);
            String faultBeanName = faultBeanDesc.getFaultBeanClassName();

            // TODO Add check that faultBeanName is correct
            if (log.isDebugEnabled()) {
                log.debug("Legacy Exception FaultBean name is = " + faultBeanName);
            }

            // Load the FaultBean Class
            Class faultBeanClass = null;
            if (faultBeanName != null && faultBeanName.length() > 0) {
                try {
                    try {
                        faultBeanClass = MethodMarshallerUtils.loadClass(faultBeanName);
                    } catch (ClassNotFoundException e){
                        faultBeanClass = MethodMarshallerUtils.loadClass(faultBeanName, fd.getOperationDescription().getEndpointInterfaceDescription().getEndpointDescription().getAxisService().getClassLoader());
                    }
                } catch (Throwable throwable) {
                    if (log.isDebugEnabled()) {
                        log.debug("Cannot load fault bean class = " + faultBeanName +
                                ".  Fallback to using the exception object");
                    }
                }
            }

            if (faultBeanClass != null) {

                // Get the properties names from the exception class
                Map<String, PropertyDescriptorPlus> pdMap =
                        marshalDesc.getPropertyDescriptorMap(t.getClass());

                // We need to assign the legacy exception data to the java bean class.
                // We will use the JAXBWrapperTool.wrap utility to do this.

                // Get the map of child objects
                Map<String, Object> childObjects = getChildObjectsMap(t, pdMap);
                Map<String, Class> declaringClass = new HashMap<String, Class>();
                List<String> childNames = new ArrayList<String>(childObjects.keySet());

                if (log.isErrorEnabled()) {
                    log.debug("List of properties on the Legacy Exception is " + childNames);
                }
                // Use the wrapper tool to get the child objects.
                JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
                Map<String, PropertyDescriptorPlus> pdMapForBean =
                        marshalDesc.getPropertyDescriptorMap(faultBeanClass);
                faultBean =
                        wrapperTool.wrap(faultBeanClass, childNames, childObjects, declaringClass, pdMapForBean);
                if (log.isErrorEnabled()) {
                    log.debug("Completed creation of the fault bean.");
                }
            } else {
                throw ExceptionFactory.makeWebServiceException(
                		Messages.getMessage("faultProcessingNotSupported",t.getClass().getName()));
            }

        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        return faultBean;
    }

    /**
     * Create an Exception using the data in the JAXB object. The specification is silent on this
     * issue.
     *
     * @param exceptionClass
     * @param jaxb
     * @param marshalDesc
     * @return
     */
    static Exception createFaultException(Class exceptionClass,
                                          Object jaxb,
                                          MarshalServiceRuntimeDescription marshalDesc) {
        Exception e = null;
        try {
            if (log.isErrorEnabled()) {
                log.debug("Create Legacy Exception for " + exceptionClass.getName());
            }
            // Get the properties names from the exception class
            Map<String, PropertyDescriptorPlus> pdMap =
                    marshalDesc.getPropertyDescriptorMap(exceptionClass);

            // Now get a list of PropertyDescriptorPlus objects that map to the jaxb bean properties
            Iterator<Entry<String, PropertyDescriptorPlus>> it = pdMap.entrySet().iterator();
            List<PropertyDescriptorPlus> piList = new ArrayList<PropertyDescriptorPlus>();
            while (it.hasNext()) {
                Entry<String, PropertyDescriptorPlus> entry = it.next();
                String propertyName = entry.getValue().getPropertyName();
                // Some propertyNames should be ignored.
                if (!ignore.contains(propertyName)) {
                    piList.add(entry.getValue());
                }
            }

            // Find a matching constructor
            List<String>  childNames = new ArrayList<String>();
            if (log.isErrorEnabled()) {
                log.debug("List of childNames on legacy exception is " + childNames);
            }
            Constructor constructor = findConstructor(exceptionClass, piList, childNames);

            if (log.isErrorEnabled()) {
                log.debug("The constructor used to create the exception is " + constructor);
            }
            // Use the wrapper tool to unwrap the jaxb object
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
            Map<String, PropertyDescriptorPlus> pdMapForBean =
                    marshalDesc.getPropertyDescriptorMap(jaxb.getClass());
            Object[] childObjects = wrapperTool.unWrap(jaxb, childNames, pdMapForBean);

            if (log.isErrorEnabled()) {
                log.debug("Calling newInstance on the constructor " + constructor);
            }
            e = (Exception)constructor.newInstance(childObjects);
        } catch (Exception ex) {
            throw ExceptionFactory.makeWebServiceException(ex);
        }
        return e;
    }

    /**
     * Find a construcor that matches this set of properties
     *
     * @param cls
     * @param pdList
     * @param childNames returned in the order that they occur in the constructor
     * @return Constructor or null
     */
    private static Constructor findConstructor(Class cls, List<PropertyDescriptorPlus> pdList,
                                               List<String> childNames) {
        Constructor[] constructors = cls.getConstructors();
        Constructor constructor = null;
        if (constructors != null) {
            for (int i = 0; i < constructors.length && constructor == null; i++) {
                Constructor tryConstructor = constructors[i];
                if (tryConstructor.getParameterTypes().length == pdList.size()) {
                    // Try and find the best match using the property types
                    List<PropertyDescriptorPlus> list =
                            new ArrayList<PropertyDescriptorPlus>(pdList);
                    List<PropertyDescriptorPlus> args = new ArrayList<PropertyDescriptorPlus>();

                    Class[] parms = tryConstructor.getParameterTypes();
                    boolean valid = true;

                    // Assume the message is first in the constructor
                    for (int j = 0; j < list.size(); j++) {
                        if ("message".equals(list.get(j).getPropertyName())) {
                            args.add(list.remove(j));
                        }
                    }
                    if (args.size() != 1 ||
                            !parms[0].isAssignableFrom(args.get(0).getPropertyType())) {
                        valid = false;
                    }

                    // Now process the rest of the args
                    for (int j = 1; j < parms.length && valid; j++) {
                        // Find a compatible argument
                        Class parm = parms[j];
                        boolean found = false;
                        for (int k = 0; k < list.size() && !found; k++) {
                            Class arg = list.get(k).getPropertyType();
                            if (parm.isAssignableFrom(arg)) {
                                found = true;
                                args.add(list.remove(k));
                            }
                        }
                        // If no compatible argument then this constructor is not valid
                        if (!found) {
                            valid = false;
                        }
                    }
                    // A constructor is found
                    if (valid) {
                        constructor = tryConstructor;
                        for (int index = 0; index < args.size(); index++) {
                            childNames.add(args.get(index).getPropertyName());
                        }
                    }
                }
            }
        }

        return constructor;
    }


    /**
     * Get the child objects map that is required by the wrapper tool.
     *
     * @param t                       Exception
     * @param ParameterDescriptorPlus map for Throwable t
     * @return Map with key is bean property names and values are objects from the Exception
     * @throws IntrospectionException
     */
    private static Map<String, Object> getChildObjectsMap(Throwable t,
                                                          Map<String, PropertyDescriptorPlus> pdMap)
            throws IntrospectionException, InvocationTargetException, IllegalAccessException {


        Map<String, Object> coMap = new HashMap<String, Object>();

        Iterator<Entry<String, PropertyDescriptorPlus>> it = pdMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, PropertyDescriptorPlus> entry = it.next();
            String propertyName = entry.getValue().getPropertyName();
            String xmlName = entry.getKey();
            // Some propertyNames should be ignored.
            if (!ignore.contains(propertyName)) {
                Object value = entry.getValue().get(t);
                coMap.put(xmlName, value);
            }
        }
        return coMap;
    }

}
