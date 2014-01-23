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

import javax.wsdl.Definition;
import javax.wsdl.Service;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Extends the WSDL11ToAxisServiceBuilder class to provide functionality to return
 * multiple AxisService objects; one for each port on each service in the WSDL 1.1 file.
 */
public class WSDL11ToAllAxisServicesBuilder extends WSDL11ToAxisServiceBuilder {
    protected static final Log log =
            LogFactory.getLog(WSDL11ToAllAxisServicesBuilder.class);

    public static final String WSDL_SERVICE_QNAME = "WSDL_SERVICE_QNAME";

    public static final String WSDL_PORT = "WSDL_PORT";

    private ArrayList<AxisService> axisServices = null;

    /**
     * Class constructor.
     *
     * @param in - Contains the wsdl 1.1 file
     */
    public WSDL11ToAllAxisServicesBuilder(InputStream in) {
        super(in);
        axisServices = new ArrayList<AxisService>();   // create an empty ArrayList
    }

    public WSDL11ToAllAxisServicesBuilder(Definition def) {
        super(def, null, null);
        axisServices = new ArrayList<AxisService>();   // create an empty ArrayList
    }

     public WSDL11ToAllAxisServicesBuilder(Definition def, String portName) {
        super(def, null, portName);
        axisServices = new ArrayList<AxisService>();   // create an empty ArrayList
    }


    /**
     * Public method to access the wsdl 1.1 file and create a List of AxisService objects.
     * For each port on each service in the wsdl, an AxisService object is created and
     * added to the List.  The name of the AxisService is changed from the service name
     * to the port name, since port names are unique to the wsdl.
     *
     * @return A List containing one AxisService object for each port in the wsdl file.
     *         The name of the AxisService is modified from the service name to the port name.
     * @throws AxisFault
     */
    public List<AxisService> populateAllServices() throws AxisFault {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Entry: populateAllServices");
            }

            setup();  // setup contains code with gathers non-service specific info
            // from the WSDL.  This only needs to be done once per WSDL.
            if (wsdl4jDefinition == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Exit: populateAllServices.  wsdl definition is null!");
                }
                return null;   // can't go any further without the wsdl
            }

            if (wsdl4jDefinition.getServices().size() > 0) {
                Iterator wsdlServIter = wsdl4jDefinition.getServices().values().iterator();
                if (wsdl4jDefinition.getServices().size() > 1){
                     // let the wsdlToservice builder to decide the port to generate binding
                     portName = null;
                } 

                while (wsdlServIter.hasNext()) {
                    Service service = (Service) wsdlServIter.next();
                    // set the serviceName on the parent to setup call to populateService
                    serviceName = service.getQName();
                    this.axisService = new AxisService();
                    // now that serviceName and portName are set, call up to the
                    // parent class to populate this service.
                    AxisService retAxisService = populateService();
                    if (retAxisService != null) {
                        axisServices.add(retAxisService);
                    }
                    // reset the port name if it had set when moving to next service
                    portName = null;
                }
            } else {
                throw new AxisFault("No services found in the WSDL at " +
                        wsdl4jDefinition.getDocumentBaseURI()
                        + " with targetnamespace "
                        + wsdl4jDefinition.getTargetNamespace());
            }


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
