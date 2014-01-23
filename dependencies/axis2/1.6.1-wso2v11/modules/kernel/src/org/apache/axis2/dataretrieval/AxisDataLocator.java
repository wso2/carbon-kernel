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

import org.apache.axis2.context.MessageContext;

/**
 * <p>AxisDataLocator interface defines getData API for retrieving data particularly
 * metadata like WSDL, Policy, Schema. Web Service engines that have different methods in
 * storing metadata or different types of data to retrieve may code their version of
 * Data Locator(s)by implementing the AxisDataLocator interface. Axis 2 support
 * Data Locator plugin by configuring in Axis2.xml or services.xml. </p>
 */

public interface AxisDataLocator {


    /**
     * Retrieves and returns data based on the specified request.
     *
     * @param request    The {@link DataRetrievalRequest} allow to specify parameters
     *                   about the request, and additional information to process the
     *                   request.
     * @param msgContext The {@link MessageContext} from the original request.
     * @return {@link Data}[] for the request.
     * @throws DataRetrievalException
     */

    public Data[] getData(DataRetrievalRequest request, MessageContext msgContext)
            throws DataRetrievalException;

}
