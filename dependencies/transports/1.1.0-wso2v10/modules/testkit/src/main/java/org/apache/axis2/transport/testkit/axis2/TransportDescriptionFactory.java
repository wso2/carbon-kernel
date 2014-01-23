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

package org.apache.axis2.transport.testkit.axis2;

import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.testkit.axis2.client.AxisTestClientContextConfigurator;
import org.apache.axis2.transport.testkit.axis2.endpoint.AxisTestEndpointContextConfigurator;

/**
 * Resource used to create the {@link TransportInDescription} and
 * {@link TransportOutDescription} objects for a transport under test.
 * <p>
 * Note that this resource is used on both client and server side.
 * If the transport needs different configurations on client and server side,
 * use {@link AxisTestClientContextConfigurator} and/or
 * {@link AxisTestEndpointContextConfigurator}.
 */
public interface TransportDescriptionFactory {
    TransportOutDescription createTransportOutDescription() throws Exception;
    
    /**
     * Create a TransportInDescription for the transport under test.
     * 
     * @return the transport description
     * @throws Exception
     */
    TransportInDescription createTransportInDescription() throws Exception;
}
