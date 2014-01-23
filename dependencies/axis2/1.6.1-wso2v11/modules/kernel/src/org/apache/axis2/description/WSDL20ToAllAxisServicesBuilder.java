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

package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.woden.WSDLException;
import org.apache.woden.wsdl20.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends the WSDL20ToAxisServiceBuilder class to provide functionality to return
 * multiple AxisService objects; one for each endpoint on each service in the WSDL 2.0 file.
 */
public class WSDL20ToAllAxisServicesBuilder extends WSDL20ToAxisServiceBuilder {
    protected static final Log log =
            LogFactory.getLog(WSDL20ToAllAxisServicesBuilder.class);

    private ArrayList<AxisService> axisServices = null;

    /**
     * Class constructor.
     *
     * @param in - Contains the wsdl 2.0 file
     */
    public WSDL20ToAllAxisServicesBuilder(InputStream in) {
        super(in, null, null);
        axisServices = new ArrayList<AxisService>();   // create an empty ArrayList
    }

    public WSDL20ToAllAxisServicesBuilder(String wsdlUri, String endpointName) throws WSDLException {
        super(wsdlUri, null, endpointName);
        axisServices = new ArrayList<AxisService>();   // create an empty ArrayList
    }

    /**
     * Public method to access the wsdl 2.0 file and create a List of AxisService objects.
     * For each endpoint on each service in the wsdl, an AxisService object is created and
     * added to the List.  The name of the AxisService is changed from the service name
     * to the the following: <service name>#<endpoint name>.  Note that the endpoint name
     * is not unique to a wsdl 2.0 file.  Multiple services in the file may have endpoints
     * with the same name.  Therefore the name of the AxisService needs to be a combination
     * of service/endpoint name to be unique to the wsdl.
     *
     * @return A List containing one AxisService object for each port in the wsdl file.
     *         The name of the AxisService is modified to uniquely represent the service/endpoint
     *         pair.  The format of the name is "<wsdl service name>#<wsdl endpoint name>"
     * @throws AxisFault
     */
    public List<AxisService> populateAllServices() throws AxisFault {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Entry: populateAllServices");
            }
            setup();  // setup contains code with gathers non-service specific info
            // from the WSDL.  This only needs to be done once per WSDL.
            if (description == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Exit: populateAllServices.  wsdl description is null!");
                }
                return null;   // can't go any further without the wsdl
            }
            Service[] services = description.getServices();
            for (int i = 0; i < services.length; i++) {
                Service service = services[i];
                // set the serviceName on the parent to setup call to populateService
                serviceName = service.getName();
                this.axisService = new AxisService();
                    AxisService retAxisService = populateService();
                    if (retAxisService != null) {
                        axisServices.add(retAxisService);
                    } // end if axisService was returned
                } // end for all ports of a service
            if (log.isDebugEnabled()) {
                log.debug("Exit: populateAllServices.");
            }
            return axisServices;
        } catch (AxisFault e) {
            throw e;  // just rethrow any AxisFaults
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("populateAllServices caught Exception.  Converting to AxisFault. " +
                        e.toString());
            }
            throw AxisFault.makeFault(e);
        }
    }

}
