/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.transport.base.datagram;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.transport.base.MetricsCollector;
import org.apache.axis2.transport.base.ParamUtils;
import org.apache.axis2.transport.base.ProtocolEndpoint;

/**
 * Endpoint description.
 * This class is used by the transport to store information
 * about an endpoint, e.g. the Axis service it is bound to.
 * Transports extend this abstract class to store additional
 * transport specific information, such as the port number
 * the transport listens on.
 */
public abstract class DatagramEndpoint extends ProtocolEndpoint {
    private String contentType;
    private MetricsCollector metrics;

	public String getContentType() {
        return contentType;
    }

	public MetricsCollector getMetrics() {
        return metrics;
    }

	public void setMetrics(MetricsCollector metrics) {
		this.metrics = metrics;
	}

    @Override
    public boolean loadConfiguration(ParameterInclude params) throws AxisFault {
        contentType = ParamUtils.getRequiredParam(
                params, "transport." + getListener().getTransportName() + ".contentType");
        return true;
    }
}
