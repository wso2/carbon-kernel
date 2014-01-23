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

import javax.jws.WebParam;

/**
 * A ParameterDescripton corresponds to parameter to a method on an SEI. That SEI could be explicit
 * (i.e. WebService.endpointInterface=sei.class) or implicit (i.e. public methods on the service
 * implementation are the contract and thus the implicit SEI).
 * <p/>
 * ParameterDescriptons contain information that is only relevent for and SEI-based service, i.e.
 * one that is invoked via specific methods. This class does not exist for Provider-based services
 * (i.e. those that specify WebServiceProvider)
 * <p/>
 * <pre>
 *  <b>ParameternDescription details</b>
 * <p/>
 *      CORRESPONDS TO:      A parameter to a method on an SEI (on both Client and Server)
 * <p/>
 *      AXIS2 DELEGATE:      None
 * <p/>
 *      CHILDREN:            None
 * <p/>
 *      ANNOTATIONS:
 *          WebParam [181]
 * <p/>
 *      WSDL ELEMENTS:
 *          message parts
 * <p/>
 * </pre>
 */
public interface ParameterDescription {

    public OperationDescription getOperationDescription();

    public String getParameterName();

    public String getTargetNamespace();

    public String getPartName();

    public boolean isHolderType();
    
    // Indicates whether or not an @XMLList annotation was found on a parameter
    public boolean isListType();
    
    public Class getParameterType();

    public Class getParameterActualType();

    public boolean isHeader();

    public WebParam.Mode getMode();
    
    /**
     * @return AttachmentDescription for this parameter or null
     */
    public AttachmentDescription getAttachmentDescription();

}