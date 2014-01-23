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

package org.apache.axis2.dataretrieval;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The Default Axis2 Data Locator implementation
 */

public class AxisDataLocatorImpl implements AxisDataLocator {
    private static final Log log = LogFactory.getLog(AxisDataLocatorImpl.class);

    // HashMap to cache Data elements defined in ServiceData. 
    private HashMap dataMap = new HashMap();

    private AxisService axisService;


    /**
     * Constructor
     *
     * @throws DataRetrievalException
     */
    public AxisDataLocatorImpl(AxisService in_axisService) throws DataRetrievalException {
        super();
        axisService = in_axisService;
    }

    /**
     * Retrieves and returns data based on the specified request.
     */
    public Data[] getData(DataRetrievalRequest request,
                          MessageContext msgContext) throws DataRetrievalException {
        Data[] data = null;
        String dialect = request.getDialect();
        String identifier = request.getIdentifier();
        String key = dialect;
        ArrayList dataList = new ArrayList();
        if (identifier != null) {
            key = key + identifier;
            if (dataMap.get(key) != null) {
                dataList.add(dataMap.get(key));
            }
        } else {
            dataList = getDataList(dialect);
        }


        AxisDataLocator dataLocator = DataLocatorFactory
                .createDataLocator(dialect, (ServiceData[]) dataList.toArray(new ServiceData[0]));

        if (dataLocator != null) {
            try {
                data = dataLocator.getData(request, msgContext);
            }
            catch (Throwable e) {
                log.info("getData request failed for dialect, " + dialect, e);
                throw new DataRetrievalException(e);
            }
        } else {
            String message = "Failed to instantiate Data Locator for dialect, " + dialect;
            log.info(message);
            throw new DataRetrievalException(message);
        }
        return data;
    }

    /*
    * For AxisService use only!
    */
    public void loadServiceData() {
        DataRetrievalUtil util = DataRetrievalUtil.getInstance();

        OMElement serviceData = null;
        String file = "META-INF/" + DRConstants.SERVICE_DATA.FILE_NAME;
        try {
            serviceData = util.buildOM(axisService.getClassLoader(),
                                       "META-INF/" + DRConstants.SERVICE_DATA.FILE_NAME);
        } catch (DataRetrievalException e) {
            // It is not required to define ServiceData for a Service, just log a warning message

            String message = "Check loading failure for file, " + file;
            log.debug(message + ".Message = " + e.getMessage());
            log.debug(message, e);
        }
        if (serviceData != null) {
            cachingServiceData(serviceData);
        }
    }

    /*
    * caching ServiceData for Axis2 Data Locators
    */
    private void cachingServiceData(OMElement e) {
        Iterator i = e.getChildrenWithName(new QName(
                DRConstants.SERVICE_DATA.DATA));
        String saveKey = "";
        while (i.hasNext()) {
            ServiceData data = new ServiceData((OMElement) i.next());
            saveKey = data.getDialect();

            String identifier = data.getIdentifier();
            if (identifier != null) {
                saveKey = saveKey + identifier;
            }
            dataMap.put(saveKey, data);


        }

    }

    /*
    * Return ServiceData for specified dialect
    */
    private ArrayList getDataList(String dialect) {
        ArrayList dataList = new ArrayList();
        Iterator keys = dataMap.keySet().iterator();

        while (keys.hasNext()) {
            String keyStr = (String) keys.next();
            // get all Data element that matching the dialect
            if (keyStr.indexOf(dialect) == 0) {
                dataList.add(dataMap.get(keyStr));
            }
        }
        return dataList;
    }
}