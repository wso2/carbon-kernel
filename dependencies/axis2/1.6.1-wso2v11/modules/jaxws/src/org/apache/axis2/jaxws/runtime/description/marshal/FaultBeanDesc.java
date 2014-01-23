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

package org.apache.axis2.jaxws.runtime.description.marshal;

/**
 * Description of FaultBean for a FaultDescription. The FaultBean is used to marshal and unmarshal
 * the exception.  The FaultBean is determined via annotation, faultInfo and package introspection.
 */
public interface FaultBeanDesc {

    /**
     * Get the class name of the fault bean for the FaultDescription. Note that the FaultBean may
     * not be a bean.  It can be a non-bean (i.e. String or int)
     * <p/>
     * Algorithm: 1) The class defined on @WebFault of the exception 2) If not present or invalid,
     * the class defined by getFaultInfo. 3) If not present, the class is found by looking for the a
     * class named <exceptionName>Bean in the interface's package. 4) If not present, the class is
     * found by looking for the a class named <exceptionName>Bean in the interface + jaxws package
     *
     * @return
     */
    public String getFaultBeanClassName();

    /**
     * Get the local name of the fault bean. Algorithm: 1) The name defined on the @WebFault of the
     * exception. 2) If not present, the name defined via the @XmlRootElement of the fault bean
     * class. 3) If not present, the <exceptionName>Bean
     *
     * @return local name
     */
    public String getFaultBeanLocalName();


    /**
     * Get the targetNamespace of the fault bean. Algorithm: 1) The namespace defined on the
     * @WebFault of the exception. 2) If not present, the namespace defined via the @XmlRootElement
     * of the class name. 3) If not present, the namespace of the method's declared class +
     * "/jaxws"
     *
     * @return local name
     */
    public String getFaultBeanNamespace();


}
