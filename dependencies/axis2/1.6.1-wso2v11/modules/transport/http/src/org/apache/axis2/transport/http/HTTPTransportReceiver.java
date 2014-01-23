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


package org.apache.axis2.transport.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Class HTTPTransportReceiver
 */
public class HTTPTransportReceiver {
    public static Map getGetRequestParameters(String requestURI) {

        Map map = new HashMap();
        if (requestURI == null || "".equals(requestURI)) {
            return map;
        }
        char[]       chars = requestURI.toCharArray();
        final int NOT_BEGUN = 1500;
        final int INSIDE_NAME = 1501;
        final int INSIDE_VALUE = 1502;
        int state = NOT_BEGUN;
        StringBuffer name = new StringBuffer();
        StringBuffer value = new StringBuffer();

        for (int index = 0; index < chars.length; index++) {
            if (state == NOT_BEGUN) {
                if (chars[index] == '?') {
                    state = INSIDE_NAME;
                }
            } else if (state == INSIDE_NAME) {
                if (chars[index] == '=') {
                    state = INSIDE_VALUE;
                } else {
                    name.append(chars[index]);
                }
            } else if (state == INSIDE_VALUE) {
                if (chars[index] == ',') {
                    state = INSIDE_NAME;
                    map.put(name.toString(), value.toString());
                    name.delete(0, name.length());
                    value.delete(0, value.length());
                } else {
                    value.append(chars[index]);
                }
            }
        }

        if (name.length() + value.length() > 0) {
            map.put(name.toString(), value.toString());
        }

        return map;
    }

    /**
     * Returns the HTML text for the list of services deployed.
     * This can be delegated to another Class as well
     * where it will handle more options of GET messages.
     *
     * @return Returns String.
     */
    public static String getServicesHTML(ConfigurationContext configurationContext) {
        String temp = "";
        Map services = configurationContext.getAxisConfiguration().getServices();
        Hashtable erroneousServices =
                configurationContext.getAxisConfiguration().getFaultyServices();
        boolean status = false;

        if ((services != null) && !services.isEmpty()) {
            status = true;

            Collection serviceCollection = services.values();

            temp += "<h2>" + "Deployed services" + "</h2>";

            for (Iterator it = serviceCollection.iterator(); it.hasNext();) {

                AxisService axisService = (AxisService) it.next();

                Iterator iterator = axisService.getOperations();

                temp += "<h3><a href=\"" + axisService.getName() + "?wsdl\">" +
                        axisService.getName() + "</a></h3>";

                if (iterator.hasNext()) {
                    temp += "Available operations <ul>";

                    for (; iterator.hasNext();) {
                        AxisOperation axisOperation = (AxisOperation) iterator.next();

                        temp += "<li>" + axisOperation.getName().getLocalPart() + "</li>";
                    }

                    temp += "</ul>";
                } else {
                    temp += "No operations specified for this service";
                }
            }
        }

        if ((erroneousServices != null) && !erroneousServices.isEmpty()) {
            temp += "<hr><h2><font color=\"blue\">Faulty Services</font></h2>";
            status = true;

            Enumeration faultyservices = erroneousServices.keys();

            while (faultyservices.hasMoreElements()) {
                String faultyserviceName = (String) faultyservices.nextElement();

                temp += "<h3><font color=\"blue\">" + faultyserviceName + "</font></h3>";
            }
        }

        if (!status) {
            temp = "<h2>There are no services deployed</h2>";
        }

        temp = "<html><head><title>Axis2: Services</title></head>" + "<body>" + temp
                + "</body></html>";

        return temp;
    }

    // NOTE: This method is no longer used by the standard Axis2 HTTP transport (see WSCOMMONS-405).
    //       However it is still used by Synapse's NIO HTTP transport.
    public static String printServiceHTML(String serviceName,
                                          ConfigurationContext configurationContext) {
        String temp = "";
        try {
            AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
            AxisService axisService = axisConfig.getService(serviceName);
            Iterator iterator = axisService.getOperations();
            temp += "<h3>" + axisService.getName() + "</h3>";
            temp += "<a href=\"" + axisService.getName() + "?wsdl\">wsdl</a> <br/> ";
            temp += "<i>Service Description :  " + axisService.getServiceDescription() +
                    "</i><br/><br/>";
            if (iterator.hasNext()) {
                temp += "Available operations <ul>";
                for (; iterator.hasNext();) {
                    AxisOperation axisOperation = (AxisOperation) iterator.next();
                    temp += "<li>" + axisOperation.getName().getLocalPart() + "</li>";
                }
                temp += "</ul>";
            } else {
                temp += "No operations specified for this service";
            }
            temp = "<html><head><title>Axis2: Services</title></head>" + "<body>" + temp
                    + "</body></html>";
        }
        catch (AxisFault axisFault) {
            temp = "<html><head><title>Service has a fualt</title></head>" + "<body>"
                    + "<hr><h2><font color=\"blue\">" + axisFault.getMessage() +
                    "</font></h2></body></html>";
        }
        return temp;
    }
}
