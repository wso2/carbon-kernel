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

/**
 * Transport implementation for the UDP protocol.
 * <p>
 * This package contains a transport implementation allowing Axis to
 * send and receive UDP packets. It is an implementation of "raw" UDP in the
 * sense that the message is directly extracted from the UDP payload without
 * any intermediate application protocol. This has several important implications:
 * <ul>
 *   <li>The only way to route the incoming message to the appropriate Axis service
 *       is to bind the service a specific UDP port. The port number must be
 *       explicitly defined in the service configuration. This is different
 *       from protocols such as HTTP where the message can be routed
 *       based on the URL in the request.</li>
 *   <li>The transport has no way to detect the content type of an incoming
 *       message. Indeed, there is no equivalent to HTTP's
 *       <tt>Content-Type</tt> header. Again the expected content type must be
 *       configured explicitly for the service.</li>
 *   <li>Since UDP doesn't provide any mean to correlate responses to requests,
 *       the transport can only be used for asynchronous communication.</li>
 * </ul>
 * See the documentation of {@link org.apache.synapse.transport.udp.UDPListener}
 * for more information about how to configure a service to accept UDP packets.
 * Endpoint references for the UDP transport are assumed to follow the following
 * syntax:
 * <pre>
 * udp://<em>host</em>:<em>port</em>?contentType=...</pre>
 * <p>
 * The UDP transport can be enabled in the Axis configuration as follows:
 * <pre>
 * &lt;transportReceiver name="udp" class="org.apache.synapse.transport.udp.UDPListener"/>
 * &lt;transportSender name="udp" class="org.apache.synapse.transport.udp.UDPSender"/></pre>
 * It should be noted that given its characteristics, UDP is not a
 * suitable transport protocol for SOAP, except maybe in very particular
 * circumstances. Indeed, UDP is an unreliable protocol:
 * <ul>
 *   <li>There is no delivery guarantee, i.e. packets may be lost.</li>
 *   <li>Messages may arrive out of order.</li>
 *   <li>Messages may be duplicated, i.e. delivered twice.</li>
 * </ul>
 * However the unit tests show an example of how to use this transport with SOAP
 * and WS-Addressing to achieve two-way asynchronous communication.
 * Note that the transport has not been designed to implement the
 * <a href="http://specs.xmlsoap.org/ws/2004/09/soap-over-udp/soap-over-udp.pdf">SOAP
 * over UDP specification</a> and will probably not be interoperable.
 * <p>
 * The main purpose of this transport implementation is to integrate Axis (and in
 * particular Synapse) with existing UDP based protocols. See
 * {@link org.apache.synapse.format.syslog} for an example of this kind
 * of protocol.
 * 
 * <h4>Known issues and limitations</h4>
 * 
 * <ul>
 *   <li>Packets longer than the configured maximum packet size
 *       are silently truncated. Packet truncation should be detected
 *       and trigger an error.</li>
 *   <li>The listener doesn't implement all management operations
 *       specified by
 *       {@link org.apache.synapse.transport.base.ManagementSupport}.</li>
 *   <li>The listener assumes that services are bound to unique UDP ports
 *       and predispatches incoming requests based on port numbers.
 *       When SOAP with WS-Addressing is used, the packets could be
 *       received on a single port and dispatched based on the <tt>To</tt>
 *       header. This is not supported.</li>
 *   <li>It might be useful to allow configuration of the content type at the
 *       transport level rather than the service level. In this case, the
 *       <tt>contentType</tt> parameter would not be included in the endpoint
 *       reference. This is necessary for interoperability with the SOAP over UDP
 *       standard.</li>
 *   <li>Technically, it would be quite easy to support binding several UDP ports
 *       to the same service. However, the way endpoints are configured
 *       at the service level doesn't allow this for the moment. Indeed,
 *       using simple text properties only allows to specify the configuration
 *       of a single endpoint.</li>
 *   <li>The transport sender uses a randomly chosen UDP source port. Some
 *       UDP based services may check the source port and discard the packet.
 *       Also, in two-way communication scenarios, stateful firewalls will
 *       not be able to correlate the exchanged packets and may drop
 *       some of them.</li>
 * </ul>
 */
package org.apache.axis2.transport.udp;